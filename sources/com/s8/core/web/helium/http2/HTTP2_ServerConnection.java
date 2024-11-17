package com.s8.core.web.helium.http2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.s8.core.web.helium.http2.messages.HTTP2_Message;



/**
 * 
 * @author pc
 *
 */
public abstract class HTTP2_ServerConnection extends HTTP2_Connection {

	
	public HTTP2_ServerConnection(SelectionKey key, SocketChannel channel, HTTP2_WebConfiguration configuration) throws IOException {
		super("server", key, channel, configuration);
	}

	/**
	 * 
	 * @param frame
	 */
	public abstract void HTTP2_onRequestReceived(HTTP2_Message request);


}
