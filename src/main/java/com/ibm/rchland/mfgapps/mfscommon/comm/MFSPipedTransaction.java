/* @ Copyright IBM Corporation 2007. All rights reserved.
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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLParser;

import com.ibm.rchland.mfgapps.mfscommon.exception.MFSCommException;

/**
 * <code>MFSPipedTransaction</code> is a subclass of
 * <code>MFSTransaction</code> that defines the strategy used to parse XML
 * based transactions using piped streams.
 * @author The MFS Client Development Team
 */
public class MFSPipedTransaction
	extends MFSTransaction
{
	/**
	 * The <code>PipedOutputStream</code> to which the transaction's output is
	 * sent. A <code>PipedInputStream</code> connected to this stream should
	 * be used to process the transaction's output.
	 */
	private PipedOutputStream fieldOut;

	/**
	 * Constructs a new <code>MFSPipedTransaction</code>.
	 * @param xml the transaction's input
	 */
	public MFSPipedTransaction(String xml)
	{
		super(xml);
	}

	/**
	 * Constructs a new <code>MFSPipedTransaction</code>.
	 * @param xml the transaction's input
	 * @param message the message displayed while the transaction executes
	 */
	public MFSPipedTransaction(String xml, String message)
	{
		super(xml, message);
	}

	/**
	 * Connects the specified <code>PipedInputStream</code> to the
	 * <code>PipedOutputStream</code> used by this
	 * <code>MFSPipedTransaction</code>.
	 * @param in the <code>PipedInputStream</code> from which the
	 *        transaction's output will be read
	 * @throws IOException if there is a problem connecting the piped streams
	 */
	public void connectStreams(PipedInputStream in)
		throws IOException
	{
		this.fieldOut = new PipedOutputStream(in);
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

	/**
	 * Sets the error message to <code>erms</code>.
	 * @param erms the error message
	 */
	public void setErms(String erms)
	{
		setOutput("<ERMS>" + erms + "</ERMS>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** {@inheritDoc} */
	protected String startTransaction()
		throws MFSCommException
	{
		//~1C Use getTransactionName
		return getTransactionName();
	}

	/** {@inheritDoc} */
	protected int parseReturnCode(String data)
	{
		if (data.length() == 23 && data.trim().length() == 0)
		{
			return MFSComm.END_OF_TRANSMISSION;
		}
		int start = data.indexOf("<RRET>"); //$NON-NLS-1$
		int end = data.indexOf("</RRET>"); //$NON-NLS-1$
		if (start != -1 && end != -1)
		{
			return Integer.parseInt(data.substring(start + 6, end).trim());
		}
		return 0;
	}

	/** {@inheritDoc} */
	protected void processBuffer(String data)
		throws MFSCommException
	{
		try
		{
			this.fieldOut.write(data.getBytes());
		}
		catch (IOException ioe)
		{
			throw new MFSCommException(MFSComm.COMM_ERROR, ioe);
		}
	}

	/** {@inheritDoc} */
	protected void endTransaction(int returnCode)
		throws MFSCommException
	{
		try
		{
			this.fieldOut.close();
		}
		catch (IOException ioe)
		{
			throw new MFSCommException(ioe.getMessage(), ioe);
		}
		setOutput(""); //$NON-NLS-1$
		setReturnCode(returnCode);
	}

	/** {@inheritDoc} */
	protected void setError(String error, int returnCode)
	{
		setOutput("<ERMS>" + error + "</ERMS>"); //$NON-NLS-1$ //$NON-NLS-2$
		setReturnCode(returnCode);
	}
}
