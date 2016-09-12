/* @ Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLDocument;
import mfsxml.MfsXMLParser;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSXmlTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;

public class RTV_WUBYPS extends ServerTransaction {
	private MFSActionable actionable;
	private String inputINPN;
	private String inputINSQ;
	private String inputLOCAL;
	private String outputMCTL;
	private String outputPRLN;
	
	public RTV_WUBYPS(MFSActionable actionable) {
		super();
		this.actionable = actionable;
	}

	public void setInputINPN(String inputINPN) {
		this.inputINPN = inputINPN;
	}

	public void setInputINSQ(String inputINSQ) {
		this.inputINSQ = inputINSQ;
	}

	public void setInputLOCAL(String inputLOCAL) {
		this.inputLOCAL = inputLOCAL;
	}

	public String getOutputMCTL() {
		return outputMCTL;
	}

	public String getOutputPRLN() {
		return outputPRLN;
	}

	@Override
	protected boolean validateInputs() {
		return true;
	}

	@Override
	protected void buildInput() {
		MfsXMLDocument xml_data1 = new MfsXMLDocument("RTV_WUBYPS"); //$NON-NLS-1$
		xml_data1.addOpenTag("DATA"); //$NON-NLS-1$
		xml_data1.addCompleteField("INPN", this.inputINPN); //$NON-NLS-1$
		xml_data1.addCompleteField("INSQ", this.inputINSQ); //$NON-NLS-1$
		xml_data1.addCompleteField("LOCAL", this.inputLOCAL); //$NON-NLS-1$
		xml_data1.addCloseTag("DATA"); //$NON-NLS-1$
		xml_data1.finalizeXML();
		
		this.setInput(xml_data1.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction rtv_wubyps = new MFSXmlTransaction(this.getInput()); 
		rtv_wubyps.setActionMessage("Retrieving Work Unit, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(rtv_wubyps, this.actionable);

		this.setOutput(rtv_wubyps.getOutput());
		this.setReturnCode(rtv_wubyps.getReturnCode());	
		if (this.getReturnCode() != 0) {
			this.setErrorMessage(rtv_wubyps.getErms());			
		}
	}

	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == 0) {
			MfsXMLParser xmlParser = new MfsXMLParser(this.getOutput());							
			try {
				this.outputMCTL = xmlParser.getField("MCTL"); //$NON-NLS-1$
				this.outputPRLN = xmlParser.getField("PRLN"); //$NON-NLS-1$
			} 
			catch (MISSING_XML_TAG_EXCEPTION e) {
				this.setErrorMessage("Error parsing result from " + this.getClass().getName() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				this.setReturnCode(1);
			} 
		}		
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}
}