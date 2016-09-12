/* @ Copyright IBM Corporation 2003, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-11      38139JM  R Prechel        -Redone for Java 5 printing
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.util.Hashtable;
import java.util.Vector;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLParser;

/**
 * <code>MFSPartDetailTableModel</code> is the <code>MFSTableModel</code>
 * for the Part Detail Table.
 * @author The MFS Client Development Team
 */
public class MFSPartDetailTableModel
	extends MFSTableModel
{
	private static final long serialVersionUID = 1L;

	/** The number of columns in an <code>MFSPartDetailTableModel</code>. */
	private static final int COL_COUNT = 4;

	/** The urls <code>Hashtable</code>. */
	@SuppressWarnings("rawtypes")
	public Hashtable urls = new Hashtable();

	/**
	 * Constructs a new <code>MFSPartDetailTableModel</code> that will be used
	 * for printing by the MFS Print Server.
	 * @param model the <code>MFSPartDetailTableModel</code> to copy
	 */
	public MFSPartDetailTableModel(MFSPartDetailTableModel model)
	{
		super(false);
		this.headers = new String[] {
				"Flag ID", "Flag Value", "Description", "Comments"
		};
		int length = model.data.length;
		this.data = new String[length][COL_COUNT];

		for (int rowIndex = 0; rowIndex < length; rowIndex++)
		{
			for (int colIndex = 0; colIndex < COL_COUNT; colIndex++)
			{
				this.data[rowIndex][colIndex] = model.data[rowIndex][colIndex];
			}
		}
	}

	/**
	 * Constructs a new <code>MFSPartDetailTableModel</code> that will be used
	 * for display by the MFS Client.
	 * @param xmlData the data used to populate the model
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MFSPartDetailTableModel(String xmlData)
	{
		super(false);
		this.headers = new String[] {
				"Flag ID", "Flag Value", "Description", "Comments"
		};
		Vector rows = new Vector();
		MfsXMLParser xmlParser = new MfsXMLParser(xmlData);

		// The XML is of the form:
		// <FLD><NAME>fieldName</NAME><VALU>fieldValue</VALU></FLD>
		final String FIELD = "FLD"; //$NON-NLS-1$
		final String NAME = "NAME"; //$NON-NLS-1$
		final String VALUE = "VALU"; //$NON-NLS-1$

		// These are the field names to look for.
		// The Flag Value FLD element immediately
		// follows the Flag ID FLD element.
		final String FLAG_ID = "Q2FGID"; //$NON-NLS-1$
		final String COMMENT = "Q2CMNT"; //$NON-NLS-1$
		final String URL = "RJHTML"; //$NON-NLS-1$
		final String DESCRIPTION = "RJCMNT"; //$NON-NLS-1$

		try
		{
			MfsXMLParser tempParser = new MfsXMLParser(xmlParser.getNextField(FIELD));

			//Skip until Flag ID
			while (!tempParser.getField(NAME).equals(FLAG_ID))
			{
				tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
			}

			while (true)
			{
				/* Get Flag ID */
				String[] rowData = new String[COL_COUNT];
				rowData[0] = tempParser.getField(VALUE);

				/* Get Flag Value */
				tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
				rowData[1] = tempParser.getField(VALUE);

				/* Skip until Comment, URL, Description, or next Flag ID */
				tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
				String name = tempParser.getField(NAME);
				while (!name.equals(COMMENT) && !name.equals(URL)
						&& !name.equals(DESCRIPTION) && !name.equals(FLAG_ID))
				{
					tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
					name = tempParser.getField(NAME);
				}

				/* Get Comment for Flag ID */
				if (name.equals(COMMENT))
				{
					rowData[3] = tempParser.getField(VALUE);
				}
				else
				{
					rowData[3] = "";
				}

				/* Skip until URL, Description, or next Flag ID */
				if (name.equals(COMMENT)
						|| (!name.equals(URL) && !name.equals(DESCRIPTION) && !name.equals(FLAG_ID)))
				{
					tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
					if (tempParser.getUnparsedXML().length() != 0)
					{
						name = tempParser.getField(NAME);
						while (!name.equals(URL) && !name.equals(DESCRIPTION)
								&& !name.equals(FLAG_ID))
						{
							tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
							name = tempParser.getField(NAME);
						}
					}
				}

				// Check for RJ10 data
				if (tempParser.getUnparsedXML().length() != 0)
				{
					name = tempParser.getField(NAME);
					if (!name.equals(FLAG_ID))
					{
						/* Get the URL for this Flag ID Value */
						if (name.equals(URL))
						{
							String url = tempParser.getField(VALUE);
							this.urls.put(rowData[0].trim(), url);
						}

						if (name.equals(URL)
								|| (!name.equals(DESCRIPTION) && !name.equals(FLAG_ID)))
						{
							/* Skip until Description or next Flag ID */
							tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
							name = tempParser.getField(NAME);
							while (!name.equals(DESCRIPTION) && !name.equals(FLAG_ID))
							{
								tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
								name = tempParser.getField(NAME);
							}

							if (name.equals(DESCRIPTION))
							{
								rowData[2] = tempParser.getField(VALUE);
							}
							else
							{
								rowData[2] = "";
							}
						}
						else
						{
							rowData[2] = "";
						}
					}
					else
					{
						rowData[2] = "FLAG NOT FOUND IN RJ10";
					}
				}
				else
				{
					rowData[2] = "FLAG NOT FOUND IN RJ10";
				}

				rows.add(rowData);

				if (tempParser.getUnparsedXML().length() != 0)
				{
					/* Skip until next Flag ID */
					while (!tempParser.getField(NAME).equals(FLAG_ID))
					{
						tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
					}
				}
				else
				{
					tempParser.setUnparsedXML(xmlParser.getNextField(FIELD));
				}
			}
		}
		catch (MISSING_XML_TAG_EXCEPTION mt)
		{
			System.out.println("Missing tag error");
		}

		this.data = new String[rows.size()][COL_COUNT];
		rows.copyInto(this.data);
	}

	/**
	 * Returns the help URL for the specified Flag ID.
	 * @param flagID the Flag ID for which a help URL should be returned
	 * @return the help URL
	 */
	public String getUrl(String flagID)
	{
		return (String) this.urls.get(flagID);
	}
}
