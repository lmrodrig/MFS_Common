/* @ Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-23      37676JM  R Prechel        -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <code>MFSActiveCommLogInfo</code> is the {@link MFSCommLogInfo} returned by
 * the {@link MFSCommLogger#createLogInfo()} method when network measurement
 * logging is enabled. The {@link #startTransaction} method stores the name and
 * start time of the transaction. The {@link #endTransaction} method determines
 * the end time of the transaction and invokes the
 * {@link MFSCommLogger#writeToLog(String)} method.
 * @author The MFS Client and Print Server Development Team
 */
public class MFSActiveCommLogInfo
	implements MFSCommLogInfo
{
	/** The <code>DateFormat</code> for the start and end timestamps. */
	private static final DateFormat FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd-HH.mm.ss.SSS"); //$NON-NLS-1$

	/** Stores the XML for a single transaction record. */
	private StringBuffer buffer;

	/** Constructs a new <code>MFSActiveCommLogInfo</code>. */
	public MFSActiveCommLogInfo()
	{
		super();
	}

	/** {@inheritDoc} */
	public void startTransaction(String transactionName)
	{
		this.buffer = new StringBuffer();
		this.buffer.append("<TRX>"); //$NON-NLS-1$
		this.buffer.append(transactionName);
		this.buffer.append("</TRX>"); //$NON-NLS-1$
		this.buffer.append("<STT>"); //$NON-NLS-1$
		this.buffer.append(FORMAT.format(new Date()));
		this.buffer.append("</STT>"); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public void endTransaction()
	{
		this.buffer.append("<ENT>"); //$NON-NLS-1$
		this.buffer.append(FORMAT.format(new Date()));
		this.buffer.append("</ENT>"); //$NON-NLS-1$

		MFSCommLogger.writeToLog(this.buffer.toString());
		this.buffer = null;
	}
}
