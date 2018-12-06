using System;
using System.Collections.Generic;

namespace Ddp.Api
{
    //TODO DS check if we need IDisposable when Chronicle C# api is implemented
    public interface IDataCache<K, V> : IDictionary<K, V>, IDisposable
    {
        bool AddEventListener(IDataCacheEventListener<K, V> dataCacheEventListener);

        bool RemoveEventListener(IDataCacheEventListener<K, V> dataCacheEventListener);
    }
}