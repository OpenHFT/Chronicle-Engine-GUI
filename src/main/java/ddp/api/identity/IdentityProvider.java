package ddp.api.identity;

import ddp.api.security.*;
import ddp.api.util.*;

/**
 * Builder for {@link ClientIdentity} objects.
 */
public class IdentityProvider
{
    /**
     * Creates an instance of {@link ClientIdentity} based on the given arguments and the hostname of the machine.
     * @param clientId Client unique identifier.
     * @param password Client password in clear-text, will be hashed.
     * @param entity Client's entity.
     * @return An instance of {@link ClientIdentity} with the given values set and the hostname retrieved from the machine where the process runs.
     */
    public static ClientIdentity getClientIdentity(String clientId, String password, String entity)
    {
        String hostname = MachineInfoUtils.getHostname();

        return getClientIdentity(clientId, password, entity, hostname);
    }

    //TODO DS test this and above

    /**
     * Creates an instance of {@link ClientIdentity} based on the given arguments.
     * @param clientId Client unique identifier.
     * @param password Client password in clear-text, will be hashed.
     * @param entity Client's entity.
     * @param hostname Hostname of clients machine.
     * @return An instance of {@link ClientIdentity} with the given values set.
     */
    public static ClientIdentity getClientIdentity(String clientId, String password, String entity, String hostname)
    {
        String hashedPassword = PasswordHasher.getPasswordHash(password);

        ClientIdentity clientIdentity = new ClientIdentity(clientId, hashedPassword, entity, hostname);

        return clientIdentity;
    }
}