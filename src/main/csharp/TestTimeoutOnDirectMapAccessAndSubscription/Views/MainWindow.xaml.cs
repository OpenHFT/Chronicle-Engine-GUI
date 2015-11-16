using System.Windows;
using TestTimeoutOnDirectMapAccessAndSubscription.ViewModels;

namespace TestTimeoutOnDirectMapAccessAndSubscription.Views
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            DataContext = new MainWindowViewModel();
        }
    }
}
