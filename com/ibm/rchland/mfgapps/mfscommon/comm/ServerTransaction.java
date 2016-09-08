/* © Copyright IBM Corporation 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2010-11-01      49513JM  Toribio H.       -Initial version
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

/**
 * ServerTransaction is the abstract transaction for all Server Calls
 */
public abstract class ServerTransaction {	
	private String input; 
	private String output;
	private int returnCode;
	private String errorMessage;

	protected void setInput(String input) {
		this.input = input;
	}

	protected String getInput() {
		return input;
	}

	protected void setOutput(String output) {
		this.output = output;
	}

	protected String getOutput() {
		return output;
	}

	protected void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public int getReturnCode() {
		return returnCode;
	}

	protected void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	protected abstract boolean validateInputs();
	
	protected String paddingLeft(String strInput, int size) {
		if(strInput == null) {
			strInput = ""; //$NON-NLS-1$
		}
		return String.format("%-" + size + "s", strInput); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected abstract void buildInput();
	
	protected abstract void parseOutput();
	
	protected abstract boolean validateOutputs();
	
	protected abstract void callTransaction();
	
	@Override
	public boolean equals(Object object) {
		if(object == null || !(object instanceof ServerTransaction)) {
			return false;
		}
		return this.input.equals(((ServerTransaction)object).input);
	}
	
	public final boolean execute() {
		if(this.validateInputs()) {
			this.buildInput();	
			if(this instanceof Cacheable) {
				return this.executeAsCacheable();
			} 
			else {
				return this.executeAsServerTransaction();
			}			
		}
		return false;
	}			
	
	private boolean executeAsServerTransaction() {
		this.callTransaction();
		this.parseOutput();
		return this.validateOutputs();		
	}
	
	private boolean executeAsCacheable() {
		boolean cacheUsed = false;		
		Cacheable cacheable = (Cacheable)this;
		
		ServerTransaction cached = (ServerTransaction) cacheable.getCache().get(cacheable);				
		if(null != cached) {
			this.output = cached.output;
			this.returnCode = cached.returnCode;
			this.errorMessage = cached.errorMessage;		
			cacheUsed = true;
		}
		else {
			this.callTransaction();
		}
		this.parseOutput();
		if(this.validateOutputs()) {
			if(!cacheUsed) {
				cacheable.getCache().add((Cacheable) this);
			}					
			return true;
		}	
		return false;
	}	
}