/* © Copyright IBM Corporation 2005, 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag 	IPSR/PTR 	Name             Details
 * ---------- ---- 	-------- 	---------------- ----------------------------------
 * 2007-01-29      	34242JR  	R Prechel       -Java 5 version
 * 2007-06-18   ~1 	37556CD  	T He            -Changed loadConfiguration() method
 * 2007-11-06   ~2 	40104PB  	R Prechel       -Environment switch changes
 * 2008-01-15   ~3 	37616JL  	D Pietrasik     -Add methods to handle object values
 * 2008-04-03   ~4 	37616JL  	R Prechel       -Add containsNmbrPrlnFlagEntry
 * 2008-07-29   ~5 	38990JL  	Santiago D      -Add loadPlomConfiguration() method
 * 2010-03-06   ~6 	42558JL  	Santiago SC     -Add getPlomConfigs() method
 * 2010-03-18	~7 	47595MZ		Ray Perry	 	-Create hashtable of RTV_HDREC values
 * 2010-08-31   ~8 	46704EM  	Edgar V.        -Return string instead a boolean in containsNmbrPrlnFlagEntry function()
 * 2010-11-01   ~9 	49513JM  	Toribio H.      -Add Generics to cooHash and remove headerHash because is substituted for Trx Cache
 * 2014-01-17	~10	RCQ00267733	VH Avila		-Add new Label Prefix
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import com.ibm.rchland.mfgapps.client.utils.messagebox.IGSMessageBox;
import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.exception.MFSException;
import com.ibm.rchland.mfgapps.mfscommon.exception.MFSInvalidConfigEntryException;

/**
 * <code>MFSConfig</code> stores a set of configuration entries. A
 * configuration entry consists of a <code>String</code> label and a
 * <code>String</code> value. The value may be the empty <code>String</code>.
 * @author The MFS Client Development Team
 */
public class MFSConfig
{
	/**
	 * The value returned by {@link #getConfigValue(String)} if a configuration
	 * entry with the specified label does not exist.
	 */
	public static final String NOT_FOUND = "Not Found"; //$NON-NLS-1$

	//~2C Change the name of SERVER to MFSSRV and PORT to MFSRTR
	/** The label for the server name configuration entry. */
	public static final String MFSSRV = "MFSSRV"; //$NON-NLS-1$

	/** The label for the server port configuration entry. */
	public static final String MFSRTR = "MFSRTR"; //$NON-NLS-1$

	/** The label for the computer name configuration entry. */
	public static final String COMPUTER_NAME = "computername"; //$NON-NLS-1$

	//~2A Add VERSION, COMPNAME, and PLANT
	/** The label for the version configuration entry. */
	public static final String VERSION = "VERSION"; //$NON-NLS-1$

	/** The label prefix for the environment computer name configuration entry. */
	public static final String COMPNAME = "COMPNAME"; //$NON-NLS-1$

	/** The label prefix for the plant description configuration entry. */
	public static final String PLANT = "PLANT"; //$NON-NLS-1$
	
	/** The label prefix for the user configuration entry. */	/*~10A*/
	public static final String USER = "USER"; //$NON-NLS-1$		/*~10A*/

	/** The length of a configuration entry label. */
	private static final int LABEL_LENGTH = 50;

	/** The length of a configuration entry value. */
	private static final int VALUE_LENGTH = 100;

	/**
	 * The length of a configuration entry record. Equal to
	 * <code>LABEL_LENGTH + VALUE_LENGTH</code>.
	 */
	private static final int RECORD_LENGTH = LABEL_LENGTH + VALUE_LENGTH;

	/** The sole instance of <code>MFSConfig</code>. */
	private static final MFSConfig INSTANCE = new MFSConfig();

	/** The <code>Hashtable</code> used to hold the configuration entries. */
	@SuppressWarnings("rawtypes")
	private Hashtable configuration = new Hashtable();

