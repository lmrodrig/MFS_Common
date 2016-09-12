/* @ Copyright IBM Corporation 2005, 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15      34242JR  R Prechel        -Java 5 version
 * 2007-11-06   ~1 40104PB  R Prechel        -Send MFSSRV and MFSRTR in request.
 *                                           -Removed redundant code.
 * 2008-02-07   ~2 40845MZ  R Prechel        -Strip back calls to RTV_MCHIP
 * 2010-11-01  ~03 49513JM	Toribio H.   	 -Make RTV_IRCD Cacheable 
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;

import com.ibm.rchland.mfgapps.client.utils.io.IGSPad;
import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.trx.RTV_MCHIP;

/**
 * <code>MFSPrintDriver</code> sends data to the print server.
 * @author The MFS Client Development Team
 */
public class MFSPrintDriver
{
	/** The <code>Socket</code>. */
	private Socket sock = null;

	//~1C Renamed sockIn to dataIn to mirror name in PrintHelper
	/** A <code>DataInputStream</code> for the <code>Socket</code>. */
	private DataInputStream dataIn = null;

	/** An <code>ObjectOutputStream</code> for the <code>Socket</code>. */
	private ObjectOutputStream objectOut = null;

	/** <code>true</code> for debug mode. */
	public boolean debug = true;

	/** The <code>Hashtable</code> used to store transaction information. */
	@SuppressWarnings("rawtypes")
	private Hashtable fields = new Hashtable();

	/** Constructs a new <code>MFSPrintDriver</code>. */
	public MFSPrintDriver()
	{
		super();
	}

	/**
	 * Adds a value to the fields <code>Hashtable</code>
	 * @param key the key for the value
	 * @param value the value
	 */
	@SuppressWarnings("unchecked")
	public final void addValue(String key, Object value)
	{
		if (!key.trim().equals("") || value != null) //$NON-NLS-1$
		{
			this.fields.put(key, value);
		}
	}

	/**
	 * Adds a value to the fields <code>Hashtable</code>
	 * @param key the key for the value
	 * @param value the value
	 */
	@SuppressWarnings("unchecked")
	public final void addValue(String key, String value)
	{
		if (!key.trim().equals("") || !value.trim().equals("")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			this.fields.put(key, value);
		}
	}

	//~1C Changed to correctly close the connection
	//and not call finalize
	/** Closes the <code>Socket</code> and its streams. */
	public final void closeConnections()
	{
		//~2C Use IGSXMLTransaction.closeConnection
		IGSXMLTransaction.closeConnection(this.sock, this.dataIn, this.objectOut);
		this.objectOut = null;
		this.dataIn = null;
		this.sock = null;
	}

	/**
	 * Initializes the <code>Socket</code> connection.
	 * @param trans the transaction
	 * @return 0 on success; nonzero on failure
	 */
	private final int initialize(String trans)
	{
		final MFSConfig config = MFSConfig.getInstance();
		String prntSvr = config.getConfigValue(trans);
		String prntPort = MFSConfig.NOT_FOUND; //~1C

		// The following checks to see if a port has been specified.
		if (prntSvr.indexOf("|", prntSvr.indexOf("|") + 1) != -1) //$NON-NLS-1$ //$NON-NLS-2$
		{
			prntSvr = prntSvr.substring(prntSvr.lastIndexOf("|") + 1).trim(); //$NON-NLS-1$
			prntPort = config.getConfigValue("PrinterPort" + prntSvr.substring(11)).trim(); //$NON-NLS-1$
			prntSvr = config.getConfigValue(prntSvr).trim();
		}
		// No port was specified
		else
		{
			prntSvr = config.getConfigValue("PrintServer").trim(); //$NON-NLS-1$
		}

		if (prntPort.equals(MFSConfig.NOT_FOUND)) //~1C
		{
			prntPort = config.getConfigValue("PrinterPort").trim(); //$NON-NLS-1$
		}

		try
		{
			//~2C Use localhost if environment switching is not enabled and the
			// computer name equals the name of the print server. Otherwise,
			// call RTV_MCHIP to retrieve the IP address.
			InetAddress address = null;
			if (config.isEnvironmentSwitchEnabled() == false
					&& config.getConfigValue(MFSConfig.COMPUTER_NAME).equalsIgnoreCase(prntSvr))
			{
				address = InetAddress.getLocalHost();
			}
			else
			{
				//~1C Use IGSPad instead of local method
				RTV_MCHIP rtvMCHIP = new RTV_MCHIP(); //~03
				rtvMCHIP.setInputPrintServer(IGSPad.pad(prntSvr, 20));				
				if (!rtvMCHIP.execute())
				{
					System.out.println(rtvMCHIP.getErrorMessage());
					return rtvMCHIP.getReturnCode();
				}
				address = InetAddress.getByName(rtvMCHIP.getOutputIPAddres());
			}
			int port = 0;
			try
			{
				port = Integer.parseInt(prntPort);
			}
			catch (Exception e)
			{
				port = 2500;
			}

			if (this.debug)
			{
				StringBuffer message = new StringBuffer();
				message.append("Opening socket to "); //$NON-NLS-1$
				message.append(address);
				message.append(" using port = "); //$NON-NLS-1$
				message.append(port);
				message.append(".\n"); //$NON-NLS-1$
				System.out.println(message);
			}

			this.sock = new Socket(address, port);
			this.dataIn = new DataInputStream(this.sock.getInputStream());
			this.objectOut = new ObjectOutputStream(this.sock.getOutputStream());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	/**
	 * Sends the transaction data to the print server.
	 * @param transaction the transaction name
	 * @param copies the number of copies
	 * @return 0 on success; nonzero on failure
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final int print(String transaction, int copies)
	{
		int rc = 0;

		if ((rc = initialize(transaction)) != 0)
		{
			return rc;
		}

		final MFSConfig config = MFSConfig.getInstance();
		//~1C Use copy constructor
		Hashtable myHash = new Hashtable(this.fields);
		myHash.put("TRAN", transaction); //$NON-NLS-1$
		myHash.put("QTY", (new Integer(copies)).toString()); //$NON-NLS-1$
		//~1C Add MFSSRV and MFSRTR to the Hashtable
		myHash.put(MFSConfig.MFSSRV, config.getConfigValue(MFSConfig.MFSSRV));
		myHash.put(MFSConfig.MFSRTR, config.getConfigValue(MFSConfig.MFSRTR));

		try
		{
			this.objectOut.writeObject(myHash);
			System.out.println(this.objectOut.toString());

			this.objectOut.flush();
			rc = this.dataIn.readInt();
		}
		catch (NotSerializableException ns)
		{
			rc = -10;
			System.err.println("Error: " + ns.getMessage() + " is not serializable."); //$NON-NLS-1$ //$NON-NLS-2$
			ns.printStackTrace();
		}
		catch (IOException e)
		{
			//~1C Removed the loop logic.  Error out instead.
			rc = -10;
			System.out.println(e);
		}

		if (this.debug)
		{
			System.out.println("Data Sent = " + myHash.toString() + ", rc = " + rc + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		this.fields.clear();
		closeConnections();
		return rc;
	}
}
