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
 * <code>MFSFixedTransaction</code> is a subclass of
 * <code>MFSTransaction</code> that defines the strategy used to parse
 * fixed-format (i.e., positional based) transactions.
 * @author The MFS Client Development Team
 */
public class MFSFixedTransaction
	extends MFSTransaction
{
	/** The maximum length of a positional transaction's name. */
	private static final int TRANSACTION_NAME_MAX_LENGTH = 10;

	/** The maximum length of a positional transaction's return code. */
	private static final int RETURN_CODE_MAX_LENGTH = 10;

	/** Used to accumulate the transaction's output. */
	private StringBuffer fieldBuffer = null;

	/**
	 * Constructs a new <code>MFSFixedTransaction</code>.
	 * @param input the transaction's input
	 */
	public MFSFixedTransaction(String input)
	{
		super(input);
	}

	/**
	 * Constructs a new <code>MFSFixedTransaction</code>.
	 * @param input the transaction's input
	 * @param message the message displayed while the transaction executes
	 */
	public MFSFixedTransaction(String input, String message)
	{
		super(input, message);
	}

	/** {@inheritDoc} */
	public String getErms()
	{
		return getOutput();
	}

	//~1A New method
	/** {@inheritDoc} */
	public String getTransactionName()
		throws MFSCommException
	{
		try
		{
			return getInput().substring(0, TRANSACTION_NAME_MAX_LENGTH).trim();
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			throw new MFSCommException(NO_TRX_FOUND);
		}
	}

	/** {@inheritDoc} */
	protected String startTransaction()
		throws MFSCommException
	{
		this.fieldBuffer = new StringBuffer();
		//~1C Use getTransactionName
		return getTransactionName();
	}

	/** {@inheritDoc} */
	protected int parseReturnCode(String data)
		throws MFSCommException
	{
		try
		{
			return Integer.parseInt(data.substring(0, RETURN_CODE_MAX_LENGTH).trim());
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			throw new MFSCommException(ioobe.getMessage(), ioobe);
		}
	}

	/** {@inheritDoc} */
	protected void processBuffer(String data)
		throws MFSCommException
	{
		try
		{
			this.fieldBuffer.append(data.substring(RETURN_CODE_MAX_LENGTH));
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			throw new MFSCommException(ioobe.getMessage(), ioobe);
		}
	}

	/** {@inheritDoc} */
	protected void endTransaction(int returnCode)
		throws MFSCommException
	{
		String result = this.fieldBuffer.toString();

		//~1C Use equals method instead of ==
		if (returnCode != 0 && result.equals(getInput()))
		{
			throw new MFSCommException(OUTPUT_EQUALS_INPUT);
		}

		setOutput(this.fieldBuffer.toString());
		setReturnCode(returnCode);
		this.fieldBuffer = null;
	}

	/** {@inheritDoc} */
	protected void setError(String error, int returnCode)
	{
		setOutput(error);
		setReturnCode(returnCode);
	}
}
