/* © Copyright IBM Corporation 2007, 2010. All rights reserved.
 * Dates should follow the ISO 8601 standard YYYY-MM-DD
 * The @ symbol has a predefined meaning in Java.
 * Thus, Flags should not start with the @ symbol.  Please use ~ instead.
 *
 * Date       Flag IPSR/PTR Name             Details
 * ---------- ---- -------- ---------------- ----------------------------------
 * 2007-02-22      34242JR  R Prechel        -Initial version
 * 2007-05-23   ~1 37676JM  R Prechel        -Perform network measurement logging
 *                                           -Allow creation of multiple MFSComm objects
 * 2007-11-02   ~2 40104PB  R Prechel        -Constant name change. String variable for debugging.
 * 2008-02-07   ~3 30635SE  R Prechel        -Add support for IGSXMLTransaction.
 *              ~3 40845MZ                   (Checked in using MFSCOMFUNC feature)
 *              ~4 47595MZ  Ray Perry        -Shenzhen efficiency new method to thread calls
 ******************************************************************************/
package com.ibm.rchland.mfgapps.mfscommon.comm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.ibm.rchland.mfgapps.client.utils.xml.IGSXMLTransaction;
import com.ibm.rchland.mfgapps.mfscommon.MFSActionable;
import com.ibm.rchland.mfgapps.mfscommon.MFSConfig;
import com.ibm.rchland.mfgapps.mfscommon.exception.MFSCommException;

/**
 * <code>MFSComm</code> sends a transaction's input to the server and receives
 * the transaction's output and return code from the server.
 * @author The MFS Client Development Team
 */
