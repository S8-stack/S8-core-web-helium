package com.s8.stack.arch.helium.http2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.s8.stack.arch.helium.http2.frames.HTTP2_Frame;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Context;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Decoder;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Encoder;
import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;
import com.s8.stack.arch.helium.http2.settings.HTTP2_Settings;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
import com.s8.stack.arch.helium.http2.streams.HTTP2_StreamMapping;
import com.s8.stack.arch.helium.ssl.SSL_Connection;


/**
 * 
 * @author pc
 *
 */
public abstract class HTTP2_Connection extends SSL_Connection {

	private HPACK_Context HPACK_context;

	private HTTP2_Inbound inbound;

	private HTTP2_Outbound outbound;


	/**
	 * connection level error
	 */
	public HTTP2_Error error = HTTP2_Error.NO_ERROR;

	private HTTP2_StreamMapping streamMapping;

	public HTTP2_Settings settings = new HTTP2_Settings();

	private HPACK_Decoder decoder;

	private HPACK_Encoder encoder;

	private boolean isVerbose;

	public HTTP2_Connection(SocketChannel socketChannel, HTTP2_WebConfiguration configuration) throws IOException{
		super(socketChannel);
		inbound = new HTTP2_Inbound(this, configuration);
		outbound = new HTTP2_Outbound(this, configuration);
	}


	@Override
	public abstract HTTP2_Endpoint getEndpoint();

	
	public void HTTP2_initialize(HTTP2_WebConfiguration config) throws IOException {


		// initialize rx and SSL
		// --> bind inbound and outbound
		SSL_initialize(config);

		isVerbose = config.isHTTP2Verbose;

		// save context
		HPACK_context = getEndpoint().HPACK_getContext();

		decoder = new HPACK_Decoder(
				HPACK_context, 
				settings.headerTableSize, 
				config.isHPACKVerbose);

		encoder = new HPACK_Encoder(
				HPACK_context, 
				settings.headerTableSize, 
				config.isHuffmanEncoding, 
				config.isHPACKVerbose);

		streamMapping = new HTTP2_StreamMapping(this, config.isHTTP2Verbose);

	
		/* bind HTTP2_level of inbound/outbound */
		inbound.HTTP2_bind(this);
		outbound.HTTP2_bind(this);		
		
		/* Initialize connection by sending SETTINGS Frame */
		outbound.push(settings.getFrame());
		
	}



	@Override
	public HTTP2_Inbound getInbound() {
		return inbound;
	}

	@Override
	public HTTP2_Outbound getOutbound() {
		return outbound;
	}



	public HPACK_Encoder getEncoder() {
		return encoder;
	}

	public HPACK_Decoder getDecoder() {
		return decoder;
	}

	public HTTP2_Settings getSettings() {
		return settings;
	}

	public boolean parse(ByteBuffer buffer) {
		return false;
	}

	public HTTP2_Stream getStream(int identifier) {
		return streamMapping.get(identifier);
	}



	/**
	 *  <h1>Connection Error Handling</h1>
	 *  <p>
	 *  A connection error is any error that prevents further processing of 
	 *  the frame layer or corrupts any connection state.
	 *  </p>
	 *  <p>
	 *  An endpoint that encounters a connection error SHOULD first send a 
	 *  GOAWAY frame (Section 6.8) with the stream identifier of the last
	 *  stream that it successfully received from its peer. The GOAWAY frame 
	 *  includes an error code that indicates why the connection is 
	 *  terminating. <b>After sending the GOAWAY frame for an error condition, 
	 *  the endpoint MUST close the TCP connection.</b>
	 *  </p>
	 *  <p>
	 *  It is possible that the GOAWAY will not be reliably received by the 
	 *  receiving endpoint ([RFC7230], Section 6.6 describes how an immediate 
	 *  connection close can result in data loss). In the event of a connection 
	 *  error, GOAWAY only provides a best-effort attempt to communicate with 
	 *  the peer about why the connection is being terminated.
	 *  </p>
	 *  <p>
	 *  An endpoint can end a connection at any time. In particular, an endpoint 
	 *  MAY choose to treat a stream error as a connection error. Endpoints SHOULD 
	 *  send a GOAWAY frame when ending a connection, providing that circumstances 
	 *  permit it.
	 *  </p>
	 * @param error
	 * @return a flag indicating if the I/O is stopped
	 */
	public boolean onError(HTTP2_Error error) {

		// report error
		this.error = error;

		//parsing is altered, so stop it (see above)
		return true;
	}



	/**
	 * <p>
	 * This method MUST NOT be made thread-safe since encapsulated into thread-safe
	 * code at <code>RxConnection</code> level, thanks to Rx pull/pushOps internal
	 * blocks (where <code>HTTP2_onMessageReceived</code> is called) methods are
	 * thread-safe thanks to CAS
	 * </p>
	 * 
	 * @param frame
	 */
	public abstract void HTTP2_onMessageReceived(HTTP2_Message message);



	@Override
	public void close() {

		// close SSL
		super.close();

		if(HTTP2_isVerbose()) {
			System.out.println("[HTTP2_Connection] A close has been notified");
		}
	}


	public boolean HTTP2_isVerbose() {
		return isVerbose;
	}




	public void resumeSending() {
		resume();
	}


	public void send(HTTP2_Frame frame) {
		outbound.push(frame);
	}


	public HPACK_Context HPACK_getContext() {
		return HPACK_context;
	}

}
