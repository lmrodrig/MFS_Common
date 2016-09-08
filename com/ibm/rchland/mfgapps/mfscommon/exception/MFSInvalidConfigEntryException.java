/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-11-06      40104PB  R Prechel        -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.exception;

import java.text.MessageFormat;

/**
 * <code>MFSInvalidConfigEntryException</code> is a subclass of
 * <code>MFSException</code> thrown to indicate an invalid configuration entry
 * was specified.
 * @author The MFS Client Development Team
 */
public class MFSInvalidConfigEntryException
	extends MFSException
{
	private static final long serialVersionUID = 1L;
	/** The <code>MessageFormat</code> pattern for the error message. */
	private static final String PATTERN = "Missing or invalid {0} configuration entry.\nPlease contact MFS Support.";

	/**
	 * Constructs a new <code>MFSInvalidConfigEntryException</code>.
	 * @param label the label for the invalid configuration entry
	 * @param programException the value returned by
	 *        {@link #isProgramException()}
	 */
	public MFSInvalidConfigEntryException(String label, boolean programException)
	{
		super(MessageFormat.format(PATTERN, new Object[] {label}));
		setProgramException(programException);
	}
}
