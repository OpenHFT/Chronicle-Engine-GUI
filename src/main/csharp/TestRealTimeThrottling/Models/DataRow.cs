using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Practices.Prism.Mvvm;
using TestRealTimeThrottling.Services;

namespace TestRealTimeThrottling.Models
{
    public class DataRow : BindableBase
    {
        private double _value;
        public DataRow(MarketDataService marketDataService, string key)
        {
            Key = key;
            marketDataService.RegisterMarketDataSubscription(key, ValueCallBack);
        }

        /// <summary>
        /// This callback is called when there is an update to the relevant market data key
        /// </summary>
        /// <param name="update">double containing updated rate</param>
        private void ValueCallBack(double update)
        {
            Console.WriteLine("Key: " +  Key + ", Value: " + Value);
            Value = update;
        }


        public string Key { get; set; }
        public double Value
        {
            get { return _value; }
            set { SetProperty(ref _value, value); }
        }
    }
}
