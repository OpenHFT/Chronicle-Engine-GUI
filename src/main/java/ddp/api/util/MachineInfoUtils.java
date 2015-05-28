package ddp.api.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Utilities to help get information about the host machine.
 */
public class MachineInfoUtils {
    /**
     * Get the hostname of the machine where the process is running as defined by environment
     * variables. Environment independent.
     *
     * @return Hostname of machine where the process is running.
     */
    public static String getHostname() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            //As specified on Windows
            String host = System.getenv("COMPUTERNAME");

            if (host != null) {
                return host;
            }

            //As specified on Linux
            host = System.getenv("HOSTNAME");

            if (host != null) {
                return host;
            }

            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e1) {
                return "localhost";
            }
        }
    }
}