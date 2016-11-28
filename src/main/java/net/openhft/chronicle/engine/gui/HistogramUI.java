package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import net.openhft.chronicle.engine.api.column.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class HistogramUI {

    public String getDescription() {
        return "Basic bar with point width and range set";
    }


    protected Component getChart(MapColumnView mapColumnView) {

        ClosableIterator<? extends Row> iterator = mapColumnView.iterator(orderBytKey());


        PlotOptionsColumn plotOptions;
        Chart chart;
        chart = new Chart(ChartType.COLUMN);

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Visualize point width and point range");

        plotOptions = new PlotOptionsColumn();
        // plotOptions.setPointRange(10);
        plotOptions.setPointWidth(100);
        conf.setPlotOptions(plotOptions);

        HistogramBucket histogramBucket = new HistogramBucket();

        List<DataSeriesItem> data = new ArrayList<>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            HistogramBucket histogramBucket1 = row.copyTo(histogramBucket);
            data.add(new DataSeriesItem(histogramBucket1.label(), histogramBucket.count()));
        }

        conf.setSeries(new DataSeries(data));

        chart.drawChart(conf);

        return chart;
    }

    @NotNull
    private ColumnViewInternal.SortedFilter orderBytKey() {
        ColumnViewInternal.SortedFilter sortedFilter = new ColumnViewInternal.SortedFilter();
        sortedFilter.marshableOrderBy = Collections.singletonList(new ColumnViewInternal
                .MarshableOrderBy("key"));
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