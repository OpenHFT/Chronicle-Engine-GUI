package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import net.openhft.chronicle.engine.api.column.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

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

            final List lables = new ArrayList();
            final List<DataSeriesItem> data = new ArrayList<>();
            final ClosableIterator<? extends Row> iterator = columnView.iterator(sortedFilter(vaadinChart));

            while (iterator.hasNext()) {
                Row row = iterator.next();

                final Object o = row.get(vaadinChart.columnNameField());

                final String columnName = (barChartProperties.xAxisLableRender == null) ?
                        o.toString() : barChartProperties.xAxisLableRender.apply(o);

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
    private ColumnViewInternal.SortedFilter sortedFilter(final VaadinChart vaadinChart) {

        ColumnViewInternal.SortedFilter sortedFilter = new ColumnViewInternal.SortedFilter();

        long countFromEnd = vaadinChart.barChartProperties().countFromEnd;
        if (countFromEnd > 0) {
            sortedFilter.countFromEnd = countFromEnd;
        }

        sortedFilter.marshableOrderBy = singletonList(new ColumnViewInternal
                .MarshableOrderBy(vaadinChart.columnNameField()));

        BarChartProperties barChartProperties = vaadinChart.barChartProperties();
        if (barChartProperties.filter != null)
            sortedFilter.marshableFilters = singletonList(barChartProperties.filter);
        return sortedFilter;
    }

}