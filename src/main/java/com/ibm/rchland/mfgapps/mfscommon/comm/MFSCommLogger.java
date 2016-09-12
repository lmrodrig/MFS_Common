/* @ Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-23      37676JM  R Prechel        -Initial version
 * 2007-11-02   ~1 40104PB  R Prechel        -Constant name change.
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import mfsxml.MfsXMLDocument;
import mfsxml.MfsXMLParser;

import com.ibm.rchland.mfgapps.client.utils.io.IGSFileUtils;
import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;

/**
 * <code>MFSCommLogger</code> provides the core functionality for network
 * measurement logging. The {@link #setActive(String)} method determines whether
 * network measurement logging is enabled based on the NETWORKMEASURE XML
 * element. If enabled, the class maintains the local network measurement log
 * file and creates the XML for the LOGNETM transaction.
 * @author The MFS Client and Print Server Development Team
 */
public class MFSCommLogger
{
	/**
	 * The shared instance of <code>MFSNullCommLogInfo</code> returned by
	 * {@link #createLogInfo()} when network measurement logging is not enabled.
	 */
	private static final MFSNullCommLogInfo NULL_INFO = new MFSNullCommLogInfo();

	/**
	 * The maximum number of network measurement records (RCD elements) allowed
	 * in the input of the LOGNETM transaction.
	 */
	private static final int MAX_RECORDS = 20000;

	/** The <code>File</code> for the network measurement log file. */
	private static File logFile = null;

	/** The <code>PrintWriter</code> for the network measurement log file. */
	private static PrintWriter logWriter = null;

	/**
	 * Determines whether network measurement logging is enabled. If the
	 * specified <code>xml</code> contains a NETWORKMEASURE element and the
	 * value of the NETWORKMEASURE element data is YES, network measurement
	 * logging is enabled; otherwise network measurement logging is not enabled.
	 * If network measurement logging is enabled, a new local network
	 * measurement log file is created in the logs directory and the variables
	 * {@link #logFile} and {@link #logWriter} are assigned to reference and
	 * write to the log file. Otherwise, the variables {@link #logFile} and
	 * {@link #logWriter} are assigned the value <code>null</code>.
	 * @param xml the XML that contains the NETWORKMEASURE element
	 */
	public static void setActive(String xml)
	{
		boolean enable = false;
		try
		{
			MfsXMLParser parser = new MfsXMLParser(xml);
			String value = parser.getFieldOnly("NETWORKMEASURE"); //$NON-NLS-1$
			if ("YES".equals(value)) //$NON-NLS-1$
			{
				enable = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (enable)
		{
			try
			{
				String pattern = "'net'MMddkkmmss.'txt'"; //$NON-NLS-1$
				String fileName = new SimpleDateFormat(pattern).format(new Date());
				MFSCommLogger.logFile = IGSFileUtils.getLogFile(fileName);
				if (MFSCommLogger.logFile.exists())
				{
					IGSFileUtils.archive(MFSCommLogger.logFile);
					MFSCommLogger.logFile = IGSFileUtils.getLogFile(fileName);
				}
				Writer w = new BufferedWriter(new FileWriter(MFSCommLogger.logFile));
				MFSCommLogger.logWriter = new PrintWriter(w, true);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				reset();
			}
		}
		else
		{
			reset();
		}
	}

	/**
	 * @return a new {@link MFSActiveCommLogInfo} if network measurement logging
	 *         is enabled or a shared instance of {@link MFSNullCommLogInfo} if
	 *         network measurement logging is not enabled
	 */
	public static MFSCommLogInfo createLogInfo()
	{
		if (MFSCommLogger.logWriter == null)
		{
			return NULL_INFO;
		}
		return new MFSActiveCommLogInfo();
	}

	/**
	 * Writes a single network measurement record to the local network
	 * measurement log file.
	 * @param record the network measurement record. The record should be in XML
	 *        format and contain the following XML elements
	 *        <dl>
	 *        <dt>TRX</dt>
	 *        <dd>Transaction Name</dd>
	 *        <dt>STT</dt>
	 *        <dd>Start Timestamp (YYYY-MM-DD-HH.MM.SS.MMM)</dd>
	 *        <dt>ENT</dt>
	 *        <dd>End Timestamp (YYYY-MM-DD-HH.MM.SS.MMM)</dd>
	 *        </dl>
	 * @throws NullPointerException if network measurement logging is not
	 *         enabled. (Note: This method should only be called if network
	 *         measurement logging is enabled.)
	 */
	public static void writeToLog(String record)
	{
		MFSCommLogger.logWriter.println(record);
	}

	/**
	 * If network measurement logging is enabled, this method creates the XML
	 * for the LOGNETM transaction using the first {@link #MAX_RECORDS} network
	 * measurement records from the local network measurement log file and
	 * disables network measurement logging.
	 * @param clientID the client ID (MFSCLIENT or MFSPRINT)
	 * @return the XML for the LOGNETM transaction or <code>null</code> if
	 *         network measurement logging was not enabled
	 */
	public static String createLOGNETM_XML(String clientID)
	{
		if (MFSCommLogger.logWriter == null)
		{
			return null;
		}

		//~1C MFSConfig.PORT changed to MFSConfig.MFSRTR
		MFSConfig config = MFSConfig.getInstance();
		String port = config.getConfigValue(MFSConfig.MFSRTR);
		String user = config.getConfigValue("USER"); //$NON-NLS-1$

		MfsXMLDocument document = new MfsXMLDocument("LOGNETM"); //$NON-NLS-1$
		document.addOpenTag("DATA"); //$NON-NLS-1$
		document.addOpenTag("HEADER"); //$NON-NLS-1$
		document.addCompleteField("CLID", clientID); //$NON-NLS-1$
		document.addCompleteField("PORT", port); //$NON-NLS-1$
		document.addCompleteField("USER", user); //$NON-NLS-1$
		document.addCloseTag("HEADER"); //$NON-NLS-1$
		readLogFile(document);
		document.addCloseTag("DATA"); //$NON-NLS-1$
		document.finalizeXML();

		reset();

		return document.toString();
	}

	/**
	 * Reads the first {@link #MAX_RECORDS} network measurement records from the
	 * network measurement log file and adds them to the specified
	 * {@link MfsXMLDocument}.
	 * @param document the <code>MfsXMLDocument</code> for the LOGNETM
	 *        transaction
	 */
	private static void readLogFile(MfsXMLDocument document)
	{
		BufferedReader input = null;
		try
		{
			input = new BufferedReader(new FileReader(MFSCommLogger.logFile));
			String line;
			int count = 0;
			while ((line = input.readLine()) != null && count++ < MAX_RECORDS)
			{
				document.addCompleteField("RCD", line); //$NON-NLS-1$
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				if (input != null)
				{
					input.close();
				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}

	/** Helper method used to reset the state of <code>MFSCommLogger</code>. */
	private static void reset()
	{
		MFSCommLogger.logFile = null;
		if (MFSCommLogger.logWriter != null)
		{
			MFSCommLogger.logWriter.close();
		}
		MFSCommLogger.logWriter = null;
	}
}
