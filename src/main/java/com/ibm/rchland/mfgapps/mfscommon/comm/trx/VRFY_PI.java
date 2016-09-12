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

public class VRFY_PI extends ServerTransaction implements Cacheable {
	private static Cache cache = null;
	private MFSActionable actionable;
	private String inputMSN;
	private String inputMspi;
	private String inputMatp;
	
	public VRFY_PI(MFSActionable actionable) {
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

	public void setInputMSN(String inputMSN) {
		this.inputMSN = inputMSN;
	}

	public void setInputMspi(String inputMspi) {
		this.inputMspi = inputMspi;
	}

	public void setInputMatp(String inputMatp) {
		this.inputMatp = inputMatp;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("VRFY_PI   ");  //$NON-NLS-1$
		inputBuffer.append(this.inputMSN);
		inputBuffer.append(this.inputMspi);
		inputBuffer.append(this.inputMatp);
		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction vrfy_pi = new MFSFixedTransaction(this.getInput());
		vrfy_pi.setActionMessage("Verifying Plant Indicator, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(vrfy_pi, this.actionable);

		this.setOutput(vrfy_pi.getOutput());
		this.setReturnCode(vrfy_pi.getReturnCode());
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() != 0) {
			this.setErrorMessage(this.getOutput());
		}		
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}
}