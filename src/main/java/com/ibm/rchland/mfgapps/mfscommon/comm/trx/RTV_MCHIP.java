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

import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cacheable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.Cache;
import com.ibm.rchland.mfgapps.mfscommon.comm.TrxManager;

public class RTV_MCHIP extends ServerTransaction implements Cacheable {
	private static Cache cache = null;
	private String inputPrintServer;
	private String outputIPAddres;

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

	public void setInputPrintServer(String inputPrintServer) {
		this.inputPrintServer = inputPrintServer;
	}

	public String getOutputIPAddres() {
		return outputIPAddres;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("RTV_MCHIP ");  //$NON-NLS-1$
		inputBuffer.append(this.inputPrintServer);
		
		this.setInput(inputBuffer.toString());		
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_mchip = new MFSFixedTransaction(this.getInput());
		MFSComm.getInstance();
		MFSComm.execute(rtv_mchip);

		this.setOutput(rtv_mchip.getOutput());
		this.setReturnCode(rtv_mchip.getReturnCode());
		if(this.getReturnCode() != 0) {
			this.setErrorMessage("Error getting print server ip address.\n" + rtv_mchip.getErms()); //$NON-NLS-1$
		}		
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == 0) {
			this.outputIPAddres = this.getOutput().trim();
		}		
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}
}