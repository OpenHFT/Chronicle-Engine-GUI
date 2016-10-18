package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.renderers.ImageRenderer;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.engine.api.column.Column;
import net.openhft.chronicle.engine.api.column.ColumnView;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.map.ObjectSubscription;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.vaadin.ui.Grid.HeaderCell;
import static com.vaadin.ui.Grid.HeaderRow;

/**
 * @author Rob Austin.
 */
class ColumnViewController<K, V> {

    private final ColumnView columnView;
    private MapViewUI view;

    ColumnViewController(ColumnView columnView, MapViewUI view, String path) {
        this.columnView = columnView;
        this.view = view;
        view.path.setValue(path);
        view.recordCount.setValue(Long.toString(columnView.longSize()));

        if (columnView instanceof MapView) {
            ObjectSubscription objectSubscription = ((MapView) columnView).asset().getView
                    (ObjectSubscription.class);

            onMapViewChange(view, objectSubscription);

            objectSubscription.registerDownstream(changeEvent -> onMapViewChange(view, objectSubscription));
        }

    }

    private void onMapViewChange(MapViewUI view, ObjectSubscription objectSubscription) {
        view.topicSubscriberCount.setValue(Integer.toString(objectSubscription
                .topicSubscriberCount()));
        view.keySubscriberCount.setValue(Integer.toString(objectSubscription
                .keySubscriberCount()));

        view.entrySubscriberCount.setValue(Integer.toString(objectSubscription
                .entrySubscriberCount()));

        view.keyStoreValue.setValue(objectSubscription.getClass().getSimpleName());
    }

    boolean keyValid;
    boolean valueValid;

    public void init() {
        view.gridHolder.removeAllComponents();

        //final Container.Indexed data = createContainer();
        final Container.Indexed data = createContainer(new ColumnQueryDelegate<>(columnView));
        final GeneratedPropertyContainer generatedPropertyContainer = addDeleteButton(data);

        final Grid grid = new Grid(generatedPropertyContainer);
        grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        grid.removeAllColumns();
        List<Column> columns = columnView.columns();
        for (Column column : columns) {
            final Grid.Column column1 = grid.addColumn(column.name);
            //  column1.set(column.hi)
        }

        grid.setEditorEnabled(true);
        grid.setEditorBuffered(false);
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);


        view.addButton.addClickListener((ClickListener) event -> {


            // Create a sub-window and set the content
            Window subWindow = new Window("Add New Row");
            subWindow.setClosable(false);
            subWindow.setModal(true);
            subWindow.setResizeLazy(true);
            subWindow.setResizable(false);
            subWindow.setSizeUndefined();
            subWindow.setWidth(300, Sizeable.Unit.PIXELS);
            subWindow.setDraggable(true);

            FormLayout form = new FormLayout();
            form.setMargin(true);

            final List<Column> columns1 = columnView.columns();
            for (Column column : columns1) {

                AbstractField field;
                if (column.type == Date.class) {
                    field = new DateField(column.name);
                }
                if (column.type == boolean.class) {
                    field = new CheckBox(column.name);
                } else {
                    field = new TextField(column.name);
                }

                if (column.primaryKey)
                    field.setRequired(true);
                if (column.type == boolean.class) {
                    field.setValue(Boolean.FALSE.toString());
                } else if (column.type.isPrimitive() || Number.class.isAssignableFrom(column.type))
                    field.setValue(ObjectUtils.convertTo(column.type, 0).toString());

                field.addValidator((Validator) value -> {
                    try {
                        ObjectUtils.convertTo(column.type, value);
                    } catch (Exception e) {
                        throw new Validator.InvalidValueException("can not convert to " + column.type.getSimpleName());
                    }
                });

                form.addComponent(field);
            }

            final HorizontalLayout buttons = new HorizontalLayout();
            buttons.setMargin(true);
            buttons.setSpacing(true);
            final Button cancel = new Button("Cancel");
            cancel.addClickListener((ClickListener) event1 -> subWindow.close());
            buttons.addComponent(cancel);
            final Button ok = new Button("Ok");
            ok.addClickListener((ClickListener) event1 -> subWindow.close());
            buttons.addComponent(ok);
            buttons.setComponentAlignment(ok, Alignment.MIDDLE_RIGHT);
            buttons.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);
            form.addComponent(buttons);
            subWindow.setContent(form);


            // Center it in the browser window
            subWindow.center();

            // Open it in the UI
            UI.getCurrent().addWindow(subWindow);


            //    columnView.addRow(k, v);
        });

        columnView.registerChangeListener(() -> {
            ((SQLContainer) data).refresh();
            view.recordCount.setValue(Long.toString(columnView.longSize()));
        });

        if (data instanceof SQLContainer) {
            ((SQLContainer) data).setAutoCommit(true);
        }

        if (columnView.canDeleteRow()) {
            // Render a button that deletes the data row (item)
            final Grid.Column deleteColumn = grid.addColumn("delete");
            deleteColumn.setWidth(64);
            deleteColumn.setLastFrozenColumn();
            deleteColumn.setHeaderCaption("");
            deleteColumn.setEditable(false);
            deleteColumn.setResizable(false);

            grid.setCellStyleGenerator(cellRef ->
                    "delete".equals(cellRef.getPropertyId()) ? "rightalign" : null);

            ImageRenderer renderer = new ImageRenderer(e -> grid.getContainerDataSource().removeItem(e.getItemId()));
            deleteColumn.setRenderer(renderer);
        }

        view.gridHolder.addComponent(grid);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        if (data instanceof Container.Filterable)

        {

            // Create a header row to hold column filters
            final HeaderRow filterRow = grid.appendHeaderRow();
            final Container.Filterable filterable = (Container.Filterable) data;

            // Set up a filter for all columns
            for (Object pid : grid.getContainerDataSource()
                    .getContainerPropertyIds()) {

                if ("delete".equals(pid))
                    continue;

                final HeaderCell cell = filterRow.getCell(pid);

                // Have an input field to use for filter
                TextField filterField = new TextField();
                filterField.setHeight(24, Sizeable.Unit.PIXELS);
                filterField.setWidth(100, Sizeable.Unit.PERCENTAGE);

                // Update filter When the filter input is changed
                filterField.addTextChangeListener(change -> {
                    // Can't modify filters so need to replace
                    // data.removeContainerFilters(pid);

                    final Collection<SimpleStringFilter> containerFilters = (Collection)
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

    private Class type(final String typeName) {
        Optional<Column> first = ((List<Column>) columnView.columns())
                .stream().filter(c -> c.name.equals(typeName))
                .findFirst();

        if (!first.isPresent())
            throw new IllegalStateException(typeName + " not found");

        return first.get().type;

    }


    private static Container.Indexed createContainer(final QueryDelegate delegate) {
        Container.Indexed container = null;
        try {
            container = new SQLContainer(delegate);
        } catch (SQLException e) {
            throw Jvm.rethrow(e);
        }

        return container;
    }

    private static GeneratedPropertyContainer addDeleteButton(Container.Indexed container) {
        final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(container);

        gpc.addGeneratedProperty("delete",
                new PropertyValueGenerator<Resource>() {
                    @Override
                    public Resource getValue(Item item, Object itemId, Object propertyId) {
                        return new ThemeResource("trash3.png");
                    }

                    @Override
                    public Class<Resource> getType() {
                        return Resource.class;
                    }
                });

        return gpc;
    }


}

