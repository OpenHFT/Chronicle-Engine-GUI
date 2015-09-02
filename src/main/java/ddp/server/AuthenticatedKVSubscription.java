package ddp.server;

import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.map.EventConsumer;
import net.openhft.chronicle.engine.map.ObjectKVSSubscription;
import net.openhft.chronicle.engine.query.Filter;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

//TODO DS implement the rest of the functionality from VanillaKVSSubscription.
public class AuthenticatedKVSubscription<K, V> implements ObjectKVSSubscription<K, V> {
    private final SessionProvider sessionProvider;
    private Asset asset;
    private ObjectKVSSubscription underlying;
    private RequestContext requestContext;

    public AuthenticatedKVSubscription(RequestContext requestContext, Asset asset,
                                       ObjectKVSSubscription underlying) {
        System.out.println(this.getClass().getName() + ": constructor");
        this.requestContext = requestContext;
        this.asset = asset;
        this.underlying = underlying;
        this.sessionProvider = asset.findView(SessionProvider.class);
    }

    @Override
    public void registerKeySubscriber(@NotNull RequestContext rc, @NotNull Subscriber subscriber, @NotNull Filter filter) {
        System.out.println(this.getClass().getName() + ": registerKeySubscriber");

        //TODO DS consider what to do with bootstrapping if anything
        isAuthenticated();
        underlying.registerKeySubscriber(rc, subscriber, filter);
    }


    @Override
    public void registerTopicSubscriber(RequestContext rc, TopicSubscriber subscriber) {
        System.out.println(this.getClass().getName() + ": registerTopicSubscriber");

        //TODO DS consider what to do with bootstrapping if anything
        isAuthenticated();
        underlying.registerTopicSubscriber(rc, subscriber);
    }


    @Override
    public boolean needsPrevious() {
        System.out.println(this.getClass().getName() + ": needsPrevious");

        isAuthenticated();
        return underlying.needsPrevious();
    }

    @Override
    public void setKvStore(KeyValueStore store) {
        System.out.println(this.getClass().getName() + ": setKvStore");

        isAuthenticated();
        underlying.setKvStore(store);
    }

    @Override
    public void notifyEvent(MapEvent changeEvent) {
        System.out.println(this.getClass().getName() + ": notifyEvent - " + changeEvent + "    -     " + Instant.now());

        isAuthenticated();
        underlying.notifyEvent(changeEvent);
    }

    @Override
    public boolean hasSubscribers() {
        System.out.println(this.getClass().getName() + ": hasSubscribers");

        isAuthenticated();
        return underlying.hasSubscribers();
    }

    @Override
    public void registerSubscriber(@NotNull RequestContext rc,
                                   @NotNull Subscriber subscriber,
                                   @NotNull Filter filter) {
        System.out.println(this.getClass().getName() + ": registerSubscriber");

        isAuthenticated();
        underlying.registerSubscriber(rc, subscriber, filter);
    }

    @Override
    public void unregisterSubscriber(@NotNull Subscriber subscriber) {
        System.out.println(this.getClass().getName() + ": unregisterSubscriber");

        isAuthenticated();
        underlying.unregisterSubscriber(subscriber);
    }

    @Override
    public void unregisterTopicSubscriber(@NotNull TopicSubscriber subscriber) {
        System.out.println(this.getClass().getName() + ": unregisterTopicSubscriber");

        isAuthenticated();
        underlying.unregisterTopicSubscriber(subscriber);
    }

    @Override
    public void registerDownstream(EventConsumer subscription) {
        System.out.println(this.getClass().getName() + ": registerDownstream");

        isAuthenticated();
        underlying.registerDownstream(subscription);
    }

    @Override
    public int keySubscriberCount() {
        System.out.println(this.getClass().getName() + ": keySubscriberCount");

        isAuthenticated();
        return underlying.keySubscriberCount();
    }

    @Override
    public int entrySubscriberCount() {
        System.out.println(this.getClass().getName() + ": entrySubscriberCount");

        isAuthenticated();
        return underlying.entrySubscriberCount();
    }

    @Override
    public int topicSubscriberCount() {
        System.out.println(this.getClass().getName() + ": topicSubscriberCount");

        isAuthenticated();
        return underlying.topicSubscriberCount();
    }


    @Override
    public void close() {
        System.out.println(this.getClass().getName() + ": close");

        isAuthenticated();
        //todo Is there a problem with the underlying (RemoteKVSSubscription)
        //in that it doesn't send out events onEndOfSubscription
        underlying.close();
    }

    private void isAuthenticated() {
        SessionDetails sessionDetails = sessionProvider.get();
        if (sessionDetails != null) {
            String userId = sessionDetails.userId();

            System.out.println(this.getClass().getName() + ": Authenticating " + userId);
        }
    }
}