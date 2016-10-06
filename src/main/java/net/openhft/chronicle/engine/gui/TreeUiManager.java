package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.management.ManagementTools;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.tree.HostIdentifier;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.threads.NamedThreadFactory;
import net.openhft.chronicle.threads.Threads;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Rob Austin.
 */
public class TreeUiManager {

    protected Component newComponent(AssetTree tree) {

        TreeUI result = new TreeUI();

        registerViewOfTree(tree, result.tree);
        return result;
    }


    private static void registerViewOfTree(@NotNull AssetTree tree, Tree result) {
        Threads.withThreadGroup(tree.root().getView(ThreadGroup.class), () -> {
            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("tree-watcher", true));

            SessionProvider view = tree.root().findView(SessionProvider.class);
            final SessionDetails sessionDetails = view.get();

            ses.submit(() -> {
                // set the session details on the JMX thread, to the same as the server system session details.
                final SessionProvider view0 = tree.root().findView(SessionProvider.class);
                view0.set(sessionDetails);
            });

            tree.registerSubscriber("", TopologicalEvent.class, e -> {
                        // give the collection time to be setup.
                        if (e.assetName() != null) {
                            ses.schedule(() -> handleTreeUpdate(tree, e, ses, result), 2000, TimeUnit
                                    .MILLISECONDS);
                        }
                    }
            );
            return null;
        });
    }


    private static HashMap<String, Item> itemByPath = new HashMap<>();

    private static void handleTreeUpdate(@NotNull AssetTree tree, @NotNull TopologicalEvent e, @NotNull ScheduledExecutorService ses, Tree result) {
        try {
            HostIdentifier hostIdentifier = tree.root().getView(HostIdentifier.class);
            int hostId = hostIdentifier == null ? 0 : hostIdentifier.hostId();
            String treeName = tree.toString();
            if (e.added()) {

                Asset asset = tree.getAsset(e.fullName());
                assert asset != null;


                final Item item = result.addItem(e.name());
                itemByPath.put(e.fullName(), item);
                if (!"/".equals(e.assetName()))
                    result.setParent(itemByPath.get(e.assetName()), item);

                MapView mapView = asset.getView(MapView.class);
                if (mapView != null) {
                    result.setChildrenAllowed(item, false);
                } else
                    result.setChildrenAllowed(item, true);

              /*  ObjectKeyValueStore<Object, Object> view = null;

                for (Class c : new Class[]{ObjectKeyValueStore.class, SubscriptionKeyValueStore.class}) {

                    view = (ObjectKeyValueStore) asset.getView(c);
                    if (view != null) {
                        break;
                    }
                }

                if (view == null) {
                    return;
                }


                ObjectSubscription objectSubscription = asset.getView(ObjectSubscription.class);
                //ObjectName atName = new ObjectName(createObjectNameUri(e.assetName(),e.name(),treeName));

                //start Dynamic MBeans Code
                Map<String, String> m = new HashMap<>();
                m.put("size", "" + view.longSize());
                m.put("keyType", view.keyType().getName());
                m.put("valueType", view.valueType().getName());
                m.put("topicSubscriberCount", "" + objectSubscription.topicSubscriberCount());
                m.put("keySubscriberCount", "" + objectSubscription.keySubscriberCount());
                m.put("entrySubscriberCount", "" + objectSubscription.entrySubscriberCount());
                m.put("keyStoreValue", objectSubscription.getClass().getName());
                m.put("path", e.assetName() + "-" + e.name());

                for (int i = 0; i < view.segments(); i++) {
                    view.entriesFor(i, entry -> {
                        if (entry.getValue().toString().length() > 256) {
                            m.put("~" + entry.getKey().toString(), entry.getValue().toString().substring(0, 256) + "...");
                        } else {
                            m.put("~" + entry.getKey().toString(), entry.getValue().toString());
                        }
                    });
                }*/
                //     dynamicMBean = new AssetTreeDynamicMBean(m);
                //      ObjectName atName = new ObjectName(createObjectNameUri(hostId, e.assetName(), e
                //            .name(), treeName));
                //   registerTreeWithMBean(dynamicMBean, atName);
                //end Dynamic MBeans Code

                //   tree.registerSubscriber(e.fullName(), MapEvent.class, (MapEvent me) ->
                //         ses.schedule(() -> handleAssetUpdate(view0, atName, objectSubscription,
                //      e.assetName() + "-" + e.name()), 100, TimeUnit.MILLISECONDS));

                //AssetTreeJMX atBean = new AssetTreeJMX(view,objectKVSSubscription,e.assetName() + "-" + e.name(),getMapAsString(view));
                //registerTreeWithMBean(atBean, atName);

            } else {
                //  ObjectName atName = new ObjectName(createObjectNameUri(hostId, e.assetName(), e
                //        .name(), treeName));
                //  unregisterTreeWithMBean(atName);
            }
        } catch (Throwable t) {
            Jvm.warn().on(ManagementTools.class, "Error while handle AssetTree update", t);
        }
    }
}
