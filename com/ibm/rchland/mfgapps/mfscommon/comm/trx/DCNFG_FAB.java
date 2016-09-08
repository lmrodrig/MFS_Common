/* © Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-06-13      50244JR  Edgar Mercado    -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;

public class DCNFG_FAB extends ServerTransaction {
	private MFSActionable actionable;
	private String inputMCTL;
	private String inputUSER;
	private String inputPGM;
	private String inputTMCTL;
	private String inputCTYP;
	
	public DCNFG_FAB(MFSActionable actionable) {
		super();
		this.actionable = actionable;		
	}

	public void setActionable(MFSActionable actionable) {
		this.actionable = actionable;
	}

	public void setInputMCTL(String inputMCTL) {
		this.inputMCTL = inputMCTL;
	}
	
	public void setInputUSER(String inputUSER) {
		this.inputUSER = inputUSER;
	}	

	public void setInputPGM(String inputPGM) {
		this.inputPGM = inputPGM;
	}
	
	public void setInputTMCTL(String inputTMCTL) {
		this.inputTMCTL = inputTMCTL;
	}
	
	public void setInputCTYP(String inputCTYP) {
		this.inputCTYP = inputCTYP;
	}	


	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		StringBuffer inputBuffer = new StringBuffer(100);
		inputBuffer.append("DCNFG_FAB ");  //$NON-NLS-1$
		inputBuffer.append(this.inputMCTL);
		inputBuffer.append(this.inputUSER);
		inputBuffer.append(this.inputPGM);
		inputBuffer.append(this.inputTMCTL);
		inputBuffer.append(this.inputCTYP);
		
		this.setInput(inputBuffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction dcnfg_fab = new MFSFixedTransaction(this.getInput());
		dcnfg_fab.setActionMessage("Preparing Work Unit, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(dcnfg_fab, this.actionable);

		this.setOutput(dcnfg_fab.getOutput());
		this.setReturnCode(dcnfg_fab.getReturnCode());
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
	
