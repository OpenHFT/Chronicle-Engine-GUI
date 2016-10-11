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
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.renderers.ImageRenderer;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.map.ObjectSubscription;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import static com.vaadin.ui.Grid.HeaderCell;
import static com.vaadin.ui.Grid.HeaderRow;

/**
 * @author Rob Austin.
 */
public class MapViewController<K, V> {

    private final MapView mapView;
    private MapViewUI view;

    public MapViewController(MapView mapView, MapViewUI view, String path) {
        this.mapView = mapView;
        this.view = view;
        view.path.setValue(path);
        view.recordCount.setValue(Long.toString(mapView.longSize()));
        view.keyType.setValue(mapView.keyType().getSimpleName().toString());
        view.valueType.setValue(mapView.valueType().getSimpleName().toString());
        // view.valueType.setValue(mapView.g().getSimpleName().toString());
        // mapView.

        ObjectSubscription objectSubscription = mapView.asset().getView(ObjectSubscription.class);

        onMapViewChange(view, objectSubscription);

        objectSubscription.registerDownstream(changeEvent -> onMapViewChange(view, objectSubscription));


    }

    private void onMapViewChange(MapViewUI view, ObjectSubscription objectSubscription) {
        view.topicSubscriberCount.setValue(Integer.toString(objectSubscription
                .topicSubscriberCount()));
        view.keySubscriberCount.setValue(Integer.toString(objectSubscription
                .keySubscriberCount()));

        view.entrySubscriberCount.setValue(Integer.toString(objectSubscription
                .entrySubscriberCount()));

        view.keyStoreValue.setValue(objectSubscription.getClass()
                .getSimpleName().toString());
    }

    public void init() {
        view.gridHolder.removeAllComponents();

        //final Container.Indexed data = createContainer();
        final Container.Indexed data = createContainer(new MapQueryDelegate<K, V>(mapView));
        final GeneratedPropertyContainer generatedPropertyContainer = addDeleteButton(data);

        final Grid grid = new Grid(generatedPropertyContainer);
        grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        grid.getColumn("key").setMinimumWidth(100);
        grid.getColumn("value").setMinimumWidth(100);

        grid.setEditorEnabled(true);
        grid.setEditorBuffered(false);
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);


        view.addButton.addClickListener((Button.ClickListener) event -> {
            mapView.put(view.addKey.getValue(), view.addValue.getValue());
            view.addKey.setValue("");
            view.addValue.setValue("");
        });


        mapView.registerSubscriber(mapEvent -> {
            ((SQLContainer) data).refresh();
            view.recordCount.setValue(Long.toString(mapView.longSize()));
        });

        if (data instanceof SQLContainer) {
            ((SQLContainer) data).setAutoCommit(true);
        }

        // Render a button that deletes the data row (item)
        final Grid.Column deleteColumn = grid.getColumn("delete");
        deleteColumn.setWidth(64);
        deleteColumn.setLastFrozenColumn();
        deleteColumn.setHeaderCaption("");

        grid.setCellStyleGenerator(cellRef ->
                "delete".equals(cellRef.getPropertyId()) ? "rightalign" : null);

        ImageRenderer renderer = new ImageRenderer(e -> grid.getContainerDataSource().removeItem(e.getItemId()));
        deleteColumn.setRenderer(renderer);

        view.gridHolder.addComponent(grid);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        if (data instanceof Container.Filterable) {

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


    public static Container.Indexed createContainer(final QueryDelegate delegate) {
        Container.Indexed container = null;
        try {
            container = new SQLContainer(delegate);
        } catch (SQLException e) {
            Jvm.rethrow(e);
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
