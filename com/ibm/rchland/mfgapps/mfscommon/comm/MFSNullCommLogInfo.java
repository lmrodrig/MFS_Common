/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-23      37676JM  R Prechel        -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

/**
 * <code>MFSNullCommLogInfo</code> is the {@link MFSCommLogInfo} returned by
 * the {@link MFSCommLogger#createLogInfo()} method when network measurement
 * logging is not enabled. The {@link #startTransaction} and
 * {@link #endTransaction} methods do nothing and the class maintains no state.
 * @author The MFS Client and Print Server Development Team
 */
public class MFSNullCommLogInfo
	implements MFSCommLogInfo
{
	/** Constructs a new <code>MFSNullCommLogInfo</code>. */
	public MFSNullCommLogInfo()
	{
		super();
	}

	/** {@inheritDoc} */
	public void startTransaction(String transactionName)
	{
		//Does nothing
	}

	/** {@inheritDoc} */
	public void endTransaction()
	{
		//Does nothing
	}
}
