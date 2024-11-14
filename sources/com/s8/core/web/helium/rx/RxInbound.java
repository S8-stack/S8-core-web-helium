package com.s8.core.web.helium.rx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * <h1>IOReactive</h1>
 * <p>
 * Based on the "don't call us, we'll call you" principle. 
 * Namely, use this class by overriding this method and supply 
 * bytes when required.
 * </p>
 * <p>
 * Note that is the responsibility of the application to flip/clear/compact buffer
 * </p>
 * @author pc
 *
 */
public abstract class RxInbound {



	public enum Need {

		NONE, RECEIVE, SHUT_DOWN;

	}


	public final String name;



	/**
	 * /**
	 * <p>
	 * <b>Important notice</b>: ByteBuffer buffer (as retrieved by
	 * <code>getNetworkBuffer()</code> method) is passed in write mode state.
	 * </p>
	 *
	 * @param networkBuffer the network buffer
	 * @param resizer a handler to trigger resizing
	 * @throws IOException 
	 */
	public abstract void onRxReceived() throws IOException;


	/**
	 * 
	 * @throws IOException
	 */
	public abstract void onRxRemotelyClosed() throws IOException;


	public abstract void onRxReceptionFailed(IOException exception) throws IOException;

	/**
	 * the channel
	 */
	SocketChannel socketChannel;

	/**
	 * trigger receiving
	 * <p>
	 * <b>Note</b>: The specifications of these methods enable implementations to employ
	 * efficient machine-level atomic instructions that are available on
	 * contemporary processors. However on some platforms, support may entail some
	 * form of internal locking. Thus the methods are not strictly guaranteed to be
	 * non-blocking -- a thread may block transiently before performing the
	 * operation.
	 * </p>
	 * <p>Cycle is the following:</p>
	 * <ul>
	 * <li>Set to <code>true</code> by <code>receive()</code> method</li>
	 * <li>Reset or continued when calling <code>onReceived()</code> method, using output flag</li>
	 * </ul>
	 */
	private Need need;



	/**
	 * MUST be left in READ mode.
	 */
	public ByteBuffer networkBuffer;

	private int nBytes;


	/**
	 * Settings
	 */
	public final boolean Rx_isVerbose;




	public RxInbound(String name, RxWebConfiguration configuration) {
		super();
		this.name = name + ".inbound";
		need = Need.NONE;

		this.Rx_isVerbose = configuration.isRxVerbose;


	}


	public abstract RxConnection getConnection();



	/**
	 * Compares <code>sessionProposedCapacity<code> with buffer's capacity. If buffer's capacity is smaller,
	 * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
	 * with capacity twice the size of the initial one.
	 *
	 * @param buffer - the buffer to be enlarged.
	 * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
	 * @return A new buffer with a larger capacity.
	 */
	public void increaseNetworkBufferCapacity(int sessionProposedCapacity) {

		/* allocate new buffer */
		ByteBuffer extendedBuffer = ByteBuffer.allocate(
				sessionProposedCapacity > networkBuffer.capacity() ? sessionProposedCapacity : 
					networkBuffer.capacity() * 2);

		/* copy remaining content */
		extendedBuffer.put(networkBuffer);

		/* replace */
		networkBuffer = extendedBuffer;

		/* network buffer is now in READ mode */
		networkBuffer.flip();
	}


	/**
	 * 
	 * @param connection
	 */
	public void Rx_bind(RxConnection connection) {
		this.socketChannel = connection.getSocketChannel();


	}


	/**
	 * 
	 * @param capacity
	 */
	public void initializeNetworkBuffer(int capacity) {
		// set buffer so that first compact left it ready for writing
		networkBuffer = ByteBuffer.allocate(capacity);
		networkBuffer.position(0);
		networkBuffer.limit(0);
	}




	public Need getState() {
		return need;
	}






	/**
	 * @throws IOException 
	 */
	public void receive() {



		/* <DEBUG> 

		System.out.println(">>Receive asked (previously: "+need+")");
		if(need==Need.SHUT_DOWN) {
			throw new RuntimeException("Cannot ask for sending once shut down has been requested");
		}
		</DEBUG> */

		// update flag
		need = Need.RECEIVE;


		getConnection().pullInterestOps();

		// notify selector
		getConnection().wakeup();
	}


	/**
	 * 
	 * @return false if inbound has not terminated during read attempt, true otherwise
	 * @throws IOException 
	 */
	public Need read() throws IOException {

		try {

			if(need==Need.RECEIVE) {



				/* buffer WRITE_MODE start of section */
				// optimize inbound buffer free space
				networkBuffer.compact();

				// read
				nBytes = socketChannel.read(networkBuffer);

				if(nBytes==-1) {
					onRxRemotelyClosed();
				}

				// flip
				networkBuffer.flip();
				/* buffer WRITE_MODE end of section */

				// trigger callback function with buffer ready for reading
				if(nBytes > 0) { 

					/* clear need, wait for upper layer ot decide if we need more recieve */
					need = Need.NONE;

					/* transmit to upper layer*/
					onRxReceived(); 
				}
			}
		}
		catch(IOException exception) {

			if(Rx_isVerbose) {
				System.out.println("[RxInbound] read encounters an exception: "+exception.getMessage());
				System.out.println("\t --> require SHUT_DOWN");
			}

			onRxReceptionFailed(exception);

			need = Need.SHUT_DOWN;
		}

		return need;
	};




	public int getBytecount() {
		return nBytes;
	}


}
