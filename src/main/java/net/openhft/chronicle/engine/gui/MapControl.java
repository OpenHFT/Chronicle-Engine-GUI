package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.renderers.ButtonRenderer;
import net.openhft.chronicle.engine.api.map.MapView;

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


        BeanItemContainer<GridExampleBean> data = createContainer();

        GeneratedPropertyContainer generatedPropertyContainer = addDeleteButton(data);
        Grid grid = new Grid(generatedPropertyContainer);
        grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
        grid.setHeight(100, Sizeable.Unit.PERCENTAGE);

        //grid.setContainerDataSource(generatedPropertyContainer);

        // Render a button that deletes the data row (item)
        grid.getColumn("delete")
                .setRenderer(new ButtonRenderer(e -> // Java 8
                        grid.getContainerDataSource()
                                .removeItem(e.getItemId())));

        view.gridHolder.addComponent(grid);

        // Create a header row to hold column filters
        HeaderRow filterRow = grid.appendHeaderRow();


        // Set up a filter for all columns
        for (Object pid : grid.getContainerDataSource()
                .getContainerPropertyIds()) {
            HeaderCell cell = filterRow.getCell(pid);

            // Have an input field to use for filter
            TextField filterField = new TextField();
            filterField.setColumns(8);

            // Update filter When the filter input is changed
            filterField.addTextChangeListener(change -> {
                // Can't modify filters so need to replace
                data.removeContainerFilters(pid);

                // (Re)create the filter if necessary
                if (!change.getText().isEmpty())
                    data.addContainerFilter(
                            new SimpleStringFilter(pid,
                                    change.getText(), true, false));
            });
            cell.setComponent(filterField);
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


    public static BeanItemContainer<GridExampleBean> createContainer() {
        BeanItemContainer<GridExampleBean> container = new BeanItemContainer<GridExampleBean>(
                GridExampleBean.class);
        for (int i = 0; i < 1000; i++) {
            container.addItem(new GridExampleBean("key=" + i, "value=" + i));
        }


        return container;
    }

    private static GeneratedPropertyContainer addDeleteButton(BeanItemContainer<GridExampleBean> container) {
        GeneratedPropertyContainer gpc =
                new GeneratedPropertyContainer(container);

        gpc.addGeneratedProperty("delete",
                new PropertyValueGenerator<String>() {

                    @Override
                    public String getValue(Item item, Object itemId,
                                           Object propertyId) {
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
