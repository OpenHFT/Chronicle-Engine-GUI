
namespace Ddp.Api
{
    public class DataCacheConfiguration
    {
        private string _hostname;
        private string _ipAddress;
        private int _port;
        private string _cacheName;

        public DataCacheConfiguration(string hostname, string ipAddress, int port, string cacheName)
        {
            _hostname = hostname;
            _ipAddress = ipAddress;
            _port = port;
            _cacheName = cacheName;
        }

        public string Hostname
        {
            get { return _hostname; }
        }

        public string IpAddress
        {
            get { return _ipAddress; }
        }

        public int Port
        {
            get { return _port; }
        }

        public string CacheName
        {
            get { return _cacheName; }
        }

        public override string ToString()
        {
            string toString = "DataCacheConfiguration{ hostname= " + Hostname
                + ", ipAddress= " + IpAddress
                + ", port= " + Port
                + ", cacheName= " + CacheName
                + " }";

            return toString;
        }
    }
}