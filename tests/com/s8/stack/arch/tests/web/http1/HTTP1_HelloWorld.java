package com.s8.stack.arch.tests.web.http1;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.arch.silicon.SiliconConfiguration;
import com.s8.arch.silicon.SiliconEngine;
import com.s8.stack.arch.helium.http1.HTTP1_Connection;
import com.s8.stack.arch.helium.http1.HTTP1_Endpoint;
import com.s8.stack.arch.helium.http1.HTTP1_Server;
import com.s8.stack.arch.helium.http1.HTTP1_WebConfiguration;
import com.s8.stack.arch.helium.http1.headers.ContentLength;
import com.s8.stack.arch.helium.http1.headers.ContentType;
import com.s8.stack.arch.helium.http1.headers.MIME_Type;
import com.s8.stack.arch.helium.http1.headers.TransferEncoding;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public class HTTP1_HelloWorld {

	
	public static class H1Server extends HTTP1_Server {

		private HTTP1_WebConfiguration config;
		
		SiliconEngine app;
		
		private SiliconConfiguration appConfig;
		
		public H1Server() {
			super();
			config = new HTTP1_WebConfiguration();
			appConfig = new SiliconConfiguration();
			app = new SiliconEngine(appConfig);
			app.start();
		}

		@Override
		public HTTP1_Connection open(SocketChannel socketChannel) throws IOException {
			HTTP1_Connection connection = new H1Connection(socketChannel, this);
			connection.Rx_initialize(config);
			return connection;
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
	
	public static class H1Connection extends HTTP1_Connection {

		public H1Connection(SocketChannel socketChannel, HTTP1_Endpoint endpoint) throws IOException {
			super(socketChannel, endpoint);
		}

		@Override
		public void onReceivedRequest(HTTP1_Request request) {
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
			
			HTTP1_Response response = new HTTP1_Response();
			response.line.statusCode = 200;
			response.line.statusText = "OK";
			response.contentType = new ContentType(MIME_Type.TEXT_HTML);
			response.contentLength = new ContentLength(bytes.length);
			response.transferEncoding = new TransferEncoding("identity");
			
			response.body = bytes;
			
			//name=Joe%20User&request=Send%20me%20one%20of%20your%20catalogue
			
			getOutbound().push(response);	
		}
	}
	
	public static void main(String[] args) throws Exception {
				
		H1Server server = new H1Server();
		server.start();
	}

}
