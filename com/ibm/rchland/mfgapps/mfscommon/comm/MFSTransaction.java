/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-02-22      34242JR  R Prechel        -Initial version
 * 2007-05-22   ~1 37676JM  R Prechel        -Add getTransactionName method
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import com.ibm.rchland.mfgapps.mfscommon.exception.MFSCommException;

/**
 * <code>MFSTransaction</code> is a Java bean that encapsulates a
 * transaction's input, output, and return code, along with the message
 * displayed while the transaction executes.
 * <p>
 * In addition to the bean properties, <code>MFSTransaction</code> declares
 * abstract methods that determine the strategy used to process the transaction.
 * These methods are declared with a protected access level, as they should not be
 * used outside the comm framework of the
 * <code>com.ibm.rchland.mfgapps.mfscommon.comm</code> package.
 * @author The MFS Client Development Team
 */
public abstract class MFSTransaction
{
	/** The error message displayed when a transaction name was not found. */
	public static final String NO_TRX_FOUND = "No transaction found in string.";

	/** The error message displayed when the output equals the input. */
	public static final String OUTPUT_EQUALS_INPUT = "Output Equals Input Exception. An unknown communication error has occurred. Please contact support.";

	/** The error message displayed when an error occurred but no ERMS tag was found. */
	public static final String NO_ERMS_TAG = "An error occurred but no ERMS tag was found.";

	/** The message displayed while the transaction executes. */
	private String fieldActionMessage = " "; //$NON-NLS-1$

	/** The transaction's input. */
	private String fieldInput = ""; //$NON-NLS-1$

	/** The transaction's output. */
	private String fieldOutput = ""; //$NON-NLS-1$

	/** The transaction's return code. */
	private int fieldReturnCode = 0;

	/**
	 * Constructs a new <code>MFSTransaction</code>.
	 * @param input the transaction's input
	 */
	public MFSTransaction(String input)
	{
		this.fieldInput = input;
	}

	/**
	 * Constructs a new <code>MFSTransaction</code>.
	 * @param input the transaction's input
	 * @param message the message displayed while the transaction executes
	 */
	public MFSTransaction(String input, String message)
	{
		this.fieldInput = input;
		this.fieldActionMessage = message;
	}

	/**
	 * Returns the transaction's action message.
	 * @return the message displayed while the transaction executes
	 */
	public String getActionMessage()
	{
		return this.fieldActionMessage;
	}

	/**
	 * Returns the transaction's error message if an error occurred.
	 * @return the transaction's error message if an error occurred
	 */
	public abstract String getErms();

	/**
	 * Returns the transaction's input.
	 * @return the transaction's input
	 */
	public String getInput()
	{
		return this.fieldInput;
	}

	/**
	 * Returns the transaction's output.
	 * @return the transaction's output
	 */
	public String getOutput()
	{
		return this.fieldOutput;
	}

	/**
	 * Returns the transaction's return code.
	 * @return the transaction's return code
	 */
	public int getReturnCode()
	{
		return this.fieldReturnCode;
	}

	//~1A New method
	/**
	 * Returns the transaction's name.
	 * @return the transaction's name
	 * @throws MFSCommException if there is no transaction name in the input
	 */
	public abstract String getTransactionName()
		throws MFSCommException;

	/**
	 * Sets the transaction's action message.
	 * @param message the message displayed while the transaction executes
	 */
	public void setActionMessage(String message)
	{
		this.fieldActionMessage = message;
	}

	/**
	 * Sets the transaction's input.
	 * @param input the transaction's input
	 */
	public void setInput(String input)
	{
		this.fieldInput = input;
		this.fieldOutput = ""; //$NON-NLS-1$
		this.fieldReturnCode = 0;
	}

	/**
	 * Sets the transaction's output.
	 * @param output the transaction's output
	 */
	public void setOutput(String output)
	{
		this.fieldOutput = output;
	}

	/**
	 * Sets the transaction's return code.
	 * @param returnCode the transaction's return code
	 */
	public void setReturnCode(int returnCode)
	{
		this.fieldReturnCode = returnCode;
	}

	/**
	 * Returns a string representation of this <code>MFSTransaction</code>.
	 * @return a string representation of this <code>MFSTransaction</code>
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Input: ");
		buffer.append(this.fieldInput);
		buffer.append("\nOutput: ");
		buffer.append(this.fieldOutput);
		buffer.append("\nReturn code: ");
		buffer.append(this.fieldReturnCode);
		buffer.append("\nAction Message: ");
		buffer.append(this.fieldActionMessage);
		return buffer.toString();
	}

	/**
	 * Called by the {@link MFSComm} at the start of the transaction's execution.
	 * @return the transaction's name
	 * @throws MFSCommException if there is no transaction name in the input
	 */
	protected abstract String startTransaction()
		throws MFSCommException;

	/**
	 * Parses the return code for the specified buffer of data.
	 * @param data a buffer of data returned by the server
	 * @return the return code for the specified buffer of data
	 * @throws MFSCommException if there is no return code in the data
	 */
	protected abstract int parseReturnCode(String data)
		throws MFSCommException;

	/**
	 * Processes the specified buffer of data.
	 * @param data a buffer of data returned by the server
	 * @throws MFSCommException if an error occurs while processing the data
	 */
	protected abstract void processBuffer(String data)
		throws MFSCommException;

	/**
	 * Called by the {@link MFSComm} at the end of the transaction's execution.
	 * @param returnCode the return code for the transaction
	 * @throws MFSCommException if there is a problem with the transaction's
	 *         output or return code
	 */
	protected abstract void endTransaction(int returnCode)
		throws MFSCommException;

	/**
	 * Used to set the output and return code if an error occurred.
	 * @param error a message describing the error
	 * @param returnCode the return code for the transaction
	 */
	protected abstract void setError(String error, int returnCode);
}
