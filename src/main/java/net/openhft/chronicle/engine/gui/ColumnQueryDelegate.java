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
import net.openhft.chronicle.engine.api.column.Row;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rob Austin.
 */
public class ColumnQueryDelegate<K, V> implements QueryDelegate {

    private final ColumnView columnView;
    private List<Container.Filter> filters = Collections.EMPTY_LIST;
    private List<OrderBy> orderBys = Collections.EMPTY_LIST;

    public ColumnQueryDelegate(@NotNull ColumnView columnView) {
        this.columnView = columnView;
    }

    @Override
    public int getCount() throws SQLException {

        if (filters.isEmpty() && orderBys.isEmpty())
            return columnView.rowCount(null);

        return columnView.rowCount(toQuery(0, filters));
    }

    @Override
    public ResultSet getResults(int offset, int pageLength) throws SQLException {
        Iterator<Row> iterator = newIterator(offset);
        int iteratorIndex = 0;

        while (offset > iteratorIndex && iterator.hasNext()) {
            iterator.next();
            iteratorIndex++;
        }

        return new MapViewResultSet<K, V>(iterator, pageLength, columnView.columns());
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


    private Iterator<Row> newIterator(int fromIndex) {
        Query query = toQuery(fromIndex, filters);
        return columnView.iterator(query);
    }

    @NotNull
    private Query toQuery(int fromIndex, List<Container.Filter> filters) {
        final Query query = new Query();
        query.fromIndex = fromIndex;

        for (Container.Filter filter0 : filters) {
            if (filter0 instanceof SimpleStringFilter) {
                SimpleStringFilter filter = (SimpleStringFilter) filter0;
                query.marshableFilters.add(
                        new MarshableFilter(filter.getPropertyId().toString(),
                                filter.getFilterString()));
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

        final Map<String, Object> oldRow = new HashMap<>();
        final Map<String, Object> newRow = new HashMap<>();

        for (Column c : columnView.columns()) {
            final ColumnProperty cp = (ColumnProperty) row.getItemProperty(c.name);
            newRow.put(c.name, cp.getValue());
            oldRow.put(c.name, cp.getOldValue());
        }

        return columnView.changedRow(newRow, oldRow);
    }

    @Override
    public boolean removeRow(RowItem row) throws UnsupportedOperationException, SQLException {

        final Map<String, Object> oldRow = new HashMap<>();

        for (Column c : columnView.columns()) {
            final ColumnProperty cp = (ColumnProperty) row.getItemProperty(c.name);
            oldRow.put(c.name, cp.getOldValue());
        }

        return columnView.changedRow(Collections.EMPTY_MAP, oldRow) == 1;

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
        return columnView.columns().stream()
                .filter(c -> c.primaryKey).map(c -> c.name)
                .collect(Collectors.toList());
    }

    public boolean containsRowWithKey(Object... keys) throws SQLException {
        return columnView.containsRowWithKey(keys);
    }
}
