
namespace Ddp.Api.Security
{
    /// <summary>
    /// Enum indicating data access level.
    /// </summary>
    public enum DataAccessLevel
    {
        Read,
        Subscribe,
        Write, //TODO DS should we have a specific access level which allows the deletion/removal of keys?
        Lock,
        All
    }
}