package org.test;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import javax.servlet.annotation.WebServlet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        UserConsole components = new UserConsole();
        setContent(components);

        components.readBps.addComponent(chart("readBps"));
        components.writeBps.addComponent(chart("writeBps"));
    }


    /**
     * Runs given task repeatedly until the reference component is attached
     *
     * @param component
     * @param task
     * @param interval
     * @param initialPause a timeout after tas is started
     */
    public static void runWhileAttached(final Component component,
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




    private Chart chart(final String text) {
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
          //  plotOptions.setColor(SolidColor.ALICEBLUE);
           // plotOptions.setLineWidth(3);
            series2.setPlotOptions(plotOptions);
        }
        final DataSeries series = new DataSeries();
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setColor(new SolidColor("#F0F0F0"));
        series.setPlotOptions(plotOptions);


        series.setName("Random data");
        for (int i = -(5*60); i <= 0; i++) {
            series.add(new DataSeriesItem(
                    System.currentTimeMillis() + i * 1000, random.nextDouble()));
            series2.add(new DataSeriesItem(
                    System.currentTimeMillis() + i * 1000, 0.5 +((random.nextDouble() * 0.2) -
                    0.1)));
        }
        runWhileAttached(chart, new Runnable() {

            @Override
            public void run() {
                final long x = System.currentTimeMillis();
                final double y = random.nextDouble();
                DataSeriesItem item = new DataSeriesItem(x, y);
                DataSeriesItem item1 = new DataSeriesItem(x, 0.5 + ((random.nextDouble() * 0.2) -
                        0.1));
                series.add(item, true, true);
                series2.add(item1, true, true);

                series.update(item);
                series2.update(item1);
            }
        }, 2000, 2000);


        configuration.addSeries(series);
        configuration.addSeries(series2);

        chart.drawChart(configuration);
        chart.setWidth(100, Unit.PERCENTAGE);
        return chart;
    }



    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


    public static void main(String[] args) {

    }
}
