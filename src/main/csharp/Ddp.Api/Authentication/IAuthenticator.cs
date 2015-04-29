using Ddp.Api.Identity;

namespace Ddp.Api.authentication
{
    internal interface IAuthenticator
    {
        bool AuthenticateClient(ClientIdentity clientIdentity);
    }
}