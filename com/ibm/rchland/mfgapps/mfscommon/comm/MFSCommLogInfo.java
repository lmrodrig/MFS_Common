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
 * The <code>MFSCommLogInfo</code> interface and concrete implementations are
 * designed around the Null Object Design Pattern. If network measurement
 * logging is enabled, an instance of {@link MFSActiveCommLogInfo} maintains the
 * information for a single transaction call before it is written to the local
 * network measurement log file by the {@link MFSCommLogger#writeToLog(String)}
 * method. Otherwise, an instance of {@link MFSNullCommLogInfo} does nothing.
 * @author The MFS Client and Print Server Development Team
 */
public interface MFSCommLogInfo
{
	/**
	 * Invoked by the Comm model at the beginning of a transaction. If network
	 * measurement logging is enabled, this method should store the name and
	 * start time of the transaction. Otherwise, this method should do nothing.
	 * @param transactionName the name of the transaction
	 */
	public void startTransaction(String transactionName);

	/**
	 * Invoked by the Comm model at the end of a transaction. If network
	 * measurement logging is enabled, this method should determine the end time
	 * of the transaction and invoke the {@link MFSCommLogger#writeToLog(String)}
	 * method. Otherwise, this method should do nothing.
	 */
	public void endTransaction();
}
