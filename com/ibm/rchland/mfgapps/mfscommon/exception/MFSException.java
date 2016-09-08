/* © Copyright IBM Corporation 2006, 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2006-06-28      31801JM  R Prechel        -Initial version
 * 2007-01-15      34242JR  R Prechel        -Java 5 version; use cause facility from Throwable
 * 2007-11-06      40104PB  R Prechel        -Extend IGSException instead of Exception
 * 2008-01-11      39619JL  R Prechel        -IGSException package change
 * 2010-03-08   ~1 42558JL  Santiago SC      -New constructor
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.exception;

import com.ibm.rchland.mfgapps.client.utils.exception.IGSException;

/**
 * <code>MFSException</code> is a subclass of {@link IGSException} thrown in
 * the <em>MFS Client</em> and the <em>MFS Print Server</em> to indicate
 * exceptional situations. Future exception classes developed for the
 * <em>MFS Client</em> and/or the <em>MFS Print Server</em> should extend
 * <code>MFSException</code> unless {@link RuntimeException} must be extended.
 * <code>MFSException</code>s are checked exceptions since
 * <code>MFSException</code> does not extend {@link RuntimeException}. Thus,
 * if a future exception class must be an unchecked exception, it cannot extend
 * <code>MFSException</code>.
 * @author The MFS Client Development Team
 */
public class MFSException
	extends IGSException
{
	/**
	 * Identifies the original class version for which this class is capable of
	 * writing streams and from which it can read.
	 */
	private static final long serialVersionUID = 2L;

	/** Constructs a new <code>MFSException</code>. */
	public MFSException()
	{
		super();
	}

	/**
	 * Constructs a new <code>MFSException</code> with the specified detail
	 * <code>message</code> and <code>cause</code>.
	 * @param message the detail message which can be retrieved using the
	 *        {@link Throwable#getMessage()} method
	 * @param cause the cause which can be retrieved using the
	 *        {@link Throwable#getCause()} method
	 */
	public MFSException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new <code>MFSException</code> with the specified detail
	 * <code>message</code>.
	 * @param message the detail message which can be retrieved using the
	 *        {@link Throwable#getMessage()} method
	 */
	public MFSException(String message)
	{
		super(message);
	}
	
	//~1A
	/**
	 * Constructs a new <code>MFSException</code> with the specified detail
	 * <code>message</code>.
	 * @param message the detail message which can be retrieved using the
	 *        {@link Throwable#getMessage()} method
	 */
	public MFSException(String message, boolean setProgramException)
	{
		super(message);
		
		setProgramException(setProgramException);
	}

	/**
	 * Constructs a new <code>MFSException</code> with the specified
	 * <code>cause</code>.
	 * @param cause the cause which can be retrieved using the
	 *        {@link Throwable#getCause()} method
	 */
	public MFSException(Throwable cause)
	{
		super(cause);
	}
}
