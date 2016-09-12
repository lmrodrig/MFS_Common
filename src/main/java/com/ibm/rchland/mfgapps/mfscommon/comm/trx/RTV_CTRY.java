/* @ Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 * 2010-11-17  ~01 49513JM  Toribio H.       -Add Efficiency Check to enable Cache
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

public class RTV_CTRY extends ServerTransaction implements Cacheable {
	private static Cache cache = null;
	private MFSActionable actionable;
	private String inputPN;
	private String inputTypz;
	private String outputCooList;
	
	public RTV_CTRY(MFSActionable actionable) {
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

	public void setInputPN(String inputPN) {
		this.inputPN = inputPN;
	}

	public void setInputTypz(String inputTypz) {
		this.inputTypz = inputTypz;
	}

	public String getOutputCooList() {
		return outputCooList;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("RTV_CTRY  ");  //$NON-NLS-1$
		inputBuffer.append(this.inputPN);
		if(this.inputTypz != null && this.inputTypz.length() != 0) {
			inputBuffer.append(this.inputTypz);
		}		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_ctry = new MFSFixedTransaction(this.getInput());
		rtv_ctry.setActionMessage("Retrieving Countries, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(rtv_ctry, this.actionable);
		
		this.setOutput(rtv_ctry.getOutput());
		this.setReturnCode(rtv_ctry.getReturnCode());
		if(this.getReturnCode() != 0) {
			this.setErrorMessage(rtv_ctry.getErms());
		}		
	}

	@Override
	protected void parseOutput() {		
		if(this.getReturnCode() == 0) {
			this.outputCooList = this.getOutput();
		}
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}
	
}