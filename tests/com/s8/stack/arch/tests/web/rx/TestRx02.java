package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.s8.stack.arch.helium.rx.NetworkBufferResizer;
import com.s8.stack.arch.helium.rx.RxClient;
import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxServer;
import com.s8.stack.arch.helium.rx.RxWebConfiguration;
import com.s8.stack.arch.silicon.SiliconConfiguration;
import com.s8.stack.arch.silicon.SiliconEngine;

public class TestRx02 {

	public static void main(String[] args) throws Exception {


		SiliconConfiguration appConfig = new SiliconConfiguration();
		
		RxWebConfiguration webConfig = new RxWebConfiguration();
		
		webConfig.port = 1336;
		
		SiliconEngine app = new SiliconEngine(appConfig);
		
		app.start();

		RxServer server = new RxServer() {
			
			@Override
			public SiliconEngine getEngine() {
				return app;
			}

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return webConfig;
			}

			@Override
			public RxConnection open(SocketChannel socketChannel) throws IOException {

				return new RxConnection_Impl01(this, socketChannel, 
						new RxInbound_Impl01(1024, webConfig) {

					@Override
					public void onRxReceived(ByteBuffer buffer, NetworkBufferResizer resizer) throws IOException {
						int length = buffer.remaining();
						byte[] bytes = new byte[length];
						buffer.get(bytes);
						System.out.println("Server > "+new String(bytes));

						receive(); // always reading
					}

					@Override
					public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
					}

					@Override
					public void onRxReceptionFailed(ByteBuffer networkBuffer, IOException exception) {
					}

					
				}, new RxOutbound_Impl01(1024, webConfig) {

					@Override
					public void onRxSending(ByteBuffer networkBuffer, NetworkBufferResizer resizer) {
					}

					@Override
					public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
					}

					@Override
					public void onRxFailed(ByteBuffer networkBuffer, IOException exception) {
					}
				});
			}
		};


		RxWebConfiguration clientConfig = new RxWebConfiguration();
		clientConfig.port = 1336;
		clientConfig.hostname = "localhost";

		RxClient client = new RxClient() {

			private byte[] bytes = "Hi this is the client speaking now!!!zeoinzoicnpioz".getBytes();

			private int index = 0;

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return clientConfig;
			}

			@Override
			public RxConnection open(Selector selector, SocketChannel socketChannel) throws IOException {
				return new RxConnection_Impl01(this, socketChannel, 
						new RxInbound_Impl01(1024, webConfig) {

							@Override
							public void onRxReceived(ByteBuffer networkBuffer, NetworkBufferResizer resizer) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onRxReceptionFailed(ByteBuffer networkBuffer, IOException exception) {
								// TODO Auto-generated method stub
								
							}

							
						},
						new RxOutbound_Impl01(1024, webConfig) {

							@Override
							public void onRxSending(ByteBuffer outbound, NetworkBufferResizer resizer) throws IOException {
								int n = Math.min(outbound.remaining(), bytes.length);
								outbound.put(bytes, index, n);
								index+=n;
								if(index<bytes.length) {
									send();
								}
							}

							@Override
							public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onRxFailed(ByteBuffer networkBuffer, IOException exception) {
								// TODO Auto-generated method stub
								
							}

						});
			}
			
			@Override
			public void stop() {
			}

		};



		server.start();

		client.start();

		Thread.sleep(2000);
		client.send();

	}

}
