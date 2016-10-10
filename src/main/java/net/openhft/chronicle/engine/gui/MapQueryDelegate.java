package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import net.openhft.chronicle.engine.api.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * @author Rob Austin.
 */
public class MapQueryDelegate<K, V> implements QueryDelegate {

    private final MapView<K, V> mapView;
    private List<Container.Filter> filters = Collections.EMPTY_LIST;
    private Iterator<Map.Entry<K, V>> iterator;
    private long iteratorIndex = 0;
    private Comparator<? super Map.Entry<K, V>> sorted = (o1, o2) -> 1;
    private List<OrderBy> orderBys = Collections.EMPTY_LIST;

    public MapQueryDelegate(@NotNull MapView<K, V> mapView) {
        this.mapView = mapView;
        iterator = newIterator(filters, sorted);
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
    public ResultSet getResults(int offset, int pageLength) throws SQLException {
        Iterator<Map.Entry<K, V>> iterator = newIterator(filters, sorted);
        int iteratorIndex = 0;
        if (offset < iteratorIndex) {
            this.iterator = newIterator(filters, sorted);
            iteratorIndex = 0;
        }

        while (offset < iteratorIndex && this.iterator.hasNext()) {
            this.iterator.next();
            iteratorIndex++;
        }

        return new MapViewResultSet<K, V>(iterator, pageLength);

    }


    @Override
    public boolean implementationRespectsPagingLimits() {
        return true;
    }

    @Override
    public void setFilters(List<Container.Filter> filters) throws UnsupportedOperationException {

        if (filters == null)
            filters = Collections.EMPTY_LIST;

        if (this.filters.equals(filters))
            return;

        this.filters = filters;
        this.iterator = newIterator(filters, sorted);
        this.iteratorIndex = 0;
    }

    @NotNull
    private Iterator<Map.Entry<K, V>> newIterator(List<Container.Filter> filters, final Comparator<? super Map.Entry<K, V>> sorted) {

        return mapView.entrySet().stream()
                .filter(kvEntry -> filter(filters, kvEntry))
                .sorted(sorted)
                .iterator();
    }

    private boolean filter(@NotNull List<Container.Filter> filters, @NotNull Map.Entry<K, V> kvEntry) {
        for (Container.Filter f1 : filters) {

            if (f1 instanceof SimpleStringFilter) {
                SimpleStringFilter filter = (SimpleStringFilter) f1;
                Object item;
                if ("value".equals(filter.getPropertyId()))
                    item = kvEntry.getValue();
                else if ("key".equals(filter.getPropertyId())) {
                    item = kvEntry.getKey();
                } else {
                    throw new UnsupportedOperationException();
                }

                if (!item.toString().toLowerCase().contains(filter.getFilterString().toLowerCase()))
                    return false;

            } else {
                throw new UnsupportedOperationException();
            }

        }
        return true;
    }


    @Override
    public void setOrderBy(List<OrderBy> orderBys) throws UnsupportedOperationException {
        final List<OrderBy> orderBys0 = (orderBys == null) ? Collections.EMPTY_LIST : orderBys;

        if (this.orderBys.equals(orderBys0))
            return;

        this.orderBys = orderBys0;

        this.sorted = (Comparator<Map.Entry<K, V>>) (o1, o2) -> {

            for (OrderBy order : orderBys0) {

                int result = 0;
                if ("key".equals(order.getColumn()))
                    result = ((Comparable) o1.getKey()).compareTo(o2.getKey());
                else if ("value".equals(order.getColumn()))
                    result = ((Comparable) o1.getValue()).compareTo(o2.getValue());

                result *= order.isAscending() ? 1 : -1;
                if (result != 0)
                    return result;

            }

            return 0;
        };

        iterator = newIterator(filters, sorted);
        iteratorIndex = 0;
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
        System.out.println("beginTransaction");
    }

    @Override
    public void commit() throws SQLException {
        System.out.println("commit");
    }

    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public List<String> getPrimaryKeyColumns() {
        return singletonList("key");
    }

    @Override
    public boolean containsRowWithKey(Object... keys) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }
}
