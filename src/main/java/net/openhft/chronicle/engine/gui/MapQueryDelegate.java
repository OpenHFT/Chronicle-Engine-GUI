package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.ColumnProperty;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import net.openhft.chronicle.engine.api.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Collections.singletonList;
import static net.openhft.chronicle.engine.map.VaadinLambda.*;

/**
 * @author Rob Austin.
 */
public class MapQueryDelegate<K, V> implements QueryDelegate {

    private final MapView<K, V> mapView;
    private List<Container.Filter> filters = Collections.EMPTY_LIST;
    private List<OrderBy> orderBys = Collections.EMPTY_LIST;

    public MapQueryDelegate(@NotNull MapView<K, V> mapView) {
        this.mapView = mapView;
    }

    @Override
    public int getCount() throws SQLException {

        if (filters.isEmpty())
            return mapView.size();

        // todo improve this
        Iterator<Map.Entry<K, V>> entryIterator = newIterator(0, filters);
        int i = 0;

        while (entryIterator.hasNext()) {
            entryIterator.next();
            i++;
        }

        return i;

    }

    @Override
    public ResultSet getResults(int offset, int pageLength) throws SQLException {
        Iterator<Map.Entry<K, V>> iterator = newIterator(offset, filters);
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


    private Iterator<Map.Entry<K, V>> newIterator(int fromIndex,
                                                  final List<Container.Filter> filters) {
        final Query<K, V> query = new Query<>();
        query.fromIndex = fromIndex;

        for (Container.Filter f : filters) {
            if (f instanceof SimpleStringFilter) {
                SimpleStringFilter filter = (SimpleStringFilter) f;
                Type type = Type.valueOf(filter.getPropertyId().toString().toLowerCase());
                String filterString = filter.getFilterString();
                query.marshableFilters.add(new MarshableFilter(type, filterString));
            }
        }

        for (OrderBy orderBy : orderBys) {
            query.marshableOrderBy.add(toMarshableOrderBy(orderBy));
        }

        return mapView.applyTo(iteratorFunction(), query);
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
        final ColumnProperty keyP = (ColumnProperty) row.getItemProperty("key");
        final ColumnProperty valueP = (ColumnProperty) row.getItemProperty("value");
        final ColumnProperty oldValueP = (ColumnProperty) row.getItemProperty("value");

        K key = (K) keyP.getValue();
        K oldKey = (K) keyP.getOldValue();
        V value = (V) oldValueP.getValue();
        V oldValue = (V) valueP.getOldValue();


        if (keyP.isModified()) {
            mapView.remove(oldKey);
            mapView.put(key, value);
            return 1;
        }

        if (!valueP.isModified())
            return 0;

        if (oldValue == null) {
            mapView.put(key, (V) valueP.getValue());
            return 1;
        }

        boolean replace = mapView.replace(key, oldValue, value);
        return replace ? 1 : 0;
    }

    @Override
    public boolean removeRow(RowItem row) throws UnsupportedOperationException, SQLException {
        ColumnProperty keyP = (ColumnProperty) row.getItemProperty("key");
        final K key = (K) keyP.getValue();
        Object old = mapView.remove(key);
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
            if (!mapView.containsKey(k))
                return false;
        }

        return true;
    }
}
