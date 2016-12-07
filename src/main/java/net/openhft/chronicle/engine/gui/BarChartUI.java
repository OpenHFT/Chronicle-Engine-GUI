package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import net.openhft.chronicle.engine.api.column.BarChart;
import net.openhft.chronicle.engine.api.column.ClosableIterator;
import net.openhft.chronicle.engine.api.column.ColumnViewInternal;
import net.openhft.chronicle.engine.api.column.Row;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class BarChartUI {


    protected Component getChart(BarChart barChart) {

        ColumnViewInternal columnView = barChart.columnView();
        ClosableIterator<? extends Row> iterator = columnView.iterator(orderBytKey());

        PlotOptionsColumn plotOptions;
        Chart chart;
        chart = new Chart(ChartType.COLUMN);
        chart.setHeight(100, Sizeable.Unit.PERCENTAGE);

        Configuration conf = chart.getConfiguration();
        conf.setTitle(barChart.title());

        XAxis x = new XAxis();
        conf.addxAxis(x);

        plotOptions = new PlotOptionsColumn();
        // plotOptions.setPointRange(10);
        plotOptions.setPointWidth(100);
        conf.setPlotOptions(plotOptions);


        List lables = new ArrayList();
        List<DataSeriesItem> data = new ArrayList<>();
        while (iterator.hasNext()) {
            Row row = iterator.next();

            String columnName = row.get(barChart.columnNameField()).toString();
            lables.add(columnName);

            Number number = (Number) row.get(barChart.columnValueField());
            data.add(new DataSeriesItem(columnName, number));
        }
        x.setCategories((String[]) lables.toArray(new String[lables.size()]));
        conf.addxAxis(x);

        conf.setSeries(new DataSeries(data));

        chart.drawChart(conf);

        return chart;
    }

    @NotNull
    private ColumnViewInternal.SortedFilter orderBytKey() {
        ColumnViewInternal.SortedFilter sortedFilter = new ColumnViewInternal.SortedFilter();
        sortedFilter.marshableOrderBy = Collections.singletonList(new ColumnViewInternal
                .MarshableOrderBy("order"));
        return sortedFilter;
    }


    protected void setup(PlotOptionsColumn plotOptions, Chart chart) {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        final Slider slider = new Slider("Value (1-100)", 1, 100);
        slider.setWidth("200px");
        slider.setValue(100d);

        final NativeSelect option = new NativeSelect();
        option.setCaption("Option");
        option.setNullSelectionAllowed(true);
        option.addItem("pointWidth");
        option.addItem("pointRange");
        option.setValue("pointWidth");
        option.setImmediate(true);
        option.addValueChangeListener((ValueChangeListener) event -> slider.setEnabled(event.getProperty().getValue() != null));

        horizontalLayout.addComponent(option);
        horizontalLayout.addComponent(slider);
        Button button = new Button("Apply");
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (slider.isEnabled()) {
                    if (option.getValue().equals("pointWidth")) {
                        plotOptions.setPointWidth(slider.getValue());
                        plotOptions.setPointRange(null);
                    } else {
                        plotOptions.setPointRange(slider.getValue());
                        plotOptions.setPointWidth(null);
                    }
                } else {
                    plotOptions.setPointRange(null);
                    plotOptions.setPointWidth(null);
                }
                chart.drawChart();
            }
        });
        horizontalLayout.addComponent(button);


    }


}