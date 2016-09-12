/* @ Copyright IBM Corporation 2011. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-11-14|    |588440  |Santiago SC     |-Initial version, Java 5.0
 * 2011-11-29|    |50723FR |Toribio H       |-Grey Market, fix VRFY_PART logic
 ******************************************************************************/

package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSComm;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSFixedTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.MFSTransaction;
import com.ibm.rchland.mfgapps.mfscommon.comm.ServerTransaction;

/**
 * @author The MFS Client Development Team
 */
public class VRFY_PART extends ServerTransaction
{
	private MFSActionable actionable;
	private String inputPrln;
	private String inputItem;
	private String inputInpn;
	private String inputInsq;
	private String inputInca;
	private String inputInec;
	private String inputCwun;
	private String inputCooc;
	private String inputUnpr;
	private String inputMctl;
	private String inputMmdl;
	private String inputPll1;
	private String inputPll2;
	private String inputPll3;
	private String inputPll4;
	private String inputPll5;
	private String inputPari;
	private String inputMfgn;
	private String inputIdss;
	private String inputCrct;
	private String inputRcon;
	private String inputMatp;
	private String inputMspi;
	private String inputMcsn;
	private String inputMs;
	private String inputTypz;
	private String inputAmsi;
	private String input8CharCellType;
	private String inputNmbr;
	private String inputMalc;
	private String inputMilc;
	private boolean isPnriDoNotCollect = false;
	protected String transaction = "VRFY_PART "; //$NON-NLS-1$
	
	public VRFY_PART(MFSActionable actionable)
	{
		this.actionable = actionable;
	}
	
	@Override
	public String getOutput()
	{
		return super.getOutput();
	}
	
	@Override
	protected String getInput()
	{
		return super.getInput();
	}
		
	@Override
	protected boolean validateInputs() {			
		this.inputPrln = this.paddingLeft(this.inputPrln, 8);
		this.inputItem = this.paddingLeft(this.inputItem, 12);
		this.inputInpn = this.paddingLeft(this.inputInpn, 12);
		this.inputInca = this.paddingLeft(this.inputInca, 12);
		this.inputInec = this.paddingLeft(this.inputInec, 12);
		this.inputInsq = this.paddingLeft(this.inputInsq, 12);
		this.inputUnpr = this.paddingLeft(this.inputUnpr, 1);
		this.inputMctl = this.paddingLeft(this.inputMctl, 8);
		this.inputCwun = this.paddingLeft(this.inputCwun, 8);
		this.inputMmdl = this.paddingLeft(this.inputMmdl, 4);
		this.inputPll1 = this.paddingLeft(this.inputPll1, 4);
		this.inputPll2 = this.paddingLeft(this.inputPll2, 4);
		this.inputPll3 = this.paddingLeft(this.inputPll3, 4);
		this.inputPll4 = this.paddingLeft(this.inputPll4, 4);
		this.inputPll5 = this.paddingLeft(this.inputPll5, 4);
		this.inputPari = this.paddingLeft(this.inputPari, 1);
		this.inputMfgn = this.paddingLeft(this.inputMfgn, 7);
		this.inputIdss = this.paddingLeft(this.inputIdss, 4);
		this.inputCrct = this.paddingLeft(this.inputCrct, 4);
		this.inputRcon = this.paddingLeft(this.inputRcon, 1);
		this.inputMatp = this.paddingLeft(this.inputMatp, 4);
		this.inputMspi = this.paddingLeft(this.inputMspi, 2);
		this.inputMs = this.paddingLeft(this.inputMs, 7);
		this.inputMcsn = this.paddingLeft(this.inputMcsn, 7);		
		this.inputTypz = this.paddingLeft(this.inputTypz, 1);
		this.inputAmsi = this.paddingLeft(this.inputAmsi, 1);
		this.inputCooc = this.paddingLeft(this.inputCooc, 2);
		this.input8CharCellType = this.paddingLeft(MFSConfig.getInstance().get8CharCellType(), 8);
		this.inputNmbr = this.paddingLeft(this.inputNmbr, 4);
		this.inputMalc = this.paddingLeft(this.inputMalc, 7); 
		this.inputMilc = this.paddingLeft(this.inputMilc, 12);
		
		return true;
	}

