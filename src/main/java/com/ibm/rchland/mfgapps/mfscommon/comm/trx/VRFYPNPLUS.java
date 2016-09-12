/* @ Copyright IBM Corporation 2011-2012. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2011-11-14      588440   Santiago SC      -Initial version, Java 5.0
 * 2011-11-29|    |50723FR |Toribio H.      |-Grey Market, fix VRFYPNPLUS logic
 * 2011-12-20| ~01|50723FR |Toribio H.      |-Add subdata output parsing from trx
 * 2012-01-02| ~02|D629693 |Edgar Mercado   |-Add user and cell input fields.
 *           |    |D629692 |                |-Add a new SUBPART attribute CSNI to know if a part needs to be serialized.
 * 2012-01-12| ~03|D627612 |Toribio H.      |-Add PLOM as input   
 * 2012-01-22| ~04|E638153 |Edgar Mercado   |-Add two SUBPART attributes MLRI and COOC. Also, parse SUBSTITUTES XML for substitute parts
 * 2012-01-27| ~05|D615310 |Edgar Mercado   |-Add support to parse COOS XML to get the COO list for each commodity part number
 * 2012-06-05| ~06|E692532 |Edgar Mercado   |-GreyMarket (CR783/RCQ00203226): Inventory Accuracy. Rework/Reapply functionality       
 ******************************************************************************/

package com.ibm.rchland.mfgapps.mfscommon.comm.trx;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLDocument;
import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.struct.MFSPart;
import com.ibm.rchland.mfgapps.mfscommon.struct.MFSSubstitute;      // ~04A
import com.ibm.rchland.mfgapps.mfscommon.struct.MFSCoo;             // ~05A

/**
 * @author The MFS Client Development Team
 */
public class VRFYPNPLUS extends VRFY_PART 
{
	private String inputOffline = NO_FLAG;
	private String inputAutoSerialize = NO_FLAG;
	private String inputUseVendorAsBuiltData = NO_FLAG;
	private String inputInstallFlag = NO_FLAG;
	private String inputUser;	                 // ~02A
	private String inputCell;	                 // ~02A	
	private String inputPlom;	                 // ~03A
	private String outputMctl;
	private String outputInpn;
	private String outputInsq;
	private String outputCoo;
	private String outputSubData;
	private String outputIdss;                   // ~06A
	private String outputPwun;                   // ~06A
	private List<MFSPart> outputSubParts;
	private MFSSubstitute outputSubstitutes;     // ~04A
	private MFSCoo outputCoos;                   // ~05A	
	
	public static int COLLECT_SUB_PART_DATA = 777;
	public static String YES_FLAG = "Y"; //$NON-NLS-1$
	public static String NO_FLAG = "N"; //$NON-NLS-1$
	public static String REBUILD_FLAG = "R"; //$NON-NLS-1$ //~06C
	
	public static String SUBDATA_NEW = "N"; //$NON-NLS-1$
	public static String SUBDATA_EDI = "E"; //$NON-NLS-1$
	public static String SUBDATA_CURRENT = "C"; //$NON-NLS-1$
	
	public static String INSTALL_COMPLETE = "C"; //$NON-NLS-1$
	public static String INSTALL_DELETE = "D"; //$NON-NLS-1$
	public static String INSTALL_UPDATE = "U"; //$NON-NLS-1$ // ~06A
	public static String SET_ACTIVE_INACTIVE = "L"; //$NON-NLS-1$ // ~06A
	
	public VRFYPNPLUS(MFSActionable actionable) {
		super(actionable);
		this.outputSubParts = new Vector<MFSPart>();
		this.outputSubstitutes = new MFSSubstitute();  // ~04A
		this.outputCoos = new MFSCoo();                // ~05A
	}

