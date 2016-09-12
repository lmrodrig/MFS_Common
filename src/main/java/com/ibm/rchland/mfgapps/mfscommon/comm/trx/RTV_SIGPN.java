/* @ Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 * 2010-11-17  ~01 49513JM  Toribio H.       -Add Efficiency Check to enable Cache
 * 2013-03-08| ~02|RCQ00231649|Edgar V.     |-Send a new parameter when SrchLog.
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cacheable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cache;
import com.ibm.rchland.mfgapps.mfscommon.comm.TrxManager;

public class RTV_SIGPN extends ServerTransaction implements Cacheable{
	
	/** Default value sent to RTV_SIGPN. */
	public static final String SEARCH_LOG_DEFAULT_VALUE = "N";//~02A
	
	private static Cache cache = null;
	private MFSActionable actionable;
	private String inputWU;
	private String inputSearchLogFlag = SEARCH_LOG_DEFAULT_VALUE;//~02A
	private String outputCOO;
	private String outputCNAME;
	
	
	public RTV_SIGPN(MFSActionable actionable) {
		super();
		this.actionable = actionable;
	}

	public Cache getCache() {
		if(cache == null)
		{
			if(MFSConfig.getInstance().containsConfigEntry("EFFICIENCYON")) //~01 //$NON-NLS-1$
			{
				cache = new Cache(Cache.ENABLED);
				TrxManager.getInstace().addCached(cache);
			}
			else
			{
				cache = new Cache(Cache.DISABLED);
			}			
		}				
		return cache;
	}

	public void setActionable(MFSActionable actionable) {
		this.actionable = actionable;
	}

	public void setInputWU(String inputWU) {
		this.inputWU = inputWU;
		
	}
	/**
	 * ~02A
	 * @param inputSearchLogFlag
	 */
	public void setInputSearchLogFlag(String inputSearchLogFlag) {
		this.inputSearchLogFlag = inputSearchLogFlag;
		
	}	

	public String getOutputCOO() {
		return outputCOO;
	}

	public String getOutputCNAME() {
		return outputCNAME;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("RTV_SIGPN ");  //$NON-NLS-1$
		inputBuffer.append(this.inputWU);
		inputBuffer.append(this.inputSearchLogFlag);//~02A
		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_sigpn = new MFSFixedTransaction(this.getInput());
		rtv_sigpn.setActionMessage("Retrieving Country, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(rtv_sigpn, this.actionable);

		this.setOutput(rtv_sigpn.getOutput());
		this.setReturnCode(rtv_sigpn.getReturnCode());
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == 0) {
			this.outputCOO = this.getOutput().substring(0, 2);
			this.outputCNAME = this.getOutput().substring(2, 42).trim() + " " + //$NON-NLS-1$
								this.getOutput().substring(42, 81).trim();       					
			
		}
		else {
			this.setErrorMessage(this.getOutput());
		}
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}

}
