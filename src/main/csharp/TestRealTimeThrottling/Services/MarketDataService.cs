using System;
using System.Collections.Generic;
using OpenHFT;
using OpenHFT.PubSub;
using TestRealTimeThrottling.Utilities;

namespace TestRealTimeThrottling.Services
{
    /// <summary>
    /// All market data updates are registered via this service to the Chronicle map. 
    /// </summary>
    public class MarketDataService : IDisposable
    {
        private readonly ChronicleMapClient _chronicleMapClient;

        private readonly IDictionary<string, string> _extraParameters;

        /// <summary>
        /// Constructor creating ChronicleMapClient for specified host and port.
        /// Throttle period set to default value.
        /// </summary>
        /// <param name="host">Host name</param>
        /// <param name="port">Port number</param>
        public MarketDataService(string host, int port)
        {
            _chronicleMapClient = ChronicleUtil.GetClient(host: host, port: port);
            _extraParameters = new Dictionary<string, string>();
            // Set throttling to 5 seconds
            _extraParameters.Add(MapParameters.ThrottlePeriodMs, "5000");
        }

        /// <summary>
        /// Register to market data updates
        /// </summary> 
        /// <param name="key">The specific market data key to subscribe to.</param>
        /// <param name="callback">The callback method.</param>
        /// <returns>ISubscription object that can be used to unsubscribe.</returns>
        public ISubscription RegisterMarketDataSubscription(string key, Subscriber<double> callback)
        {
            var chronicleCallback = new Subscriber<double>(callback);
            return _chronicleMapClient.registerKeySubscriber("/adept/marketdata/realtime", key, chronicleCallback, extraParameters:_extraParameters);
        }

        /// <summary>
        /// Disposes the chronicle map client
        /// </summary>
        public void Dispose()
        {
            if (_chronicleMapClient != null)
            {
                _chronicleMapClient.Dispose();
            }
        }
    }
}
