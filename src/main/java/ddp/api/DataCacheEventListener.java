package ddp.api;

/**
 * Data update listener.
 *
 * @param <K> Key type of the data cache.
 * @param <V> Value type of the data cache.
 */
public interface DataCacheEventListener<K, V>
{
    //TODO DS Providing default implementations that do nothing will still suffer deserialisation cost right? Also, we cannot provide default interface implementations in C#.
    /**
     * Fired when a value for the given key is inserted or updated.
     *
     * @param key      Key for which the value is inserted/updated.
     * @param newValue Updated value for the given key.
     * @param oldValue Old value for the given key (null if it didn't exist).
     */
    public void onPut(K key, V newValue, V oldValue);

    /**
     * Fired when a value for the given key is inserted or updated. This method ignores the previous value.
     *
     * @param key      Key for which the value is inserted/updated.
     * @param newValue Updated value for the given key.
     */
    public void onPut(K key, V newValue);

    /**
     * Fired when the give key/value pair is removed from the Data Cache.
     *
     * @param key   Key removed.
     * @param value Value removed.
     */
    public void onRemove(K key, V value);

    /**
     * Fired when the give key and associated value is removed from the Data Cache. Ignoring the value.
     *
     * @param key Key removed.
     */
    public void onRemove(K key);
}