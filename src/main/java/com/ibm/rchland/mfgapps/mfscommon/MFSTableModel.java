/* @ Copyright IBM Corporation 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-05-02      38139JM  R Prechel        -Initial version
 * 2007-05-16   ~1 38716JM  R Prechel        -Removed check against empty String in getValueAt
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * <code>MFSTableModel</code> is a <code>TableModel</code> used by the MFS
 * Client and MFS Print Server.
 * @author The MFS Client Development Team
 */
public class MFSTableModel
	extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	/** The column names for this <code>TableModel</code>. */
	protected String[] headers = {};

	/**
	 * The data for this <code>TableModel</code>. The first index is the row
	 * index and the second index is the column index.
	 */
	protected Object[][] data = {{}};

	/** <code>true</code> if the last column contains page break information. */
	private boolean hasPageBreakColumn = false;

	/**
	 * Constructs a new <code>MFSTableModel</code>.
	 * @param hasPageBreakColumn <code>true</code> if the last column contains
	 *        page break information
	 */
	public MFSTableModel(boolean hasPageBreakColumn)
	{
		super();
		this.hasPageBreakColumn = hasPageBreakColumn;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int columnIndex)
	{
		return this.data[0][columnIndex].getClass();
	}
	
	/** {@inheritDoc} */
	public int getColumnCount()
	{
		return this.headers.length;
	}

	/** {@inheritDoc} */
	public String getColumnName(int column)
	{
		return this.headers[column];
	}

	/** {@inheritDoc} */
	public int getRowCount()
	{
		return this.data.length;
	}

	/**
	 * Returns the headings for the table (i.e., the column names).
	 * @return the headings for the table
	 */
	public String[] getTableHeadings()
	{
		return this.headers;
	}

	/** {@inheritDoc} */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		//~1D Removed check against empty String
		return this.data[rowIndex][columnIndex];
	}

	/**
	 * Returns <code>true</code> if the page break information for the
	 * specified <code>row</code> indicates a page break should occur.
	 * @param row the row index
	 * @return <code>true</code> if the page break information for
	 *         <code>row</code> indicates a page break should occur
	 */
	public boolean pageBreak(int row)
	{
		return this.hasPageBreakColumn
				&& Boolean.TRUE.equals(this.data[row][this.headers.length]);
	}

	/**
	 * Sets the table data of this <code>TableModel</code>.
	 * @param tableData the <code>Vector</code> of table data. Each element of
	 *        the <code>Vector</code> should contain the data for a row stored
	 *        in an array of <code>Object</code>s.
	 */
	@SuppressWarnings("rawtypes")
	public void setData(Vector tableData)
	{
		int colCount = (this.hasPageBreakColumn
				? this.headers.length + 1
				: this.headers.length);
		this.data = new Object[tableData.size()][colCount];
		tableData.copyInto(this.data);
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Sets the headings for the table (i.e., the column names).
	 * @param headers the headings for the table
	 */
	public void setTableHeadings(String[] headers)
	{
		this.headers = headers;
	}
}
