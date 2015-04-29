using System;
using System.Collections;
using System.Collections.Generic;

namespace Ddp.Api
{
    internal sealed class ChronicleDataCache<K, V> : IDataCache<K, V>
    {
        private IDictionary<K, V> _chronicleMapPlaceholder; 
        private readonly IList<DataCacheConfiguration> _dataCacheConfigurations;
        //TODO DS hold reference to Chronicle Stateless client

        internal ChronicleDataCache(DataCacheConfiguration dataCacheConfiguration)
            : this(new List<DataCacheConfiguration> {dataCacheConfiguration})
        { }

        internal ChronicleDataCache(IList<DataCacheConfiguration> dataCacheConfigurations)
        {
            _dataCacheConfigurations = dataCacheConfigurations;

            //TODO DS initialise Chronicle map with failover...
        }

        private T ExecuteWithFailover<T>(Func<T> methodToExecute)
        {
            //TODO DS implement retry/failover
            //TODO DS possible make another implementation for void methods
            return methodToExecute();
        }

        public IEnumerator<KeyValuePair<K, V>> GetEnumerator()
        {
            return ExecuteWithFailover(() => _chronicleMapPlaceholder.GetEnumerator());
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            //GetEnumerator will already retry
            return GetEnumerator();
        }

        public void Add(KeyValuePair<K, V> item)
        {

            ExecuteWithFailover<object>(() =>
            {
                _chronicleMapPlaceholder.Add(item);
                return null; //TODO DS call void failover method
            });
        }

        public void Clear()
        {
            ExecuteWithFailover<object>(() =>
            {
                _chronicleMapPlaceholder.Clear();
                return null; //TODO DS call void failover method
            });
        }

        public bool Contains(KeyValuePair<K, V> item)
        {
            return ExecuteWithFailover(() => _chronicleMapPlaceholder.Contains(item));
        }

        public void CopyTo(KeyValuePair<K, V>[] array, int arrayIndex)
        {
            ExecuteWithFailover<object>(() =>
            {
                _chronicleMapPlaceholder.CopyTo(array, arrayIndex);
                return null; //TODO DS call void failover method
            });
        }

        public bool Remove(KeyValuePair<K, V> item)
        {
            return ExecuteWithFailover(() => _chronicleMapPlaceholder.Remove(item));
        }

        //TODO DS implement
        public int Count
        {
            get
            {
                return ExecuteWithFailover(() => _chronicleMapPlaceholder.Count);
            }
        }

        public bool IsReadOnly
        {
            get
            {
                return ExecuteWithFailover(() => _chronicleMapPlaceholder.IsReadOnly);
            }
        }

        public bool ContainsKey(K key)
        {
            return ExecuteWithFailover(() => _chronicleMapPlaceholder.ContainsKey(key));
        }

        public void Add(K key, V value)
        {
            ExecuteWithFailover<object>(() =>
            {
                _chronicleMapPlaceholder.Add(key, value);
                return null; //TODO DS call void failover method
            });
        }

        public bool Remove(K key)
        {
            return ExecuteWithFailover(() => _chronicleMapPlaceholder.Remove(key));
        }

        public bool TryGetValue(K key, out V value)
        {
            //TODO DS cannot use out or ref in generic method - IMPLEMENT
            //return ExecuteWithFailover(() => _chronicleMapPlaceholder.TryGetValue(key, out value);
            throw new NotImplementedException();
        }

        public V this[K key]
        {
            get
            {
                //TODO DS test
                return ExecuteWithFailover(() => _chronicleMapPlaceholder[key]);
            }
            set
            {
                ExecuteWithFailover(() => _chronicleMapPlaceholder[key] = value);
            }
        }

        //TODO DS implement
        public ICollection<K> Keys
        {
            get
            {
                return ExecuteWithFailover(() => _chronicleMapPlaceholder.Keys);
            }
        }

        public ICollection<V> Values
        {
            get
            {
                return ExecuteWithFailover(() => _chronicleMapPlaceholder.Values);
            }
        }

        public void Dispose()
        {
            if (_chronicleMapPlaceholder != null)
            {
                //TODO DS dispose
            }
        }

        public bool AddEventListener(IDataCacheEventListener<K, V> dataCacheEventListener)
        {
            throw new NotImplementedException();
        }

        public bool RemoveEventListener(IDataCacheEventListener<K, V> dataCacheEventListener)
        {
            throw new NotImplementedException();
        }
    }
}