public class MFSComm
	implements Runnable
{
	/** The general communication error message. */
	public static final String COMM_ERROR = "Communication Error. Please contact support.";

	/** The error message displayed when no transaction output data was sent. */
	public static final String NO_DATA_SENT = "Communication Error: No data was sent. Please contact support.";

	/** The error code returned when a communication error occurs. */
	public static final int COMM_ERROR_CODE = -10;

	/** The return code indicating the end of a transaction's output. */
	public static final int END_OF_TRANSMISSION = 9999;

	/** Used to create unique MFSComm Thread names. */
	private static int threadNumber = 0; //~3A

	/** The shared instance of <code>MFSComm</code>. */
	private static final MFSComm INSTANCE = startComm();

	/** The lock <code>Object</code> for synchronization. */
	private final Object lock = new Object();

	/** Encapsulates a transaction's input, output, and return code. */
	private MFSTransaction transaction = null;

	/** Encapsulates a transaction's input, output, and return code. */
	private IGSXMLTransaction xmlTransaction = null;

	/**
	 * Returns the shared instance of <code>MFSComm</code>.
	 * @return the shared instance of <code>MFSComm</code>
	 */
	public static MFSComm getInstance()
	{
		return INSTANCE;
	}

	//~1C Made public
	/**
	 * Creates a new <code>MFSComm</code> and starts a transaction
	 * <code>Thread</code> for the <code>MFSComm</code>.
	 * @return the new <code>MFSComm</code>
	 */
	public static MFSComm startComm()
	{
		MFSComm result = new MFSComm();
		//~3C Name thread and set priority
		Thread thread = new Thread(result, "MFSComm-" + threadNumber++); //$NON-NLS-1$
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
		return result;
	}

	//~1C Made public
	/** Constructs a new <code>MFSComm</code>. */
	public MFSComm()
	{
		super();
	}

	/**
	 * Executes transactions posted to the transaction <code>Thread</code>.
	 * Uses synchronization and the {@link Object#wait() wait} method to avoid
	 * busy waiting.
	 */
	public void run()
	{
		while (true)
		{
			synchronized (this.lock)
			{
				// Block until a transaction is posted
				//~3C Check xmlTransaction
				while (this.transaction == null && this.xmlTransaction == null)
				{
					try
					{
						this.lock.wait();
					}
					catch (InterruptedException ie)
					{
						// Nothing to do
					}
				}

				//~3C Check if transaction or xmlTransaction
				if (this.transaction != null)
				{
					//~3C Execute transaction
					execute(this.transaction);
					this.transaction = null;
				}
				else
				{
					//~3A Execute xmlTransaction
					execute(this.xmlTransaction);
					this.xmlTransaction = null;
				}
				this.lock.notifyAll();
			}
		}
	}

	//~3A New method
	/**
	 * Executes a transaction and blocks until the transaction is finished. The
	 * transaction is executed on the transaction thread and the specified
	 * <code>MFSActionable</code> is updated to indicate the transaction
	 * action message.
	 * <p>
	 * Only one transaction can execute on the transaction thread at a time. If
	 * a transaction is running when this method is invoked, this method will
	 * block until the previous transaction is finished before executing the
	 * specified <code>IGSXMLTransaction</code>.
	 * @param t the <code>IGSXMLTransaction</code> to execute
	 * @param actionable the <code>MFSActionable</code> that indicates the
	 *        transaction is executing
	 */
	public void execute(IGSXMLTransaction t, MFSActionable actionable)
	{
		final String message = t.getActionMessage();
		actionable.startAction(message);

		// Post the transaction to the transaction thread
		synchronized (this.lock)
		{
			// Block if a transaction is running
			while (this.transaction != null || this.xmlTransaction != null)
			{
				try
				{
					this.lock.wait();
				}
				catch (InterruptedException ie)
				{
					// Nothing to do
				}
			}

			// Run the transaction on the transaction Thread
			this.xmlTransaction = t;
			this.lock.notifyAll();
		}

		// Wait until transaction is done
		while (this.xmlTransaction == t)
		{
			actionable.updateAction(message, -1);
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ie)
			{
				// Nothing to do
			}
		}

		actionable.stopAction();
	}

	/**
	 * Executes a transaction and blocks until the transaction is finished. The
	 * transaction is executed on the transaction thread and the specified
	 * <code>MFSActionable</code> is updated to indicate the transaction
	 * action message.
	 * <p>
	 * Only one transaction can execute on the transaction thread at a time. If 
	 * a transaction is running when this method is invoked, this method will
	 * block until the previous transaction is finished before executing the
	 * specified <code>MFSTransaction</code>.
	 * @param t the <code>MFSTransaction</code> to execute
	 * @param actionable the <code>MFSActionable</code> that indicates the
	 *        transaction is executing
	 */
	public void execute(MFSTransaction t, MFSActionable actionable)
	{
		final String message = t.getActionMessage();
		actionable.startAction(message);

		// Post the transaction to the transaction thread
		synchronized (this.lock)
		{
			// Block if a transaction is running
			//~3A Check xmlTransaction
			while (this.transaction != null || this.xmlTransaction != null)
			{
				try
				{
					this.lock.wait();
				}
				catch (InterruptedException ie)
				{
					// Nothing to do
				}
			}

			// Run the transaction on the transaction Thread
			this.transaction = t;
			this.lock.notifyAll();
		}

		// Wait until transaction is done
		while (this.transaction == t)
		{
			actionable.updateAction(message, -1);
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ie)
			{
				// Nothing to do
			}
		}

		actionable.stopAction();
	}

	//~3A New method
	/**
	 * Executes a transaction and blocks until the transaction is finished. The
	 * transaction is executed on the calling thread.
	 * @param t the <code>IGSXMLTransaction</code> to execute
	 */
	public static void execute(IGSXMLTransaction t)
	{
		MFSCommLogInfo logInfo = MFSCommLogger.createLogInfo();
		logInfo.startTransaction(t.getTransactionName());
		t.run();
		logInfo.endTransaction();
	}

	//~3A Made static and changed to execute the transaction
	/**
	 * Executes a transaction and blocks until the transaction is finished. The
	 * transaction is executed on the calling thread.
	 * @param t the <code>MFSTransaction</code> to execute
	 */
	public static void execute(MFSTransaction t)
	{
		Socket socket = null;
		DataInputStream dataIn = null;
		DataOutputStream dataOut = null;
		try
		{
			//~1A Perform network measurement logging
			MFSCommLogInfo logInfo = MFSCommLogger.createLogInfo();
			logInfo.startTransaction(t.getTransactionName());

			//~2C MFSConfig.SERVER and MFSConfig.PORT changed
			//to MFSConfig.MFSSRV and MFSConfig.MFSRTR
			String host = MFSConfig.getInstance().getConfigValue(MFSConfig.MFSSRV);
			String port = MFSConfig.getInstance().getConfigValue(MFSConfig.MFSRTR);
			socket = new Socket(host, Integer.parseInt(port));
			InputStream in = new BufferedInputStream(socket.getInputStream());
			dataIn = new DataInputStream(in);
			OutputStream out = new BufferedOutputStream(socket.getOutputStream());
			dataOut = new DataOutputStream(out);

			//Send the transaction's input.
			IGSXMLTransaction.writeData(dataOut, t.startTransaction());
			IGSXMLTransaction.writeData(dataOut, "         0" + t.getInput()); //$NON-NLS-1$

			//Receive the transaction's output
			receive(t, dataIn);

			//~1A End network measurement logging
			logInfo.endTransaction();
		}
		catch (MFSCommException ce)
		{
			t.setError(ce.getMessage(), COMM_ERROR_CODE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			t.setError(COMM_ERROR, COMM_ERROR_CODE);
		}

		IGSXMLTransaction.closeConnection(socket, dataIn, dataOut);
	}

	//~4
	/**
	 * Executes a transaction and blocks until the transaction is finished. The
	 * transaction is executed on the calling thread.
	 * @param t the <code>MFSTransaction</code> to execute
	 * @param threaded this parm doesn't matter 
	 */
	public static void execute(final IGSXMLTransaction t, boolean threaded)
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				MFSCommLogInfo logInfo = MFSCommLogger.createLogInfo();
				logInfo.startTransaction(t.getTransactionName());
				t.run();
				logInfo.endTransaction();
			}
		};

		/* Spawn thread */
		thread.start();
	}

	/**
	 * Executes the <code>MFSPipedTransaction</code> on the transaction
	 * thread. This method returns once the <code>MFSPipedTransaction</code>
	 * is on the transaction thread.
	 * <p>
	 * Only one transaction can execute on the transaction thread at a time. If
	 * a transaction is running when this method is invoked, this method will
	 * block until the previous transaction is finished before executing the
	 * specified <code>MFSPipedTransaction</code>.
	 * @param t the <code>MFSPipedTransaction</code> to execute
	 */
	public void execute(MFSPipedTransaction t)
	{
		//Post the transaction to the transaction thread
		synchronized (this.lock)
		{
			// Block if a transaction is running
			//~3A Check xmlTransaction
			while (this.transaction != null || this.xmlTransaction != null)
			{
				try
				{
					this.lock.wait();
				}
				catch (InterruptedException ie)
				{
					// Nothing to do
				}
			}

			// Run the transaction on the transaction Thread
			this.transaction = t;
			this.lock.notifyAll();
		}
	}

	//~3A Made static
	/**
	 * Receives the transaction's output and return code.
	 * @param t the <code>MFSTransaction</code>
	 * @param dataIn the <code>DataInputStream</code> from which to read
	 * @throws MFSCommException if a communications error occurs
	 * @throws IOException if an I/O error occurs
	 */
	private static void receive(MFSTransaction t, DataInputStream dataIn)
		throws MFSCommException, IOException
	{
		// Return Code and Previous Return Code
		int rc = 0, previousRC = 0;
		// Read the first buffer of data from the socket
		String data = IGSXMLTransaction.readData(dataIn);

		// The end of transmission return code is sent in its own buffer.
		// An end of transmission return code in the first buffer is an error.
		if ((rc = t.parseReturnCode(data)) == END_OF_TRANSMISSION)
		{
			throw new MFSCommException(NO_DATA_SENT);
		}

		// Process the data and read the next buffer until
		// the end of transmission return code is received.
		do
		{
			previousRC = rc;
			t.processBuffer(data);
			data = IGSXMLTransaction.readData(dataIn);
			rc = t.parseReturnCode(data);
		}
		while (rc != END_OF_TRANSMISSION);

		// Indicate the transaction is complete
		t.endTransaction(previousRC);
	}
}
