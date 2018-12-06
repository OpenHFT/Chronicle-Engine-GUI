using System;
using System.Collections.Generic;
using System.Linq;
using System.Security;
using System.Text;
using Ddp.Api.Identity;

namespace Ddp.Api.Authorisation
{
    internal class EntitlementsManager
    {
        //TODO DS this is subject to change
        private IDictionary<string, IDictionary<string, Entitlement>> _entitlements;

        internal EntitlementsManager(IDictionary<String, IDictionary<String, Entitlement>> entitlements)
        {
            _entitlements = entitlements;
        }

        public Entitlement GetUserEntitlementsForDataCache(String dataCacheName, ClientIdentity clientIdentity)
        {
            IDictionary<String, Entitlement> userEntitlements = _entitlements[clientIdentity.ClientId];

            if (userEntitlements == null)
            {
                String errorMessage = String.Format("User '{0}' does not have any Data Cache entitlements!", clientIdentity.ClientId);

                //TODO DS log

                throw new SecurityException(errorMessage);
            }

            Entitlement entitlement = userEntitlements[dataCacheName];

            if (entitlement == null)
            {
                String errorMessage = String.Format("User with identity '{0}' does not have any entitlements on Data Cache '{1}'.",
                        clientIdentity,
                        dataCacheName);

                //TODO DS log

                throw new SecurityException(errorMessage);
            }

            return entitlement;
        }
    }
}