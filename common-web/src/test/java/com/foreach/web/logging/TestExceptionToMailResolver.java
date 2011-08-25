package com.foreach.web.logging;

import com.foreach.context.ApplicationEnvironment;
import com.foreach.mail.MailService;
import com.foreach.context.ApplicationContextInfo;
import com.foreach.web.logging.ExceptionToMailResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestExceptionToMailResolver
{

	private ExceptionToMailResolver resolver;

	private MailService mailService;
	private ApplicationContextInfo applicationContextInfo;
	private String toAddress;
	private String fromAddress;

	@Before
	public void prepareForTest()
	{
		resolver = new ExceptionToMailResolver();

		mailService = mock( MailService.class );

		applicationContextInfo = new ApplicationContextInfo();
		applicationContextInfo.setApplicationName( "TestExceptionToMailResolver" );
		applicationContextInfo.setBuildNumber( 1L );
		applicationContextInfo.setEnvironment( ApplicationEnvironment.TEST );
		applicationContextInfo.setBuildDate( new Date( ) );

		toAddress = "devnull@foreach.be";
		fromAddress = "noreply@foreach.be";

		resolver.setMailService( mailService );
		resolver.setWebApplicationContext( applicationContextInfo );
		resolver.setToAddress( toAddress );
		resolver.setFromAddress( fromAddress );
	}

	@Test
	// Don't break if request attributes set by an optional RequestlogInterceptor are unavailable when throwing an Exception.
	public void optionalRequestLogInterceptor()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response= new MockHttpServletResponse();

		resolver.doResolveException( request, response, null, new Exception( ) );

		verify( mailService ).sendMimeMail( eq( fromAddress ), eq( toAddress ), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String,File>> anyObject() );
	}

	@Test
	public void resolverRecoversIfMailServiceThrowsRuntimeException()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response= new MockHttpServletResponse();

		when( mailService.sendMimeMail( anyString(), anyString(), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String,File>> anyObject() ) ).thenThrow( new NullPointerException() );

		resolver.doResolveException( request, response, null, new Exception() );
	}

	@Test
	public void addCookiesAndAttributesAndStuffLikeForARealMessage()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response= new MockHttpServletResponse();

		Cookie lu = new Cookie( "lu", "bah" );
		lu.setDomain( "www.cookie.org" );
		lu.setPath( "/blikkendoos/" );

		Cookie delacre = new Cookie( "delacre", "gaatwel" );
		Cookie destrooper = new Cookie( "destrooper", "toch wat vettig" );

		request.setCookies( lu, delacre, destrooper  );

		request.setAttribute( "casting cost", "2 witte bollekes" );

		request.addParameter( "kleur", "donkere oker" );

		request.addHeader( "X-Dontlookatthecontent", "if so inclined" );

		HttpSession session = new MockHttpSession();
		session.setAttribute( "sessionId", "supposedToBeALongString" );

		request.setSession( session );

		resolver.doResolveException( request, response, null, new Exception( ) );

		verify( mailService ).sendMimeMail( eq( fromAddress ), eq( toAddress ), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String,File>> anyObject() );
	}

}