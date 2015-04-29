package ddp.api.util;

/**
 * Utilities to help get information about the host machine.
 */
public class MachineInfoUtils
{
    /**
     * Get the hostname of the machine where the process is running as defined by environment variables.
     * Environment independent.
     *
     * @return Hostname of machine where the process is running.
     */
    public static String getHostname()
    {
        //As specified on Windows
        String host = System.getenv("COMPUTERNAME");

        if (host != null)
        {
            return host;
        }

        //As specified on Linux
        host = System.getenv("HOSTNAME");

        if (host != null)
        {
            return host;
        }

        return null;
    }
}