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
public class MapControl {


    private MapViewUI view;


    public MapControl(MapView data, MapViewUI view, String path) {
        this.view = view;
        view.path.setValue(path);
    }

    public void init() {
        view.gridHolder.removeAllComponents();

        Container.Indexed data = createContainer();

        GeneratedPropertyContainer generatedPropertyContainer = addDeleteButton(data);
        Grid grid = new Grid(generatedPropertyContainer);
        grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        grid.getColumn("key").setMinimumWidth(100);
        grid.getColumn("value").setMinimumWidth(100);

        // Render a button that deletes the data row (item)
        Grid.Column deleteColumn = grid.getColumn("delete");
        deleteColumn.setWidth(100);
        deleteColumn.setLastFrozenColumn();

        grid.setCellStyleGenerator(cellRef -> // Java 8
                "delete".equals(cellRef.getPropertyId()) ?
                        "rightalign" : null);

        deleteColumn
                .setRenderer(new ButtonRenderer(e -> // Java 8
                        grid.getContainerDataSource()
                                .removeItem(e.getItemId())));

        view.gridHolder.addComponent(grid);


        if (data instanceof Container.Filterable) {
            // Create a header row to hold column filters
            HeaderRow filterRow = grid.appendHeaderRow();

            Container.Filterable data1 = (Container.Filterable) data;

            // Set up a filter for all columns
            for (Object pid : grid.getContainerDataSource()
                    .getContainerPropertyIds()) {
                if ("delete".equals(pid))
                    continue;

                HeaderCell cell = filterRow.getCell(pid);

                // Have an input field to use for filter
                TextField filterField = new TextField();
                filterField.setHeight(24, Sizeable.Unit.PIXELS);
                filterField.setWidth(100, Sizeable.Unit.PERCENTAGE);

                // Update filter When the filter input is changed
                filterField.addTextChangeListener(change -> {
                    // Can't modify filters so need to replace
                    // data.removeContainerFilters(pid);


                    Collection<SimpleStringFilter> containerFilters = (Collection) data1.getContainerFilters();

                    Optional<SimpleStringFilter> first = containerFilters.stream().filter(x -> x.getPropertyId().equals(pid)).findFirst();
                    if (first.isPresent())
                        data1.removeContainerFilter(first.get());


                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty())
                        data1.addContainerFilter(
                                new SimpleStringFilter(pid,
                                        change.getText(), true, false));
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

        for (int i = 0; i < 1000; i++) {
            container.addItem(new GridExampleBean("key=" + i, "value=" + i));
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
