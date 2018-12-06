package ddp.api.authentication;

import ddp.api.identity.*;
import net.openhft.chronicle.map.*;

import java.io.*;

public class ChronicleAuthenticator implements Authenticator
{
    private String _chronicleAuthenticationMapPath = "C:\\LocalFolder\\Chronicle\\AuthenticationMap";
    private ChronicleMap<String, String> _chronicleAuthenticationMap;
    private Authenticator _authenticator;

    public ChronicleAuthenticator()
    {
        File chronicleFile = new File(_chronicleAuthenticationMapPath);
        //TODO DS this should be a stateless client to server and exceptions should not be handled here
        try
        {
            _chronicleAuthenticationMap = ChronicleMapBuilder
                    .of(String.class, String.class).putReturnsNull(true).createPersistedTo
                (chronicleFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        _authenticator = new CredentialsMapAuthenticator(_chronicleAuthenticationMap);
    }

    @Override
    public boolean authenticateClient(ClientIdentity clientIdentity)
    {
        return _authenticator.authenticateClient(clientIdentity);
    }
}