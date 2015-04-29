package ddp.api.authentication;

import ddp.api.identity.*;
import org.slf4j.*;

import java.util.*;

/**
 * {@link ddp.api.authentication.Authenticator} checking against a {@link java.util.Map} with username as key and
 * password as value.
 */
public class CredentialsMapAuthenticator implements Authenticator
{
    private static final Logger _logger = LoggerFactory.getLogger(CredentialsMapAuthenticator.class);

    private Map<String, String> _usernamePasswordMap;

    /**
     * Map with user names as key and password as value (should be hashed or similar).
     * @param usernamePasswordMap Username/password map.
     */
    public CredentialsMapAuthenticator(Map<String, String> usernamePasswordMap){
        _usernamePasswordMap = usernamePasswordMap;
    }

    @Override
    public boolean authenticateClient(ClientIdentity clientIdentity)
    {
        String userId = clientIdentity.getClientId();

        if (userId == null || userId.isEmpty())
        {
            _logger.error("User id is null or empty. User id: '{}'", userId);

            return false;
        }

        String hashedPwd = _usernamePasswordMap.get(userId);

        if (hashedPwd == null || hashedPwd.isEmpty())
        {
            _logger.error("Incorrect password!. User id: '{}'", userId);

            return false;
        }
        else if (clientIdentity.checkPassword(hashedPwd))
        {
            return true;
        }
        else
        {
            _logger.error("Incorrect password!. User id: '{}'", userId);

            return false;
        }
    }
}