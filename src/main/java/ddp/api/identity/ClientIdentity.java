package ddp.api.identity;

import java.io.*;

//TODO DS do we need a Config package to put this and config exception and config into?
//TODO DS currently an interface so we can use OpenHFT lang to create bytes marshallable class.
//TODO DS we probably need a - final - implementation of this to ensure that no one is able to override it?
public final class ClientIdentity implements Externalizable
{
    private String _clientId;
    private String _password; //Hashed password
    private String _entity;
    private String _hostname;

    /**
     * Creates a new immutable instance of ClientIdentity. Password should be a hash value and not the clear-text
     * password.
     * @param clientId Client unique identifier.
     * @param password Hashed password - do not store original clear-text password.
     * @param entity Entity the client belongs to.
     */
    ClientIdentity(String clientId, String password, String entity, String hostname)
    {
        _clientId = clientId;
        _password = password;
        _entity = entity;
        _hostname = hostname;
    }

    public String getClientId()
    {
        return _clientId;
    }

    public String getEntity()
    {
        return _entity;
    }

    public String getHostname()
    {
        return _hostname;
    }

    //TODO DS unit test
    /**
     * Checks whether the given (hashed) password matches the password for this identity.
     * @param password Password to be checked against this identity's password for a match.
     * @return True if given password matches the identity's password, false otherwise
     */
    public boolean checkPassword(String password)
    {
        if(password == null || password.isEmpty() || _password == null || _password.isEmpty())
        {
            return false;
        }

        return password.equals(_password);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(getClientId());
        out.writeUTF(_password);
        out.writeUTF(getEntity());
        out.writeUTF(getHostname());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _clientId = in.readUTF();
        _password = in.readUTF();
        _entity = in.readUTF();
        _hostname = in.readUTF();
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UserIdentity{ ");
        stringBuilder.append("entity= ").append(getEntity());
        stringBuilder.append(", ");
        stringBuilder.append("hostname= ").append(getHostname());
        stringBuilder.append(", ");
        stringBuilder.append("clientId= ").append(getClientId());
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }
}