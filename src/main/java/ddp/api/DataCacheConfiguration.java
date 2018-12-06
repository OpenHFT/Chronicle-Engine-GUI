package ddp.api;

public class DataCacheConfiguration
{
    private String _hostname;
    private String _ipAddress;
    private int _port;
    private String _cacheName;

    public DataCacheConfiguration(String hostname, String ipAddress, int port, String cacheName)
    {
        _hostname = hostname;
        _ipAddress = ipAddress;
        _port = port;
        _cacheName = cacheName;
    }

    public String getHostname()
    {
        return _hostname;
    }

    public String getIpAddress()
    {
        return _ipAddress;
    }

    public int getPort()
    {
        return _port;
    }

    public String getCacheName()
    {
        return _cacheName;
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DataCacheConfiguration{ ");
        stringBuilder.append("hostname= ").append(getHostname());
        stringBuilder.append(", ");
        stringBuilder.append("ipAddress= ").append(getIpAddress());
        stringBuilder.append(", ");
        stringBuilder.append("port= ").append(getPort());
        stringBuilder.append(", ");
        stringBuilder.append("cacheName= ").append(getCacheName());
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }
}