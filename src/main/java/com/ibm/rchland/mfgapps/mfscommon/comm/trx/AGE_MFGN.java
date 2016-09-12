/* @ Copyright IBM Corporation 2014. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2014-01-03      00208431 Edgar Mercado    -Initial version
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

public class AGE_MFGN extends ServerTransaction implements Cacheable {
	private static Cache cache = null;
	private MFSActionable actionable;
	private String inputWU;	
	private String inputUserId;
	private String outputMessage;
	
	public AGE_MFGN(MFSActionable actionable) {
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

	public void setInputWU(String inputWU) {
		this.inputWU = inputWU;
	}
	
	public void setInputUserId(String inputUserId) {
		this.inputUserId = inputUserId;
	}	

	public String getMessage() {
		return outputMessage;
	}
	
	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("AGE_MFGN  ");  //$NON-NLS-1$
		inputBuffer.append(this.inputWU);
		inputBuffer.append(this.inputUserId);
				
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction age_mfgn = new MFSFixedTransaction(this.getInput());
		age_mfgn.setActionMessage("Performing archive of the work unit, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(age_mfgn, this.actionable);
		
		this.setOutput(age_mfgn.getOutput());
		this.setReturnCode(age_mfgn.getReturnCode());
		if(this.getReturnCode() != 0) {
			this.setErrorMessage(age_mfgn.getErms());
		}		
	}

	@Override
	protected void parseOutput() {		
		if(this.getReturnCode() == 0) {
			this.outputMessage = this.getOutput();
		}
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}
	
}