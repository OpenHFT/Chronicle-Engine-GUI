package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.renderers.ButtonRenderer;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.map.MapView;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import static com.vaadin.ui.Grid.HeaderCell;
import static com.vaadin.ui.Grid.HeaderRow;

/**
 * @author Rob Austin.
 */
public class MapControl<K, V> {

    private final MapView mapView;
    private MapViewUI view;

    public MapControl(MapView mapView, MapViewUI view, String path) {
        this.mapView = mapView;
        this.view = view;
        view.path.setValue(path);
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

        mapView.registerSubscriber(mapEvent -> ((SQLContainer) data).refresh());

        if (data instanceof SQLContainer) {
            ((SQLContainer) data).setAutoCommit(true);
        }

        // Render a button that deletes the data row (item)
        final Grid.Column deleteColumn = grid.getColumn("delete");
        deleteColumn.setWidth(100);
        deleteColumn.setLastFrozenColumn();

        grid.setCellStyleGenerator(cellRef ->
                "delete".equals(cellRef.getPropertyId()) ? "rightalign" : null);

        deleteColumn.setRenderer(
                new ButtonRenderer(e -> grid.getContainerDataSource().removeItem(e.getItemId())));

        view.gridHolder.addComponent(grid);

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

    public static class GridExampleBean {
        private String key;
        private String value;

        public GridExampleBean() {
        }

        public GridExampleBean(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static Container.Indexed createContainer() {
        BeanItemContainer<GridExampleBean> container = new BeanItemContainer<GridExampleBean>(
                GridExampleBean.class);
        for (int i = 0; i < 1000; i++) {
            container.addItem(new GridExampleBean("key=" + i, "value=" + i));
        }
        return container;
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
                new PropertyValueGenerator<String>() {
                    @Override
                    public String getValue(Item item, Object itemId, Object propertyId) {
                        return "Delete"; // The caption
                    }

                    @Override
                    public Class<String> getType() {
                        return String.class;
                    }
                });

        return gpc;
    }


}