	/** The <code>Map</code> of plant descriptions to alternate computer names. */
	@SuppressWarnings("rawtypes")
	private Map environmentMap = new LinkedHashMap(); //~2A

	/** A <code>Hashtable</code> to save coo values **/
	private Hashtable<String, String> cooHash = new Hashtable<String, String>();

	/**
	 * Returns the sole instance of <code>MFSConfig</code>.
	 * @return the sole instance of <code>MFSConfig</code>
	 */
	public static MFSConfig getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Constructs a new <code>MFSConfig</code>. This class implements the
	 * <cite>Singleton </cite> design pattern. To ensure only one instance of
	 * <code>MFSConfig</code> exists, the only constructor has
	 * <code>private</code> visibility.
	 */
	private MFSConfig()
	{
		super();
	}

	/**
	 * Returns the sole instance of <code>MFSConfig</code>. Used by Java
	 * serialization; ensures only one instance of <code>MFSConfig</code>
	 * exists.
	 * @return the sole instance of <code>MFSConfig</code>
	 */
	protected Object readResolve()
	{
		return INSTANCE;
	}

	/**
	 * Downloads the configuration entries for this computer from the specified
	 * <code>server</code> using the specified <code>port</code>.
	 * @param server the name of the server from which the configuration entries
	 *        are downloaded
	 * @param port the server port from which the configuration entries are
	 *        downloaded
	 * @throws MFSException if an error occurs while loading the configuration
	 */
	@SuppressWarnings("unchecked")
	public void loadConfiguration(String server, String port)
		throws MFSException
	{
		if (server == null || port == null)
		{
			throw new MFSException("Server and port cannot be null."); //$NON-NLS-1$
		}

		//~2C Use computerName, MFSSRV, and MFSRTR variables
		final String computerName = MFSComputerName.getComputerName();
		this.configuration.clear(); //~1
		this.environmentMap.clear(); //~2A
		this.configuration.put(MFSSRV, server);
		this.configuration.put(MFSRTR, port);
		this.configuration.put(COMPUTER_NAME, computerName);

		String input = "RTV_CONF  " + computerName; //$NON-NLS-1$
		MFSTransaction rtv_conf = new MFSFixedTransaction(input);
		MFSComm.getInstance();
		MFSComm.execute(rtv_conf);

		if (rtv_conf.getReturnCode() != 0)
		{
			//~2A Use MessageFormat
			String pattern = "Error while downloading configuration for {0} from {1}.\nrc={2}\nmessage={3}"; //$NON-NLS-1$
			Integer rc = new Integer(rtv_conf.getReturnCode());
			Object args[] = {computerName, server, rc, rtv_conf.getOutput()};
			throw new MFSException(MessageFormat.format(pattern, args));
		}

		String data = rtv_conf.getOutput();
		if (data.trim().length() == 0)
		{
			throw new MFSException("Downloaded data has zero length."); //$NON-NLS-1$
		}

		while (data.trim().length() != 0 && RECORD_LENGTH <= data.length())
		{
			String label = data.substring(0, LABEL_LENGTH).trim();
			String value = data.substring(LABEL_LENGTH, RECORD_LENGTH).trim();
			setConfigValue(label, value);
			data = data.substring(RECORD_LENGTH);
		}

		//~2A Start environment switch setup
		if (isEnvironmentSwitchEnabled())
		{
			// Validate the config entries for the current environment
			// (COMPNAME0/PLANT0) and make sure an alternate is configured
			String compnameKey = COMPNAME + 0;
			String plantKey = PLANT + 0;
			if (!containsConfigEntry(compnameKey))
			{
				throw new MFSInvalidConfigEntryException(compnameKey, false);
			}
			if (!containsConfigEntry(plantKey))
			{
				throw new MFSInvalidConfigEntryException(plantKey, false);
			}
			if (!computerName.equals(getConfigValue(compnameKey)))
			{
				throw new MFSInvalidConfigEntryException(compnameKey, false);
			}

			compnameKey = COMPNAME + 1;
			plantKey = PLANT + 1;
			if (!containsConfigEntry(compnameKey))
			{
				throw new MFSInvalidConfigEntryException(compnameKey, false);
			}
			if (!containsConfigEntry(plantKey))
			{
				throw new MFSInvalidConfigEntryException(plantKey, false);
			}

			int index = 1;
			while (containsConfigEntry(compnameKey = MFSConfig.COMPNAME + index)
					&& containsConfigEntry(plantKey = MFSConfig.PLANT + index))
			{
				this.environmentMap.put(getConfigValue(plantKey),
						getConfigValue(compnameKey));
				index++;
			}
		}
		//~2A End environment switch setup
	}


