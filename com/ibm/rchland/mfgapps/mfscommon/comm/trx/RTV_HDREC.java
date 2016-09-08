/* © Copyright IBM Corporation 2010. All rights reserved.
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

import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cacheable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cache;
import com.ibm.rchland.mfgapps.mfscommon.comm.TrxManager;

public final class RTV_HDREC extends ServerTransaction implements Cacheable 
{
	private static Cache cache = null;
	private String inputPRLN;
	private String inputITEM;
	private String inputINPN;
	private String inputHDR;
	private String inputECRI;
	private String outputHWEC;
	private String outputINPN;
	private String outputCOOC;

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
	
	public void setInputPRLN(String inputPRLN) {
		this.inputPRLN = inputPRLN;
	}

	public void setInputITEM(String inputITEM) {
		this.inputITEM = inputITEM;
	}

	public void setInputINPN(String inputINPN) {
		this.inputINPN = inputINPN;
	}

	public void setInputHDR(String inputHDR) {
		this.inputHDR = inputHDR;
	}

	public void setInputECRI(String inputECRI) {
		this.inputECRI = inputECRI;
	}

	public String getOutputHWEC() {
		return outputHWEC;
	}

	public String getOutputINPN() {
		return outputINPN;
	}

	public String getOutputCOOC() {
		return outputCOOC;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("RTV_HDREC ");  //$NON-NLS-1$
		inputBuffer.append(this.inputPRLN);
		inputBuffer.append(this.inputITEM);
		inputBuffer.append(this.inputINPN);
		inputBuffer.append(this.inputHDR);
		inputBuffer.append(this.inputECRI);
		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_hdrec = new MFSFixedTransaction(this.getInput());
		MFSComm.getInstance();
		MFSComm.execute(rtv_hdrec);	
		this.setOutput(rtv_hdrec.getOutput());
		this.setReturnCode(rtv_hdrec.getReturnCode());
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == 0) {
			this.outputHWEC = this.getOutput().substring(0,12);
			this.outputINPN = this.getOutput().substring(12,24);
			this.outputCOOC = this.getOutput().substring(24,26).trim();		
			if (this.outputHWEC.equals("*NONE       ")) //$NON-NLS-1$
			{
				this.outputHWEC = "            "; //$NON-NLS-1$
			}
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