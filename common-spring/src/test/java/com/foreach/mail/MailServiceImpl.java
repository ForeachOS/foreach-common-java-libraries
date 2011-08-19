package com.foreach.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.apache.log4j.Logger;

public class MailServiceImpl extends AbstractMailService implements MailService
{
	private JavaMailSender mailSender;

	private String originator;

	private String serviceBccRecipients;

	private Logger logger;


	public void setMailSender( JavaMailSender mailSender )
	{
		this.mailSender = mailSender;
	}

	public final JavaMailSender getMailSender()
	{
		return mailSender;
	}

	public final void setOriginator( String originator )
	{
		this.originator = originator;
	}

	public final String getOriginator()
	{
		return originator;
	}

	public final void setServiceBccRecipients( String serviceBccRecipients )
	{
		this.serviceBccRecipients = serviceBccRecipients;
	}

	public final String getServiceBccRecipients()
	{
		return serviceBccRecipients;
	}

	public final void setLogger( Logger logger )
	{
		this.logger = logger;
	}

	public final Logger getLogger( )
	{
		return logger;
	}
}
