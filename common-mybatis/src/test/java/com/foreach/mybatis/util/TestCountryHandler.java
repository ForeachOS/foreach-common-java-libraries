package com.foreach.mybatis.util;

import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class TestCountryHandler
{
	private final String[] countryCodes = new String[] { "Aus", "Zim", "Foo", null };


	@Test
	public void codeToCountryConversion() throws SQLException
	{
		CountryHandler handler = new CountryHandler();

		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryCodes );

		while ( rs.next() ) {
			Country country = (Country) handler.getResult( rs, "country" );
			String code = rs.getString( "country" );

			Assert.assertSame( Country.getByCode( code ), country );

			if ( country != null ) {
				Assert.assertTrue( country.getCode().equalsIgnoreCase( code ) );
			}
		}
	}

	@Test
	public void countryToCodeConversion() throws SQLException
	{
		CountryHandler handler = new CountryHandler();

		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( String code : countryCodes ) {
			Country country = Country.getByCode( code );

			handler.setParameter( stmt, 1, country, JdbcType.VARCHAR );

			if ( country != null ) {
				Assert.assertTrue( StringUtils.equalsIgnoreCase( code, (String) stmt.getParameter( 1 ) ) );
			}
			else {
				Assert.assertNull( stmt.getParameter( 1 ) );
			}
		}
	}

	@Test(expected = NotImplementedException.class)
	public void callableNotSupported() throws SQLException
	{
		MockCallableStatement stmt = new MockCallableStatement( new MockConnection(), "" );

		new CountryHandler().getResult( stmt, 1 );
	}

}