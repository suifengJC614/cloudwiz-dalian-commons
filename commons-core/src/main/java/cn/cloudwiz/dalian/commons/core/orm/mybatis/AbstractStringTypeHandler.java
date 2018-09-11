package cn.cloudwiz.dalian.commons.core.orm.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractStringTypeHandler<T> extends BaseTypeHandler<T>{

	protected abstract String toString(T value);
	
	protected abstract T fromString(String code);
	
	@Override
	public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String json = rs.getString(columnName);
		if(rs.wasNull()){
			return null;
		}
		return fromString(json);
	}

	@Override
	public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String json = rs.getString(columnIndex);
		if(rs.wasNull()){
			return null;
		}
		return fromString(json);
	}

	@Override
	public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String json = cs.getString(columnIndex);
		if(cs.wasNull()){
			return null;
		}
		return fromString(json);
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int columnIndex, T value, JdbcType type)
			throws SQLException {
		ps.setString(columnIndex, toString(value));
	}

}
