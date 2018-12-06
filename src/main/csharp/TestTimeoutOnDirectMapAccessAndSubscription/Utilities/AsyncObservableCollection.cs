using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Threading;

namespace TestTimeoutOnDirectMapAccessAndSubscription.Utilities
{
    /// <summary>
    /// This class is used used to update collections that are bound to the view in threads
    /// other than the GUI thread.  It will be marshalled to GUI thread.  The constraint in use 
    /// is that it must be created on the UI thread so that events are raised in this thread.
    /// NB scalar properties are marshalled to the UI automatically using the PropertyChange event.
    /// See: http://www.thomaslevesque.com/2009/04/17/wpf-binding-to-an-asynchronous-collection/
    /// Most recent version used from here:
    /// https://gist.github.com/thomaslevesque/10023516
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class AsyncObservableCollection<T> : ObservableCollection<T>
    {
        private readonly SynchronizationContext _synchronizationContext = SynchronizationContext.Current;
        public AsyncObservableCollection()
        {
        }
        public AsyncObservableCollection(IEnumerable<T> list)
            : base(list)
        {
        }
        private void ExecuteOnSyncContext(Action action)
        {
            if (SynchronizationContext.Current == _synchronizationContext)
            {
                action();
            }
            else
            {
                _synchronizationContext.Send(_ => action(), null);
            }
        }
        protected override void InsertItem(int index, T item)
        {
            ExecuteOnSyncContext(() => base.InsertItem(index, item));
        }
        protected override void RemoveItem(int index)
        {
            ExecuteOnSyncContext(() => base.RemoveItem(index));
        }
        protected override void SetItem(int index, T item)
        {
            ExecuteOnSyncContext(() => base.SetItem(index, item));
        }
        protected override void MoveItem(int oldIndex, int newIndex)
        {
            ExecuteOnSyncContext(() => base.MoveItem(oldIndex, newIndex));
        }
        protected override void ClearItems()
        {
            ExecuteOnSyncContext(() => base.ClearItems());
        }
    }
}
