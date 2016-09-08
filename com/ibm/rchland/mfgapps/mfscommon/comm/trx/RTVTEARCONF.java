/* © Copyright IBM Corporation 2011. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-06-10      50244JR  Edgar Mercado    -Initial version
 ******************************************************************************/

package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLDocument;
import mfsxml.MfsXMLParser;
import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;

public class RTVTEARCONF extends RTV_MFSDTA {
	private String inputMCTL;
	private String outputPROD;
	private String outputPN;
	private String outputSN;
	private String outputPIDS;
	
	public RTVTEARCONF(MFSActionable actionable) {
		super(actionable,"RTVTEARCONF");	
	}
	
	public void setInputMCTL(String inputMCTL) {
		this.inputMCTL = inputMCTL;
	}	
		
	public String getOutputPROD() {
		return outputPROD;
	}
	
	public String getOutputPN() {
		return outputPN;
	}
	
	public String getOutputSN() {
		return outputSN;
	}
	
	public String getOutputPIDS() {
		return outputPIDS;
	}
	
	protected void buildInput() {
		MfsXMLDocument xml_params = new MfsXMLDocument();	
						
		xml_params.addCompleteField("MCTL", this.inputMCTL);
		this.buildInput(xml_params.toString());
	}	
	
	protected void parseOutput(){
		
		super.parseOutput();
		if(this.getReturnCode() == 0) {
			MfsXMLParser xmlParser = new MfsXMLParser(this.getOutputDATA());							
			try {
				this.outputPROD = xmlParser.getField("PROD");
				this.outputPN = xmlParser.getField("SAPM");
				this.outputSN = xmlParser.getField("SAPN");
				this.outputPIDS = xmlParser.getField("PIDS");				
			} 
			catch (MISSING_XML_TAG_EXCEPTION e) {
				this.setErrorMessage("Error parsing result from " + this.getClass().getName() + ": " + e.getMessage()); 
				this.setReturnCode(1);
			} 
		}				
	}	
	

}
