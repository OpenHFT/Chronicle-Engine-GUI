
namespace Ddp.Api
{
    public interface IDataCacheEventListener<K, V>
    {
        /// <summary>
        /// Fired when a value for the given key is inserted or updated.
        /// </summary>
        /// <param name="key">Key for which the value is inserted/updated.</param>
        /// <param name="newValue">Updated value for the given key.</param>
        /// <param name="oldValue">Old value for the given key (null if it didn't exist).</param>
        void OnPut(K key, V newValue, V oldValue);

        /// <summary>
        /// Fired when a value for the given key is inserted or updated. This method ignores the previous value.
        /// </summary>
        /// <param name="key">Key for which the value is inserted/updated.</param>
        /// <param name="newValue">Updated value for the given key.</param>
        void OnPut(K key, V newValue);

        /// <summary>
        /// Fired when the give key/value pair is removed from the Data Cache.
        /// </summary>
        /// <param name="key">Key removed.</param>
        /// <param name="value">Value removed.</param>
        void OnRemove(K key, V value);

        /// <summary>
        /// Fired when the give key and associated value is removed from the Data Cache. Ignoring the value.
        /// </summary>
        /// <param name="key">Key removed.</param>
        void OnRemove(K key);
    }
}