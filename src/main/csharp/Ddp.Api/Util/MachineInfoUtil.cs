using System;

namespace Ddp.Api.Util
{
    public static class MachineInfoUtil
    {
        public static string GetHostname()
        {
            return Environment.MachineName;
        }
    }
}