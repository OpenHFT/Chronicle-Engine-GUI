package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import net.openhft.chronicle.engine.api.column.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;

@SuppressWarnings("serial")
public class ChartUI {


    @NotNull
    protected Component getChart(@NotNull VaadinChart vaadinChart) {

        final ColumnViewInternal columnView = vaadinChart.columnView();
        @NotNull final Chart chart;

        chart = new Chart(ChartType.COLUMN);
        chart.setHeight(100, Sizeable.Unit.PERCENTAGE);

        final Configuration conf = chart.getConfiguration();
        final ChartProperties chartProperties = vaadinChart.chartProperties();
        conf.setTitle(chartProperties.title);

        @Nullable String yAxisLabel = null;

        boolean hasXAxis = false;

        for (@NotNull VaadinChartSeries vaadinChartSeries : vaadinChart.series()) {

            @NotNull final List lables = new ArrayList();
            @NotNull final List<DataSeriesItem> data = new ArrayList<>();
            final ClosableIterator<? extends Row> iterator = columnView.iterator(sortedFilter(vaadinChart));

            while (iterator.hasNext()) {
                Row row = iterator.next();

                final Object o = row.get(vaadinChart.columnNameField());

                final Function<Object, String> xAxisLableRender = chartProperties.xAxisLabelRender;

                if (o != null) {
                    final String columnName = (xAxisLableRender == null) ? o.toString() : xAxisLableRender
                            .apply(o);

                    lables.add(columnName);

                    @NotNull Number number = (Number) row.get(vaadinChartSeries.field);
                    data.add(new DataSeriesItem(columnName, number));
                }
            }

            if (!hasXAxis) {
                hasXAxis = true;
                @NotNull XAxis x = new XAxis();
                x.setCategories((String[]) lables.toArray(new String[lables.size()]));
                conf.addxAxis(x);
            }

            @NotNull DataSeries dataSeries = new DataSeries(data);

            switch (vaadinChartSeries.type()) {
                case SPLINE: {
                    dataSeries.setPlotOptions(new PlotOptionsSpline());
                    break;
                }

                case COLUMN: {
                    @NotNull PlotOptionsColumn plotOptions = new PlotOptionsColumn();
                    plotOptions.setPointWidth(vaadinChartSeries.width());
                    dataSeries.setPlotOptions(plotOptions);
                    break;
                }
            }


            final String ylabel = vaadinChartSeries.yAxisLabel();

            if (ylabel != null && !ylabel.contentEquals("") && (yAxisLabel == null || !yAxisLabel
                    .equals(ylabel))) {
                @NotNull YAxis yAxis = new YAxis();
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
    private ColumnViewInternal.SortedFilter sortedFilter(@NotNull final VaadinChart vaadinChart) {

        @NotNull ColumnViewInternal.SortedFilter sortedFilter = new ColumnViewInternal.SortedFilter();

        long countFromEnd = vaadinChart.chartProperties().countFromEnd;
        if (countFromEnd > 0) {
            sortedFilter.countFromEnd = countFromEnd;
        }

        sortedFilter.marshableOrderBy = singletonList(new ColumnViewInternal
                .MarshableOrderBy(vaadinChart.columnNameField()));

        ChartProperties chartProperties = vaadinChart.chartProperties();
        if (chartProperties.filter != null)
            sortedFilter.marshableFilters = singletonList(chartProperties.filter);
        return sortedFilter;
    }

}