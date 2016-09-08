/* © Copyright IBM Corporation 2006, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2006-06-28      31801JM  R Prechel        -Initial version
 * 2007-01-15      34242JR  R Prechel        -Java 5 version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.exception;

/**
 * <code>MFSTransactionException</code> is a subclass of
 * <code>MFSException</code> thrown to indicate a server transaction failed.
 * @author The MFS Client Development Team
 */
public class MFSTransactionException
	extends MFSException
{
	/**
	 * Identifies the original class version for which this class is capable of
	 * writing streams and from which it can read.
	 */
	private static final long serialVersionUID = 1L;

	/** The name of the server transaction. */
	private String transaction;

	/** The input to the server transaction. */
	private String input;

	/** The output from the server transaction. */
	private String output;

	/** The return code from the server transaction. */
	private int rc;

	/**
	 * Constructs a new <code>MFSTransactionException</code>.
	 * @param transaction the name of the server transaction
	 * @param input the input to the server transaction
	 * @param output the output from the server transaction
	 * @param rc the return code from the server transaction
	 */
	public MFSTransactionException(String transaction, String input, String output, int rc)
	{
		super("transaction = " + transaction + "; rc = " + rc); //$NON-NLS-1$ //$NON-NLS-2$
		this.transaction = transaction;
		this.input = input;
		this.output = output;
		this.rc = rc;
	}

	/**
	 * Returns the name of the server transaction.
	 * @return the name of the server transaction
	 */
	public final String getTransactionName()
	{
		return this.transaction;
	}

	/**
	 * Returns the input to the server transaction.
	 * @return the input to the server transaction
	 */
	public final String getInput()
	{
		return this.input;
	}

	/**
	 * Returns the output from the server transaction.
	 * @return the output from the server transaction
	 */
	public final String getOuput()
	{
		return this.output;
	}

	/**
	 * Returns the return code from the server transaction.
	 * @return the return code from the server transaction
	 */
	public final int getRC()
	{
		return this.rc;
	}
}
