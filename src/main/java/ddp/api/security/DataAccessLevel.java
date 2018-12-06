package ddp.api.security;

/**
 * Enum indicating data access level.
 */
public enum DataAccessLevel
{
    READ,
    SUBSCRIBE,
    WRITE,  //TODO DS should we have a specific access level which allows the deletion/removal of keys?
    LOCK,
    ALL
}