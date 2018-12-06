using Ddp.Api.Security;
using Ddp.Api.Util;

namespace Ddp.Api.Identity
{
    public static class IdentityProvider
    {
        public static ClientIdentity GetClientIdentity(string cliendId, string password, string entity)
        {
            string hostname = MachineInfoUtil.GetHostname();

            return GetClientIdentity(cliendId, password, entity, hostname);
        }

        public static ClientIdentity GetClientIdentity(string cliendId, string password, string entity, string hostname)
        {
            string hashedPassword = PasswordHasher.GetPasswordHash(password);

            return new ClientIdentity(cliendId, hashedPassword, entity, hostname);
        }
    }
}