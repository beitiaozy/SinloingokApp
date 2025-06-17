package com.sinloingok.app.dao.handler;

import com.sinloingok.app.dao.status.BaseEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericEnumTypeHandler<E extends Enum<E> & BaseEnum> extends BaseTypeHandler<E> {
    private final Class<E> type;

    public GenericEnumTypeHandler(Class<E> type) {
        if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, parameter.getCode());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object code = rs.getObject(columnName);
        return code == null ? null : codeOf(code);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object code = rs.getObject(columnIndex);
        return code == null ? null : codeOf(code);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object code = cs.getObject(columnIndex);
        return code == null ? null : codeOf(code);
    }

    private E codeOf(Object code) {
        for (E e : type.getEnumConstants()) {
            if (e.getCode().toString().equals(code.toString())) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知枚举值: " + code + " for type " + type.getSimpleName());
    }
}
