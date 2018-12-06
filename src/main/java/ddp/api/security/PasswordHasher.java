package ddp.api.security;

/**
 * Manages the hashing of passwords
 */
public class PasswordHasher
{
    /**
     * Generates an one-way hash value for the password.
     * @param password Password for which to generate hashed value.
     * @return Hash value of the given password.
     */
    public static String getPasswordHash(String password)
    {
        //TODO DS check for null and empty string and hash password - encrupted hash needs to be the same in Java and C# - http://commons.apache.org/proper/commons-codec/
        return password;
    }
}