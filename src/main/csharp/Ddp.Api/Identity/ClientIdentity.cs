using System;

namespace Ddp.Api.Identity
{
    //TODO DS needs to be serializable/bytesmarshallable
    public sealed class ClientIdentity
    {
        private readonly string _clientId;
        private readonly string _password;
        private readonly string _entity;
        private readonly string _hostname;

        internal ClientIdentity(string clientId, string password, string entity, string hostname)
        {
            _clientId = clientId;
            _password = password;
            _entity = entity;
            _hostname = hostname;
        }

        public string ClientId
        {
            get { return _clientId; }
        }

        public string Entity
        {
            get { return _entity; }
        }

        public string Hostname
        {
            get { return _hostname; }
        }

        internal bool CheckPassword(string password)
        {
            if (String.IsNullOrEmpty(password) || String.IsNullOrEmpty(_password))
            {
                return false;
            }

            return password == _password;
        }

        public override string ToString()
        {
            string toString = "UserIdentity{ entity= " + Entity
                + ", hostname= " + Hostname
                + ", clientId= " + ClientId
                + " }";

            return toString;
        }
    }
}