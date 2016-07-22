package topicsubscriptionrepro;

import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.map.EventConsumer;
import net.openhft.chronicle.engine.map.ObjectSubscription;
import net.openhft.chronicle.engine.query.Filter;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

//TODO DS implement the rest of the functionality from VanillaKVSSubscription.
public class DdpAuthenticationKeyValueSubscription<K, V> implements ObjectSubscription<K, V>
{
    private final String _assetName;
    private final SessionProvider _sessionProvider;
    private final Asset _asset;
    private final ObjectSubscription _underlying;
    private final RequestContext _requestContext;

    public DdpAuthenticationKeyValueSubscription(RequestContext requestContext, Asset asset,
                                                 ObjectSubscription underlying)
    {
        System.out.println(this.getClass().getName() + ": constructor");

        _requestContext = requestContext;
        _asset = asset;
        _assetName = asset.fullName();
        _sessionProvider = asset.findView(SessionProvider.class);
        _underlying = underlying;
    }

    @Override
    public void registerKeySubscriber(@NotNull RequestContext rc, @NotNull Subscriber subscriber, @NotNull Filter filter)
    {
        System.out.println(this.getClass().getName() + ": registerKeySubscriber - hashcode " + hashCode());

        isAuthenticated();
        _underlying.registerKeySubscriber(rc, subscriber, filter);
    }

    @Override
    public void registerTopicSubscriber(RequestContext rc, TopicSubscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": registerTopicSubscriber - hashcode " + hashCode());

        isAuthenticated();
        _underlying.registerTopicSubscriber(rc, subscriber);
    }

    @Override
    public boolean needsPrevious()
    {
        System.out.println(this.getClass().getName() + ": needsPrevious");

        isAuthenticated();
        return _underlying.needsPrevious();
    }

    @Override
    public void setKvStore(KeyValueStore store)
    {
        System.out.println(this.getClass().getName() + ": setKvStore");

        isAuthenticated();
        _underlying.setKvStore(store);
    }

    @Override
    public void notifyEvent(MapEvent changeEvent)
    {
        System.out.println(this.getClass().getName() + ": notifyEvent - " + changeEvent + "    -     " + Instant.now());

        isAuthenticated();
        _underlying.notifyEvent(changeEvent);
    }

    @Override
    public boolean hasSubscribers()
    {
        System.out.println(this.getClass().getName() + ": hasSubscribers");

        isAuthenticated();
        return _underlying.hasSubscribers();
    }

    @Override
    public void registerSubscriber(@NotNull RequestContext rc,
                                   @NotNull Subscriber subscriber,
                                   @NotNull Filter filter)
    {
        System.out.println(this.getClass().getName() + ": registerSubscriber - hashcode " + hashCode());

        isAuthenticated();
        _underlying.registerSubscriber(rc, subscriber, filter);
    }

    @Override
    public void unregisterSubscriber(@NotNull Subscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": unregisterSubscriber");

        isAuthenticated();
        _underlying.unregisterSubscriber(subscriber);
    }

    @Override
    public void unregisterTopicSubscriber(@NotNull TopicSubscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": unregisterTopicSubscriber");

        isAuthenticated();
        _underlying.unregisterTopicSubscriber(subscriber);
    }

    @Override
    public void registerDownstream(EventConsumer subscription)
    {
        System.out.println(this.getClass().getName() + ": registerDownstream");

        isAuthenticated();
        _underlying.registerDownstream(subscription);
    }

    @Override
    public int keySubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": keySubscriberCount");

        isAuthenticated();
        return _underlying.keySubscriberCount();
    }

    @Override
    public int entrySubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": entrySubscriberCount");

        isAuthenticated();
        return _underlying.entrySubscriberCount();
    }

    @Override
    public int topicSubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": topicSubscriberCount");

        isAuthenticated();
        return _underlying.topicSubscriberCount();
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        isAuthenticated();
        //in that it doesn't send out events onEndOfSubscription
        _underlying.close();
    }

    private void isAuthenticated()
    {
        SessionDetails sessionDetails = _sessionProvider.get();

        String userId = sessionDetails.userId();

        System.out.println(this.getClass().getName() + ": Authenticating " + userId);

        //Do nothing - everyone is authenticated
    }
}