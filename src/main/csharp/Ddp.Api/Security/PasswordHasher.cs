using System;

namespace Ddp.Api.Security
{
    public static class PasswordHasher
    {
        public static string GetPasswordHash(String password)
        {
            //TODO DS check for null and empty string and hash password - encrupted hash needs to be the same in Java and C#
            return password;
        }
    }
}