	@Override
	protected boolean validateInputs() {
		if(super.validateInputs()) {
			if(this.inputOffline == null || !this.inputOffline.equals(YES_FLAG)) {
				this.inputOffline = NO_FLAG;
			}
			if(this.inputAutoSerialize == null || !this.inputAutoSerialize.equals(YES_FLAG)) {
				this.inputAutoSerialize = NO_FLAG;
			}
			if(this.inputUseVendorAsBuiltData == null || !this.inputUseVendorAsBuiltData.equals(YES_FLAG)) {
				this.inputUseVendorAsBuiltData = NO_FLAG;
			} /* Add INSTALL_UPDATE for ReWork, REBUILD_FLAG for ReBuild and SET_ACTIVE_INACTIVE to the condition  ~06C*/
			if(this.inputInstallFlag == null || 
				(!this.inputInstallFlag.equals(YES_FLAG) && 
					!this.inputInstallFlag.equals(INSTALL_COMPLETE) &&
					!this.inputInstallFlag.equals(INSTALL_DELETE) && 
					!this.inputInstallFlag.equals(INSTALL_UPDATE) &&
				    !this.inputInstallFlag.equals(REBUILD_FLAG) &&
				    !this.inputInstallFlag.equals(SET_ACTIVE_INACTIVE))) {
				this.inputInstallFlag = NO_FLAG;
			}			
			return true;
		}
		return false;
	}

