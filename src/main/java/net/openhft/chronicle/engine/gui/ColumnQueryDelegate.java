package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.ColumnProperty;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import net.openhft.chronicle.engine.api.column.Column;
import net.openhft.chronicle.engine.api.column.ColumnView;
import net.openhft.chronicle.engine.api.column.ColumnView.MarshableFilter;
import net.openhft.chronicle.engine.api.column.ColumnView.MarshableOrderBy;
import net.openhft.chronicle.engine.api.column.ColumnView.Query;
import net.openhft.chronicle.engine.api.column.ColumnView.Type;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author Rob Austin.
 */
public class ColumnQueryDelegate<K, V> implements QueryDelegate {

    private final ColumnView<K> columnView;
    private List<Container.Filter> filters = Collections.EMPTY_LIST;
    private List<OrderBy> orderBys = Collections.EMPTY_LIST;

    public ColumnQueryDelegate(@NotNull ColumnView<K> mapView) {
        this.columnView = mapView;
    }

    @Override
    public int getCount() throws SQLException {

        if (filters.isEmpty() && orderBys.isEmpty())
            return (int) columnView.longSize();

        return columnView.size(newQuery(0, filters));

    }

    @Override
    public ResultSet getResults(int offset, int pageLength) throws SQLException {
        Iterator<? extends Map.Entry<K, ?>> iterator = newIterator(offset);
        int iteratorIndex = 0;

        while (offset > iteratorIndex && iterator.hasNext()) {
            iterator.next();
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

    }


    private Iterator<? extends Map.Entry<K, ?>> newIterator(int fromIndex) {
        if (fromIndex == 0 && filters.isEmpty() && orderBys.isEmpty()) {
            return columnView.entrySet().iterator();
        }

        Query<K> query = newQuery(fromIndex, filters);
        return columnView.iterator(query);
    }

    @NotNull
    private Query<K> newQuery(int fromIndex, List<Container.Filter> filters) {
        final Query<K> query = new Query<>();
        query.fromIndex = fromIndex;

        for (Container.Filter f : filters) {
            if (f instanceof SimpleStringFilter) {
                SimpleStringFilter filter = (SimpleStringFilter) f;
                ColumnView.Type type = Type.valueOf(filter.getPropertyId().toString().toLowerCase
                        ());
                String filterString = filter.getFilterString();
                query.marshableFilters.add(new MarshableFilter(type, filterString));
            }
        }

        for (OrderBy orderBy : orderBys) {
            query.marshableOrderBy.add(toMarshableOrderBy(orderBy));
        }
        return query;
    }


    @NotNull
    private MarshableOrderBy toMarshableOrderBy(OrderBy orderBy) {
        return new MarshableOrderBy(orderBy.getColumn(), orderBy.isAscending());
    }

    @Override
    public void setOrderBy(List<OrderBy> orderBys) throws UnsupportedOperationException {
        final List<OrderBy> orderBys0 = (orderBys == null) ? Collections.EMPTY_LIST : orderBys;

        if (this.orderBys.equals(orderBys0))
            return;

        this.orderBys = orderBys0;
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
        int count = 0;
        if (!row.isModified())
            return 0;

        final ColumnProperty column = (ColumnProperty) row.getItemProperty("key");
        final Object key = column.getValue();
        final Object oldKey = column.getOldValue();

        for (Column c : columnView.columns()) {
            final ColumnProperty cp = (ColumnProperty) row.getItemProperty(c);
            if (cp.isModified())

                columnView.onCellChanged(
                        c.name,
                        (K) key,
                        (K) oldKey,
                        cp.getValue(),
                        cp.getOldValue());
            count++;
        }

        return count;
    }

    @Override
    public boolean removeRow(RowItem row) throws UnsupportedOperationException, SQLException {
        ColumnProperty keyP = (ColumnProperty) row.getItemProperty("key");
        final K key = (K) keyP.getValue();
        Object old = columnView.remove(key);
        return old != null;
    }

    @Override
    public void beginTransaction() throws SQLException {

    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public List<String> getPrimaryKeyColumns() {
        return singletonList("key");
    }

    @Override
    public boolean containsRowWithKey(Object... keys) throws SQLException {
        for (Object k : keys) {
            if (!columnView.containsKey((K) k))
                return false;
        }

        return true;
    }
}
