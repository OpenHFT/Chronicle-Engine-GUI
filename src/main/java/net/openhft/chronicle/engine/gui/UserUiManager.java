package net.openhft.chronicle.engine.gui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Rob Austin.
 */
class UserUiManager {


    Component newComponent() {
        final UserUI components = new UserUI();
        components.readBps.addComponent(newRandomChart("readBps"));
        components.writeBps.addComponent(newRandomChart("writeBps"));
        return components;
    }

    private Chart newRandomChart(final String text) {
        final Random random = new Random();

        final Chart chart = new Chart();
        chart.setWidth("500px");

        final Configuration configuration = chart.getConfiguration();

        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText(text);

        XAxis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(200);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("K Bytes Per Second"));
        yAxis.setPlotLines(new PlotLine(0, 1, new SolidColor("#808080")));

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series2 = new DataSeries();
        {
            PlotOptionsLine plotOptions = new PlotOptionsLine();
            series2.setPlotOptions(plotOptions);
        }
        final DataSeries series = new DataSeries();
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setColor(new SolidColor("#F0F0F0"));
        series.setPlotOptions(plotOptions);


        series.setName("Random data");
        for (int i = -(5 * 60); i <= 0; i++) {
            series.add(new DataSeriesItem(
                    System.currentTimeMillis() + i * 1000, random.nextDouble()));
            series2.add(new DataSeriesItem(
                    System.currentTimeMillis() + i * 1000, 0.5 + ((random.nextDouble() * 0.2) -
                    0.1)));
        }
        runWhileAttached(chart, () -> {
            final long x = System.currentTimeMillis();
            final double y = random.nextDouble();
            DataSeriesItem item = new DataSeriesItem(x, y);
            DataSeriesItem item1 = new DataSeriesItem(x, 0.5 + ((random.nextDouble() * 0.2) -
                    0.1));
            series.add(item, true, true);
            series2.add(item1, true, true);

            series.update(item);
            series2.update(item1);
        }, 2000, 2000);


        configuration.addSeries(series);
        configuration.addSeries(series2);

        chart.drawChart(configuration);
        chart.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return chart;
    }


    /**
     * Runs given task repeatedly until the reference component is attached
     *
     * @param component
     * @param task
     * @param interval
     * @param initialPause a timeout after tas is started
     */
    private static void runWhileAttached(final Component component,
                                         final Runnable task, final int interval, final int initialPause) {
        // Until reliable push available in our demo servers
        UI.getCurrent().setPollInterval(interval);

        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(initialPause);
                    while (component.getUI() != null) {
                        Future<Void> future = component.getUI().access(task);
                        future.get();
                        Thread.sleep(interval);
                    }
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                    System.out.println("Stopping repeating command due to an exception");

                } catch (com.vaadin.ui.UIDetachedException e) {
                } catch (Exception e) {
                    System.out.println("Unexpected exception while running scheduled update");
                }
                System.out.println("Thread stopped");
            }

            ;
        };
        thread.start();
    }


}