	@Override
	protected void buildInput() 
	{
		super.transaction = "VRFYPNPLUS"; //$NON-NLS-1$		
		super.buildInput();
				
		StringBuffer buffer = new StringBuffer(this.getInput());
		buffer.append(this.inputOffline);
		buffer.append(this.inputAutoSerialize);
		buffer.append(this.inputUseVendorAsBuiltData);
		buffer.append(this.inputInstallFlag);
		buffer.append(this.paddingLeft(this.inputUser, 8));  //~02A
		buffer.append(this.paddingLeft(this.inputCell, 8));  //~02A
		buffer.append(this.paddingLeft(this.inputPlom, 3));  //~03A
		
		this.setInput(buffer.toString());
	}
	/**
	 * Parses the subPartsInfo string which is the VRFYPNPLUS output.
	 * subPartsInfo structure:
	 * 
	 * <MCTL>xxxxxxxx</MCTL> - work unit
	 * <INPN>xxxxxxxxxxxx</INPN> - installed part number
	 * <INSQ>xxxxxxxxxxxxxxxxxxx</INSQ> - installed serial number
	 * <COOC>xx</COOC> - country of origin
	 * <SUBDATA>xxxx</SUBDATA> - subassembly Data Type (N = New, E = EDI, C = Current)
	 * <SUBPARTS>
	 * <SUBPART>
	 * 		<INPN>xxxxxxxxxxxx</INPN> - part number
	 * 		<INSQ>xxxxxxxxxxxxxxxxxx</INSQ> - serial number
	 *		<CDES>xxxxxxxx</CDES> - description
	 *	    <IDSP>x</IDSP> - install indicator
	 *      <CRCT>xxxx</CRCT> - component count for sequence
	 *      <CSNI>x</CSNI> - collect part seq nbr indicator     ~02A
	 *      <MLRI>x</MLRI> - MLR Indicator                      ~04A
	 *      <PNRI>1</PNRI> - Part Number required Indicator     ~05A
	 *      <COOC>x</COOC> - Country Of Origin subpart          ~04A
	 * </SUBPART>
	 * ...
	 * <SUBPART>
	 * ...
	 * </SUBPART>
	 * </SUBPARTS>
	 * *Parse SUBSTITUTES portion                               ~04A
	 * <SUBSTITUTES>                                          
	 *  <SUBSTITUTE>
	 *	 <CALLEDPN>xxxxxxxxxxxx</CALLEDPN>
	 *    <PNLIST>
	 *     <PN>yyyyyyyyyyyy</PN>
     *     ...
     *    </PNLIST>
     *  </SUBSTITUTE>
     * ...
     * </SUBSTITUTES> 
     * *Parse COOS portion                                     ~05A 
     * <COOS>
	 *  <PNCOO>
	 *   <PN>xxxxxxxxxxxx</PN>
	 *    <COOLIST>
	 *     <COO>aa</COO>
	 *     <COO>bb</COO>
	 *    </COOLIST>
	 *  </PNCOO>
	 *  ...
	 *	<PNCOO>
	 *   <PN>yyyyyyyyyyyy</PN>
	 *    <COOLIST>
	 *     <COO>aa</COO>
	 *    </COOLIST>
	 *  </PNCOO>
	 * </COOS>                                
     *                                                              	 */
	@Override
	protected void parseOutput() {
		if(this.getReturnCode() == COLLECT_SUB_PART_DATA ) {
			IGSXMLDocument subPartsDoc = new IGSXMLDocument(this.getOutput());
			
			this.outputMctl = subPartsDoc.getNextElement("MCTL"); //$NON-NLS-1$
			this.outputIdss = subPartsDoc.getNextElement("IDSS"); //$NON-NLS-1$    ~06A
			this.outputPwun = subPartsDoc.getNextElement("PWUN"); //$NON-NLS-1$    ~06A			
			this.outputInpn = subPartsDoc.getNextElement("INPN"); //$NON-NLS-1$
			this.outputInsq = subPartsDoc.getNextElement("INSQ"); //$NON-NLS-1$
			this.outputCoo = subPartsDoc.getNextElement("COO"); //$NON-NLS-1$	
			this.outputSubData = subPartsDoc.getNextElement("SUBDATA"); //$NON-NLS-1$
			this.outputSubParts.clear();
			
			if(null != subPartsDoc.stepIntoElement("SUBPARTS")) { //$NON-NLS-1$
				MFSPart subPart = null;
				
				while(null != subPartsDoc.stepIntoElement("SUBPART")) //$NON-NLS-1$
				{
					subPart = new MFSPart();
					subPart.setInpn(subPartsDoc.getNextElement("INPN").trim()); //$NON-NLS-1$
					subPart.setInsq(subPartsDoc.getNextElement("INSQ").trim()); //$NON-NLS-1$
					subPart.setDesc(subPartsDoc.getNextElement("CDES")); //$NON-NLS-1$
					subPart.setIdsp(subPartsDoc.getNextElement("IDSP")); //$NON-NLS-1$
					subPart.setCrct(subPartsDoc.getNextElement("CRCT").trim()); //$NON-NLS-1$
					subPart.setCsni(subPartsDoc.getNextElement("CSNI").trim()); //$NON-NLS-1$     ~02A
					subPart.setMlri(subPartsDoc.getNextElement("MLRI").trim()); //$NON-NLS-1$     ~04A
					subPart.setPnri(subPartsDoc.getNextElement("PNRI").trim()); //$NON-NLS-1$     ~05A
					subPart.setCooc(subPartsDoc.getNextElement("COOC").trim()); //$NON-NLS-1$     ~04A
					
					subPartsDoc.stepOutOfElement();			
					outputSubParts.add(subPart);
				}				
			}
			
			// Parse SUBSTITUTES portion on the XML     ~04A
			subPartsDoc.stepOutOfElement();			// SUBPARTS tag
			if(null != subPartsDoc.stepIntoElement("SUBSTITUTES")) { //$NON-NLS-1$
			    String calledpn = null;

				while(null != subPartsDoc.stepIntoElement("SUBSTITUTE")) //$NON-NLS-1$
				{
					calledpn = subPartsDoc.getNextElement("CALLEDPN").trim();
					ArrayList<String> pnList = new ArrayList<String>();
					
					if(null != subPartsDoc.stepIntoElement("PNLIST"))
					{
						while(null != subPartsDoc.stepIntoElement("PN")) //$NON-NLS-1$
						{
							pnList.add(subPartsDoc.getNextElement("PN").trim());
							subPartsDoc.stepOutOfElement();
						}
						subPartsDoc.stepOutOfElement();									
					}
					
					outputSubstitutes.setCalledPn_PnList(calledpn, pnList);
					subPartsDoc.stepOutOfElement();					
				}				
			} // End Parse SUBSTITUTES portion on the XML     ~04A
			
			// Parse COOS portion on the XML                  ~05A
			subPartsDoc.stepOutOfElement();			// SUBSTITUTES tag
			if(null != subPartsDoc.stepIntoElement("COOS")) { //$NON-NLS-1$
			    String pncoo = null;

				while(null != subPartsDoc.stepIntoElement("PNCOO")) //$NON-NLS-1$
				{
					pncoo = subPartsDoc.getNextElement("PN").trim();
					ArrayList<String> cooList = new ArrayList<String>();
					
					if(null != subPartsDoc.stepIntoElement("COOLIST"))
					{
						while(null != subPartsDoc.stepIntoElement("COO")) //$NON-NLS-1$
						{
							cooList.add(subPartsDoc.getNextElement("COO").trim());
							subPartsDoc.stepOutOfElement();
						}
						subPartsDoc.stepOutOfElement();									
					}
					
					outputCoos.setPn_cooList(pncoo, cooList);
					subPartsDoc.stepOutOfElement();					
				}				
			} // End Parse COOS portion on the XML            ~05A			
						
		}
		// does nothing the outputs could be xml or strings
		else if(0 == this.getReturnCode() && this.inputInstallFlag == NO_FLAG) {
			IGSXMLDocument subPartsDoc = new IGSXMLDocument(this.getOutput());
			
			this.outputMctl = subPartsDoc.getNextElement("MCTL"); //$NON-NLS-1$
			this.outputIdss = subPartsDoc.getNextElement("IDSS"); //$NON-NLS-1$    ~06A
			this.outputPwun = subPartsDoc.getNextElement("PWUN"); //$NON-NLS-1$    ~06A			
			this.outputInpn = subPartsDoc.getNextElement("INPN"); //$NON-NLS-1$
			this.outputInsq = subPartsDoc.getNextElement("INSQ"); //$NON-NLS-1$
			this.outputCoo = subPartsDoc.getNextElement("COO"); //$NON-NLS-1$
			this.outputSubData = subPartsDoc.getNextElement("SUBDATA"); //$NON-NLS-1$
			
			this.outputSubParts.clear();
		}
		else if(0 != this.getReturnCode()) 
		{
			this.setErrorMessage(this.getOutput());
		}	
	}
	