	//~5A
	/**
	 * Loads the plom configuration entries
	 * @throws MFSException if an error occurs while loading the configuration
	 */	
	public void loadPlomConfiguration()
		throws MFSException
	{
		IGSXMLTransaction rtvPloms = new IGSXMLTransaction("RTV_PLOMS");  //$NON-NLS-1$
		rtvPloms.setActionMessage("Retrieving Plom configurations..."); //$NON-NLS-1$
		rtvPloms.startDocument();
		rtvPloms.endDocument();

		MFSComm.execute(rtvPloms);		
		
		if(rtvPloms.getReturnCode() != 0)
		{
			throw new MFSException(rtvPloms.getErms());
		}		
		
		// ~6C - Use StringBuffer
		while(rtvPloms.stepIntoElement("REC") != null) //$NON-NLS-1$
		{
			String plom = rtvPloms.getNextElement("PLOM"); //$NON-NLS-1$
			
			String label = "PLOM,"+plom; //$NON-NLS-1$
			
			StringBuffer value = new StringBuffer();			
			value.append("<REC>"); //$NON-NLS-1$
			value.append("<PLOM>"); value.append(plom); value.append("</PLOM>"); //$NON-NLS-1$ //$NON-NLS-2$
			value.append("<SPLT>"); value.append(rtvPloms.getNextElement("SPLT")); value.append("</SPLT>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			value.append("<PLNT>"); value.append(rtvPloms.getNextElement("PLNT")); value.append("</PLNT>");			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			value.append("<WTCC>"); value.append(rtvPloms.getNextElement("WTCC")); value.append("</WTCC>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			value.append("<ENVC>"); value.append(rtvPloms.getNextElement("ENVC")); value.append("</ENVC>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			value.append("<DISP>"); value.append(rtvPloms.getNextElement("DISP")); value.append("</DISP>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			value.append("</REC>");			 //$NON-NLS-1$
			value.trimToSize();
			
			setConfigValue(label, value.toString());
			rtvPloms.stepOutOfElement();
		}
	}	
	
	//~6A
	/**
	 * Returns an <code>ArrayList</code> of Strings containing the plom configurations.
	 * @return the <code>ArrayList</code> of Strings containing the plom configurations,
	 * 		 	null if no plom configs were found.
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<String> getPlomConfigs()
	{
		ArrayList<String> plomConfigs = new ArrayList<String>();
		Enumeration configLabels = getConfigLabels();
		
		String configLabel = ""; //$NON-NLS-1$
		String plomConfigHeader = "PLOM,"; //$NON-NLS-1$
		
		while(configLabels.hasMoreElements())
		{
			configLabel = configLabels.nextElement().toString();
			
			if(configLabel.startsWith(plomConfigHeader))
			{
				plomConfigs.add(getConfigValue(configLabel));
			}
		}
		
		if(plomConfigs.isEmpty())
		{
			plomConfigs = null;
		}
		
		return plomConfigs;
	}

	/**
	 * Returns the object value of the configuration entry with the specified
	 * <code>label</code>.
	 * @param label the label of the configuration entry
	 * @return the value of the configuration entry or null if a
	 *         configuration entry with the specified <code>label</code> does
	 *         not exist
	 */
	public Object getConfigObject(String label) /* ~3A */
	{
	    return this.configuration.get(label);
	}

	
	/**
	 * Returns the value of the configuration entry with the specified
	 * <code>label</code>.
	 * @param label the label of the configuration entry
	 * @return the value of the configuration entry or {@link #NOT_FOUND} if a
	 *         configuration entry with the specified <code>label</code> does
	 *         not exist
	 */
	public String getConfigValue(String label)
	{
		Object value = this.configuration.get(label);
		return (value == null ? NOT_FOUND : value.toString());
	}

	/**
	 * Returns the value of the CELL config entry padded to 8 characters.
	 * @return the value of the CELL config entry padded to 8 characters
	 */
	public String get8CharCell()
	{
		return getConfigValue("CELL").concat("         ").substring(0, 8); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the value of the CELLTYPE config entry padded to 8 characters.
	 * @return the value of the CELLTYPE config entry padded to 8 characters
	 */
	public String get8CharCellType()
	{
		return getConfigValue("CELLTYPE").concat("         ").substring(0, 8); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the value of the USER config entry padded to 8 characters.
	 * @return the value of the USER config entry padded to 8 characters
	 */
	public String get8CharUser()
	{
		return getConfigValue("USER").concat("         ").substring(0, 8); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns an <code>Enumeration</code> of the configuration entry labels.
	 * @return an <code>Enumeration</code> of the configuration entry labels
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration getConfigLabels()
	{
		return this.configuration.keys();
	}

	/**
	 * Adds a configuration entry with the specified <code>label</code> and
	 * <code>value</code>. If a configuration entry with the specified
	 * <code>label</code> already exists, the old value is replaced with the
	 * specified <code>value</code>.
	 * @param label the name of the configuration entry
	 * @param value the value of the configuration entry
	 */
	@SuppressWarnings("unchecked")
	public void setConfigValue(String label, Object value)  /* ~3C */
	{
		this.configuration.put(label, value);
	}

	/**
	 * Removes the configuration entry with the specified <code>label</code>.
	 * @param label the label of the configuration entry to remove
	 */
	public void removeConfigEntry(String label)
	{
		this.configuration.remove(label);
	}

	/**
	 * Returns <code>true</code> if this <code>MFSConfig</code> contains a
	 * configuration entry with the specified <code>label</code>;
	 * <code>false</code> otherwise.
	 * @param label the configuration entry label to check for
	 * @return <code>true</code> iff this <code>MFSConfig</code> contains a
	 *         configuration entry for the specified <code>label</code>
	 */
	public boolean containsConfigEntry(String label)
	{
		return this.configuration.containsKey(label);
	}
	
	//~4A New method
	/**
	 * Returns <code>true</code> if this <code>MFSConfig</code> contains a
	 * configuration entry for the specified <code>name</code>, the specified
	 * <code>nmbr</code> or *ALL, the specified <code>prln</code> or *ALL,
	 * and the specified <code>flag</code> or B.
	 * @param name the name of the configuration entry label
	 * @param nmbr the operation number
	 * @param prln the product line
	 * @param flag "E" for end, "S" for suspend
	 * @return <code>true</code> iff this <code>MFSConfig</code> contains
	 *         the specified configuration entry
	 */
	public String containsNmbrPrlnFlagEntry(String name, String nmbr, String prln, //~8C
												String flag)
	{
		final String nmbrPrln = nmbr + "," + prln; //$NON-NLS-1$
		
		if (!name.endsWith(",")) //$NON-NLS-1$
		{
			name = name + ","; //$NON-NLS-1$
		}

		//~8A Return de config value if found, else if not config value -> NOT_FOUND else null
		if (containsConfigEntry(name + nmbrPrln + "," + flag))//$NON-NLS-1$
		{
		  return (getConfigValue(name + nmbrPrln + "," + flag).toString()); //$NON-NLS-1$
		}
		else if (containsConfigEntry(name + nmbrPrln + ",B"))//$NON-NLS-1$
		{
		  return (getConfigValue(name + nmbrPrln + ",B").toString()); //$NON-NLS-1$
		}
		else if (containsConfigEntry(name + nmbr + ",*ALL," + flag))//$NON-NLS-1$
		{
		  return (getConfigValue(name + nmbr + ",*ALL," + flag).toString()); //$NON-NLS-1$
		}
		else if (containsConfigEntry(name + nmbr + ",*ALL,B"))//$NON-NLS-1$
		{
		  return (getConfigValue(name + nmbr + ",*ALL,B").toString()); //$NON-NLS-1$
		}
		else if (containsConfigEntry(name + "*ALL," + prln + "," + flag))//$NON-NLS-1$ //$NON-NLS-2$
		{
		  return (getConfigValue(name + "*ALL," + prln + "," + flag).toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (containsConfigEntry(name + "*ALL," + prln + ",B"))//$NON-NLS-1$ //$NON-NLS-2$
		{
		  return (getConfigValue(name + "*ALL," + prln + ",B").toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (containsConfigEntry(name + "*ALL,*ALL," + flag))//$NON-NLS-1$
		{
		  return (getConfigValue(name + "*ALL,*ALL," + flag).toString()); //$NON-NLS-1$
		}
		else if (containsConfigEntry(name + "*ALL,*ALL,B"))//$NON-NLS-1$
		{
		  return (getConfigValue(name + "*ALL,*ALL,B").toString()); //$NON-NLS-1$
		}
		else
		{
		  return null;
		}		
	}

	/**
	 * Returns a string representation of this <code>MFSConfig</code> in the
	 * form of a set of configuration entries, enclosed in braces and separated
	 * by a comma and a space. Each configuration entry is rendered as the
	 * configuration entry label, an equals sign, and the configuration entry
	 * value.
	 * @return a string representation of this <code>MFSConfig</code>
	 */
	public String toString()
	{
		return this.configuration.toString();
	}

	//~2A New method
	/**
	 * Displays the message box used to confirm an environment switch.
	 * @param comp the parent <code>Component</code> of the message box
	 * @param environment the new environment
	 * @return <code>true</code> iff the user confirmed the switch
	 */
	public boolean confirmSwitch(Component comp, String environment)
	{
		String title = "Confirm MFS Plant Selection"; //$NON-NLS-1$
		String pattern = "You are about to switch to the ''{0}'' environment.\nAre you sure you want to continue?"; //$NON-NLS-1$
		Object[] args = {environment};
		String msg = MessageFormat.format(pattern, args);
		return IGSMessageBox.showYesNoMB(comp, title, msg, null);
	}

	//~2A New method
	/**
	 * Returns the computer name for the specified environment name.
	 * @param environment the environment name
	 * @return the computer name for the specified environment name
	 */
	public String getComputerName(String environment)
	{
		return (String) this.environmentMap.get(environment);
	}

	//~2A New method
	/**
	 * Returns a <code>Vector</code> containing the environment names.
	 * @return the environment names
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector getEnvironments()
	{
		return new Vector(this.environmentMap.keySet());
	}

	//~2A New method
	/**
	 * Returns <code>true</code> iff environment switching is enabled.
	 * @return <code>true</code> iff environment switching is enabled
	 */
	public boolean isEnvironmentSwitchEnabled()
	{
		return containsConfigEntry("BUTTON,LOGON,SWITCH"); //$NON-NLS-1$
	}
		
	/**
	 * Returns the <code>Hashtable</code> of COO values
	 * @return <code>Hashtable</code> of COO values
	 */
	public Hashtable<String, String> getCooHash() {
		return this.cooHash;
	}
}