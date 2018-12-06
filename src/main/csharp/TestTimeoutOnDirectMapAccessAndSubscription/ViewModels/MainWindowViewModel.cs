using System.Data;
using System.Net;
using Microsoft.Practices.Prism.Mvvm;
using OpenHFT;
using OpenHFT.PubSub;
using TestTimeoutOnDirectMapAccessAndSubscription.Utilities;

namespace TestTimeoutOnDirectMapAccessAndSubscription.ViewModels
{
    public class MainWindowViewModel : BindableBase
    {
        private readonly AsyncObservableCollection<object> _collection;
        public MainWindowViewModel()
        {
            const int chroniclePort = 7890;
            var networkCredentials = new NetworkCredential("cSharpClient", "password");
            var configMapSubscriber = new ChronicleMapClient(networkCredentials, "localhost", chroniclePort, timeoutSecs: 10);

            // If below is used, i.e. new instance of ChronicleMapClient, then everything seems to work.
            //var configSubscriber =  new ChronicleMapClient(networkCredentials, "localhost", chroniclePort, timeoutSecs: 10);

            // If the instance for subscriber is set to THE SAME ChronicleMapClient INSTANCE then the timeout occurs.  I tried increasing timeout to 60 seconds and get the same issue.
            var configSubscriber = configMapSubscriber;


            IExtendedDictionary<string, string> valEnvsMap = configMapSubscriber.getMap("/adept/examples/mapcollection1", false);
            // AsynObserbableCollection will synch back updates to be on the thread that created it using SynchronizationContext
            _collection = new AsyncObservableCollection<object>();
            var chronicleCallback = new Subscriber<string>(HandleCallback);
            configSubscriber.registerKeySubscriber("/adept/examples/mapcollection2", "2", chronicleCallback, false, true);
            string yaml = valEnvsMap["1"];
        }


        /// <summary>
        /// Handle updates for eurodollar config events
        /// </summary>
        /// <param name="yaml">The yaml representation of EurodollarMarketDataModelConfig.</param>
        private void HandleCallback(string yaml)
        {
            _collection.Clear();
        }
    }
}