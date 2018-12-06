package ddp.api;

import java.io.*;
import java.util.*;

public interface DataCache<K, V> extends Map<K, V>, Closeable
{
    //TODO DS should we have methods for compress and put?

    //TODO DS do we need an event listener which takes a list of keys and only fires when all keys have been updated?

    boolean addEventListener(DataCacheEventListener dataCacheEventListener);

    boolean removeEventListener(DataCacheEventListener dataCacheEventListener);

    void close();
}