package musiverification.helpers;

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

/**
 * Created by daniels on 15/09/2015.
 */
public class CheckSessionDetailsSubscription<K, V> implements ObjectSubscription<K, V>
{
    private final SessionProvider _sessionProvider;
    private final ObjectSubscription _underlying;

    public CheckSessionDetailsSubscription(RequestContext requestContext, Asset asset,
                                           ObjectSubscription underlying)
    {
        _sessionProvider = asset.findView(SessionProvider.class);
        _underlying = underlying;

//        checkSessionDetails();
    }

    private void checkSessionDetails()
    {
        SessionDetails sessionDetails = _sessionProvider.get();

        if (sessionDetails == null)
        {
            throw new IllegalArgumentException("Session Details are null!");
        }
        else {
            System.out.println("SessionDetails: " + sessionDetails);
        }

        String userId = sessionDetails.userId();

        if (userId == null || "".equals(userId))
        {
            throw new IllegalArgumentException("UserId is either null or empty!");
        }
        else
        {
            System.out.println("UserId: " + userId);
        }

        String domain = sessionDetails.domain();

        if (domain == null || "".equals(domain))
        {
            System.err.println("Domain: " + domain);
//            throw new IllegalArgumentException("Domain is either null or empty!");
        }
        else
        {
            System.out.println("Domain: " + domain);
        }
    }

    @Override
    public void registerKeySubscriber(RequestContext rc, Subscriber<K> subscriber, Filter<K> filter)
    {
        checkSessionDetails();
        _underlying.registerKeySubscriber(rc, subscriber, filter);
    }

    @Override
    public void registerTopicSubscriber(RequestContext rc, TopicSubscriber<K, V> subscriber)
    {
        checkSessionDetails();
        _underlying.registerTopicSubscriber(rc, subscriber);
    }

    @Override
    public void unregisterTopicSubscriber(TopicSubscriber subscriber)
    {
        checkSessionDetails();
        _underlying.unregisterTopicSubscriber(subscriber);
    }

    @Override
    public void registerDownstream(EventConsumer<K, V> subscription)
    {
//        checkSessionDetails();
        _underlying.registerDownstream(subscription);
    }

    @Override
    public boolean needsPrevious()
    {
//        checkSessionDetails();
        return _underlying.needsPrevious();
    }

    @Override
    public void setKvStore(KeyValueStore<K, V> store)
    {
//        checkSessionDetails();
        _underlying.setKvStore(store);
    }

    @Override
    public void notifyEvent(MapEvent<K, V> changeEvent)
    {
//        checkSessionDetails();
        _underlying.notifyEvent(changeEvent);
    }

    @Override
    public boolean hasSubscribers()
    {
//        checkSessionDetails();
        return _underlying.hasSubscribers();
    }

    @Override
    public void registerSubscriber(RequestContext rc, Subscriber<MapEvent<K, V>> subscriber, Filter<MapEvent<K, V>> filter)
    {
        checkSessionDetails();
        _underlying.registerSubscriber(rc, subscriber, filter);
    }

    @Override
    public void unregisterSubscriber(Subscriber subscriber)
    {
        checkSessionDetails();
        _underlying.unregisterSubscriber(subscriber);
    }

    @Override
    public int keySubscriberCount()
    {
//        checkSessionDetails();
        return _underlying.keySubscriberCount();
    }

    @Override
    public int entrySubscriberCount()
    {
//        checkSessionDetails();
        return _underlying.entrySubscriberCount();
    }

    @Override
    public int topicSubscriberCount()
    {
//        checkSessionDetails();
        return _underlying.topicSubscriberCount();
    }

    @Override
    public int subscriberCount()
    {
//        checkSessionDetails();
        return _underlying.subscriberCount();
    }

    @Override
    public void close()
    {
//        checkSessionDetails();
        _underlying.close();
    }
}