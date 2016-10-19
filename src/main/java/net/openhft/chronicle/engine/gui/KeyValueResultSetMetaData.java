package net.openhft.chronicle.engine.gui;

import net.openhft.chronicle.engine.api.column.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Rob Austin.
 */
public class KeyValueResultSetMetaData implements ResultSetMetaData {

    private final List<Column> columns;

    public KeyValueResultSetMetaData(List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return columns.size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return 0;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return columns.get(column - 1).name;
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return columns.get(column - 1).name;
    }

    @NotNull
    @Override
    public String getSchemaName(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int getScale(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @NotNull
    @Override
    public String getTableName(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @NotNull
    @Override
    public String getCatalogName(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @NotNull
    @Override
    public String getColumnTypeName(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @NotNull
    @Override
    public String getColumnClassName(int column) throws SQLException {
        return "java.lang.String";
    }

    @NotNull
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("todo");
    }
}
