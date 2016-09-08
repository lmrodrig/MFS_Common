/* © Copyright IBM Corporation 2005, 2007. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-01-15   ~1 34242JR  R Prechel        -Java 5 version
 *                                           -Used MFSConfig instead of Configuration
 ******************************************************************************/
package common;

import java.io.IOException;
import java.util.Vector;

import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;

/**
 * Insert the type's description here. Creation date: (8/20/2003 1:19:13 PM)
 * @author: David Fichtinger
 */
public class DasdComm
	extends Comm
{
	private byte[] fieldByteData = null;

	/** Constructs a new <code>DasdComm</code>. */
	public DasdComm()
	{
		super();
	}

	/**
	 * Constructs a new <code>DasdComm</code>.
	 * @param config the <code>MFSConfig</code>
	 * @param input the transaction input
	 */
	public DasdComm(MFSConfig config, String input) //~1C
	{
		super(config, input);
	}

	/**
	 * Constructs a new <code>DasdComm</code>.
	 * @param input the transaction input
	 */
	public DasdComm(String input)
	{
		super(input);
	}

	/**
	 * Gets the byteData property (byte[]) value.
	 * @return The byteData property value.
	 * @see #setByteData
	 */
	public byte[] getByteData()
	{
		return this.fieldByteData;
	}

	/**
	 * This method was created in VisualAge.
	 * @return java.lang.String
	 * @exception java.io.IOException The exception description.
	 */
	private byte[] readNetData()
		throws IOException
	{
		/* first read the length of the data */
		byte[] bLen = new byte[10];
		getSin().readFully(bLen, 0, 10);
		String sLen = new String(bLen);
		int len = Integer.parseInt(sLen.trim());

		/* read the data */
		byte[] bData = new byte[len];
		getSin().readFully(bData, 0, len);

		return bData;
	}

	/**
	 * Perform the recvData method.
	 * @return int
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int recvData()
	{
		/* Perform the recvData method. */

		int rc = 0;
		int prev_rc = 0;
		StringBuffer buf = new StringBuffer();
		byte curByteBuf[];
		byte tmpByteBuf[];
		Vector mrVector = new Vector();
		boolean xml = false;

		try
		{
			tmpByteBuf = readNetData();

			//still want the string version of the data around, so convert
			//to string
			String netData = new String(tmpByteBuf);

			//define array of length = to that of (tmpbuf size - 10)
			//need to exclude 10 chars of length definition
			curByteBuf = new byte[tmpByteBuf.length - 10];

			for (int i = 0; i < curByteBuf.length; i++)
				curByteBuf[i] = tmpByteBuf[i + 10];

			mrVector.add(curByteBuf);

			if (netData.substring(0, 1).equals("<"))
			{
				xml = true;
				mfsxml.MfsXMLParser xmlParser = new mfsxml.MfsXMLParser(netData);
				rc = Integer.parseInt(xmlParser.getField("RRET").trim());
				buf.append(netData);
			}
			else
			{
				rc = Integer.parseInt(netData.substring(0, 10).trim());
				buf.append(netData.substring(10));
			}

			//if first rc = 9999, no data was sent ? Error out
			if (rc == 9999)
			{
				if (xml)
					setData(new String(
							"<ERMS>Communication Error? No data was sent!! Contact Support.</ERMS>"));
				else
					setData(new String(
							"Communication Error? No data was sent!! Contact Support."));
				rc = -10;
			}
			else
			{
				while (rc != 9999)
				{
					prev_rc = rc;

					tmpByteBuf = readNetData();

					netData = new String(tmpByteBuf);
					if (xml)
					{
						mfsxml.MfsXMLParser xmlParser = new mfsxml.MfsXMLParser(netData);
						rc = Integer.parseInt(xmlParser.getField("RRET").trim());
						buf.append(netData);
					}
					else
					{
						rc = Integer.parseInt(netData.substring(0, 10).trim());
						buf.append(netData.substring(10));
					}
					if (rc != 9999)
					{
						//define array of length = to that of (tmpbuf size -
						// 10)
						//need to exclude 10 chars of length definition
						curByteBuf = new byte[tmpByteBuf.length - 10];

						for (int i = 0; i < curByteBuf.length; i++)
							curByteBuf[i] = tmpByteBuf[i + 10];

						mrVector.add(curByteBuf);
					}

				}

				if (rc == 9999) /* end of data */
				{
					rc = prev_rc;
				}

				setData(new String(buf.toString()));

				int full_length = 0;
				int runner = 0;
				for (int i = 0; i < mrVector.size(); i++)
				{
					byte[] tmp = (byte[]) (mrVector.elementAt(i));
					full_length += tmp.length;
				}

				byte worker[] = new byte[full_length];

				for (int i = 0; i < mrVector.size(); i++)
				{
					byte[] tmp = (byte[]) (mrVector.elementAt(i));
					for (int j = 0; j < tmp.length; j++)
					{
						worker[runner] = tmp[j];
						runner++;
					}
				}
				setByteData(worker);
			}

		}

		catch (Exception e)
		{
			setData(new String(
					"Communication Error has Occurred within recvData(), Contact Support."));
			System.out
					.println("Communication Error has Occurred within recvData(). \nError = "
							+ e.toString());
			rc = -10;
		}

		return rc;
	}

	/**
	 * Sets the byteData property (byte[]) value.
	 * @param byteData The new value for the property.
	 * @see #getByteData
	 */
	public void setByteData(byte[] byteData)
	{
		byte[] oldValue = this.fieldByteData;
		this.fieldByteData = byteData;
		firePropertyChange("byteData", oldValue, byteData);
	}
}
