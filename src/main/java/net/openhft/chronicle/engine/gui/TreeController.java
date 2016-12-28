package net.openhft.chronicle.engine.gui;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Tree;
import net.openhft.chronicle.engine.api.column.ColumnViewInternal;
import net.openhft.chronicle.engine.api.column.MapColumnView;
import net.openhft.chronicle.engine.api.column.QueueColumnView;
import net.openhft.chronicle.engine.api.column.VaadinChart;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.query.Filter;
import net.openhft.chronicle.engine.tree.QueueView;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.threads.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Rob Austin.
 */
class TreeController {

    static {
        RequestContext.loadDefaultAliases();
    }

    private static final String MAP_VIEW = "::map_view";
    private static final String QUEUE_VIEW = "::queue_view";
    private static final String BAR_CHART_VIEW = "::barchart_view";
    @NotNull
    final ItemClickEvent.ItemClickListener clickListener;
    @NotNull
    private final ChartUI histogramUI = new ChartUI();
    @NotNull
    static ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory
            ("scheduler", true));

    TreeController(@NotNull final AssetTree remoteTree,
                   @NotNull TreeUI treeUI) {

        final Tree tree = treeUI.tree;

        @NotNull RequestContext rc = RequestContext.requestContext("")
                .elementType(TopologicalEvent.class).bootstrap(true);

        @NotNull final Subscriber<TopologicalEvent> sub = e -> updateTree(tree, e, remoteTree);
        remoteTree.acquireSubscription(rc).registerSubscriber(rc, sub, Filter.empty());

        clickListener = click -> {
            final String source = click.getItemId().toString();
            treeUI.contents.removeAllComponents();

            if (source.endsWith(BAR_CHART_VIEW)) {
                @NotNull final Asset asset = findAsset(source, remoteTree);
                @NotNull VaadinChart chart = asset.acquireView(VaadinChart.class);
                treeUI.contents.addComponent(histogramUI.getChart(chart));
                return;
            }

            if (source.endsWith(MAP_VIEW) || source.endsWith(QUEUE_VIEW)) {
                @NotNull MapViewUI mapViewUI = new MapViewUI();
                treeUI.contents.addComponent(mapViewUI);

                @NotNull Asset asset = findAsset(source, remoteTree);

                @Nullable
                final ColumnViewInternal view = source.endsWith(MAP_VIEW) ?
                        asset.acquireView(MapColumnView.class) :
                        asset.acquireView(QueueColumnView.class);

                @NotNull
                ColumnViewController mapControl = new ColumnViewController(view,
                        mapViewUI,
                        path(source));
                mapControl.init();

            }

        };

        tree.addItemClickListener(clickListener);
    }


    @NotNull
    public Asset findAsset(@NotNull String source, @NotNull AssetTree remoteTree) {
        @NotNull final String path = path(source);
        return remoteTree.acquireAsset(path);
    }

    private String path(@NotNull String source) {
        final int len = source.length();

        for (@NotNull String view : new String[]{MAP_VIEW, QUEUE_VIEW, BAR_CHART_VIEW}) {
            if (source.endsWith(view))
                return source.substring(0, len - view.length());
        }

        throw new IllegalStateException();
    }

    private void updateTree(@NotNull Tree tree, @NotNull TopologicalEvent e, @NotNull AssetTree assetTree) {

        if (e.assetName() == null)
            return;

        tree.markAsDirty();

        tree.addItem(e.fullName());
        tree.setItemCaption(e.fullName(), e.name());

        if (!"/".equals(e.assetName()))
            tree.setParent(e.fullName(), e.assetName());

        tree.setItemIcon(e.fullName(), new StreamResource(
                () -> TreeController.class.getResourceAsStream("folder.png"), "folder"));

        tree.setChildrenAllowed(e.fullName(), true);


        Set<Class> viewTypes = e.viewTypes();
        viewTypes.forEach(System.out::println);

        try {

            if (viewTypes.stream().anyMatch(VaadinChart.class::isAssignableFrom))
                addBarChart(tree, e, assetTree);

            if (viewTypes.stream().anyMatch(QueueView.class::isAssignableFrom)) {
                addQueue(tree, e);
                return;
            }

            if (viewTypes.stream().anyMatch(MapView.class::isAssignableFrom)) {
                addMap(tree, e);
                return;
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void addBarChart(@NotNull final Tree tree, @NotNull TopologicalEvent e, @NotNull AssetTree
            assetTree) {
        tree.addItem(e.fullName() + BAR_CHART_VIEW);
        tree.setParent(e.fullName() + BAR_CHART_VIEW, e.fullName());

        // we can make an RPC call engine, while inside a TopologicalEvent
        executorService.submit(() -> {
            @NotNull VaadinChart chart = assetTree.acquireAsset(e.fullName()).acquireView(VaadinChart.class);
            final String menuLabel = chart.chartProperties().menuLabel;
            tree.setItemCaption(e.fullName() + BAR_CHART_VIEW, menuLabel == null ? "bar-chart" :
                    menuLabel);
        });

        tree.setItemIcon(e.fullName() + BAR_CHART_VIEW, new StreamResource(
                () -> TreeController.class.getResourceAsStream("chart.png"), "chart"));
        tree.setChildrenAllowed(e.fullName() + BAR_CHART_VIEW, false);
    }

    private void addMap(@NotNull Tree tree, @NotNull TopologicalEvent e) {
        tree.addItem(e.fullName() + MAP_VIEW);
        tree.setParent(e.fullName() + MAP_VIEW, e.fullName());
        tree.setItemCaption(e.fullName() + MAP_VIEW, "map");
        tree.setItemIcon(e.fullName() + MAP_VIEW, new StreamResource(
                () -> TreeController.class.getResourceAsStream("map.png"), "map"));
        tree.setChildrenAllowed(e.fullName() + MAP_VIEW, false);
    }

    private void addQueue(@NotNull Tree tree, @NotNull TopologicalEvent e) {
        tree.addItem(e.fullName() + QUEUE_VIEW);
        tree.setParent(e.fullName() + QUEUE_VIEW, e.fullName());
        tree.setItemCaption(e.fullName() + QUEUE_VIEW, "queue");
        tree.setItemIcon(e.fullName() + QUEUE_VIEW, new StreamResource(
                () -> TreeController.class.getResourceAsStream("queue.png"), "queue"));
        tree.setChildrenAllowed(e.fullName() + QUEUE_VIEW, false);
    }

}
