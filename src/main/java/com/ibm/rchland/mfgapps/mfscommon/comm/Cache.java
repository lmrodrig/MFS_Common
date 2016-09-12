/* @ Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 * 2010-11-17  ~01 49513JM  Toribio H.       -Add Disable Cache configurable option
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 *  Cache is the handler that contains the cacheList and the necessary methods
 *  that maintain the {@link Cached} cacheList.
 */
public class Cache  {
	/** List to hold cached objects */
	private Vector<Cached> cacheList = new Vector<Cached>();

	/** Cache disable Control for Cached Objects 
	  * that need to have Cache functionallity Disabled. */
	private boolean disabled = false; //~01

	/** Max Objects saved in cache */
	private int maxCacheSize = 200;

	/** Max Duration of cache in Seconds,  after this time it will be
	 *  reomoved from cacheList. Default is <code>CACHE_DURATION_IGNORED</code>. */  
	private int maxCacheDuration = CACHE_DURATION_IGNORED;
	
	/** Ignore Cache Duration Value */
	public final static int CACHE_DURATION_IGNORED = -1;

	/** Disable Cache Constant Value */
	public final static boolean DISABLED = true;

	/** Disable Cache Constant Value */
	public final static boolean ENABLED = false;
	
	/** No args constructor uses Default fields values. */
	public Cache() 
	{
		/* Constructor with Default values */
	}	

	/** Constructor with Cache Disabled. 
	 * @param disbled. This is for Cacheable Objects that will 
	 * 					disabled their Cache functionality.
	 */
	public Cache(boolean disabled) 
	{
		this();
		this.disabled = disabled;
	}	

	/** Constructor with max cache Size and max cache duration. 
	 * @param maxCacheSize. Max Objects saved in cache.
	 * @param maxCacheDuration. Max Duration of cache in Seconds. */
	public Cache(int maxCacheSize, int maxCacheDuration) 
	{
		this();
		/* Reaffirm Cache Duration possible values */
		if(maxCacheDuration == 0)
		{   /* This is like no Cache, it will expire immediately */
			this.disabled = true;
		}
		else if(maxCacheDuration < 0)  
		{	/* Cache will exist, it will not. (Only removed if cache limit is reached) */
			this.maxCacheDuration = CACHE_DURATION_IGNORED;
		}
		else
		{
			this.maxCacheDuration = maxCacheDuration;
		}	
		this.maxCacheSize = maxCacheSize;
	}
	
	/**
	 * @return true if cacheSize has been exeeded, otherwise false;
	 */
	private boolean isMaxCacheSize() 
	{
		if(this.cacheList.size() >= this.maxCacheSize) 	
		{
			return true;
		}
		else 
		{
			return false;	
		}		
	}

	/**
	 * @return true if disabled; false if not.
	 */
	public boolean isDisabled() 
	{
		return this.disabled;
	}

	private void removeOldestRequested() 
	{
		Iterator<Cached> cacheIterator = this.cacheList.iterator();
		Cached cached = null;
		Cached lessRequested = null;
		
		while(cacheIterator.hasNext()) 
		{
			cached = cacheIterator.next();
			
			if(this.maxCacheDuration != CACHE_DURATION_IGNORED && 
				cached.hasExpired()) 
			{
				lessRequested = cached;  	// We found a candidate, so exit
				break; 						// to avoid comparing all the cached objects.
			}
			else if(lessRequested == null) 
			{
				lessRequested = cached;
			}
			else if(cached.getLastRequest().before(lessRequested.getLastRequest())) 
			{
				lessRequested = cached;
			}
		}
		if(lessRequested != null) 
		{
			this.cacheList.remove(lessRequested);
		}
	}
		
	public void add(Cacheable cacheable) 
	{
		if(!this.disabled) //~01
		{
			if(this.isMaxCacheSize()) 
			{
				this.removeOldestRequested(); 
			} 
			this.cacheList.add(new Cached(cacheable, this.maxCacheDuration));
		}
	}
	
	public Cacheable get(Cacheable cacheable) 
	{
		if(!this.disabled) //~01
		{
			Iterator<Cached> cacheIterator = this.cacheList.iterator();
			
			Cached cached = null;		
			while(cacheIterator.hasNext()) 
			{
				cached = cacheIterator.next();	
				
				if(cached.getCacheable().equals(cacheable)) 
				{
					if(this.maxCacheDuration != CACHE_DURATION_IGNORED && 
						cached.hasExpired()) 
					{
						cacheIterator.remove();
						return null;										
					}		
					else 
					{
						cached.resetLastRequest();
						return cached.getCacheable();										
					}
				}			
			}
		}
		return null;
	}	
	
	/** Clear the Cache */
	public void clear()
	{
		this.cacheList.clear();
	}
	
	/**
	 * Class to add values to Cacheable object that will be saved in cacheList
	 */
	private class Cached {
		private Cacheable cacheable;
		private Date lastRequest;
		private Date expires;

		public Cached(Cacheable cacheable, int maxCacheDuration) 
		{
			long expireDate = System.currentTimeMillis() + (maxCacheDuration * 1000);
			
			this.cacheable = cacheable;
			this.lastRequest = new Date();
			this.expires = new Date(expireDate);
		}

		public Cacheable getCacheable() 
		{
			return this.cacheable;
		}
		
		public Boolean hasExpired() 
		{
			return expires.before(new Date());
		}
		
		public void resetLastRequest() 
		{
			this.lastRequest = new Date();
		}

		public Date getLastRequest() 
		{
			return this.lastRequest;
		}			
	}	
}