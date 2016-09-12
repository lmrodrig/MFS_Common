/* @ Copyright IBM Corporation 2006, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2006-10-24      36621JM  R Prechel        -Initial version
 * 2007-01-15      34242JR  R Prechel        -Java 5 version
 * 2007-05-01   ~1 38495JM  R Prechel        -Log OS Information
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.net.InetAddress;
import java.net.UnknownHostException;

import mfsxml.MfsXMLDocument;

/**
 * <code>MFSLogInfoTransactionXML</code> creates the XML <code>String</code>
 * for the <code>LOGTRKINFO</code> transaction.
 * @author The MFS Client Development Team
 */
public class MFSLogInfoTransactionXML
{
	/** The client ID. */
	private String clientID;

	/** The unique name of the computer. */
	private String uniqueName;

	/** The cell id of the computer. */
	private String cellID;

	/** The version number of the client. */
	private String clientVersion;

	/** The server to which the client is connected. */
	private String server;

	/** The server port to which the client is connected. */
	private String port;

	/** The ID of the user logged into the client. */
	private String userID;

	/** The cell type of the computer. */
	private String cellType;

	/** General field one for future use. */
	private String generalOne;

	/** General field two for future use. */
	private String generalTwo;

	/** General field three for future use. */
	private String generalThree;

	/**
	 * Constructs a new <code>MFSLogInfoTransactionXML</code>.
	 * @param clientID the client ID
	 */
	public MFSLogInfoTransactionXML(String clientID)
	{
		this.clientID = clientID;
	}

	/**
	 * Creates the XML <code>String</code> for the LOGTRKINFO transaction.
	 * @return the XML <code>String</code> for the LOGTRKINFO transaction
	 */
	public String createXML()
	{
		String value;
		MfsXMLDocument document = new MfsXMLDocument("LOGTRKINFO"); //$NON-NLS-1$
		document.addOpenTag("DATA"); //$NON-NLS-1$
		append("CLID", this.clientID, document); //$NON-NLS-1$
		append("UNNM", this.uniqueName, document); //$NON-NLS-1$
		append("CELL", this.cellID, document); //$NON-NLS-1$

		try
		{
			value = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException uhe)
		{
			value = null;
		}
		append("IPAD", value, document); //$NON-NLS-1$

		value = System.getProperty("java.version"); //$NON-NLS-1$
		append("REVS", value, document); //$NON-NLS-1$

		append("CLVS", this.clientVersion, document); //$NON-NLS-1$
		append("SRVR", this.server, document); //$NON-NLS-1$
		append("PORT", this.port, document); //$NON-NLS-1$
		append("USER", this.userID, document); //$NON-NLS-1$
		append("CTYP", this.cellType, document); //$NON-NLS-1$
		append("GEN1", this.generalOne, document); //$NON-NLS-1$
		append("GEN2", this.generalTwo, document); //$NON-NLS-1$
		append("GEN3", this.generalThree, document); //$NON-NLS-1$
		document.addCloseTag("DATA"); //$NON-NLS-1$
		document.finalizeXML();

		return document.toString();
	}

	/**
	 * If <code>value</code> is not <code>null</code>, appends an XML
	 * element for the given element <code>name</code> and <code>value</code>
	 * to the specified <code>document</code>.
	 * @param name the name of the XML element
	 * @param value the value of the XML element
	 * @param document the <code>MfsXMLDocument</code>
	 */
	private void append(String name, String value, MfsXMLDocument document)
	{
		if (value != null && (value = value.trim()).length() != 0)
		{
			document.addCompleteField(name, value);
		}
	}

	/**
	 * Determines the values used to construct the XML <code>String</code>
	 * from the specified <code>configuration</code> as follows: <br>
	 * computername &rarr; Unique Name <br>
	 * CELL &rarr; Cell ID <br>
	 * VERSION &rarr; Client Version <br>
	 * MFSSRV &rarr; Server <br>
	 * MFSRTR &rarr; Port <br>
	 * USER &rarr; User ID <br>
	 * CELLTYPE &rarr; Cell Type <br>
	 */
	public void initValues()
	{
		//~1C Removed MFSConfig parameter
		MFSConfig configuration = MFSConfig.getInstance();
		this.uniqueName = configuration.getConfigValue("computername"); //$NON-NLS-1$
		this.cellID = configuration.getConfigValue("CELL"); //$NON-NLS-1$
		this.clientVersion = configuration.getConfigValue("VERSION"); //$NON-NLS-1$
		this.server = configuration.getConfigValue("MFSSRV"); //$NON-NLS-1$
		this.port = configuration.getConfigValue("MFSRTR"); //$NON-NLS-1$
		this.userID = configuration.getConfigValue("USER"); //$NON-NLS-1$
		this.cellType = configuration.getConfigValue("CELLTYPE"); //$NON-NLS-1$
	}

	/**
	 * Sets the element data for the computer's cell ID.
	 * @param cellID the computer's cell id
	 */
	public void setCellID(String cellID)
	{
		this.cellID = cellID;
	}

	/**
	 * Sets the element data for the computer's cell type.
	 * @param cellType the computer's cell type
	 */
	public void setCellType(String cellType)
	{
		this.cellType = cellType;
	}

	/**
	 * Sets the element data for the version number of the client.
	 * @param clientVersion the version number of the client
	 */
	public void setClientVersion(String clientVersion)
	{
		this.clientVersion = clientVersion;
	}

	/**
	 * Sets the value used for the GEN1 element.
	 * @param generalOne the value of the GEN1 element
	 */
	public void setGeneralOne(String generalOne)
	{
		this.generalOne = generalOne;
	}

	/**
	 * Sets the value used for the GEN3 element.
	 * @param generalThree the value of the GEN3 element
	 */
	public void setGeneralThree(String generalThree)
	{
		this.generalThree = generalThree;
	}

	/**
	 * Sets the value used for the GEN2 element.
	 * @param generalTwo the value of the GEN2 element
	 */
	public void setGeneralTwo(String generalTwo)
	{
		this.generalTwo = generalTwo;
	}
	
	//~1A New method
	/** Sets the value of the GEN1 element to OS information. */
	public void setOSInfo()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(System.getProperty("os.name")); //$NON-NLS-1$
		buffer.append(':');
		buffer.append(System.getProperty("os.version")); //$NON-NLS-1$
		
		if(buffer.length() <= 20)
		{
			this.generalOne = buffer.toString();
		}
		else
		{
			this.generalOne = buffer.substring(0, 20);
		}
	}

	/**
	 * Sets the element data for the server port to which the client is
	 * connected.
	 * @param port the server port to which the client is connected
	 */
	public void setPort(String port)
	{
		this.port = port;
	}

	/**
	 * Sets the element data for the server to which the client is connected.
	 * @param server the server to which the client is connected
	 */
	public void setServer(String server)
	{
		this.server = server;
	}

	/**
	 * Sets the element data for the unique name of the computer.
	 * @param uniqueName the unique name of the computer
	 */
	public void setUniqueName(String uniqueName)
	{
		this.uniqueName = uniqueName;
	}

	/**
	 * Sets the element data for the ID of the user logged into the client.
	 * @param userID the ID of the user logged into the client
	 */
	public void setUserID(String userID)
	{
		this.userID = userID;
	}
}
