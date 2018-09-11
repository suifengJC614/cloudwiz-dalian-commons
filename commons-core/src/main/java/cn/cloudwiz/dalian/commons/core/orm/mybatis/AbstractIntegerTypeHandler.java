package cn.cloudwiz.dalian.commons.core.orm.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractIntegerTypeHandler<T> extends BaseTypeHandler<T>{

	protected abstract int toInteger(T value);
	
	protected abstract T fromInteger(int code);
	
	@Override
	public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int value = rs.getInt(columnName);
		if(rs.wasNull()){
			return null;
		}
		return fromInteger(value);
	}

	@Override
	public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int value = rs.getInt(columnIndex);
		if(rs.wasNull()){
			return null;
		}
		return fromInteger(value);
	}

	@Override
	public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int value = cs.getInt(columnIndex);
		if(cs.wasNull()){
			return null;
		}
		return fromInteger(value);
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int columnIndex, T value, JdbcType type)
			throws SQLException {
		ps.setInt(columnIndex, toInteger(value));
	}

}
