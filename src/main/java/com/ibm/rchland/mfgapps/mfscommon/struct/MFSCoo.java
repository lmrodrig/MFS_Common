/* @ Copyright IBM Corporation 2012. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2012-01-28      D615310  Edgar Mercado    -Initial version Java 5.0
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.struct;

import java.util.Hashtable;
import java.util.ArrayList;

/**
 * The <code>MFSCoo</code> contains all info related to a MFS COO for a part number
 * @author The MFS Client Development Team
 */
public class MFSCoo
{	
	/** The substitute(s) Hastable */
	private Hashtable<String, ArrayList<String>> coohash;
	
	/**
	 * @return the substitute part list for a called pn
	 */
	public Hashtable<String, ArrayList<String>> getPn_cooList() {
		return coohash;
	}

	/**
	 * @param callenpn the called part number to set as hashtable key
	 * @param pnlist the part number list for the called pn
	 */
	public void setPn_cooList(String pn, ArrayList <String>list) {
		if(null == this.coohash)
		{
			this.coohash = new Hashtable<String, ArrayList<String>>();
		}
		this.coohash.put(pn,list);			
	}		
}
