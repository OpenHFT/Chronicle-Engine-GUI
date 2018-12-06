using System;
using System.Collections.Generic;
using System.Security;
using Ddp.Api.Identity;
using Ddp.Api.Security;
using NLog;

namespace Ddp.Api.Authorisation
{
    internal class Entitlement
    {
        private static Logger _log = LogManager.GetCurrentClassLogger();

        private ClientIdentity _clientIdentity; //TODO DS consider using higher level identity?
        private string _dataCacheName; //TODO DS is this needed in the entitlement?
        private ISet<DataAccessLevel> _dataAccessLevels;
        private ISet<AdminAccessLevel> _adminAccessLevels; //TODO DS is it even possible to do any admin tasks dynamically? Do we want to allow it?

        public ClientIdentity ClientIdentity
        {
            get { return _clientIdentity; }
        }

        public string DataCacheName
        {
            get { return _dataCacheName; }
        }

        public ISet<DataAccessLevel> DataAccessLevels
        {
            get { return _dataAccessLevels; }
        }

        public ISet<AdminAccessLevel> AdminAccessLevels
        {
            get { return _adminAccessLevels; }
        }

        internal void OnEntitlementUpdate(Entitlement updatedEntitlement)
        {
            if (_clientIdentity.Equals(updatedEntitlement.ClientIdentity)
                && _dataCacheName == updatedEntitlement.DataCacheName)
            {
                _log.Info("Entitlement has been updated for user '{0}' on Data Cache '{1}'. Old DataAccessLevels: '{2}', " +
                            "New DataAccessLevels: '{3}', " +
                            "Old AdminAccessLevels: '{4}', " +
                            "New AdminAccessLevels: '{5}'.",
                    _clientIdentity, _dataCacheName, _dataAccessLevels, updatedEntitlement.DataAccessLevels,
                    _adminAccessLevels, updatedEntitlement.AdminAccessLevels);

                //If user previously had subscribe access that is now revoked
                if (_dataAccessLevels.Contains(DataAccessLevel.Subscribe)
                        && !updatedEntitlement.DataAccessLevels.Contains(DataAccessLevel.Subscribe))
                {
                    //TODO DS need to ensure that any subscribers are removed from the chronicle map.
                }

                _dataAccessLevels = updatedEntitlement.DataAccessLevels;
                _adminAccessLevels = updatedEntitlement.AdminAccessLevels;
            }
        }

        internal void CheckDataAccess(DataAccessLevel requiredAccessLevel)
        {
            //TODO DS implement and use in restricted cache...
            if (!_dataAccessLevels.Contains(requiredAccessLevel))
            {
                string errorMessage = String.Format("User '{0}' does not have the required '{1}' data access level.", _clientIdentity, requiredAccessLevel);

                _log.Error(errorMessage);

                throw new SecurityException(errorMessage);
            }
        }

        internal void CheckAdminAccess(AdminAccessLevel requiredAccessLevel)
        {
            if (!_adminAccessLevels.Contains(requiredAccessLevel))
            {
                string errorMessage = String.Format("User '{0}' does not have the required '{1}' admin access level.", _clientIdentity, requiredAccessLevel);

                _log.Error(errorMessage);

                throw new SecurityException(errorMessage);
            }
        }
    }
}