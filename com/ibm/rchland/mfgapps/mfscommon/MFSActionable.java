/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15      34242JR  R Prechel        -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

/**
 * <code>MFSActionable</code> is an interface for classes that indicate the
 * status of an action that is being performed by the application.
 * @author The MFS Client Development Team
 */
public interface MFSActionable
{
	/**
	 * Starts indicating that an action is being performed.
	 * @param message a message describing the action that is being performed
	 */
	public void startAction(String message);

	/**
	 * Updates the indicated action status. This method can be used to update
	 * the message that is displayed and/or to update the progress of the action
	 * (e.g., set the value of a progress bar).
	 * <p>
	 * Note: This method MUST only be called after {@link #startAction(String)}
	 * and before {@link #stopAction()}, otherwise its behavior is undefined.
	 * @param message a message describing the action that is being performed
	 * @param progress the percentage of the action that is complete. A value of
	 *        -1 indicates that the length of the action is unknown.
	 */
	public void updateAction(String message, int progress);

	/** Stops indicating that an action is being performed. */
	public void stopAction();
}
