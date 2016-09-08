/* © Copyright IBM Corporation 2012. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2012-01-22      E638153  Edgar Mercado    -Initial version Java 5.0
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.struct;

import java.util.Hashtable;
import java.util.ArrayList;

/**
 * The <code>MFSSubstitute</code> contains all info related to a MFS substitute for a part number
 * @author The MFS Client Development Team
 */
public class MFSSubstitute
{	
	/** The substitute(s) Hastable */
	private Hashtable<String, ArrayList<String>> substitute;
	
	/**
	 * @return the substitute part list for a called pn
	 */
	public Hashtable<String, ArrayList<String>> getCalledPn_PnList() {
		return substitute;
	}

	/**
	 * @param callenpn the called part number to set as hashtable key
	 * @param pnlist the part number list for the called pn
	 */
	public void setCalledPn_PnList(String pn, ArrayList <String>list) {
		if(null == this.substitute)
		{
			this.substitute = new Hashtable<String, ArrayList<String>>();
		}
		this.substitute.put(pn,list);			
	}		
}
