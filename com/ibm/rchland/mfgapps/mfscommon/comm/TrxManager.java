/* © Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-17      49513JM  Toribio H.       -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.rchland.mfgapps.mfscommon.comm.Cache;

/**
 * Singleton Class to Handle Cached Transactions List 
 *  In the future, will also hold Trx Buffer.
 */
public class TrxManager 
{	
	/** The <code>TrxManager</code> Singleton instance */
	private static TrxManager INSTANCE = null;
	
	/** list of the Cache of the Cached Trxs*/
	private Vector<Cache> cachedList = new Vector<Cache>();
	
	/** Private constructor to handle Singleton */
	private TrxManager() 
	{	
		/* Needs a constructor to mark as private */
	}
	
	/** Instance Retriever of the Singleton */
	public static TrxManager getInstace()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new TrxManager();
		}
		return INSTANCE;
	}
	
	/** Adds a Cache to the Cached List */
	public void addCached(Cache cache)
	{
		cachedList.add(cache);
	}
	
	/** Resets (Clears) the Cache of the Cached Trxs */
	public void resetCacheFromCachedList()
	{
		Iterator<Cache> cacheIterator = this.cachedList.iterator();
		
		while(cacheIterator.hasNext())
		{
			cacheIterator.next().clear();
		}
	}
}