	/**
	 * @param autoSerialize the autoSerialize to set
	 */
	public void setAutoSerialize(String autoSerialize) {
		this.inputAutoSerialize = autoSerialize;
	}

	/**
	 * @param install the install to set
	 */
	public void setInstall(String install) {
		this.inputInstallFlag = install;
	}

	/**
	 * @param offline the offline to set
	 */
	public void setOffline(String offline) {
		this.inputOffline = offline;
	}

	/**
	 * @param useVendorAsBuiltData the useVendorAsBuiltData to set
	 */
	public void setUseVendorAsBuiltData(String useVendorAsBuiltData) {
		this.inputUseVendorAsBuiltData = useVendorAsBuiltData;
	}
	
	/** ~02A
	 * @param user the user to set
	 */	
	public void setUser(String user) {
		this.inputUser = user;
	}	

	/** ~02A
	 * @param cell the cell to set
	 */	
	public void setCell(String cell) {
		this.inputCell = cell;
	}		

	/** ~03A
	 * @param plom the plom to set
	 */	
	public void setPlom(String plom) {
		this.inputPlom = plom;
	}		
	

	public String getOutputMctl() {
		return outputMctl;
	}

	public String getOutputInpn() {
		return outputInpn;
	}

	public String getOutputInsq() {
		return outputInsq;
	}

	public String getOutputCoo() {
		return outputCoo;
	}

	public String getOutputSubData() {
		return outputSubData;
	}
	/** ~06A
	 *  Return IDSS value for work unit
	 */			
	public String getOutputIdss() {
		return outputIdss;
	}
	
	/** ~06A
	 *  Return PWUN value for work unit
	 */			
	public String getOutputPwun() {
		return outputPwun;
	}	
	
	public List<MFSPart> getOutputSubParts() {
		return outputSubParts;
	}
	
	/** ~04A
	 *  Return MFSSubstitute list
	 */		
	public MFSSubstitute getOutputSubstitutes() {
		return outputSubstitutes;
	}
	
	/** ~05A
	 *  Return MFSCoo list
	 */		
	public MFSCoo getOutputCoos() {
		return outputCoos;
	}
}