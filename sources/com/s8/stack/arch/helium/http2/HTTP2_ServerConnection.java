package com.s8.stack.arch.helium.http2;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;



/**
 * 
 * @author pc
 *
 */
public abstract class HTTP2_ServerConnection extends HTTP2_Connection {

	
	public HTTP2_ServerConnection(SocketChannel socketChannel, HTTP2_WebConfiguration configuration) throws IOException {
		super(socketChannel, configuration);
	}

	/**
	 * 
	 * @param frame
	 */
	public abstract void HTTP2_onRequestReceived(HTTP2_Message request);


}
