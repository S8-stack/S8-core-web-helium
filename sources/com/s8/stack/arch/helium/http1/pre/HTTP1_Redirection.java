package com.s8.stack.arch.helium.http1.pre;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.arch.silicon.SiliconEngine;
import com.s8.stack.arch.helium.http1.HTTP1_Connection;
import com.s8.stack.arch.helium.http1.HTTP1_Endpoint;
import com.s8.stack.arch.helium.http1.HTTP1_Server;
import com.s8.stack.arch.helium.http1.HTTP1_WebConfiguration;
import com.s8.stack.arch.helium.http1.headers.ContentLength;
import com.s8.stack.arch.helium.http1.headers.ContentType;
import com.s8.stack.arch.helium.http1.headers.Location;
import com.s8.stack.arch.helium.http1.headers.MIME_Type;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;


/**
 * 
 * @author pierreconvert
 *
 */
public class HTTP1_Redirection extends HTTP1_Server {

	
	private SiliconEngine app;
	
	private String redirectionAddress;

	private HTTP1_WebConfiguration config;
	
	public HTTP1_Redirection(SiliconEngine app, String redirectionAddress) {
		super();
		this.app = app;
		this.redirectionAddress = redirectionAddress;
		config = new HTTP1_WebConfiguration();
	}

	@Override
	public HTTP1_Connection open(SocketChannel socketChannel) throws IOException {
		HTTP1_Connection connection = new H1Connection(socketChannel, this);
		connection.Rx_initialize(config);
		return connection;
	}


	public class H1Connection extends HTTP1_Connection {

		public H1Connection(SocketChannel socketChannel, HTTP1_Endpoint endpoint) throws IOException {
			super(socketChannel, endpoint);
		}

		@Override
		public void onReceivedRequest(HTTP1_Request request) {
		
			HTTP1_Response response = new HTTP1_Response();
			
			if(request.line.path.equals("/toto.html")) {
				byte[] bytes = ("<!DOCTYPE html>\n"
						+ "<html lang=\"en\">\n"
						+ "\n"
						+ "    <head>\n"
						+ "        <meta charset=\"UTF-8\">\n"
						+ "        <title>Hello!</title>\n"
						+ "    </head>\n"
						+ "\n"
						+ "    <body>\n"
						+ "        <h1>Hello World!</h1>\n"
						+ "        <p>This is a simple paragraph.</p>\n"
						+ "    </body>\n"
						+ "\n"
						+ "</html>").getBytes();
				
				response.line.statusCode = 200;
				response.line.statusText = "OK";
				response.contentType = new ContentType(MIME_Type.TEXT_HTML);
				response.contentLength = new ContentLength(bytes.length);
				response.body = bytes;
			}
			else {

				response.line.statusCode = 301;
				response.line.statusText = "Moved Permanently";
				response.location = new Location(redirectionAddress);
				response.contentLength = new ContentLength(0);
			}
			
			

			getOutbound().push(response);	
		}
	}

	
	public static void main(String[] args) throws Exception {
		HTTP1_Redirection server = new HTTP1_Redirection(SiliconEngine.startBasic(), "https://google.com/index.html");
		server.start();
	}

	@Override
	public HTTP1_WebConfiguration getWebConfiguration() {
		return config;
	}

	@Override
	public SiliconEngine getEngine() {
		return app;
	}
}
