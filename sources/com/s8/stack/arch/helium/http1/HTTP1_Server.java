package com.s8.stack.arch.helium.http1;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.stack.arch.helium.rx.RxServer;


/**
 * 
 * @author pierreconvert
 *
 */
public abstract class HTTP1_Server extends RxServer implements HTTP1_Endpoint {

	
	@Override
	public abstract HTTP1_WebConfiguration getWebConfiguration();
	

	@Override
	public abstract HTTP1_Connection open(SocketChannel socketChannel) throws IOException;

}
