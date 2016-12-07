package net.openhft.chronicle.engine.gui;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Tree;
import net.openhft.chronicle.engine.api.column.BarChart;
import net.openhft.chronicle.engine.api.column.ColumnViewInternal;
import net.openhft.chronicle.engine.api.column.MapColumnView;
import net.openhft.chronicle.engine.api.column.QueueColumnView;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.query.Filter;
import net.openhft.chronicle.engine.tree.QueueView;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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
    private final BarChartUI histogramUI = new BarChartUI();

    TreeController(@NotNull final AssetTree remoteTree,
                   @NotNull TreeUI treeUI) {

        final Tree tree = treeUI.tree;

        RequestContext rc = RequestContext.requestContext("")
                .elementType(TopologicalEvent.class).bootstrap(true);

        final Subscriber<TopologicalEvent> sub = e -> updateTree(tree, e, remoteTree);
        remoteTree.acquireSubscription(rc).registerSubscriber(rc, sub, Filter.empty());

        clickListener = click -> {
            final String source = click.getItemId().toString();
            treeUI.contents.removeAllComponents();

            if (source.endsWith(BAR_CHART_VIEW)) {
                final Asset asset = findAsset(source, remoteTree);
                BarChart mapColumnView = asset.acquireView(BarChart.class);
                treeUI.contents.addComponent(histogramUI.getChart(mapColumnView));
                return;
            }

            if (source.endsWith(MAP_VIEW) || source.endsWith(QUEUE_VIEW)) {
                @NotNull MapViewUI mapViewUI = new MapViewUI();
                treeUI.contents.addComponent(mapViewUI);

                Asset asset = findAsset(source, remoteTree);

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


    public Asset findAsset(String source, AssetTree remoteTree) {
        @NotNull final String path = path(source);
        return remoteTree.acquireAsset(path);
    }

    private String path(String source) {
        final int len = source.length();

        for (String view : new String[]{MAP_VIEW, QUEUE_VIEW, BAR_CHART_VIEW}) {
            if (source.endsWith(view))
                return source.substring(0, len - view.length());
        }

        throw new IllegalStateException();
    }

    private void updateTree(@NotNull Tree tree, @NotNull TopologicalEvent e, AssetTree assetTree) {

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

            if (viewTypes.stream().anyMatch(BarChart.class::isAssignableFrom))
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

    private void addBarChart(@NotNull Tree tree, @NotNull TopologicalEvent e, AssetTree assetTree) {
        tree.addItem(e.fullName() + BAR_CHART_VIEW);
        tree.setParent(e.fullName() + BAR_CHART_VIEW, e.fullName());

            tree.setItemCaption(e.fullName() + BAR_CHART_VIEW, "barChart");
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
                () -> TreeController.class.getResourceAsStream("map.png"), "map"));
        tree.setChildrenAllowed(e.fullName() + QUEUE_VIEW, false);
    }

}
