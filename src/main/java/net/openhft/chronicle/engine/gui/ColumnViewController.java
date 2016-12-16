package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.ImageRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.column.Column;
import net.openhft.chronicle.engine.api.column.ColumnViewInternal;
import net.openhft.chronicle.engine.map.ObjectSubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.vaadin.ui.AbstractTextField.TextChangeEventMode.EAGER;
import static com.vaadin.ui.Grid.HeaderCell;
import static com.vaadin.ui.Grid.HeaderRow;

/**
 * @author Rob Austin.
 */
class ColumnViewController<K, V> {

    @NotNull
    private final ColumnViewInternal columnView;
    @NotNull
    private final MapViewUI view;

    private final DecimalFormat removeFormatting;

    ColumnViewController(@NotNull ColumnViewInternal columnView, @NotNull MapViewUI view, String path) {
        this.columnView = columnView;
        this.view = view;


        view.path.setValue(path);
        view.recordCount.setValue(Long.toString(columnView.rowCount(new ColumnViewInternal.SortedFilter())));

        final ObjectSubscription objectSubscription = columnView.objectSubscription();
        onChange(view, objectSubscription);

        removeFormatting = new DecimalFormat();
        removeFormatting.setGroupingUsed(false);
    }

    class TextChangeListener implements FieldEvents.TextChangeListener {

        private Container.Filterable filterable;
        private Object pid;

        public TextChangeListener(Container.Filterable filterable, Object pid, final TextField filterField) {
            this.filterable = filterable;
            this.pid = pid;
        }

        @Override
        public void textChange(FieldEvents.TextChangeEvent change) {
            // Can't modify filters so need to replace
            // data.removeContainerFilters(pid);

            @NotNull final Collection<SimpleStringFilter> containerFilters = (Collection)
                    filterable.getContainerFilters();

            Optional<SimpleStringFilter> first = containerFilters.stream().filter(x -> x.getPropertyId().equals(pid)).findFirst();
            if (first.isPresent())
                filterable.removeContainerFilter(first.get());

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                filterable.addContainerFilter(
                        new SimpleStringFilter(pid, change.getText(), true, false));

            }
        }
    }

    class FocusListener implements FieldEvents.FocusListener {


        @Nullable
        private final TimeStampSearch timeStampSearch;

        public FocusListener(final TextField filterField,
                             FieldEvents.TextChangeListener textChangeListener) {
            timeStampSearch = new TimeStampSearch(filterField, textChangeListener);
        }

        @Override
        public void focus(FieldEvents.FocusEvent event) {

            if (!timeStampSearch.hasFocus()) {
                timeStampSearch.doSearch();
            } else {
                timeStampSearch.hasFocus(false);
            }
        }
    }

    private void onChange(@NotNull MapViewUI view, ObjectSubscription objectSubscription) {
    /*    view.topicSubscriberCount.setValue(Integer.toString(objectSubscription
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


        view.keyStoreValue.setValue(objectSubscription.getClass().getSimpleName());*/
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

            if (Number.class.isAssignableFrom(column.type) || Boolean.class.isAssignableFrom
                    (column.type)) {
                gridColumn.setWidth(120);
            }

            if (column.type == Long.class && column.name.equalsIgnoreCase("TimeStamp")) {
                gridColumn.setRenderer(new TextRenderer(), new Converter<String, Long>() {

                    @Override
                    public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale) throws ConversionException {
                        final LocalDateTime dt = LocalDateTime.parse(value);
                        return Date.from(dt.toInstant(ZoneOffset.UTC)).getTime();
                    }

                    @Override
                    public String convertToPresentation(Long value, Class<? extends String> targetType, Locale locale) throws ConversionException {
                        final Instant instant = Instant.ofEpochMilli(value);
                        final LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                        return ldt.toString();
                    }

                    @Override
                    public Class<Long> getModelType() {
                        return Long.class;
                    }

                    @Override
                    public Class<String> getPresentationType() {
                        return String.class;
                    }
                });
                gridColumn.setWidth(250);

            } else if (column.type == Long.class || column.type == Integer.class) {
                gridColumn.setRenderer(new NumberRenderer(removeFormatting));
            }
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


                // Update filter When the filter input is changed
                final TextChangeListener listener1 = new TextChangeListener(filterable, pid, filterField);
                filterField.addTextChangeListener(listener1);
                filterField.setTextChangeEventMode(EAGER);


                if ("timestamp".equalsIgnoreCase(pid.toString())) {
                    FocusListener listener = new FocusListener(filterField, listener1);
                    filterField.addFocusListener(listener);
                    filterField.setWidth(200, Sizeable.Unit.PIXELS);
                } else
                    filterField.setWidth(100, Sizeable.Unit.PIXELS);

                cell.setComponent(filterField);
            }
        }

    }

    private void refreshUI(SQLContainer data) {
       /* final long l = refreshUI.get();

        if (l + 5_000 < System.currentTimeMillis()) {
            refreshUI.set(0);
            data.refresh();
            view.recordCount.setValue(Long.toString(columnView.rowCount(Collections.emptyList())));
        }*/

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


    public static void main(String[] args) {

        DecimalFormat df = new DecimalFormat();
        df.setGroupingUsed(false);
        System.out.println(df.format(100000));
    }

}

