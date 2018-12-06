using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Practices.Prism.Mvvm;
using TestRealTimeThrottling.Models;
using TestRealTimeThrottling.Services;
using TestRealTimeThrottling.Utilities;

namespace TestRealTimeThrottling.ViewModels
{
    public class MainWindowViewModel : BindableBase
    {
        public MainWindowViewModel()
        {
            MarketDataService marketDataService = new MarketDataService("localhost", 7799);

            DataRowCollection = new AsyncObservableCollection<DataRow>();
            DataRowCollection.Add(new DataRow(marketDataService, "1"));
            DataRowCollection.Add(new DataRow(marketDataService, "2"));
            DataRowCollection.Add(new DataRow(marketDataService, "3"));
            DataRowCollection.Add(new DataRow(marketDataService, "4"));
            DataRowCollection.Add(new DataRow(marketDataService, "5"));
            DataRowCollection.Add(new DataRow(marketDataService, "6"));
            DataRowCollection.Add(new DataRow(marketDataService, "7"));
            DataRowCollection.Add(new DataRow(marketDataService, "8"));
            DataRowCollection.Add(new DataRow(marketDataService, "9"));
            DataRowCollection.Add(new DataRow(marketDataService, "10"));
            DataRowCollection.Add(new DataRow(marketDataService, "11"));
            DataRowCollection.Add(new DataRow(marketDataService, "12"));
            DataRowCollection.Add(new DataRow(marketDataService, "13"));
            DataRowCollection.Add(new DataRow(marketDataService, "14"));
            DataRowCollection.Add(new DataRow(marketDataService, "15"));
            DataRowCollection.Add(new DataRow(marketDataService, "16"));
            DataRowCollection.Add(new DataRow(marketDataService, "17"));
            DataRowCollection.Add(new DataRow(marketDataService, "18"));
            DataRowCollection.Add(new DataRow(marketDataService, "19"));

        }

        public AsyncObservableCollection<DataRow> DataRowCollection { get; private set; }
        
    }
}