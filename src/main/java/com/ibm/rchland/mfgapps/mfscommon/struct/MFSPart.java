/* @ Copyright IBM Corporation 2011. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-11-14      588440   Santiago SC      -Initial version, Java 5.0
 * 2011-12-05      50723FR  Toribio H.       -Moved to MFS Common project
 * 2012-01-04 ~01  D629692  Edgar Mercado    -Add a new attribute CSNI to know if a part needs to be serialized
 * 2012-01-22 ~02  E638153  Edgar Mercado    -Add a new attribute MLRI to know if a part needs to get COO value
 * 2012-01-28 ~03  D615310  Edgar Mercado    -Add a new attribute PNRI to know if a part can be auto-installed
 * 2012-06-13 ~04  E692532  Edgar Mercado    -GreyMarket (CR783/RCQ00203226): Inventory Accuracy. Rework/Rebuild functionality
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.struct;

import java.util.Hashtable;
import java.util.ArrayList;

/**
 * The <code>MFSPart</code> contains all info related to a mfs part (CR10)
 * @author The MFS Client Development Team
 */
public class MFSPart 
{
	private static String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/** The work unit number */
	private String mctl = EMPTY_STRING; 
	
	/** The child work unit number */
	private String cwun = EMPTY_STRING; 
	
	/** The top level pn */
	private String inpn = EMPTY_STRING; 
	
	/** The top level sn */
	private String insq = EMPTY_STRING;
	
	/** The country of origin */
	private String cooc = EMPTY_STRING; 
	
	/** The install disposition */
	private String idsp = EMPTY_STRING; 
	
	/** The part description */
	private String desc = EMPTY_STRING;
	
	/** The tower/slot */
	private String crct = EMPTY_STRING;
	
	/** The serialization indicator */
	private String csni = EMPTY_STRING;	                                 //~01A
	
	/** The MLR indicator */
	private String mlri = EMPTY_STRING;	                                 //~02A
	
	/** The MLR indicator */
	private String pnri = EMPTY_STRING;	                                 //~03A
	
	/** The Status indicator */
	private String stat = EMPTY_STRING;	                                 //~04A
	
	/** The substitute(s) Hastable */
	private Hashtable<String, ArrayList<String>> substitute;	         //~02A	
	
	/**
	 * @return the mctl
	 */
	public String getMctl() {
		return mctl;
	}

	/**
	 * @param mctl the mctl to set
	 */
	public void setMctl(String mctl) {
		this.mctl = mctl;
	}

	/**
	 * @return the cwun
	 */
	public String getCwun() {
		return cwun;
	}

	/**
	 * @param cwun the cwun to set
	 */
	public void setCwun(String cwun) {
		this.cwun = cwun;
	}

	/**
	 * @return the inpn
	 */
	public String getInpn() {
		return inpn;
	}

	/**
	 * @param inpn the inpn to set
	 */
	public void setInpn(String inpn) {
		this.inpn = inpn;
	}

	/**
	 * @return the insq
	 */
	public String getInsq() {
		return insq;
	}

	/**
	 * @param insq the insq to set
	 */
	public void setInsq(String insq) {
		this.insq = insq;
	}

	/**
	 * @return the cooc
	 */
	public String getCooc() {
		return cooc;
	}

	/**
	 * @param cooc the cooc to set
	 */
	public void setCooc(String cooc) {
		this.cooc = cooc;
	}

	/**
	 * @return the idsp
	 */
	public String getIdsp() {
		return idsp;
	}

	/**
	 * @param idsp the idsp to set
	 */
	public void setIdsp(String idsp) {
		this.idsp = idsp;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return the crct
	 */
	public String getCrct() {
		return crct;
	}

	/**
	 * @param crct the crct to set
	 */
	public void setCrct(String crct) {
		this.crct = crct;
	}
	
	/**  ~01A
	 * @return the csni
	 */
	public String getCsni() {
		return csni;
	}

	/**  ~01A
	 * @param csni the csni to set
	 */
	public void setCsni(String csni) {
		this.csni = csni;
	}	
	
	/**  ~02A
	 * @return the mlri
	 */
	public String getMlri() {
		return mlri;
	}

	/**  ~02A
	 * @param mlri the mlri to set
	 */
	public void setMlri(String mlri) {
		this.mlri = mlri;
	}
	
	/**  ~03A
	 * @return the mlri
	 */
	public String getPnri() {
		return pnri;
	}

	/**  ~03A
	 * @param mlri the mlri to set
	 */
	public void setPnri(String pnri) {
		this.pnri = pnri;
	}	
	
	/**  ~04A
	 * @return the part Status to know if a rework operation was requested
	 */
	public String getStat() {
		return stat;
	}

	/**  ~04A
	 * @param stat the status to set to know if there is a rework operation pending
	 */
	public void setStat(String stat) {
		this.stat = stat;
	}		
	
	/**  ~02A
	 * @return the substitute part list for a called pn
	 */
	public Hashtable<String, ArrayList<String>> getCalledPn_PnList() {
		return substitute;
	}

	/**  ~02A
	 * @param callenpn the called part number to set as hashtable key
	 * @param pnlist the part number list for the called pn
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setCalledPn_PnList(String callenpn, ArrayList pnlist) {
		this.substitute.put(callenpn, pnlist);
	}		
}