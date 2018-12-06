using System.Collections;
using System.Collections.Generic;
using Ddp.Api.Authorisation;
using Ddp.Api.Security;

namespace Ddp.Api
{
    //TODO DS check whether Chronicle map implements all of the below methods
    //TODO DS implement access check on all of them and retry with failover...
    public sealed class RestrictedDataCache<K, V> : IDataCache<K, V>
    {
        private Entitlement _entitlement;
        private IDataCache<K, V> _underlyingDataCache;

        internal RestrictedDataCache(IDataCache<K, V> underlyingDataCache, Entitlement entitlement)
        {
            _underlyingDataCache = underlyingDataCache;
            _entitlement = entitlement;
        }

        public IEnumerator<KeyValuePair<K, V>> GetEnumerator()
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Read);

            return _underlyingDataCache.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Read);

            return GetEnumerator();
        }

        public void Add(KeyValuePair<K, V> item)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            _underlyingDataCache.Add(item);
        }

        public void Clear()
        {
            //TODO DS should we have a specific access level which allows the deletion of keys?
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            _underlyingDataCache.Clear();
        }

        public bool Contains(KeyValuePair<K, V> item)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Read);

            return _underlyingDataCache.Contains(item);
        }

        public void CopyTo(KeyValuePair<K, V>[] array, int arrayIndex)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            _underlyingDataCache.CopyTo(array, arrayIndex);
        }

        public bool Remove(KeyValuePair<K, V> item)
        {
            //TODO DS should we have a specific access level which allows the deletion of keys?
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            return _underlyingDataCache.Remove(item);
        }

        public int Count {
            get
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Read);

                return _underlyingDataCache.Count;
            }
        }

        public bool IsReadOnly
        {
            get
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Read);

                return _underlyingDataCache.IsReadOnly;
            }
        }

        public bool ContainsKey(K key)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Read);

            return _underlyingDataCache.ContainsKey(key);
        }

        public void Add(K key, V value)
        {
            //TODO DS should we have a specific access level which allows the addition/update of keys?
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            _underlyingDataCache.Add(key, value);
        }

        public bool Remove(K key)
        {
            //TODO DS should we have a specific access level which allows the deletion of keys?
            _entitlement.CheckDataAccess(DataAccessLevel.Write);

            return _underlyingDataCache.Remove(key);
        }

        public bool TryGetValue(K key, out V value)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Read);

            return _underlyingDataCache.TryGetValue(key, out value);
        }

        //TODO DS test
        //TODO DS should we override this with put/get? Depends on Chronicle Map implementation.
        public V this[K key]
        {
            get
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Read);

                return _underlyingDataCache[key];
            }
            set
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Write);

                _underlyingDataCache[key] = value;
            }
        }

        //TODO DS implement
        public ICollection<K> Keys
        {
            get
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Read);

                return _underlyingDataCache.Keys;
            }
        }

        public ICollection<V> Values
        {
            get
            {
                _entitlement.CheckDataAccess(DataAccessLevel.Read);

                return _underlyingDataCache.Values;
            }
        }

        public void Dispose()
        {
            //TODO DS do we need to authorise this? Wouldn't think so
            if (_underlyingDataCache != null)
            {
                _underlyingDataCache.Dispose();
            }
        }

        public bool AddEventListener(IDataCacheEventListener<K, V> dataCacheEventListener)
        {
            _entitlement.CheckDataAccess(DataAccessLevel.Subscribe);

            return _underlyingDataCache.AddEventListener(dataCacheEventListener);
        }

        //TODO DS execute this when subscribe access is dynamically removed.
        public bool RemoveEventListener(IDataCacheEventListener<K, V> dataCacheEventListener)
        {
            //TODO DS do we need to authorise this? Should be okay to remove any event listeners even if no longer has authorisation to subscribe?
            return _underlyingDataCache.RemoveEventListener(dataCacheEventListener);
        }
    }
}