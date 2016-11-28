package net.openhft.chronicle.engine.gui;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Tree;
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
public class TreeController {


    static {
        RequestContext.loadDefaultAliases();
    }


    public static final String MAP_VIEW = "::map_view";
    public static final String QUEUE_VIEW = "::queue_view";

    @NotNull
    final ItemClickEvent.ItemClickListener clickListener;

    public TreeController(@NotNull AssetTree remoteTree,
                          @NotNull TreeUI treeUI) {

        final Tree tree = treeUI.tree;

        RequestContext rc = RequestContext.requestContext("")
                .elementType(TopologicalEvent.class).bootstrap(true);

        final Subscriber<TopologicalEvent> sub = e -> updateTree(tree, e);
        remoteTree.acquireSubscription(rc).registerSubscriber(rc, sub, Filter.empty());

        clickListener = click -> {
            final String source = click.getItemId().toString();
            treeUI.contents.removeAllComponents();


            if (source.endsWith(MAP_VIEW) || source.endsWith(QUEUE_VIEW)) {
                @NotNull MapViewUI mapViewUI = new MapViewUI();
                treeUI.contents.addComponent(mapViewUI);

                final int len = source.length();

                @NotNull final String path = source.endsWith(MAP_VIEW) ?
                        source.substring(0, len - MAP_VIEW.length()) :
                        source.substring(0, len - QUEUE_VIEW.length());

                @NotNull
                Asset asset = remoteTree.acquireAsset(path);

                @Nullable
                final ColumnViewInternal view = source.endsWith(MAP_VIEW) ?
                        asset.acquireView(MapColumnView.class) :
                        asset.acquireView(QueueColumnView.class);

                @NotNull
                ColumnViewController mapControl = new ColumnViewController(view, mapViewUI, path);
                mapControl.init();

            }

        };

        tree.addItemClickListener(clickListener);
    }


    private void updateTree(@NotNull Tree tree, @NotNull TopologicalEvent e) {

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


        System.out.println("*******************************     e.assetName()=" + e.fullName());

        Set<Class> viewTypes = e.viewTypes();
        viewTypes.forEach(System.out::println);


        try {
            if (viewTypes.stream().anyMatch(QueueView.class::isAssignableFrom)) {

                tree.addItem(e.fullName() + QUEUE_VIEW);
                tree.setParent(e.fullName() + QUEUE_VIEW, e.fullName());
                tree.setItemCaption(e.fullName() + QUEUE_VIEW, "queue");
                tree.setItemIcon(e.fullName() + QUEUE_VIEW, new StreamResource(
                        () -> TreeController.class.getResourceAsStream("map.png"), "map"));
                tree.setChildrenAllowed(e.fullName() + QUEUE_VIEW, false);

                System.out.println("queue at :" + e.fullName());
                return;
            }

            if (viewTypes.stream().anyMatch(MapView.class::isAssignableFrom)) {

                tree.addItem(e.fullName() + MAP_VIEW);
                tree.setParent(e.fullName() + MAP_VIEW, e.fullName());
                tree.setItemCaption(e.fullName() + MAP_VIEW, "map");
                tree.setItemIcon(e.fullName() + MAP_VIEW, new StreamResource(
                        () -> TreeController.class.getResourceAsStream("map.png"), "map"));
                tree.setChildrenAllowed(e.fullName() + MAP_VIEW, false);
                System.out.println("map at :" + e.fullName());
                return;
            }


        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.out.println("finished " + e.assetName());
        }


    }

}
