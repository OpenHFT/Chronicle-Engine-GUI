package musiverification.helpers;

import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.query.*;
import net.openhft.chronicle.network.api.session.*;

/**
 * Created by daniels on 15/09/2015.
 */
public class CheckSessionDetailsSubscription<K, V> implements ObjectKVSSubscription<K, V>
{
    private final SessionProvider _sessionProvider;
    private final ObjectKVSSubscription _underlying;
    private final SessionDetails _sessionDetails;
    private final String _userId;

    public CheckSessionDetailsSubscription(RequestContext requestContext, Asset asset,
                                                 ObjectKVSSubscription underlying)
    {
        _sessionProvider = asset.findView(SessionProvider.class);
        _sessionDetails = _sessionProvider.get();
        _userId = _sessionDetails.userId();
        _underlying = underlying;

        checkSessionDetails();
    }

    private void checkSessionDetails()
    {
//        SessionDetails sessionDetails = _sessionProvider.get();

//        if (_sessionDetails == null)
//        {
//            throw new IllegalArgumentException("Session Details are null!");
//        }

//        String userId = _sessionDetails.userId();

        if (_userId == null || "".equals(_userId))
        {
            throw new IllegalArgumentException("UserId is either null or empty!");
        }

        System.out.println("######User Id Is Set#####: " + _userId);
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
        checkSessionDetails();
        _underlying.registerDownstream(subscription);
    }

    @Override
    public boolean needsPrevious()
    {
        checkSessionDetails();
        return _underlying.needsPrevious();
    }

    @Override
    public void setKvStore(KeyValueStore<K, V> store)
    {
        checkSessionDetails();
        _underlying.setKvStore(store);
    }

    @Override
    public void notifyEvent(MapEvent<K, V> changeEvent)
    {
        checkSessionDetails();
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
        checkSessionDetails();
        return _underlying.keySubscriberCount();
    }

    @Override
    public int entrySubscriberCount()
    {
        checkSessionDetails();
        return _underlying.entrySubscriberCount();
    }

    @Override
    public int topicSubscriberCount()
    {
        checkSessionDetails();
        return _underlying.topicSubscriberCount();
    }

    @Override
    public int subscriberCount()
    {
        checkSessionDetails();
        return _underlying.subscriberCount();
    }

    @Override
    public void close()
    {
        checkSessionDetails();
        _underlying.close();
    }
}