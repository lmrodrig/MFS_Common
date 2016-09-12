/* @ Copyright IBM Corporation 2002, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-11      38139JM  R Prechel        -Redone for Java 5 printing
 * 2007-05-16   ~1 38716JM  R Prechel        -Set Test Level to empty String for MH10 records
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import mfsxml.MISSING_XML_TAG_EXCEPTION;
import mfsxml.MfsXMLParser;

/**
 * <code>MFSPartHistoryTableModel</code> is the <code>MFSTableModel</code>
 * for the Part History Table.
 * <p>
 * Implementation Note: All <code>MFSPartHistoryTableModel</code>s have
 * <code>COL_COUNT</code> columns. The Active column is not displayed by the
 * MFS Client, but is printed by the MFS Print Server as the second to last
 * column. To hide the Active column, it is placed in the last column, and
 * <code>getColumnCount</code> returns <code>COL_COUNT - 1</code>. The copy
 * constructor creates an <code>MFSPartHistoryTableModel</code> for use by the
 * MFS Print Server. Thus, if <code>getColumnCount</code> returns
 * <code>COL_COUNT - 1</code>, the last two columns are swapped.
 * @author The MFS Client Development Team
 */
public class MFSPartHistoryTableModel
	extends MFSTableModel
{
	private static final long serialVersionUID = 1L;

	/** The number of columns in an <code>MFSPartHistoryTableModel</code>. */
	private static final int COL_COUNT = 6;

	/** The empty <code>String</code>. */
	private final static String EMPTY_STRING = "";

	/** Stores the maximum number of flags. */
	private int maxFlags = 0;

	/**
	 * Constructs a new <code>MFSPartHistoryTableModel</code> that will be
	 * used for printing by the MFS Print Server.
	 * @param model the <code>MFSPartHistoryTableModel</code> to copy
	 */
	public MFSPartHistoryTableModel(MFSPartHistoryTableModel model)
	{
		super(false);
		this.headers = new String[] {
				"Date", "Time", "Test\nLevel", "Location/\nPRLN", "Active",
				"Changes Since\nLast Transaction"
		};
		this.maxFlags = model.maxFlags;
		final int rowCount = model.data.length;
		this.data = new String[rowCount][COL_COUNT];

		if (model.getColumnCount() == COL_COUNT)
		{
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
			{
				for (int colIndex = 0; colIndex < COL_COUNT; colIndex++)
				{
					this.data[rowIndex][colIndex] = model.data[rowIndex][colIndex];
				}
			}
		}
		else
		{
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
			{
				for (int colIndex = 0; colIndex < COL_COUNT - 2; colIndex++)
				{
					this.data[rowIndex][colIndex] = model.data[rowIndex][colIndex];
				}
				//Swap the last two columns
				this.data[rowIndex][COL_COUNT - 2] = model.data[rowIndex][COL_COUNT - 1];
				this.data[rowIndex][COL_COUNT - 1] = model.data[rowIndex][COL_COUNT - 2];
			}
		}
	}

	/**
	 * Constructs a new <code>MFSPartHistoryTableModel</code> that will be
	 * used for display by the MFS Client.
	 * @param xmlData the data used to populate the model
	 */
	public MFSPartHistoryTableModel(String xmlData)
	{
		super(false);
		this.headers = new String[] {
				"Date", "Time", "Test Level", "Location",
				"Changes Since Last Transaction"
		};
		buildPartHistory(xmlData);
	}

	/**
	 * Parses the specified <code>xmlData</code> to build the part history.
	 * @param xmlData the xml data returned by <code>RTVPRTHIST</code>
	 */
	@SuppressWarnings("rawtypes")
	private void buildPartHistory(String xmlData)
	{
		MfsXMLParser xmlParser = new MfsXMLParser(xmlData);
		Vector myRows = new Vector();

		try
		{
			String testLevel = getTestLevel(xmlParser);
			parseQD20Data(xmlParser, myRows);

			xmlParser = new MfsXMLParser(xmlData);
			parseMH10Data(xmlParser, myRows);

			parseCR10Data(xmlParser, myRows, testLevel);

			sort(myRows);

			/* Make sure we have other rows to compare to */
			if (myRows.size() > 1)
			{
				determineChangesMultiRow(myRows);
			}
			else
			{
				determineChangesSingleRow(myRows);
			}

			combineRows(myRows);
			setTableModelData(myRows);
		}
		catch (MISSING_XML_TAG_EXCEPTION mxte)
		{
			System.out.println(mxte.toString());
			mxte.printStackTrace();
		}
	}

	/**
	 * Returns the value of the <code>QDCTLV</code> field from the
	 * <code>QD10</code> or <code>AGQD10</code> element of the specified
	 * <code>MfsXMLParser</code>.
	 * @param xmlParser the xml parser
	 * @return the value of the <code>QDCTLV</code> field or the
	 *         {@link #EMPTY_STRING} if the field does not exist
	 * @throws MISSING_XML_TAG_EXCEPTION as thrown by an {@link MfsXMLParser}
	 */
	private String getTestLevel(MfsXMLParser xmlParser)
		throws MISSING_XML_TAG_EXCEPTION
	{
		MfsXMLParser localParser = null;
		if (xmlParser.contains("QD10"))
		{
			localParser = new MfsXMLParser(xmlParser.getFieldOnly("QD10"));
		}
		else if (xmlParser.contains("AGQD10"))
		{
			localParser = new MfsXMLParser(xmlParser.getFieldOnly("AGQD10"));
		}
		else
		{
			return EMPTY_STRING;
		}

		String fieldName = localParser.getNextField("NAME").trim();
		while (fieldName.length() > 0)
		{
			if (fieldName.equals("QDCTLV"))
			{
				return localParser.getNextField("VALU").trim();
			}
			fieldName = localParser.getNextField("NAME").trim();
		}
		return EMPTY_STRING;
	}

	/**
	 * Parses the <code>QD20</code> data from the specified
	 * <code>MfsXMLParser</code> and adds the data to <code>rows</code>.
	 * @param xmlParser the xml parser
	 * @param rows the <code>List</code> where the parsed rows of data are stored
	 * @throws MISSING_XML_TAG_EXCEPTION as thrown by an {@link MfsXMLParser}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parseQD20Data(MfsXMLParser xmlParser, List rows)
		throws MISSING_XML_TAG_EXCEPTION
	{
		final String QD20_START_TAG = "<QD20>";
		final String AGQD20_START_TAG = "<AGQD20>";

		String unparsedXML = xmlParser.getUnparsedXML();
		int startIndex = 0;
		int qd20Index = unparsedXML.indexOf(QD20_START_TAG, startIndex);
		int agqd20Index = unparsedXML.indexOf(AGQD20_START_TAG, startIndex);

		while (qd20Index >= 0 || agqd20Index >= 0)
		{
			MfsXMLParser localParser = null;
			if ((qd20Index != -1 && qd20Index < agqd20Index) || agqd20Index == -1)
			{
				localParser = new MfsXMLParser(xmlParser.getNextField("QD20"));
				startIndex = qd20Index + QD20_START_TAG.length();
			}
			else
			{
				localParser = new MfsXMLParser(xmlParser.getNextField("AGQD20"));
				startIndex = agqd20Index + AGQD20_START_TAG.length();
			}

			Hashtable rowData = new Hashtable();
			rowData.put("QD20", "YES");

			String fieldName = localParser.getNextField("NAME");
			String fieldValue = localParser.getNextField("VALU");
			while (fieldName.length() > 0 && fieldValue.length() > 0)
			{
				if (fieldName.equals("Q2INDT"))
				{
					rowData.put("DATE", fieldValue);
				}
				else if (fieldName.equals("Q2INTM"))
				{
					rowData.put("TIME", fieldValue);
				}
				else if (fieldName.equals("Q2INPN"))
				{
					rowData.put("INPN", fieldValue);
				}
				else if (fieldName.equals("Q2INSQ"))
				{
					rowData.put("INSQ", fieldValue);
				}
				else
				{
					rowData.put(fieldName, fieldValue);
				}

				fieldName = localParser.getNextField("NAME");
				fieldValue = localParser.getNextField("VALU");
			}

			rowData.put("ACTIVE", rowData.get("Q2ACTF").equals("Y") ? "Y" : "N");

			rows.add(rowData);
			qd20Index = unparsedXML.indexOf(QD20_START_TAG, startIndex);
			agqd20Index = unparsedXML.indexOf(AGQD20_START_TAG, startIndex);
		}
	}

	/**
	 * Parses the <code>MH10</code> data from the specified
	 * <code>MfsXMLParser</code> and adds the data to <code>rows</code>.
	 * @param xmlParser the xml parser
	 * @param rows the <code>List</code> where the parsed rows of data are stored
	 * @throws MISSING_XML_TAG_EXCEPTION as thrown by an {@link MfsXMLParser}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parseMH10Data(MfsXMLParser xmlParser, List rows)
		throws MISSING_XML_TAG_EXCEPTION
	{
		String nextField = xmlParser.getNextField("MH10");
		while (nextField.length() > 0)
		{
			MfsXMLParser localParser = new MfsXMLParser(nextField);
			Hashtable rowData = new Hashtable();
			rowData.put("MH10", "YES");

			String fieldName = localParser.getNextField("NAME");
			String fieldValue = localParser.getNextField("VALU");
			while (fieldName.length() > 0 && fieldValue.length() > 0)
			{
				if (fieldName.equals("MHENTD"))
				{
					rowData.put("DATE", fieldValue);
				}
				else if (fieldName.equals("MHENTT"))
				{
					rowData.put("TIME", fieldValue);
				}
				else if (fieldName.equals("MHINPN"))
				{
					rowData.put("INPN", fieldValue);
				}
				else if (fieldName.equals("MHINSQ"))
				{
					rowData.put("INSQ", fieldValue);
				}
				else
				{
					rowData.put(fieldName, fieldValue);
				}

				fieldName = localParser.getNextField("NAME");
				fieldValue = localParser.getNextField("VALU");
			}
			
			//~1A Make sure all MH10 rows have a test level value
			rowData.put("Q2CTLV", EMPTY_STRING);

			rowData.put("ACTIVE", rowData.get("MHACTT").equals("Y") ? "Y" : "N");

			rows.add(rowData);
			nextField = xmlParser.getNextField("MH10");
		}
	}

	/**
	 * Parses the <code>CR10</code> data from the specified
	 * <code>MfsXMLParser</code> and adds the data to <code>rows</code>.
	 * @param xmlParser the xml parser
	 * @param rows the <code>List</code> where the parsed rows of data are stored
	 * @param testLevel the test level from the <code>QD10</code> data
	 * @throws MISSING_XML_TAG_EXCEPTION as thrown by an {@link MfsXMLParser}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parseCR10Data(MfsXMLParser xmlParser, List rows, String testLevel)
		throws MISSING_XML_TAG_EXCEPTION
	{
		String nextField = xmlParser.getNextField("CR10");
		while (nextField.length() > 0)
		{
			MfsXMLParser localParser = new MfsXMLParser(nextField);
			Hashtable rowData = new Hashtable();
			rowData.put("CR10", "YES");

			String fieldName = localParser.getNextField("NAME");
			String fieldValue = localParser.getNextField("VALU");
			while (fieldName.length() > 0 && fieldValue.length() > 0)
			{
				if (fieldName.equals("CRCSDS"))
				{
					rowData.put("DATE", fieldValue);
				}
				else if (fieldName.equals("CRCSTS"))
				{
					rowData.put("TIME", fieldValue);
				}
				else if (fieldName.equals("CRINPN"))
				{
					rowData.put("INPN", fieldValue);
				}
				else if (fieldName.equals("CRINSQ"))
				{
					rowData.put("INSQ", fieldValue);
				}
				else
				{
					rowData.put(fieldName, fieldValue);
				}

				fieldName = localParser.getNextField("NAME");
				fieldValue = localParser.getNextField("VALU");
			}

			//Make sure all CR10 rows have a test level value
			rowData.put("Q2CTLV", testLevel);

			// The print server requires that all records have an ACTIVE flag
			Object idsp = rowData.get("CRIDSP");
			rowData.put("ACTIVE", (idsp != null && (idsp.equals("I") || idsp.equals("R"))) ? "Y" : "N");

			if (rowData.containsKey("INPN"))
			{
				rows.add(rowData);
				nextField = xmlParser.getNextField("CR10");
			}
			else
			{
				nextField = EMPTY_STRING;
			}
		}
	}

	/**
	 * Sorts the rows by date and time.
	 * @param rows the <code>Vector</code> of row data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void sort(Vector rows)
	{
		Vector sortedRows = new Vector();

		/* Insert the first row so we have something to compare with. */
		sortedRows.insertElementAt(rows.elementAt(0), 0);
		for (int outerIndex = 1; outerIndex < rows.size(); outerIndex++)
		{
			Hashtable rowData = (Hashtable) rows.elementAt(outerIndex);

			String dateString = (String) rowData.get("DATE");
			int date = Integer.parseInt(dateString.substring(6, 8)
					+ dateString.substring(0, 2) + dateString.substring(3, 5));

			boolean inserted = false;
			for (int innerIndex = 0; innerIndex < sortedRows.size(); innerIndex++)
			{
				Hashtable rowData2 = (Hashtable) sortedRows.elementAt(innerIndex);
				String dateString2 = (String) rowData2.get("DATE");
				int date2 = Integer.parseInt(dateString2.substring(6, 8)
						+ dateString2.substring(0, 2) + dateString2.substring(3, 5));

				if ((date > date2 && (date < 500000 && date2 < 500000))
						|| (date > date2 && (date > 800000 && date2 > 800000))
						|| (date < date2 && (date < 500000 && date2 > 800000)))
				{
					sortedRows.insertElementAt(rowData, innerIndex);
					innerIndex = sortedRows.size();
					inserted = true;
				}
				else if (date == date2)
				{
					String time = (String) rowData.get("TIME");
					String time2 = (String) rowData2.get("TIME");
					if (time.compareTo(time2) >= 0)
					{
						sortedRows.insertElementAt(rowData, innerIndex);
						innerIndex = sortedRows.size();
						inserted = true;
					}
				}
			}

			if (!inserted)
			{
				sortedRows.insertElementAt(rowData, sortedRows.size());
			}
		}
		rows.removeAllElements();
		rows.addAll(sortedRows);
	}

	/**
	 * Determines how the data has changed when there is more than one row.
	 * @param rows the <code>Vector</code> of row data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void determineChangesMultiRow(Vector rows)
	{
		Hashtable rowData2 = new Hashtable();
		for (int outerIndex = 0; outerIndex < rows.size(); outerIndex++)
		{
			boolean changes = false;
			String change = "";
			Hashtable rowData = (Hashtable) rows.elementAt(outerIndex);

			int innerIndex = outerIndex + 1;

			if (innerIndex < rows.size())
			{
				rowData2 = (Hashtable) rows.elementAt(innerIndex);
			}

			// Process QD20 records
			if (rowData.get("QD20") != null)
			{
				/* Check for a changed part. */
				if (!(rowData.get("INPN")).equals((rowData2.get("INPN")))
						&& !(rowData.get("INSQ")).equals((rowData2.get("INSQ"))))
				{
					changes = true;
					rowData.put("TRAN", "Part and serial number changed");
					rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
					rowData.put("NEWPN", ((String) rowData.get("INPN")).trim());
					rowData.put("NEWSN", ((String) rowData.get("INSQ")).trim());
					rowData.put("OLDPN", ((String) rowData2.get("INPN")).trim());
					rowData.put("OLDSN", ((String) rowData2.get("INSQ")).trim());
					rowData.put("FGID", ((String) rowData.get("Q2FGID")).trim());
					rowData.put("FVAL", ((String) rowData.get("Q2FVAL")).trim());
					rowData.put("CHANGE", "YES");
				}
				else if (!(rowData.get("INPN")).equals((rowData2.get("INPN"))))
				{
					changes = true;
					rowData.put("TRAN", "Part number changed");
					rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
					rowData.put("NEWPN", ((String) rowData.get("INPN")).trim());
					rowData.put("OLDPN", ((String) rowData2.get("INPN")).trim());
					rowData.put("NEWSN", ((String) rowData.get("INSQ")).trim());
					rowData.put("OLDSN", ((String) rowData.get("INSQ")).trim());
					rowData.put("CHANGE", "YES");
				}
				else if (!(rowData.get("INSQ")).equals((rowData2.get("INSQ"))))
				{
					changes = true;
					rowData.put("TRAN", "Serial number changed");
					rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
					rowData.put("NEWSN", ((String) rowData.get("INSQ")).trim());
					rowData.put("OLDSN", ((String) rowData2.get("INSQ")).trim());
					rowData.put("NEWPN", ((String) rowData.get("INPN")).trim());
					rowData.put("OLDPN", ((String) rowData.get("INPN")).trim());
					rowData.put("CHANGE", "YES");
				}
			}

			innerIndex = 0;

			//If no change, then continue with QD20
			if (rowData.get("QD20") != null && !changes)
			{
				/* Check for changed QD20 flag values */
				for (innerIndex = outerIndex + 1; innerIndex < rows.size(); innerIndex++)
				{
					rowData2 = (Hashtable) rows.elementAt(innerIndex);

					//Only compare to QD20 records...
					if (rowData2.get("QD20") != null)
					{
						if ((rowData.get("Q2FGID")).equals((rowData2.get("Q2FGID"))))
						{
							if (!(rowData.get("Q2FVAL")).equals((rowData2.get("Q2FVAL"))))
							{
								changes = true;
								rowData.put("TRAN", "Flag changed");
								rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
								rowData.put("SPLT", ((String) rowData.get("Q2SPLT")).trim());

								if (!((String) rowData.get("Q2CMNT")).trim().equals(""))
								{
									rowData.put("CMNT", ((String) rowData.get("Q2CMNT")).trim());
								}
								rowData.put("CHANGE", "YES");

								//To escape from the loop...
								innerIndex = rows.size() + 1;
							}
						}
					}
				}

				/*
				 * If still no changes, then we didn't find another of these
				 * flags in the history, so this is the first time we've seen
				 * this flag.
				 */
				if (!changes)
				{
					if (!((String) rowData.get("Q2FGID")).trim().equals(""))
					{
						/* This is the first time we are seeing this flag. */
						changes = true;
						rowData.put("TRAN", "Flag set");
						rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
						rowData.put("FGID", ((String) rowData.get("Q2FGID")).trim());
						rowData.put("NMBR", ((String) rowData.get("Q2NMBR")).trim());
						rowData.put("FVAL", ((String) rowData.get("Q2FVAL")).trim());
						rowData.put("SPLT", ((String) rowData.get("Q2SPLT")).trim());
					}
					else
					{
						changes = true;
						rowData.put("TRAN", "Tran");
						rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
						rowData.put("PLOM", ((String) rowData.get("Q2SPLT")).trim());
					}

					if (!((String) rowData.get("Q2CMNT")).trim().equals(""))
					{
						changes = true;
						rowData.put("CMNT", ((String) rowData.get("Q2CMNT")).trim());
					}

					rowData.put("CHANGE", "YES");
				}

				/* Check for Floor movement */
				innerIndex = 0;

				if (!rowData.containsKey("CHANGE"))
				{
					String productLine = (String) rowData.get("Q2PROD");
					if (productLine.equalsIgnoreCase("WANDIN")
							|| productLine.equalsIgnoreCase("WANDTHRU")
							|| productLine.equalsIgnoreCase("WANDFLR")
							|| productLine.equalsIgnoreCase("WANDOUT")
							|| productLine.equalsIgnoreCase("JOBTSORT")
							|| productLine.equalsIgnoreCase("JOBBNC")
							|| productLine.equalsIgnoreCase("WIP"))
					{
						changes = true;
					}

					if (changes)
					{
						rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
					}

					if (!changes)
					{
						/* Check for changed Part Number and Serial Number values */
						for (innerIndex = outerIndex + 1; innerIndex < rows.size(); innerIndex++)
						{
							rowData2 = (Hashtable) rows.elementAt(innerIndex);

							// Don't look at MH10 records if MHMCTL == null
							if (rowData2.get("MHMCTL") == null)
							{
								/*
								 * If either the Part or Serial number differs
								 * from the previous record...
								 */
								if (!(rowData.get("INPN")).equals((rowData2.get("INPN")))
										|| !(rowData.get("INSQ")).equals((rowData2.get("INSQ"))))
								{
									rowData.put("TRAN", "Part changed");
									rowData.put("USER", ((String) rowData.get("Q2USER")).trim());

									/* If the part number changed, record it. */
									if (!(rowData.get("INPN")).equals((rowData2.get("INPN"))))
									{
										changes = true;
										rowData.put("NEWPN", ((String) rowData.get("INPN")).trim());
										rowData.put("OLDPN", ((String) rowData2.get("INPN")).trim());
									}

									/* If the serial number changed, record it. */
									if (!(rowData.get("INSQ")).equals((rowData2.get("INSQ"))))
									{
										changes = true;
										rowData.put("NEWSN", ((String) rowData.get("INSQ")).trim());
										rowData.put("OLDSN", ((String) rowData2.get("INSQ")).trim());
									}

									rowData.put("CHANGE", "YES");

									// To escape from the loop...
									innerIndex = rows.size() + 1;
								}
							}
						}
					}

					/*
					 * If no change has been detected yet, look for a part that
					 * was skipped in test.
					 */
					if (!changes)
					{
						if (((String) rowData.get("Q2CMNT")).startsWith("Skipped by"))
						{
							changes = true;
							rowData.put("TRAN", "Part skipped test");
							rowData.put("SPLT", ((String) rowData.get("SPLT")).trim());
							rowData.put("CMNT", ((String) rowData.get("Q2CMNT")).trim());
						}

					}

					/*
					 * If no change has been detected yet, look for a Macro
					 * location change.
					 */
					if (!changes)
					{
						if ((rowData.get("MHMCTL") == null)
								&& !rowData.containsKey("CHANGE"))
						{
							rowData2 = (Hashtable) rows.elementAt(outerIndex + 1);

							if (!(rowData.get("Q2MALC")).equals((rowData2.get("Q2MALC"))))
							{
								changes = true;
								rowData.put("TRAN", "Macro location changed");
								rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
								rowData.put("SPLT", ((String) rowData.get("SPLT")).trim());

								if (!((String) rowData.get("Q2CMNT")).trim().equals(""))
								{
									rowData.put("CMNT", ((String) rowData.get("Q2CMNT")).trim());
								}

								rowData.put("CHANGE", change);
							}
						}
					}

					if (!changes)
					{
						rowData.put("CHANGE", "YES");
					}
				}
			} /* End of QD20 record checks */
			/* Beginning of MH10 record checks. */
			else if (rowData.get("MH10") != null)
			{
				// IF PART WAS REMOVED
				if (!rowData.containsKey("CHANGE")
						&& ((String) rowData.get("MHSQTY")).substring(0, 1).equals("-"))
				{
					rowData.put("TRAN", "Part removed from " + rowData.get("MHMCTL"));
					rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
					rowData.put("SAPM", ((String) rowData.get("MHSAPM")).trim());
					rowData.put("SAPN", ((String) rowData.get("MHSAPN")).trim());
					rowData.put("CHANGE", "YES");
				}
				/* Check for an unplugged part */
				else if (!rowData.containsKey("CHANGE"))
				{
					innerIndex = outerIndex + 1;
					if (innerIndex <= rows.size())
					{
						do
						{
							if (innerIndex < rows.size())
							{
								rowData2 = (Hashtable) rows.elementAt(innerIndex);
							}
							innerIndex++;
						}
						while ((rowData2.get("MHMCTL") == null) && !(innerIndex > rows.size()));

						/* If there was another MH10 record found */
						if (rowData2.get("MH10") != null)
						{
							if ((rowData.get("MHSTLC")).equals(rowData2.get("MHSFLC"))
									&& (rowData2.get("MHSTLC")).equals(rowData.get("MHSFLC")))
							{

								rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
								rowData.put("TRAN", "Part removed from work unit");
								rowData.put("CHANGE", "YES");
							}
							else if (!((String) rowData.get("MHMCTL")).equals(rowData2.get("MHMCTL")))
							{
								String fromWU = (String) rowData2.get("MHMCTL");
								String toWU = (String) rowData.get("MHMCTL");

								if (fromWU == null)
								{
									fromWU = "";
								}
								if (toWU == null)
								{
									toWU = "";
								}
								rowData.put("TRAN", "Part was moved");
								rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
								rowData.put("FROMWU", fromWU);
								rowData.put("TOWU", toWU);
								rowData.put("CHANGE", "YES");
							}
							else if (!(rowData.get("MHORNO")).equals(rowData2.get("MHORNO")))
							{
								rowData.put("TRAN", "Part install in Order " + rowData.get("MHORNO"));
								rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
								rowData.put("SAPM", ((String) rowData.get("MHSAPM")).trim());
								rowData.put("SAPN", ((String) rowData.get("MHSAPN")).trim());
								rowData.put("CHANGE", "YES");
							}
							else
							{
								rowData.put("TRAN", "Part moved from "
										+ ((String) rowData.get("MHSFLC")).trim()
										+ " to "
										+ ((String) rowData.get("MHSTLC")).trim());
								rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
								rowData.put("CHANGE", "YES");
							}
						}
						else
						{
							rowData.put("TRAN", "Installed by "
									+ ((String) rowData.get("MHUSER")).trim() + " in WU "
									+ ((String) rowData.get("MHMCTL")).trim());
							rowData.put("SAPM", ((String) rowData.get("MHSAPM")).trim());
							rowData.put("SAPN", ((String) rowData.get("MHSAPN")).trim());
							rowData.put("CHANGE", "YES");
						}
					}
				}
			} /* End of MH10 checks */
			else if (rowData.get("CR10") != null && rowData.get("CRIDSP") != null)
			{
				String cridsp = (String) rowData.get("CRIDSP");
				if (cridsp.equals("I") || cridsp.equals("R"))
				{
					rowData.put("TRAN", "Installed by "
							+ ((String) rowData.get("CRUSER")).trim() + " in WU "
							+ ((String) rowData.get("CRMCTL")).trim());
					rowData.put("CHANGE", "YES");
				}
				else if (cridsp.equals("D"))
				{
					rowData.put("TRAN", "Part removed by "
							+ ((String) rowData.get("CRUSER")).trim() + " from WU "
							+ ((String) rowData.get("CRMCTL")).trim());
					rowData.put("CHANGE", "YES");
				}
			}
		}
	}

	/**
	 * Determines how the data has changed when there is only one row.
	 * @param rows the <code>Vector</code> of row data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void determineChangesSingleRow(Vector rows)
	{
		Hashtable rowData = (Hashtable) rows.elementAt(0);
		/* This is a QD20 record. */
		if (rowData.get("QD20") != null)
		{
			rowData.put("TRAN", "Flag set");
			rowData.put("USER", ((String) rowData.get("Q2USER")).trim());
			rowData.put("FGID", ((String) rowData.get("Q2FGID")).trim());
			rowData.put("FVAL", ((String) rowData.get("Q2FVAL")).trim());
			rowData.put("NMBR", ((String) rowData.get("Q2NMBR")).trim());
			rowData.put("SPLT", ((String) rowData.get("Q2SPLT")).trim());

			if (!((String) rowData.get("Q2CMNT")).trim().equals(""))
			{
				rowData.put("CMNT", ((String) rowData.get("Q2CMNT")).trim());
			}
			rowData.put("CHANGE", "YES");

		}
		/* This is an MH10 record. */
		else if (rowData.get("MH10") != null)
		{
			if (rowData.get("MHSAPT").equals("XFR1"))
			{
				rowData.put("TRAN", "XFR1");
				rowData.put("USER", ((String) rowData.get("MHUSER")).trim());
				rowData.put("FROMLOC", ((String) rowData.get("MHSFLC")).trim());
				rowData.put("TOLOC", ((String) rowData.get("MHSTLC")).trim());
			}
			rowData.put("CHANGE", "YES");
		}
		/* This is a CR10 record */
		else if (rowData.get("CR10") != null && rowData.get("CRIDSP") != null)
		{
			String cridsp = (String) rowData.get("CRIDSP");
			if (cridsp.equals("I") || cridsp.equals("R"))
			{
				rowData.put("TRAN", "Installed by "
						+ ((String) rowData.get("CRUSER")).trim() + " in WU "
						+ ((String) rowData.get("CRMCTL")).trim());
				rowData.put("CHANGE", "YES");
			}
			else if (cridsp.equals("D"))
			{
				rowData.put("TRAN", "Part removed by "
						+ ((String) rowData.get("CRUSER")).trim() + " from WU "
						+ ((String) rowData.get("CRMCTL")).trim());
				rowData.put("CHANGE", "YES");
			}
		}
	}

	/**
	 * Combines the rows that have the same date and time.
	 * @param rows the <code>Vector</code> of row data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void combineRows(Vector rows)
	{
		Vector result = new Vector();
		int flagcount = 0;

		for (int outerIndex = 0; outerIndex < rows.size(); outerIndex++)
		{
			Hashtable rowData = (Hashtable) rows.elementAt(outerIndex);
			if (rowData.containsKey("QD20"))
			{
				if (rowData.containsKey("Q2FGID") && !rowData.containsKey("FLAGCOUNT"))
				{
					flagcount = 1;
					rowData.put("FLAGCOUNT", Integer.toString(flagcount));
					StringBuffer flag_str = new StringBuffer();
					flag_str.append("FLAG: ");
					flag_str.append(rowData.get("Q2FGID"));
					flag_str.append("   VAL: ");
					flag_str.append(rowData.get("Q2FVAL"));
					flag_str.append("   ACTIVE: ");
					flag_str.append(rowData.get("ACTIVE"));
					rowData.put("FLAG" + flagcount, flag_str.toString());
				}
				for (int i = outerIndex + 1; i < rows.size(); i++)
				{
					Hashtable rowData2 = (Hashtable) rows.elementAt(i);
					if (rowData.get("DATE").equals(rowData2.get("DATE"))
							&& rowData.get("TIME").equals(rowData2.get("TIME")))
					{
						rowData.put("TRAN", "Tran");
						if (rowData2.containsKey("USER") && !rowData.containsKey("USER"))
						{
							rowData.put("USER", rowData2.get("USER"));
						}
						if (rowData2.containsKey("SPLT") && !rowData.containsKey("SPLT"))
						{
							rowData.put("SPLT", rowData2.get("SPLT"));
						}
						if (rowData2.containsKey("NMBR") && !rowData.containsKey("NMBR"))
						{
							rowData.put("NMBR", rowData2.get("NMBR"));
						}
						if (rowData2.containsKey("FROMLOC")
								&& !rowData.containsKey("FROMLOC"))
						{
							rowData.put("FROMLOC", rowData2.get("TOLOC"));
						}
						if (rowData2.containsKey("TOLOC")
								&& !rowData.containsKey("TOLOC"))
						{
							rowData.put("TOLOC", rowData2.get("TOLOC"));
						}
						if (rowData2.containsKey("FROMWU")
								&& !rowData.containsKey("FROMWU"))
						{
							rowData.put("FROMWU", rowData2.get("FROMWU"));
						}
						if (rowData2.containsKey("TOWU") && !rowData.containsKey("TOWU"))
						{
							rowData.put("TOWU", rowData2.get("TOWU"));
						}
						if (rowData2.containsKey("NEWPN")
								&& !rowData.containsKey("NEWPN"))
						{
							rowData.put("NEWPN", rowData2.get("NEWPN"));
						}
						if (rowData2.containsKey("NEWSN")
								&& !rowData.containsKey("NEWSN"))
						{
							rowData.put("NEWSN", rowData2.get("NEWSN"));
						}
						if (rowData2.containsKey("OLDPN")
								&& !rowData.containsKey("OLDPN"))
						{
							rowData.put("OLDPN", rowData2.get("OLDPN"));
						}
						if (rowData2.containsKey("OLDSN")
								&& !rowData.containsKey("OLDSN"))
						{
							rowData.put("OLDSN", rowData2.get("OLDSN"));
						}
						if (rowData2.containsKey("CMNT") && !rowData.containsKey("CMNT"))
						{
							rowData.put("CMNT", rowData2.get("CMNT"));
						}
						if (rowData2.containsKey("Q2FGID"))
						{
							if (rowData.containsKey("FLAGCOUNT"))
							{
								flagcount = Integer.parseInt((String) rowData.get("FLAGCOUNT"));
								flagcount += 1;
							}
							else
							{
								flagcount = 1;
							}

							rowData.put("FLAGCOUNT", Integer.toString(flagcount));
							StringBuffer flag_str = new StringBuffer();
							flag_str.append("FLAG: ");
							flag_str.append(rowData2.get("Q2FGID"));
							flag_str.append("   VAL: ");
							flag_str.append(rowData2.get("Q2FVAL"));
							flag_str.append("   ACTIVE: ");
							flag_str.append(rowData2.get("ACTIVE"));
							rowData.put("FLAG" + flagcount, flag_str.toString());
						}

						outerIndex++;
					}
					else
					{
						break;
					}
				} // End For Loop
			} // END if rowData.containsKey("QD20")
			result.add(rowData);
		}

		rows.removeAllElements();
		rows.addAll(result);
	}

	/**
	 * Sets the <code>TableModel</code> data.
	 * @param rows the <code>Vector</code> of row data
	 */
	@SuppressWarnings("rawtypes")
	private void setTableModelData(Vector rows)
	{
		this.data = new String[rows.size()][6];
		int flagcount = 0;

		for (int i = 0; i < rows.size(); i++)
		{
			StringBuffer str1 = new StringBuffer();
			StringBuffer str2 = new StringBuffer();
			StringBuffer str3 = new StringBuffer();
			StringBuffer str4 = new StringBuffer();
			StringBuffer megaStr = new StringBuffer();
			boolean str2_chg = false;
			boolean str3_chg = false;
			boolean str4_chg = false;
			boolean prln_written = false;
			int linecount = 1; //This is for first line which has DATE/TIME
			Hashtable rowData = (Hashtable) rows.elementAt(i);

			if (rowData.containsKey("TRAN"))
			{
				str1.append(rowData.get("TRAN"));
				if (rowData.containsKey("USER"))
				{
					str1.append(" by ");
					str1.append(rowData.get("USER"));
				}
			}
			else if (rowData.containsKey("USER"))
			{
				str1.append("Tran by ");
				str1.append(rowData.get("USER"));
				str1.append("   ");
			}
			else
			{
				str2.append("          ");
			}

			if (rowData.containsKey("SPLT"))
			{
				str2_chg = true;
				str2.append("SAP LOC: ");
				str2.append(rowData.get("SPLT"));
				str2.append("   ");
			}
			if (rowData.containsKey("Q2NMBR"))
			{
				str2_chg = true;
				str2.append("OPER: ");
				str2.append(rowData.get("Q2NMBR"));
				str2.append("   ");
			}
			if (rowData.containsKey("FROMLOC"))
			{
				str2_chg = true;
				str2.append("FROMLOC: ");
				str2.append(rowData.get("FROMLOC"));
				str2.append("   ");
			}
			if (rowData.containsKey("TOLOC"))
			{
				str2_chg = true;
				str2.append("TOLOC: ");
				str2.append(rowData.get("TOLOC"));
				str2.append("   ");
			}
			if (rowData.containsKey("FROMWU"))
			{
				str2_chg = true;
				str2.append("FROMWU: ");
				str2.append(rowData.get("FROMWU"));
				str2.append("   ");
			}
			if (rowData.containsKey("TOWU"))
			{
				str2_chg = true;
				str2.append("TOWU: ");
				str2.append(rowData.get("TOWU"));
				str2.append("   ");
			}
			if (rowData.containsKey("SAPM"))
			{
				str2_chg = true;
				str2.append("PPN: ");
				str2.append(rowData.get("SAPM"));
				str2.append("   ");
			}
			if (rowData.containsKey("SAPN"))
			{
				str2_chg = true;
				str2.append("PSN: ");
				str2.append(rowData.get("SAPN"));
				str2.append("   ");
			}

			if (rowData.containsKey("NEWPN"))
			{
				str3_chg = true;
				str3.append("NEWPN/SN: ");
				str3.append(rowData.get("NEWPN"));
				str3.append("/");
			}
			if (rowData.containsKey("NEWSN"))
			{
				str3_chg = true;
				str3.append(rowData.get("NEWSN"));
				str3.append("   ");
			}
			if (rowData.containsKey("OLDPN"))
			{
				str3_chg = true;
				str3.append("\nOLDPN/SN: ");
				str3.append(rowData.get("OLDPN"));
				str3.append("/");
			}
			if (rowData.containsKey("OLDSN"))
			{
				str3_chg = true;
				str3.append(rowData.get("OLDSN"));
				str3.append("   ");
			}

			if (rowData.containsKey("CMNT"))
			{
				str4_chg = true;
				str4.append("COMMENT: ");
				str4.append(rowData.get("CMNT"));
			}

			megaStr.append(str1.toString());
			megaStr.append("\n");

			if (str2_chg)
			{
				linecount++;
				megaStr.append(str2.toString());
				megaStr.append("\n");
			}
			if (str3_chg)
			{
				linecount++;
				megaStr.append(str3);
				megaStr.append("\n");
			}
			if (str4_chg)
			{
				linecount++;
				megaStr.append(str4);
				megaStr.append("\n");
			}

			if (rowData.containsKey("FLAGCOUNT"))
			{
				flagcount = Integer.parseInt((String) rowData.get("FLAGCOUNT"));

				/*
				 * Keep track of the greatest number of flags. This will be used
				 * later to determine row height.
				 */
				if (flagcount > this.maxFlags)
				{
					this.maxFlags = flagcount;
				}

				for (int j = 1; j <= flagcount; j++)
				{
					if (j == 1 && (str2_chg == false && str3_chg == false && str4_chg == false))
					{
						megaStr.append(rowData.get("FLAG" + j));
						megaStr.append("\n");
					}
					else if ((j == 1 || j == 2) && str3_chg == false && str4_chg == false && prln_written == false)
					{
						prln_written = true;
						megaStr.append(rowData.get("FLAG" + j));
						megaStr.append("\n");
					}
					else
					{
						megaStr.append(rowData.get("FLAG" + j));
						megaStr.append("\n");
					}
				}
			}

			// Populate data[][] here...
			String malc = "";
			String milc = "";
			String prln = "";
			if (rowData.get("Q2MALC") != null)
			{
				malc = (String) rowData.get("Q2MALC");
			}
			if (rowData.get("Q2MILC") != null)
			{
				milc = (String) rowData.get("Q2MILC");
			}
			if (rowData.get("Q2PRLN") != null)
			{
				prln = (String) rowData.get("Q2PRLN");
			}

			this.data[i][0] = (String) rowData.get("DATE");
			this.data[i][1] = (String) rowData.get("TIME");
			this.data[i][2] = (String) rowData.get("Q2CTLV");
			this.data[i][3] = malc + "\n" + milc + "\n" + prln;

			if (getColumnCount() == COL_COUNT)
			{
				this.data[i][4] = (String) rowData.get("ACTIVE");
				this.data[i][5] = megaStr.toString();
			}
			else
			{
				this.data[i][4] = megaStr.toString();
				this.data[i][5] = (String) rowData.get("ACTIVE");
			}
		}
	}

	/**
	 * Returns the maximum number of flags.
	 * @return the maximum number of flags
	 */
	public int getMaxFlags()
	{
		return this.maxFlags;
	}
}
