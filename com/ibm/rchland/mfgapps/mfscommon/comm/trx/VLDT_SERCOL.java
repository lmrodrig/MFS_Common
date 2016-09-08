/* © Copyright IBM Corporation 2011. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-07-07      50759JR  Vicente Esteban  -Initial version
 ******************************************************************************/

package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLDocument;
import mfsxml.MfsXMLParser;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;

public class VLDT_SERCOL extends RTV_MFSDTA {

	private String inputMCTL;
	private String outputCOLLECTSERIAL;
	
	public VLDT_SERCOL(MFSActionable actionable) {
		super(actionable,"VLDTSERCOLLECT");
	}
	
	public void setInputMCTL(String inputMCTL) {
		this.inputMCTL = inputMCTL;
	}
	
	public String getOutputCOLLECTSERIAL() {
		return this.outputCOLLECTSERIAL;
	}
	
	@Override
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
				this.outputCOLLECTSERIAL = xmlParser.getField("COLLECTSERIAL");				
			} 
			catch (MISSING_XML_TAG_EXCEPTION e) {
				this.setErrorMessage("Error parsing result from " + this.getClass().getName() + ": " + e.getMessage()); 
				this.setReturnCode(1);
			} 
		}				
	}	

}
