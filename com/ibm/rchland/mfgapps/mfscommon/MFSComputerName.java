/* © Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15      34242JR  R Prechel        -Initial version
 * 2007-06-18   ~1 37556CD  T He             -Added class variable and new method
 *                                            to get/set alternate computer name.  											  
 * 2007-11-02   ~2 40104PB  R Prechel        -Remove distinction between computer name
 *                                            and alternate computer name.
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <code>MFSComputerName</code> contains a utility method to determine the
 * computer's name.
 * @author The MFS Client Development Team
 */
public class MFSComputerName
{
	/** The computer name. */
	private static String computerName = null; //~1A ~2C

	/**
	 * Constructs a new <code>MFSComputerName</code>. This class only has
	 * static variables and static methods and does not have any instance
	 * variables or instance methods. Thus, there is no reason to create an
	 * instance of <code>MFSComputerName</code>, so the only constructor is
	 * declared <code>private</code>.
	 */
	private MFSComputerName()
	{
		super();
	}

	/**
	 * Returns the computer's name (using uppercase letters).
	 * <p>
	 * If the computer's name cannot be determined, an error message is printed
	 * to the standard error output stream and the JVM terminates.
	 * @return the computer's name
	 */
	public static String getComputerName()
	{
		String result = null;

		//begin ~1A ~2C
		if (computerName != null)
		{
			result = computerName;
		} //end ~1A ~2C
		else
		{
			// Try using an environment variable to determine the computer name
			// getenv throws an Error in pre 1.5 versions of Java
			try
			{
				result = System.getenv("COMPUTERNAME"); //$NON-NLS-1$
				if (result == null)
				{
					result = System.getenv("HOSTNAME"); //$NON-NLS-1$
				}
			}
			catch (Error e)
			{
				result = null;
			}

			// If the environmnet variable didn't work, try the local host name
			if (result == null)
			{
				try
				{
					result = InetAddress.getLocalHost().getHostName();
				}
				catch (UnknownHostException uhe)
				{
					result = null;
				}
			}

			if (result == null || result.trim().length() == 0)
			{
				System.err.println("Could not determine computer name."); //$NON-NLS-1$
				System.exit(1);
			}

			// This method should only return the computer name, not the fully
			// qualified domain name, so anything after the first period is ignored.
			// For example, MYHOST is returned for myhost.rchland.ibm.com.
			int index = result.indexOf('.');
			if (index > 0)
			{
				result = result.substring(0, index);
			}
		}

		return result.toUpperCase();
	}

	//~1A New method
	//~2C Change method name
	/**
	 * Sets the computer name class variable.
	 * @param computerName the computer name
	 */
	public static void setComputerName(String computerName)
	{
		MFSComputerName.computerName = computerName;
	}
}
