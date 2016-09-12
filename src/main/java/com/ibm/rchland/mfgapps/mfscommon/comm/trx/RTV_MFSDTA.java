/* @ Copyright IBM Corporation 2011. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-06-09      50244JR  Edgar Mercado    -Initial version
 ******************************************************************************/

package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLParser;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;
import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLTransaction;

public abstract class RTV_MFSDTA extends ServerTransaction {
	private MFSActionable actionable;
	private String inputAPP = null;
	private String inputPGM = null;
	private String inputUSER = null;
	private String inputCELL = null;
	private String inputOPER;	
	private String outputDATA;
	private IGSXMLTransaction rtv_mfsdta;
	
	
	public RTV_MFSDTA(MFSActionable actionable,String oper) {
		super();
		this.setInputOPER(oper);
		this.actionable = actionable;		
	}
	
	public void setInputAPP(String inputAPP) {
		this.inputAPP = inputAPP;
	}
	
	public void setInputPGM(String inputPGM) {
		this.inputPGM = inputPGM;
	}
	
	public void setInputUSER(String inputUSER) {
		this.inputUSER = inputUSER;
	}
	
	public void setInputCELL(String inputCELL) {
		this.inputCELL = inputCELL;
	}

	public void setInputOPER(String inputOPER) {
		this.inputOPER = inputOPER;
	}	
	
	protected boolean validateInputs()	{
		return true;
	}
	
	public String getOutputDATA() {
		return outputDATA;
	}	
	
	protected void buildInput(String params) {
		this.rtv_mfsdta = new IGSXMLTransaction("RTV_MFSDTA");

		this.rtv_mfsdta.startDocument();
		
		if (!(this.inputAPP.toString().equals(null)))
		{
			  this.rtv_mfsdta.addElement("APP", this.inputAPP);
		}
		if (!(this.inputPGM.toString().equals(null)))
		{
			this.rtv_mfsdta.addElement("PGM", this.inputPGM);			
		}
		if (!(this.inputUSER.toString().equals(null)))
		{
			this.rtv_mfsdta.addElement("USER", this.inputUSER);
		}
		if (!(this.inputCELL.toString().equals(null)))
		{
			this.rtv_mfsdta.addElement("CELL", this.inputCELL);			
		}

		this.rtv_mfsdta.addElement("OPER", this.inputOPER);
		this.rtv_mfsdta.addElement("PARAMS", params);
		this.rtv_mfsdta.endDocument();
		
		this.setInput(this.rtv_mfsdta.toString());
		
	}
	
	protected void parseOutput(){
		if(this.getReturnCode() == 0) {
			MfsXMLParser xmlParser = new MfsXMLParser(this.getOutput());							
			try {
				this.outputDATA = xmlParser.getField("DATA"); //$NON-NLS-1$			
			} 
			catch (MISSING_XML_TAG_EXCEPTION e) {
				this.setErrorMessage("Error parsing result from " + this.getClass().getName() + ": " + e.getMessage()); 
				this.setReturnCode(1);
			} 
		}				
	}
	
	protected boolean validateOutputs()	{
		return (this.getReturnCode() == 0);	
	}
	
	protected void callTransaction() {
		
		this.rtv_mfsdta.setActionMessage("Retrieving MFS Data. Please Wait...");
		MFSComm.getInstance().execute(this.rtv_mfsdta, actionable);
		
		this.setOutput(this.rtv_mfsdta.getOutput());
		this.setReturnCode(this.rtv_mfsdta.getReturnCode());
		if(this.getReturnCode() != 0) {
			this.setErrorMessage(this.rtv_mfsdta.getErms());
		}		
	}	
	

}
