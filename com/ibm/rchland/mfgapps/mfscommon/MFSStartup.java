/* © Copyright IBM Corporation 2007, 2008. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-11-06      40104PB  R Prechel        -Initial version
 * 2008-01-12      39619JL  R Prechel        -Setup IGSXMLTransaction
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ibm.rchland.mfgapps.client.utils.io.IGSFileUtils;
import com.ibm.rchland.mfgapps.client.utils.messagebox.IGSMessageBox;
import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLTransaction;
import com.ibm.rchland.mfgapps.mfscommon.exception.MFSException;

/**
 * <code>MFSStartup</code> contains the startup logic used by both the MFS
 * Client and the MFS Print Server.
 * @author The MFS Client Development Team
 */
public class MFSStartup
{
	/** The key for the "Number of Hours to Save the Log Files" property. */
	public static final String HOURS = "hoursToSaveLogs"; //$NON-NLS-1$

	/** The key for the "Test Mode" property. */
	public static final String TEST_MODE = "testMode"; //$NON-NLS-1$

	/** The default port used to download the configuration. */
	public static final String DEFAULT_MFSRTR = "3050"; //$NON-NLS-1$

	/** The default number of hours to save the log files (5 days). */
	public static final int DEFAULT_HOURS = 120;

	/** Constructs a new <code>MFSStartup</code>. */
	private MFSStartup()
	{
		super();
	}

	/**
	 * Loads the configuration properties from the properties file, processes
	 * the command-line arguments, creates the log files if not in test mode,
	 * and load's the computer's configuration from the server.
	 * <p>
	 * The MFS Client and MFS Print Server can be started with zero or more
	 * command-line arguments. All command line-arguments are optional. The
	 * following options are currently defined:
	 * <dl>
	 * <dt>-t</dt>
	 * <dd>test mode</dd>
	 * </dl>
	 * @param args the command-line arguments for the application or
	 *        <code>null</code> after an environment switch occurs.
	 * @param filename the name of the configuration properties file
	 * @param redirect code>true</code> if standard out and standard err should
	 *        be redirected to the log and err file
	 * @return the configuration properties
	 * @throws MFSException if the properties cannot be loaded or as thrown by
	 *         {@link MFSConfig#loadConfiguration(String, String)}
	 */
	public static Properties configure(String[] args, String filename, boolean redirect)
		throws MFSException
	{
		Properties properties = new Properties();
		boolean testMode = false;
		String mfssrv = null;
		String mfsrtr = null;
		String hours = null;

		/* Load the configuration properties file. */
		try
		{
			InputStream in = new FileInputStream(IGSFileUtils.getFile(filename));
			properties.load(in);
			in.close();

			mfssrv = properties.getProperty(MFSConfig.MFSSRV);

			mfsrtr = properties.getProperty(MFSConfig.MFSRTR);
			if (mfsrtr == null)
			{
				mfsrtr = DEFAULT_MFSRTR;
				properties.setProperty(MFSConfig.MFSRTR, DEFAULT_MFSRTR);
			}

			hours = properties.getProperty(HOURS);
			if (hours == null)
			{
				hours = Integer.toString(DEFAULT_HOURS);
				properties.setProperty(HOURS, hours);
			}
		}
		catch (Exception e)
		{
			throw new MFSException(e);
		}

		/* Setup the log files if test mode is false. */
		/* This only needs to be performed on initial startup */
		/* and not after a switch (when args will be null). */
		if (args != null)
		{
			/* Check to see if test mode is set. */
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equalsIgnoreCase("-t")) //$NON-NLS-1$
				{
					testMode = true;
				}
			}

			/* Store the test mode value in the properties. */
			properties.setProperty(TEST_MODE, Boolean.toString(testMode));

			if (testMode == false)
			{
				try
				{
					try
					{
						int intHours = Integer.parseInt(hours);
						IGSFileUtils.setupLogFiles(intHours, redirect);
					}
					catch (NumberFormatException nfe)
					{
						IGSFileUtils.setupLogFiles(DEFAULT_HOURS, redirect);
					}
				}
				catch (IOException ioe)
				{
					String erms = "Log files were not created.";
					IGSMessageBox.showOkMB(null, null, erms, ioe);
				}
			}
		}

		//~1A Setup IGSXMLTransaction
		MFSConfig config = MFSConfig.getInstance();
		config.loadConfiguration(mfssrv, mfsrtr);
		String port = config.getConfigValue(MFSConfig.MFSRTR);
		IGSXMLTransaction.setDefaultPort(Integer.parseInt(port));
		IGSXMLTransaction.setDefaultServer(config.getConfigValue(MFSConfig.MFSSRV));
		return properties;
	}
}
