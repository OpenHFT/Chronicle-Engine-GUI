package net.openhft.chronicle.engine.gui;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.threads.NamedThreadFactory;
import net.openhft.chronicle.threads.Threads;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Rob Austin.
 */
public class TreeUiManager {

    protected Component newComponent(AssetTree tree) {

        TreeUI result = new TreeUI();

        registerViewOfTree(tree, result.tree);
        return result;
    }


    private static void registerViewOfTree(@NotNull AssetTree assetTree, Tree tree) {
        Threads.withThreadGroup(assetTree.root().getView(ThreadGroup.class), () -> {
            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("tree-watcher", true));

            SessionProvider view = assetTree.root().findView(SessionProvider.class);
            final SessionDetails sessionDetails = view.get();

            ses.submit(() -> {
                // set the session details on the JMX thread, to the same as the server system session details.
                final SessionProvider view0 = assetTree.root().findView(SessionProvider.class);
                view0.set(sessionDetails);
            });

            assetTree.registerSubscriber("", TopologicalEvent.class, e -> {

                        if (e.assetName() != null) {

                            handleTreeUpdate(assetTree, e, tree);
                        }
                    }
            );

            return null;
        });
    }

    private static void handleTreeUpdate(@NotNull AssetTree tree, @NotNull TopologicalEvent e, Tree result) {

        if (e.added()) {
            result.markAsDirty();

            Asset asset = tree.getAsset(e.fullName());
            assert asset != null;

            result.addItem(e.fullName());
            result.setItemCaption(e.fullName(), e.name());

            if (!"/".equals(e.assetName()))
                result.setParent(e.fullName(), e.assetName());

            MapView mapView = asset.getView(MapView.class);
            if (mapView != null) {
                result.setItemIcon(e.fullName(), new StreamResource(
                        () -> TreeUiManager.class.getResourceAsStream("map.png"), "map"));
                result.setChildrenAllowed(e.fullName(), false);
            } else {
                result.setItemIcon(e.fullName(), new StreamResource(
                        () -> TreeUiManager.class.getResourceAsStream("folder.png"), "folder"));

                result.setChildrenAllowed(e.fullName(), true);
            }

        }

    }
}
