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

public class RTV_IQ extends ServerTransaction implements Cacheable 
{
	private static Cache cache = null;
	private MFSActionable actionable;
	private String inputNmbr;
	private String inputPrln;
	private String inputProd;
	private String inputX;
	private String outputIQ;

	public RTV_IQ(MFSActionable actionable) {
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

	public void setInputNmbr(String inputNmbr) {
		this.inputNmbr = inputNmbr;
	}

	public void setInputPrln(String inputPrln) {
		this.inputPrln = inputPrln;
	}

	public void setInputProd(String inputProd) {
		this.inputProd = inputProd;
	}

	public void setInputX(String inputX) {
		this.inputX = inputX;
	}

	

	public String getOutputIQ() {
		return outputIQ;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("RTV_IQ    ");  //$NON-NLS-1$
		inputBuffer.append(this.inputNmbr);
		inputBuffer.append(this.inputPrln);
		inputBuffer.append(this.inputProd);
		inputBuffer.append(this.inputX);
		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_iq = new MFSFixedTransaction(this.getInput());
		rtv_iq.setActionMessage("Retrieving Inspection Questions, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(rtv_iq, this.actionable);

		this.setOutput(rtv_iq.getOutput());
		this.setReturnCode(rtv_iq.getReturnCode());
		
		if(this.getReturnCode() != 0) {
			this.setErrorMessage(rtv_iq.getErms());
		}		
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == 0) {
			this.outputIQ = this.getOutput();
		}
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}

}
