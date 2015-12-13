using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using OpenHFT;
using OpenHFT.PubSub;

namespace TestSubscriberLostConnection
{
    /// <summary>
    /// To run this test you first need to start main.FailoverBinaryWireMain.
    /// 
    /// Tests that an exception is thrown when the client looses connection to the last server in the list of servers it
    /// is configured to use.
    /// </summary>
    class Program
    {
        static void Main(string[] args)
        {
            const string host = "localhost"; // "10.0.2.2";
            string mapUri = "/failover/test/map1";
            string stopKey = "K5"; //Stop key for server on port 9088
            string stopValue = "Value-K5";

            NetworkCredential cred = new NetworkCredential("daniels", String.Empty, "mfil");

            var connectionDetails1 = GetConnectionDetails(host, 9088);

            try
            {
                using (ChronicleMapClient chronicleMapClient = new ChronicleMapClient(cred, new List<ConnectionDetails> { connectionDetails1 }, 5))
                {
                    BlockingCollection<ConnectionDetails> onDisconnect = new BlockingCollection<ConnectionDetails>(1);

                    chronicleMapClient.OnConnect += (c, m) =>
                    {
                        Console.WriteLine("OnConnect: " + c.Port + " - " + m);
                    };

                    chronicleMapClient.OnDisconnect += (c, m) =>
                    {
                        onDisconnect.Add(c);
                        Console.WriteLine("OnDisconnect: " + c.Port + " - " + m);
                    };

                    bool clientAlive = true;
                    chronicleMapClient.OnFailure +=
                        (msg, exception) =>
                        {
                            Console.WriteLine("ON ERROR " + msg + " | " + exception);
                            Console.WriteLine("Client dead");
                            clientAlive = false;
                        };

                    //Register subscriber priting out the keys that are changed
                    Subscriber<string> keysSubscriber = Console.WriteLine;
                    chronicleMapClient.registerSubscriber<string, string>(mapUri, keysSubscriber);

                    using (IExtendedDictionary<string, string> extendedDictionary = chronicleMapClient.getMap<string, string>(mapUri, cachingEnabled: false))
                    {
                        extendedDictionary.Add(stopKey, stopValue);
                    }

                    //Keep alive listening for events until we get an exception
                    while (clientAlive)
                    {
                        Console.WriteLine("sleeping; now " + DateTime.Now);
                        Thread.Sleep(200);
                    }
                }
            }
            catch (Exception e)
            {
                Console.Error.WriteLine(e.StackTrace);
            }

            Console.WriteLine("DONE!");
            Console.ReadLine();
        }

        private static ConnectionDetails GetConnectionDetails(string hostname, int port)
        {
            ConnectionDetails connectionDetails = new ConnectionDetails();
            connectionDetails.Host = hostname;
            connectionDetails.Port = port;
            connectionDetails.Name = "Con: " + hostname + ":" + port;
            return connectionDetails;
        }
    }
}