/* @ Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

/**
 *  This Interface makes a {@link ServerTransaction} Cacheable.
 *  Add a static {@link Cache} member into the Cacheable {@link ServerTransaction},
 *  and implement the getCache() method to be able access it.
 */
public interface Cacheable {
	
	public Cache getCache();
}