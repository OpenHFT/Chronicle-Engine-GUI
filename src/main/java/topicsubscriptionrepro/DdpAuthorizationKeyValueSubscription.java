package topicsubscriptionrepro;

import net.openhft.chronicle.engine.api.PermissionDeniedException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Supplier;

public class DdpAuthorizationKeyValueSubscription<K, V> implements ObjectSubscription<K, V>
{
    private static final Logger _logger = LoggerFactory.getLogger(DdpAuthorizationKeyValueSubscription.class);
    private static final String _throwOnCreateMapName = "/throw/on/create";
    private static final String _throwOnGetMapName = "/throw/on/get";

    private final String _assetName;
    private final SessionProvider _sessionProvider;
    private final Asset _asset;
    private final ObjectSubscription _underlying;
    private final RequestContext _requestContext;

    public DdpAuthorizationKeyValueSubscription(RequestContext requestContext, Asset asset,
                                                ObjectSubscription underlying)
    {
        System.out.println(this.getClass().getName() + ": constructor");

        _requestContext = requestContext;
        _asset = asset;
        _assetName = asset.name();
        _sessionProvider = asset.findView(SessionProvider.class);
        _underlying = underlying;

        //TODO DS check create permissions
    }

    @Override
    public void registerKeySubscriber(@NotNull RequestContext rc, @NotNull Subscriber subscriber, @NotNull Filter filter)
    {
        System.out.println(this.getClass().getName() + ": registerKeySubscriber - hashcode " + hashCode());

        //TODO DS consider what to do with bootstrapping if anything
        checkPermissions("SUBSCRIBE", true);

        _underlying.registerKeySubscriber(rc, subscriber, filter);
    }

    @Override
    public void registerTopicSubscriber(RequestContext rc, TopicSubscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": registerTopicSubscriber - hashcode " + hashCode());

        //TODO DS consider what to do with bootstrapping if anything
        checkPermissions("SUBSCRIBE", true);

        _underlying.registerTopicSubscriber(rc, subscriber);
    }

    @Override
    public boolean needsPrevious()
    {
        System.out.println(this.getClass().getName() + ": needsPrevious");

        //TODO DS do we need a special data permission for this? Admin or the like?
//        checkPermissions(DataPermission.CREATE);

        return _underlying.needsPrevious();
    }

    @Override
    public void setKvStore(KeyValueStore store)
    {
        System.out.println(this.getClass().getName() + ": setKvStore");

        checkPermissions("CREATE", true);

        _underlying.setKvStore(store);
    }

    @Override
    public void notifyEvent(MapEvent changeEvent)
    {
        System.out.println(this.getClass().getName() + ": notifyEvent - " + changeEvent + "    -     " + Instant.now());

        boolean checkPermissions = checkPermissions("SUBSCRIBE", false);

        if (checkPermissions("SUBSCRIBE", false))
        {
            _underlying.notifyEvent(changeEvent);
        }
    }

    @Override
    public boolean hasSubscribers()
    {
        System.out.println(this.getClass().getName() + ": hasSubscribers");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("POLL", true);

        return _underlying.hasSubscribers();
    }

    @Override
    public void registerSubscriber(@NotNull RequestContext rc,
                                   @NotNull Subscriber subscriber,
                                   @NotNull Filter filter)
    {
        System.out.println(this.getClass().getName() + ": registerSubscriber - hashcode " + hashCode());

        checkPermissions("SUBSCRIBE", true);

        _underlying.registerSubscriber(rc, subscriber, filter);
    }

    @Override
    public void unregisterSubscriber(@NotNull Subscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": unregisterSubscriber");

        //TODO DO we need to check permissions for this? Probably not

        _underlying.unregisterSubscriber(subscriber);
    }

    @Override
    public void unregisterTopicSubscriber(@NotNull TopicSubscriber subscriber)
    {
        System.out.println(this.getClass().getName() + ": unregisterTopicSubscriber");

        //TODO DO we need to check permissions for this? Probably not

        _underlying.unregisterTopicSubscriber(subscriber);
    }

    @Override
    public void registerDownstream(EventConsumer subscription)
    {
        System.out.println(this.getClass().getName() + ": registerDownstream");

        checkPermissions("SUBSCRIBE", true);

        _underlying.registerDownstream(subscription);
    }

    @Override
    public int keySubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": keySubscriberCount");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("POLL", true);

        return _underlying.keySubscriberCount();
    }

    @Override
    public int entrySubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": entrySubscriberCount");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("POLL", true);

        return _underlying.entrySubscriberCount();
    }

    @Override
    public int topicSubscriberCount()
    {
        System.out.println(this.getClass().getName() + ": topicSubscriberCount");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("POLL", true);

        return _underlying.topicSubscriberCount();
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("REMOVE", false);

        _underlying.close();
    }

    private boolean checkPermissions(String permissionRequired, boolean logAndThrowException)
    {
        SessionDetails sessionDetails = _sessionProvider.get();

        if (sessionDetails == null)
        {
            return handlePermissionDenied(logAndThrowException,
                    () -> String.format("Session details not set on request on _asset '%s'!", _assetName));
        }

        String userId = sessionDetails.userId();

        System.out.println("Checking permissions {" + permissionRequired + "} for user " + userId);

//        if (_throwOnCreateMapName.equals(_assetName) && "CREATE".equals(permissionRequired))
//        {
//            throw new PermissionDeniedException(String.format("User '%s' does not have the required '%s' data access level " +
//                    "to _asset '%s'!", userId, permissionRequired, _assetName));
//        }
//        else if (_throwOnGetMapName.equals())

        return true;
    }

    private boolean handlePermissionDenied(boolean logAndThrowException, Supplier<String> errorMessageSupplier)
    {
        if (logAndThrowException)
        {
            String errorMessage = errorMessageSupplier.get();

            _logger.error(errorMessage);

            throw new PermissionDeniedException(errorMessage);
        }

        return false;
    }
}