package com.series.common.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Java {@link String} ↔ PostgreSQL {@code uuid}。
 * 兼容 MyBatis-Plus {@code ASSIGN_UUID} 生成的 32 位无连字符十六进制串。
 */
@MappedJdbcTypes(JdbcType.OTHER)
public class UuidStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, UuidTypeHandlerSupport.toUuid(parameter), Types.OTHER);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return UuidTypeHandlerSupport.toCanonicalString(rs.getObject(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return UuidTypeHandlerSupport.toCanonicalString(rs.getObject(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return UuidTypeHandlerSupport.toCanonicalString(cs.getObject(columnIndex));
    }
}
