package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.s8.core.arch.silicon.SiliconConfiguration;
import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.web.helium.rx.RxClient;
import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxServer;
import com.s8.core.web.helium.rx.RxWebConfiguration;

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
					public void onRxReceived() throws IOException {
						int length = networkBuffer.remaining();
						byte[] bytes = new byte[length];
						networkBuffer.get(bytes);
						System.out.println("Server > "+new String(bytes));

						receive(); // always reading
					}

					@Override
					public void onRxRemotelyClosed() {
					}

					@Override
					public void onRxReceptionFailed(IOException exception) {
					}

					
				}, new RxOutbound_Impl01(1024, webConfig) {

					@Override
					public void onRxSending() {
					}

					@Override
					public void onRxRemotelyClosed() {
					}

					@Override
					public void onRxFailed(IOException exception) {
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
							public void onRxReceived() {
							}

							@Override
							public void onRxRemotelyClosed() {
							}

							@Override
							public void onRxReceptionFailed(IOException exception) {
							}

							
						},
						new RxOutbound_Impl01(1024, webConfig) {

							@Override
							public void onRxSending() throws IOException {
								int n = Math.min(networkBuffer.remaining(), bytes.length);
								networkBuffer.put(bytes, index, n);
								index+=n;
								if(index<bytes.length) {
									send();
								}
							}

							@Override
							public void onRxRemotelyClosed() {
							}

							@Override
							public void onRxFailed(IOException exception) {
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
