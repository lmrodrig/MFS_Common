/* © Copyright IBM Corporation 2005, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15   ~1 34242JR  R Prechel        -Java 5 version
 *                                           -Used MFSConfig instead of Configuration
 *                                           -Change default to a24prod from rchasa24
 * 2007-05-24   ~2 37676JM  R Prechel        -Perform network measurement logging
 ******************************************************************************/
package common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSCommLogInfo;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSCommLogger;

/**
 * <code>Comm</code> is the old server communications class. Use
 * {@link com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm} instead.
 */
public class Comm
	extends Thread
{
	public Socket fieldS = null;
	public DataInputStream fieldSin = new DataInputStream(null);
	public DataOutputStream fieldSout = new DataOutputStream(null);
	private String fieldData = new String();
	private String fieldInput = new String();
	protected transient PropertyChangeSupport propertyChange;
	private int fieldRc = 0;
	private MFSConfig fieldConfig = MFSConfig.getInstance(); //~1C
	public String server = "";
	public String port = "";
	
	/**
	 * The <code>MFSCommLogInfo</code> used to store network measurement
	 * information if network measurement logging is enabled.
	 */
	private MFSCommLogInfo logInfo; //~2A

	/** Constructs a new <code>Comm</code>. */
	public Comm()
	{
		super();
	}

	/**
	 * Constructs a new <code>Comm</code>.
	 * @param config the <code>MFSConfig</code>
	 * @param input the transaction input
	 */
	public Comm(MFSConfig config, String input) //~1C
	{
		super();
		setInput(input);
		setConfig(config);
	}

	/**
	 * Constructs a new <code>Comm</code>.
	 * @param input the transaction input
	 */
	public Comm(String input)
	{
		super();
		setInput(input);
	}

	/**
	 * Adds a <code>PropertyChangeListener</code> to the listener list.
	 * @param listener the <code>PropertyChangeListener</code> to add
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		getPropertyChange().addPropertyChangeListener(listener);
	}

	/**
	 * Connects to the server and sets up the <code>InputStream</code> and
	 * <code>OutputStream</code>.
	 * @param host the server host name
	 * @param port the server port
	 * @return 0 on success, nonzero on failure
	 */
	public int dial(String host, int port)
	{
		this.logInfo = MFSCommLogger.createLogInfo(); //~2A
		this.logInfo.startTransaction(getData()); //~2A
		int rc = 0;
		try
		{
			setS(new Socket(host, port));
			setSin(new DataInputStream(getS().getInputStream()));
			setSout(new DataOutputStream(getS().getOutputStream()));

			/* send the length of the pgm name string */
			String len = new String(getData().length() + "          ");
			len = len.substring(0, 10);
			getSout().write(len.getBytes(), 0, len.length());

			/* send the pgm string */
			getSout().write(getData().getBytes(), 0, getData().length());

		}
		catch (IOException e)
		{
			setData(new String(
					"Communication Error has Occurred within dial(), Contact Support."));
			rc = -10;
		}

		return rc;
	}

	/**
	 * Perform the endComm method.
	 */
	public void endComm()
	{
		this.logInfo.endTransaction(); //~2A
		try
		{
			if (getS() != null)
			{
				getS().close();
			}
		}

		catch (Exception e)
		{
			/* catch exception */
			System.out.println("Program Exception: " + e);
		}

		return;
	}

	/**
	 * Reports a boolean bound property update to any registered listeners.
	 * @param propertyName the programmatic name of the property that was changed
	 * @param oldValue the old value of the property
	 * @param newValue the new value of the property
	 */
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Gets the config property (mfs.Configuration) value.
	 * @return The config property value.
	 * @see #setConfig
	 */
	public MFSConfig getConfig()//~1C
	{
		return this.fieldConfig;
	}

	/**
	 * Gets the data property (java.lang.String) value.
	 * @return The data property value.
	 * @see #setData
	 */
	public String getData()
	{
		return this.fieldData;
	}

	/**
	 * Gets the input property (java.lang.String) value.
	 * @return The input property value.
	 * @see #setInput
	 */
	public String getInput()
	{
		return this.fieldInput;
	}

	/**
	 * This method was created in VisualAge.
	 * @return java.lang.String
	 */
	public String getPort()
	{
		return this.port;
	}

	/**
	 * Accessor for the propertyChange field.
	 */
	protected java.beans.PropertyChangeSupport getPropertyChange()
	{
		if (this.propertyChange == null)
		{
			this.propertyChange = new java.beans.PropertyChangeSupport(this);
		}
		return this.propertyChange;
	}

	/**
	 * Gets the rc property (int) value.
	 * @return The rc property value.
	 * @see #setRc
	 */
	public int getRc()
	{
		return this.fieldRc;
	}

	/**
	 * Gets the s property (java.net.Socket) value.
	 * @return The s property value.
	 * @see #setS
	 */
	public Socket getS()
	{
		return this.fieldS;
	}

	/**
	 * This method was created in VisualAge.
	 * @return java.lang.String
	 */
	public String getServer()
	{
		return this.server;
	}

	/**
	 * Gets the sin property (java.io.DataInputStream) value.
	 * @return The sin property value.
	 * @see #setSin
	 */
	public DataInputStream getSin()
	{
		return this.fieldSin;
	}

	/**
	 * Gets the sout property (java.io.DataOutputStream) value.
	 * @return The sout property value.
	 * @see #setSout
	 */
	public DataOutputStream getSout()
	{
		return this.fieldSout;
	}

	/**
	 * This method was created in VisualAge.
	 * @return java.lang.String
	 * @exception java.io.IOException The exception description.
	 */
	private java.lang.String readNetData()
		throws IOException
	{

		/* first read the length of the data */
		byte[] bLen = new byte[10];
		getSin().readFully(bLen, 0, 10);
		String sLen = new String(bLen);
		int len = Integer.parseInt(sLen.trim());

		/* read the data */
		byte[] bData = new byte[len];
		getSin().readFully(bData, 0, len);

		return new String(bData);
	}

	public String readNetString()
		throws IOException
	{
		return readNetData();
	}

	/**
	 * Perform the recvData method.
	 * @return int 0 on success, nonzero on failure
	 */
	public int recvData()
	{
		/* Perform the recvData method. */

		int rc = 0;
		int prev_rc = 0;
		StringBuffer buf = new StringBuffer();
		String netData;
		boolean xml = false;

		try
		{
			netData = new String(readNetData());

			if (netData.substring(0, 1).equals("<"))
			{
				xml = true;
				mfsxml.MfsXMLParser xmlParser = new mfsxml.MfsXMLParser(netData);
				rc = Integer.parseInt(xmlParser.getField("RRET").trim());
				buf.append(netData);
			}
			else
			{
				rc = Integer.parseInt(netData.substring(0, 10).trim());
				buf.append(netData.substring(10));
			}

			//if first rc = 9999, no data was sent ? Error out
			if (rc == 9999)
			{
				if (xml)
					setData(new String(
							"<ERMS>Communication Error? No data was sent!! Contact Support.</ERMS>"));
				else
					setData(new String(
							"Communication Error? No data was sent!! Contact Support."));
				rc = -10;
			}
			else
			{
				while (rc != 9999)
				{
					prev_rc = rc;
					netData = new String(readNetData());
					if (xml)
					{
						mfsxml.MfsXMLParser xmlParser = new mfsxml.MfsXMLParser(netData);
						rc = Integer.parseInt(xmlParser.getField("RRET").trim());
						buf.append(netData);
					}
					else
					{
						rc = Integer.parseInt(netData.substring(0, 10).trim());
						buf.append(netData.substring(10));
					}
				}

				if (rc == 9999) /* end of data */
				{
					rc = prev_rc;
				}

				if (rc != 0 && buf.toString().equals(getInput()))
				{
					setData("OutputEqualsInputException. An Unknown Communication Error Has Occurred. If the problem persists, please contact support!");
					rc = -10;
				}
				else
					setData(new String(buf.toString()));
			}

		}

		catch (Exception e)
		{
			if (xml)
				setData(new String(
						"<ERMS>An Exception has Occurred within recvData() - Communication Error! Contact Support.</ERMS>"));
			else
				setData(new String(
						"Communication Error has Occurred within recvData(), Contact Support."));
			System.out.println("Communication Error has Occurred within recvData(). \nError = " + e.toString());
			rc = -10;
		}

		return rc;
	}

	/**
	 * Removes a <code>PropertyChangeListener</code> from the listener list.
	 * @param listener the <code>PropertyChangeListener</code> to remove
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		getPropertyChange().removePropertyChangeListener(listener);
	}

	/**
	 * Perform the run method.
	 */
	public void run()
	{
		/* Perform the run method. */

		runTransaction();

		return;
	}

	/**
	 * Perform the runTransaction method.
	 */
	public void runTransaction()
	{
		/* Perform the runTransaction method. */
		//System.out.println("Running transaction");
		int rc = 0;

		if (getInput().substring(0, 1).equals("<"))
		{
			mfsxml.MfsXMLParser tempXmlParser = new mfsxml.MfsXMLParser(getInput());
			try
			{
				String pgm = new String(tempXmlParser.getField("TRX"));
				setData(pgm);
			}
			catch (Exception e)
			{
				setData(new String("NO TRX found in string"));
				rc = -10;

			}

		}

		else
			setData(getInput().substring(0, 10).trim());

		String myServer = getServer();
		if (myServer.equals(""))
		{
			myServer = getConfig().getConfigValue("MFSSRV");
		}

		String myPort = getPort();
		if (myPort.equals(""))
		{
			myPort = getConfig().getConfigValue("MFSRTR");
		}

		rc = dial(myServer, Integer.parseInt(myPort));

		setData(getInput());

		if (rc == 0)
		{
			rc = sendData();
		}

		if (rc == 0)
		{
			rc = recvData();
		}

		endComm();

		setRc(rc);

		return;
	}

	/**
	 * Perform the runTransaction method only up to the send. The caller will
	 * handle the receive.
	 */
	public void runTransactionSendOnly()
	{
		/* Perform the runTransaction method. */
		//System.out.println("Running transaction");
		int rc = 0;

		if (getInput().substring(0, 1).equals("<"))
		{
			mfsxml.MfsXMLParser tempXmlParser = new mfsxml.MfsXMLParser(getInput());
			try
			{
				String pgm = new String(tempXmlParser.getField("TRX"));
				setData(pgm);
			}
			catch (Exception e)
			{
				setData(new String("NO TRX found in string"));
				rc = -10;

			}

		}

		else
			setData(getInput().substring(0, 10).trim());

		String myServer = getServer();
		if (myServer.equals(""))
		{
			myServer = getConfig().getConfigValue("MFSSRV");
		}

		String myPort = getPort();
		if (myPort.equals(""))
		{
			myPort = getConfig().getConfigValue("MFSRTR");
		}

		rc = dial(myServer, Integer.parseInt(myPort));

		setData(getInput());

		if (rc == 0)
		{
			rc = sendData();
		}
		return;
	}

	/**
	 * Perform the sendData method.
	 * @return 0 on success, nonzero on failure
	 */
	public int sendData()
	{
		/* Perform the sendData method. */
		int rc = 0;

		/* First, send the length of the data string */
		try
		{
			String buf = new String("         0" + getData());
			String len = new String(buf.length() + "          ");
			len = len.substring(0, 10);
			getSout().write(len.getBytes(), 0, len.length());

			/* send the data string */
			getSout().write(buf.getBytes(), 0, buf.length());
		}
		catch (Exception e)
		{
			System.out.println("error occurred in sendData()");
			setData(new String(
					"Communication Error has Occurred within sendData(), Contact Support."));
			rc = -10;
		}

		return rc;
	}

	/**
	 * Sets the config property (mfs.Configuration) value.
	 * @param config The new value for the property.
	 * @see #getConfig
	 */
	public void setConfig(MFSConfig config)//~1C
	{
		MFSConfig oldValue = this.fieldConfig;//~1C
		this.fieldConfig = config;
		firePropertyChange("config", oldValue, config);
	}

	/**
	 * Sets the data property (java.lang.String) value.
	 * @param data The new value for the property.
	 * @see #getData
	 */
	public void setData(String data)
	{
		this.fieldData = data;
	}

	/**
	 * Sets the input property (java.lang.String) value.
	 * @param input The new value for the property.
	 * @see #getInput
	 */
	public void setInput(String input)
	{
		String oldValue = this.fieldInput;
		this.fieldInput = input;
		firePropertyChange("input", oldValue, input);
	}

	/**
	 * This method was created in VisualAge.
	 * @param newValue java.lang.String
	 */
	public void setPort(String newValue)
	{
		this.port = newValue;
	}

	/**
	 * Sets the rc property (int) value.
	 * @param rc The new value for the property.
	 * @see #getRc
	 */
	public void setRc(int rc)
	{
		int oldValue = this.fieldRc;
		this.fieldRc = rc;
		firePropertyChange("rc", new Integer(oldValue), new Integer(rc));
	}

	/**
	 * Sets the s property (java.net.Socket) value.
	 * @param s The new value for the property.
	 * @see #getS
	 */
	public void setS(Socket s)
	{
		this.fieldS = s;
	}

	/**
	 * This method was created in VisualAge.
	 * @param newValue java.lang.String
	 */
	public void setServer(String newValue)
	{
		this.server = newValue;
	}

	/**
	 * Sets the sin property (java.io.DataInputStream) value.
	 * @param sin The new value for the property.
	 * @see #getSin
	 */
	public void setSin(DataInputStream sin)
	{
		this.fieldSin = sin;
	}

	/**
	 * Sets the sout property (java.io.DataOutputStream) value.
	 * @param sout The new value for the property.
	 * @see #getSout
	 */
	public void setSout(DataOutputStream sout)
	{
		this.fieldSout = sout;
	}
}
