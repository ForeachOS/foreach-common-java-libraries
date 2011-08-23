package com.foreach.mybatis.util;

import com.foreach.utils.EnumUtils;
import com.foreach.utils.IdLookup;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class IdBasedEnumHandler<I, E extends Enum<E> & IdLookup<I>>

		extends AbstractEnumHandler<E>

		implements TypeHandler
{
	protected final E getById( I id )
	{
		E e = EnumUtils.getById( getClazz(), id );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		E e = ( parameter == null ) ? getDefaultValue() : (E) parameter;

		if( e != null) {
			setParameter( preparedStatement, i, e.getId() );
		} else {
			preparedStatement.setNull( i, JdbcType.INTEGER.TYPE_CODE );
		}
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getById( getParameter( resultSet, columnName) );
	}


	protected abstract void setParameter( PreparedStatement preparedStatement, int i, I parameter ) throws SQLException;

	protected abstract I getParameter( ResultSet resultSet, String columnName ) throws SQLException;
}