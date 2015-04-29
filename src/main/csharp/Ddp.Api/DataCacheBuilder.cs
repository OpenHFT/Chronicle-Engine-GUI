using System;
using System.Collections.Generic;
using Ddp.Api.authentication;
using Ddp.Api.Authorisation;
using Ddp.Api.Identity;
using Ddp.Api.Security;

namespace Ddp.Api
{
    public static class DataCacheBuilder
    {
        private static IDictionary<String, IDictionary<String, Entitlement>> _entitlementsCache;

        private static EntitlementsManager _entitlementsManager = new EntitlementsManager(_entitlementsCache);
        private static IAuthenticator _authenticator;

        //TODO DS this should not be hard coded - get from config?
        private static String _primaryHostname = "localhost";
        private static int _primaryPort = 3216;
        private static String _primaryIpAddress = "localhost";

        //TODO DS need to provide methods for list of configs or single config, passed in the constructor or from config???

        public static RestrictedDataCache<K, V> CreateRestrictedDataCache<K, V>(string dataCacheName, ClientIdentity clientIdentity)
        {
            if (!_authenticator.AuthenticateClient(clientIdentity))
            {
                throw new AuthenticationException(
                        String.Format("Client with id '{0}' and entity '{1}' is not authenticated to use the system!",
                                clientIdentity.ClientId, clientIdentity.Entity));
            }

            //This throws an exception if the user isn't entitled to use the cache at all
            Entitlement entitlement = _entitlementsManager.GetUserEntitlementsForDataCache(dataCacheName, clientIdentity);

            //READ is required to access
            entitlement.CheckDataAccess(DataAccessLevel.Read);

            DataCacheConfiguration dataCacheConfiguration = new DataCacheConfiguration(_primaryHostname, _primaryIpAddress, _primaryPort, dataCacheName);

            //TODO DS generate list of failover configs...

            ChronicleDataCache<K, V> chronicleDataCache = new ChronicleDataCache<K, V>(dataCacheConfiguration);

            return new RestrictedDataCache<K, V>(chronicleDataCache, entitlement);
        }
    }
}