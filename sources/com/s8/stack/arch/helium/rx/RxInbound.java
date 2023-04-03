package com.s8.stack.arch.helium.rx;

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
	public abstract void onRxReceived(ByteBuffer networkBuffer, NetworkBufferResizer resizer) throws IOException;


	public abstract void onRxRemotelyClosed(ByteBuffer networkBuffer) throws IOException;


	public abstract void onRxReceptionFailed(ByteBuffer networkBuffer, IOException exception) throws IOException;

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



	public ByteBuffer networkBuffer;

	private int nBytes;

	
	/**
	 * Settings
	 */
	public final boolean Rx_isVerbose;

	private NetworkBufferResizer resizer = new NetworkBufferResizer() {

		@Override
		public ByteBuffer resizeNetworkBuffer(int capacity) {
			return (networkBuffer = ByteBuffer.allocate(capacity));
		}		
	};




	public RxInbound(int capacity, RxWebConfiguration configuration) {
		super();
		need = Need.NONE;
		networkBuffer = ByteBuffer.allocate(capacity);
		this.Rx_isVerbose = configuration.isRxVerbose;

		// set buffer so that first compact left it ready for writing
		networkBuffer.position(0);
		networkBuffer.limit(0);
	}

	
	public abstract RxConnection getConnection();

	
	
	
	/**
	 * 
	 * @param connection
	 */
	public void Rx_bind(RxConnection connection) {
		this.socketChannel = connection.getSocketChannel();
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
				
				// no opinion on what to do next
				need = Need.NONE;
			
				/* buffer WRITE_MODE start of section */
				// optimize inbound buffer free space
				networkBuffer.compact();

				// read
				nBytes = socketChannel.read(networkBuffer);

				if(nBytes==-1) {
					onRxRemotelyClosed(networkBuffer);
				}

				// flip
				networkBuffer.flip();
				/* buffer WRITE_MODE end of section */

				// trigger callback function with buffer ready for reading
				onRxReceived(networkBuffer, resizer);
			}
		}
		catch(IOException exception) {

			if(Rx_isVerbose) {
				System.out.println("[RxInbound] read encounters an exception: "+exception.getMessage());
				System.out.println("\t --> require SHUT_DOWN");
			}

			onRxReceptionFailed(networkBuffer, exception);

			need = Need.SHUT_DOWN;
		}
		
		return need;
	};




	public int getBytecount() {
		return nBytes;
	}


}
