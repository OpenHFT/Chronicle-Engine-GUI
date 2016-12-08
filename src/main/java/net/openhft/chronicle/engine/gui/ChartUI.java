package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import net.openhft.chronicle.engine.api.column.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class ChartUI {


    protected Component getChart(VaadinChart vaadinChart) {

        final ColumnViewInternal columnView = vaadinChart.columnView();

        final Chart chart;
        chart = new Chart(ChartType.COLUMN);
        chart.setHeight(100, Sizeable.Unit.PERCENTAGE);

        final Configuration conf = chart.getConfiguration();
        final BarChartProperties barChartProperties = vaadinChart.barChartProperties();
        conf.setTitle(barChartProperties.title);

        String yAxisLabel = null;

        boolean hasXAxis = false;

        for (VaadinChartSeries vaadinChartSeries : vaadinChart.series()) {

            List lables = new ArrayList();
            List<DataSeriesItem> data = new ArrayList<>();

            ClosableIterator<? extends Row> iterator = columnView.iterator(orderBytKey(vaadinChart.columnNameField()));

            while (iterator.hasNext()) {
                Row row = iterator.next();

                String columnName = row.get(vaadinChart.columnNameField()).toString();
                lables.add(columnName);

                Number number = (Number) row.get(vaadinChartSeries.field);
                data.add(new DataSeriesItem(columnName, number));
            }

            if (!hasXAxis) {
                hasXAxis = true;
                XAxis x = new XAxis();
                x.setCategories((String[]) lables.toArray(new String[lables.size()]));
                conf.addxAxis(x);
            }

            DataSeries dataSeries = new DataSeries(data);

            switch (vaadinChartSeries.type()) {
                case SPLINE: {
                    dataSeries.setPlotOptions(new PlotOptionsSpline());
                    break;
                }

                case COLUMN: {
                    PlotOptionsColumn plotOptions = new PlotOptionsColumn();
                    plotOptions.setPointWidth(vaadinChartSeries.width());
                    dataSeries.setPlotOptions(plotOptions);
                    break;
                }
            }


            final String ylabel = vaadinChartSeries.yAxisLabel();

            if (ylabel != null && !ylabel.contentEquals("") && (yAxisLabel == null || !yAxisLabel
                    .equals(ylabel))) {
                YAxis yAxis = new YAxis();
                yAxis.setTitle(ylabel);
                conf.addyAxis(yAxis);
                yAxisLabel = ylabel;
            }

            dataSeries.setName(vaadinChartSeries.field);
            conf.addSeries(dataSeries);

        }
        chart.drawChart(conf);

        return chart;
    }

    @NotNull
    private ColumnViewInternal.SortedFilter orderBytKey(final String order) {
        ColumnViewInternal.SortedFilter sortedFilter = new ColumnViewInternal.SortedFilter();
        sortedFilter.marshableOrderBy = Collections.singletonList(new ColumnViewInternal
                .MarshableOrderBy(order));
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