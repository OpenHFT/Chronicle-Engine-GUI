package ddp.api.authentication;

import ddp.api.identity.ClientIdentity;

/**
 * Class providing functionality to authenticate a {@link ddp.api.identity.ClientIdentity}.
 */
@FunctionalInterface
public interface Authenticator
{
    /**
     * Checks whether the given client can be authenticated to use the Data Cache system.
     * @param clientIdentity Client identity to authenticate
     * @return boolean indicating whether or not the user is authenticated to use the system.
     */
    boolean authenticateClient(ClientIdentity clientIdentity);
}