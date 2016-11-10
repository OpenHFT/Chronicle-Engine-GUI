package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.ImageRenderer;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.column.Column;
import net.openhft.chronicle.engine.api.column.ColumnView;
import net.openhft.chronicle.engine.map.ObjectSubscription;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.vaadin.ui.Grid.HeaderCell;
import static com.vaadin.ui.Grid.HeaderRow;

/**
 * @author Rob Austin.
 */
class ColumnViewController<K, V> {

    @NotNull
    private final ColumnView columnView;
    @NotNull
    private final MapViewUI view;
    private final String path;


    ColumnViewController(@NotNull ColumnView columnView, @NotNull MapViewUI view, String path) {
        this.columnView = columnView;
        this.view = view;
        this.path = path;

        view.path.setValue(path);
        view.recordCount.setValue(Long.toString(columnView.rowCount(Collections.emptyList())));

        final ObjectSubscription objectSubscription = columnView.objectSubscription();
        onChange(view, objectSubscription);
        objectSubscription.registerDownstream(changeEvent -> onChange(view, objectSubscription));
    }

    private void onChange(@NotNull MapViewUI view, @NotNull ObjectSubscription objectSubscription) {
        view.topicSubscriberCount.setValue(Integer.toString(objectSubscription
                .topicSubscriberCount()));

        try {
            view.keySubscriberCount.setValue(Integer.toString(objectSubscription
                    .keySubscriberCount()));
        } catch (UnsupportedOperationException e) {
            view.keySubscriberCount.setValue("Unknown");
        }

        try {
            view.entrySubscriberCount.setValue(Integer.toString(objectSubscription
                    .entrySubscriberCount()));
        } catch (UnsupportedOperationException e) {
            view.entrySubscriberCount.setValue("Unknown");
        }


        view.keyStoreValue.setValue(objectSubscription.getClass().getSimpleName());
    }

    private final AtomicLong refreshUI = new AtomicLong();


    void init() {
        view.gridHolder.removeAllComponents();

        @NotNull final Container.Indexed data = createContainer(new ColumnQueryDelegate(columnView));
        @NotNull final GeneratedPropertyContainer generatedPropertyContainer = addDeleteButton(data);
        @NotNull final Grid grid = new Grid(generatedPropertyContainer);

        grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);
        grid.removeAllColumns();

        final List<Column> columns = columnView.columns();
        for (@NotNull Column column : columns) {
            final Grid.Column gridColumn = grid.addColumn(column.name);
            gridColumn.setSortable(column.sortable);
            gridColumn.setEditable(!column.isReadOnly());
        }

        grid.setSizeFull();

        view.addButton.addClickListener((ClickListener) event -> new AddRow(columnView).init());


        columnView.registerChangeListener(() -> {
            refreshUI.compareAndSet(0, System.currentTimeMillis());
        });

        UI.getCurrent().addPollListener(e -> {
            if (grid.isVisible())
                refreshUI((SQLContainer) data);
        });

        if (data instanceof SQLContainer) {
            ((SQLContainer) data).setAutoCommit(true);
        }

        if (columnView.canDeleteRows()) {

            grid.setEditorEnabled(true);
            grid.setEditorBuffered(false);
            grid.setSelectionMode(Grid.SelectionMode.NONE);

            // Render a button that deletes the data row (item)
            final Grid.Column deleteColumn = grid.addColumn("delete");
            deleteColumn.setWidth(64);
            //     deleteColumn.setLastFrozenColumn();
            deleteColumn.setHeaderCaption("");
            deleteColumn.setEditable(false);
            deleteColumn.setResizable(false);

            grid.setCellStyleGenerator(cellRef ->
                    "delete".equals(cellRef.getPropertyId()) ? "rightalign" : null);

            deleteColumn.setRenderer(new ImageRenderer(
                    e -> grid.getContainerDataSource().removeItem(e.getItemId())));
        }

        view.gridHolder.addComponent(grid);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        if (data instanceof Container.Filterable) {

            // Create a header row to hold column filters
            final HeaderRow filterRow = grid.appendHeaderRow();
            @NotNull final Container.Filterable filterable = (Container.Filterable) data;

            // Set up a filter for all columns
            for (Object pid : grid.getContainerDataSource()
                    .getContainerPropertyIds()) {

                if ("delete".equals(pid))
                    continue;

                final HeaderCell cell = filterRow.getCell(pid);

                // Have an input field to use for filter
                @NotNull TextField filterField = new TextField();
                filterField.setHeight(24, Sizeable.Unit.PIXELS);
                //      filterField.setWidth(100, Sizeable.Unit.PERCENTAGE);

                // Update filter When the filter input is changed
                filterField.addTextChangeListener(change -> {
                    // Can't modify filters so need to replace
                    // data.removeContainerFilters(pid);

                    @NotNull final Collection<SimpleStringFilter> containerFilters = (Collection)
                            filterable.getContainerFilters();

                    Optional<SimpleStringFilter> first = containerFilters.stream().filter(x -> x.getPropertyId().equals(pid)).findFirst();
                    if (first.isPresent())
                        filterable.removeContainerFilter(first.get());

                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty())
                        filterable.addContainerFilter(
                                new SimpleStringFilter(pid, change.getText(), true, false));
                });

                cell.setComponent(filterField);
            }
        }

    }

    private void refreshUI(SQLContainer data) {
        final long l = refreshUI.get();

        if (l + 5_000 < System.currentTimeMillis()) {
            refreshUI.set(0);
            data.refresh();
            view.recordCount.setValue(Long.toString(columnView.rowCount(Collections.emptyList())));
        }

    }

    @NotNull
    private static Container.Indexed createContainer(@NotNull final QueryDelegate delegate) {
        try {
            return new SQLContainer(delegate);
        } catch (SQLException e) {
            throw Jvm.rethrow(e);
        }
    }

    @NotNull
    private static GeneratedPropertyContainer addDeleteButton(Container.Indexed container) {
        @NotNull final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(container);

        gpc.addGeneratedProperty("delete",
                new PropertyValueGenerator<Resource>() {
                    @NotNull
                    @Override
                    public Resource getValue(Item item, Object itemId, Object propertyId) {
                        return new ThemeResource("trash3.png");
                    }

                    @NotNull
                    @Override
                    public Class<Resource> getType() {
                        return Resource.class;
                    }
                });

        return gpc;
    }


}