	@Override
	protected void buildInput () {

		StringBuffer buffer = new StringBuffer();
		buffer.append(this.transaction);
		buffer.append(this.inputPrln);
		buffer.append(this.inputItem);

		if(this.isPnriDoNotCollect)
		{
			buffer.append(this.inputItem);
		}
		else
		{
			buffer.append(this.inputInpn);
		}
		/* if ca blank use ec */
		buffer.append(!this.inputInca.trim().equals("") ? this.inputInca : this.inputInec); //$NON-NLS-1$

		buffer.append(this.inputInsq);
		buffer.append(this.inputUnpr);
		buffer.append(this.inputMctl);
		buffer.append(this.inputCwun);
		buffer.append(this.inputMmdl);
		buffer.append(this.inputPll1);
		buffer.append(this.inputPll2);
		buffer.append(this.inputPll3);
		buffer.append(this.inputPll4);
		buffer.append(this.inputPll5);
		buffer.append(this.inputPari);
		buffer.append(this.inputMfgn);
		buffer.append(this.inputIdss);
		buffer.append(this.inputCrct);
		buffer.append(this.inputRcon);
		buffer.append(this.inputMatp);
		buffer.append(this.inputMspi);
		buffer.append(this.inputMs.trim().equals("") ? inputMcsn : inputMs.substring(2)); //$NON-NLS-1$
		buffer.append(this.inputTypz);
		buffer.append(this.inputAmsi);
		buffer.append(this.inputCooc);
		buffer.append(this.input8CharCellType);
		buffer.append(this.inputNmbr);
		//~7 Pass blank malc/milc into VRFY_PART
		buffer.append(this.inputMalc);
		buffer.append(this.inputMilc);
		
		this.setInput(buffer.toString());
	}

	@Override
	protected void callTransaction() {
		MFSTransaction vrfy_part = new MFSFixedTransaction(this.getInput());
		vrfy_part.setActionMessage("Verifying Part Information, Please Wait..."); //$NON-NLS-1$
		MFSComm.getInstance().execute(vrfy_part, actionable);

		this.setOutput(vrfy_part.getOutput());
		this.setReturnCode(vrfy_part.getReturnCode());
	}

	@Override
	protected void parseOutput() {
		// does nothing the outputs could be xml or strings		
		if(0 != this.getReturnCode()) 
		{
			this.setErrorMessage(this.getOutput());
		}	
	}

	@Override
	protected boolean validateOutputs() {
		return (this.getReturnCode() == 0);
	}

	/**
	 * @param inputMcsn the inputMcsn to set
	 */
	public void setInputMcsn(String inputMcsn) {
		this.inputMcsn = inputMcsn;
	}

	/**
	 * @param inputPrln the inputPrln to set
	 */
	public void setInputPrln(String inputPrln) {
		this.inputPrln = inputPrln;
	}

	/**
	 * @param inputItem the inputItem to set
	 */
	public void setInputItem(String inputItem) {
		this.inputItem = inputItem;
	}

	/**
	 * @param isPnriDoNotCollect the isPnriDoNotCollect to set
	 */
	public void setPnriDoNotCollect(boolean isPnriDoNotCollect) {
		this.isPnriDoNotCollect = isPnriDoNotCollect;
	}

	/**
	 * @param inputMs the inputMs to set
	 */
	public void setInputMs(String inputMs) {
		this.inputMs = inputMs;
	}

	/**
	 * @param inputInpn the inputInpn to set
	 */
	public void setInputInpn(String inputInpn) {
		this.inputInpn = inputInpn;
	}

	/**
	 * @param inputInsq the inputInsq to set
	 */
	public void setInputInsq(String inputInsq) {
		this.inputInsq = inputInsq;
	}

