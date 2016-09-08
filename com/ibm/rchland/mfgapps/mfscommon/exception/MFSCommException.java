/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15      34242JR  R Prechel        -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.exception;


/**
 * <code>MFSCommException</code> is a subclass of <code>MFSException</code>
 * thrown to indicate a communication error occurred.
 * @author The MFS Client Development Team
 */
public class MFSCommException
	extends MFSException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new <code>MFSCommException</code> with the specified
	 * detail <code>message</code> and <code>cause</code>.
	 * @param message the detail message which can be retrieved using the
	 *        {@link Throwable#getMessage()} method
	 * @param cause the cause which can be retrieved using the
	 *        {@link Throwable#getCause()} method
	 */
	public MFSCommException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new <code>MFSCommException</code> with the specified
	 * detail <code>message</code>.
	 * @param message the detail message which can be retrieved using the
	 *        {@link Throwable#getMessage()} method
	 */
	public MFSCommException(String message)
	{
		super(message);
	}
}
