package losingserverupdatesissue;

/**
 * Simply class that stores string of key and value
 * Plus the lag that should be applied when process, i.e. sleep time in milliseconds
 */
public class MimicMarketDataUpdate
{
    private final String _key;
    private final String _value;
    private final long _lag;

    public MimicMarketDataUpdate(String key, String value, long lag)
    {
        _key = key;
        _value = value;
        _lag = lag;
    }

    /**
     * Market data key
     * @return
     */
    public String getKey()
    {
        return _key;
    }

    /**
     * Market data value
     * @return
     */
    public String getValue()
    {
        return _value;
    }

    /**
     * The lag in milliseconds
     * @return
     */
    public long getLag()
    {
        return _lag;
    }
}
