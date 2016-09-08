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

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLParser;

/**
 * <code>MFSXmlTransaction</code> is a subclass of <code>MFSTransaction</code>
 * that defines the strategy used to parse XML based transactions.
 * @author The MFS Client Development Team
 */
public class MFSXmlTransaction
	extends MFSTransaction
{
	/** Used to accumulate the transaction's output. */
	private StringBuffer fieldBuffer = null;

	/**
	 * Constructs a new <code>MFSXmlTransaction</code>.
	 * @param xml the transaction's input
	 */
	public MFSXmlTransaction(String xml)
	{
		super(xml);
	}

	/**
	 * Constructs a new <code>MFSXmlTransaction</code>.
	 * @param xml the transaction's input
	 * @param message the message displayed while the transaction executes
	 */
	public MFSXmlTransaction(String xml, String message)
	{
		super(xml, message);
	}

	/** {@inheritDoc} */
	public String getErms()
	{
		String errorMessage = null;
		try
		{
			MfsXMLParser xmlParser = new MfsXMLParser(getOutput());
			errorMessage = xmlParser.getField("ERMS"); //$NON-NLS-1$
		}
		catch (MISSING_XML_TAG_EXCEPTION mte)
		{
			errorMessage = NO_ERMS_TAG;
		}
		return errorMessage;
	}

	//~1A New method
	/** {@inheritDoc} */
	public String getTransactionName()
		throws MFSCommException
	{
		try
		{
			return new MfsXMLParser(this.getInput()).getField("TRX").trim(); //$NON-NLS-1$
		}
		catch (MISSING_XML_TAG_EXCEPTION mxte)
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
			return Integer.parseInt(new MfsXMLParser(data).getField("RRET").trim()); //$NON-NLS-1$
		}
		catch (MISSING_XML_TAG_EXCEPTION mste)
		{
			throw new MFSCommException("Missing rret tag.", mste); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	protected void processBuffer(String data)
		throws MFSCommException
	{
		this.fieldBuffer.append(data);
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
		setOutput("<ERMS>" + error + "</ERMS>"); //$NON-NLS-1$ //$NON-NLS-2$
		setReturnCode(returnCode);
	}
}
