package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import net.openhft.chronicle.engine.api.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rob Austin.
 */
public class MapQueryDelegate<K, V> implements QueryDelegate {

    private final MapView<K, V> mapView;

    private List<Container.Filter> filters = Collections.EMPTY_LIST;
    private Iterator<Map.Entry<K, V>> iterator;
    private long iteratorIndex;


    private Comparator<? super Map.Entry<K, V>> sorted;

    public MapQueryDelegate(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public int getCount() throws SQLException {

        if (filters.isEmpty())
            return mapView.size();

        Iterator<Map.Entry<K, V>> entryIterator = newIterator(filters, sorted);
        int i = 0;

        while (entryIterator.hasNext()) {
            entryIterator.next();
            i++;
        }
        return i;

    }

    @Override
    public ResultSet getResults(int offset, int pagelength) throws SQLException {

        if (offset < iteratorIndex) {
            iterator = newIterator(filters, sorted);
            iteratorIndex = 0;
        }

        while (offset < iteratorIndex && iterator.hasNext()) {
            iterator.next();
            iteratorIndex++;
        }

        return new MapViewResultSet<K, V>(iterator);

    }


    @Override
    public boolean implementationRespectsPagingLimits() {
        return true;
    }

    @Override
    public void setFilters(final List<Container.Filter> filters) throws UnsupportedOperationException {
        if (filters.equals(this.filters))
            return;

        this.filters = filters;

        iterator = newIterator(filters, sorted);
        iteratorIndex = 0;
    }

    private Iterator<Map.Entry<K, V>> newIterator(List<Container.Filter> filters, final Comparator<? super Map.Entry<K, V>> sorted) {
        return mapView.entrySet().stream()
                .filter(kvEntry -> filter(filters, kvEntry))
                .sorted(sorted)
                .iterator();
    }

    private boolean filter(@NotNull List<Container.Filter> filters, @NotNull Map.Entry<K, V> kvEntry) {
        for (Container.Filter f1 : filters) {
            if (!f1.passesFilter(kvEntry.getKey(), toItem(kvEntry.getValue())))
                return false;
        }
        return true;
    }

    private Item toItem(V value) {
        return null;
    }

    @Override
    public void setOrderBy(List<OrderBy> orderBys) throws UnsupportedOperationException {
        this.sorted = (Comparator<Map.Entry<K, V>>) (o1, o2) -> {

            for (OrderBy order : orderBys) {
                if ("key".equals(order.getColumn())) {
                    int i = ((Comparable) o1.getKey()).compareTo(o2.getKey());
                    if (i != 0)
                        return i;
                } else if ("value".equals(order.getColumn())) {
                    int i = ((Comparable) o1.getValue()).compareTo(o2.getValue());
                    if (i != 0)
                        return i;
                }

            }

            return 0;
        };
    }

    /**
     * Stores a row in the database. The implementation of this interface
     * decides how to identify whether to store a new row or update an existing
     * one.
     *
     * @param row A map containing the values for all columns to be stored or updated.
     * @return the number of affected rows in the database table
     * @throws UnsupportedOperationException if the implementation is read only.
     */
    @Override
    public int storeRow(RowItem row) throws UnsupportedOperationException, SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeRow(RowItem row) throws UnsupportedOperationException, SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void beginTransaction() throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void commit() throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public List<String> getPrimaryKeyColumns() {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean containsRowWithKey(Object... keys) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }
}