	/**
	 * @param inputInca the inputInca to set
	 */
	public void setInputInca(String inputInca) {
		this.inputInca = inputInca;
	}

	/**
	 * @param inputInec the inputInec to set
	 */
	public void setInputInec(String inputInec) {
		this.inputInec = inputInec;
	}

	/**
	 * @param inputCwun the inputCwun to set
	 */
	public void setInputCwun(String inputCwun) {
		this.inputCwun = inputCwun;
	}

	/**
	 * @param inputCooc the inputCooc to set
	 */
	public void setInputCooc(String inputCooc) {
		this.inputCooc = inputCooc;
	}

	/**
	 * @param inputUnpr the inputUnpr to set
	 */
	public void setInputUnpr(String inputUnpr) {
		this.inputUnpr = inputUnpr;
	}

	/**
	 * @param inputMctl the inputMctl to set
	 */
	public void setInputMctl(String inputMctl) {
		this.inputMctl = inputMctl;
	}

	/**
	 * @param inputMmdl the inputMmdl to set
	 */
	public void setInputMmdl(String inputMmdl) {
		this.inputMmdl = inputMmdl;
	}

	/**
	 * @param inputPll1 the inputPll1 to set
	 */
	public void setInputPll1(String inputPll1) {
		this.inputPll1 = inputPll1;
	}

	/**
	 * @param inputPll2 the inputPll2 to set
	 */
	public void setInputPll2(String inputPll2) {
		this.inputPll2 = inputPll2;
	}

	/**
	 * @param inputPll3 the inputPll3 to set
	 */
	public void setInputPll3(String inputPll3) {
		this.inputPll3 = inputPll3;
	}

	/**
	 * @param inputPll4 the inputPll4 to set
	 */
	public void setInputPll4(String inputPll4) {
		this.inputPll4 = inputPll4;
	}

	/**
	 * @param inputPll5 the inputPll5 to set
	 */
	public void setInputPll5(String inputPll5) {
		this.inputPll5 = inputPll5;
	}

	/**
	 * @param inputPari the inputPari to set
	 */
	public void setInputPari(String inputPari) {
		this.inputPari = inputPari;
	}

	/**
	 * @param inputMfgn the inputMfgn to set
	 */
	public void setInputMfgn(String inputMfgn) {
		this.inputMfgn = inputMfgn;
	}

	/**
	 * @param inputIdss the inputIdss to set
	 */
	public void setInputIdss(String inputIdss) {
		this.inputIdss = inputIdss;
	}

	/**
	 * @param inputCrct the inputCrct to set
	 */
	public void setInputCrct(String inputCrct) {
		this.inputCrct = inputCrct;
	}

	/**
	 * @param inputRcon the inputRcon to set
	 */
	public void setInputRcon(String inputRcon) {
		this.inputRcon = inputRcon;
	}

	/**
	 * @param inputMatp the inputMatp to set
	 */
	public void setInputMatp(String inputMatp) {
		this.inputMatp = inputMatp;
	}

	/**
	 * @param inputMspi the inputMspi to set
	 */
	public void setInputMspi(String inputMspi) {
		this.inputMspi = inputMspi;
	}

	/**
	 * @param inputTypz the inputTypz to set
	 */
	public void setInputTypz(String inputTypz) {
		this.inputTypz = inputTypz;
	}

	/**
	 * @param inputAmsi the inputAmsi to set
	 */
	public void setInputAmsi(String inputAmsi) {
		this.inputAmsi = inputAmsi;
	}

	/**
	 * @param inputNmbr the inputNmbr to set
	 */
	public void setInputNmbr(String inputNmbr) {
		this.inputNmbr = inputNmbr;
	}

	/**
	 * @param inputMalc the inputMalc to set
	 */
	public void setInputMalc(String inputMalc) {
		this.inputMalc = inputMalc;
	}

	/**
	 * @param inputMilc the inputMilc to set
	 */
	public void setInputMilc(String inputMilc) {
		this.inputMilc = inputMilc;
	}
}