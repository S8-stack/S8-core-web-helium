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





	public final String name;


	/**
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * <p>
	 * <b>Important notice</b>: ByteBuffer buffer (as retrieved by
	 * <code>getNetworkBuffer()</code> method) is passed in write mode state.
	 * </p>
	 *
	 * @param networkBuffer the network buffer
	 * @param resizer a handler to trigger resizing
	 * @throws IOException 
	 */
	protected abstract void rx_onReceived() throws IOException;


	/**
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * @throws IOException
	 */
	protected abstract void rx_onRemotelyClosed() throws IOException;


	/**
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * @param exception
	 * @throws IOException
	 */
	protected abstract void rx_onReceptionFailed(IOException exception) throws IOException;

	/**
	 * the channel
	 */
	SocketChannel socketChannel;




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

	
		this.Rx_isVerbose = configuration.isRxVerbose;
	}


	public abstract RxConnection getConnection();



	/**
	 * 
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * Compares <code>sessionProposedCapacity<code> with buffer's capacity. If buffer's capacity is smaller,
	 * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
	 * with capacity twice the size of the initial one.
	 *
	 * @param buffer - the buffer to be enlarged.
	 * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
	 * @return A new buffer with a larger capacity.
	 */
	protected void rxIncreaseNetworkBufferCapacity(int sessionProposedCapacity) {

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
	public void rxBind(RxConnection connection) {
		this.socketChannel = connection.getSocketChannel();
	}


	/**
	 * 
	 * @param capacity
	 */
	public void rxInitializeNetworkBuffer(int capacity) {
		// set buffer so that first compact left it ready for writing
		networkBuffer = ByteBuffer.allocate(capacity);
		networkBuffer.position(0);
		networkBuffer.limit(0);
	}




	/**
	 * Thread safe
	 * 
	 * @throws IOException 
	 */
	public void receive() {
		getConnection().receive();
	}


	/**
	 * 
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * @return false if inbound has not terminated during read attempt, true otherwise
	 * @throws IOException 
	 */
	void read() throws IOException {

		try {
			if(getConnection().hasNeed(Need.RECEIVE)) {

				/* buffer WRITE_MODE start of section */
				// optimize inbound buffer free space
				networkBuffer.compact();

				// read
				nBytes = socketChannel.read(networkBuffer);

				if(nBytes==-1) {
					rx_onRemotelyClosed();
				}

				// flip
				networkBuffer.flip();
				/* buffer WRITE_MODE end of section */

				// trigger callback function with buffer ready for reading
				if(nBytes > 0) { 

					/* clear need, wait for upper layer ot decide if we need more recieve */
					getConnection().clearNeed(Need.RECEIVE);

					/* transmit to upper layer*/
					rx_onReceived(); 
				}

			}
		}
		catch(IOException exception) {

			if(Rx_isVerbose) {
				System.out.println("[RxInbound] read encounters an exception: "+exception.getMessage());
				System.out.println("\t --> require SHUT_DOWN");
			}

			rx_onReceptionFailed(exception);

			/* exception -> requires shut-down */
			getConnection().clearNeed(Need.SHUT_DOWN);
		}
	};




	public int getBytecount() {
		return nBytes;
	}


}
