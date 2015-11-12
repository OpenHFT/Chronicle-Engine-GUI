using System.Net;
using OpenHFT;

namespace TestRealTimeThrottling.Utilities
{
    /// <summary>
    /// Util functions for Chronicle related operations
    /// </summary>
    public class ChronicleUtil
    {
        /// <summary>
        /// Creates a new instance of a Chroncle Map Client. Connects to the configured Chronicle server.
        /// </summary>
        /// <param name="username">Username of user connecting - default is set to cShartClient</param>
        /// <param name="password">Password for user connecting - default is set to password</param>
        /// <param name="host">Host of server to connect to - default is localhost</param>
        /// <param name="port">Port of server to connect to - default is 5801</param>
        /// <returns>Instance of ChronicleMapClient with the given config settings</returns>
        public static ChronicleMapClient GetClient(string username = "cShartClient", string password = "password", string host = "localhost", int port = 5801)
        {
            NetworkCredential networkCredentials = new NetworkCredential(username, password);
            return new ChronicleMapClient(networkCredentials, host, port, timeoutSecs: 10);
        }

        /// <summary>
        /// Get a reference to the map with the given name on the Chronicle server via the ChronicleMapClient.
        /// </summary>
        /// <typeparam name="K">Type of the map key</typeparam>
        /// <typeparam name="V">Type of the map value</typeparam>
        /// <param name="client">ChronicleMapClient used to get the map reference</param>
        /// <param name="mapName">Name (uri) of map to get</param>
        /// <param name="cachingEnabled">Bool indicating whether or not to enable client side caching</param>
        /// <returns>Reference to the remote map with the given name.</returns>
        public static IExtendedDictionary<K, V> GetMap<K, V>(ChronicleMapClient client, string mapName, bool cachingEnabled = false)
        {
            IExtendedDictionary<K, V> map = client.getMap<K, V>(mapName, cachingEnabled);

            return map;
        }
    }
}