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

		SiliconEngine ng = new SiliconEngine(appConfig);

		ng.start();

		RxServer server = new RxServer() {

			public @Override SiliconEngine getSiliconEngine() { return ng; }

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return webConfig;
			}

			@Override
			public RxConnection open(SocketChannel socketChannel) throws IOException {

				return new RxConnection_Impl01(this, socketChannel, 
						new RxInbound_Impl01("server", webConfig) {

					@Override
					public void onRxReceived() throws IOException {
						int length = networkBuffer.remaining();
						byte[] bytes = new byte[length];
						networkBuffer.get(bytes);
						System.out.println("Server > "+new String(bytes));

						receive(); // always reading
					}



				}, new RxOutbound_Impl01("server", webConfig) {

					@Override
					public void onPreRxSending() {
					}
				});
			}
		};


		RxWebConfiguration clientConfig = new RxWebConfiguration();
		clientConfig.port = 1336;
		clientConfig.hostname = "localhost";

		RxClient client = new RxClient() {

			public @Override SiliconEngine getSiliconEngine() { return ng; }

			private byte[] bytes = "Hi this is the client speaking now!!!zeoinzoicnpioz".getBytes();

			private int index = 0;

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return clientConfig;
			}

			@Override
			public RxConnection open(Selector selector, SocketChannel socketChannel) throws IOException {
				return new RxConnection_Impl01(this, socketChannel, 
						new RxInbound_Impl01("client", webConfig) {

					@Override
					public void onRxReceived() throws IOException {
					}

				},
						new RxOutbound_Impl01("client", webConfig) {

					@Override
					public void onPreRxSending() throws IOException {
						int n = Math.min(networkBuffer.remaining(), bytes.length);
						networkBuffer.put(bytes, index, n);
						index+=n;
						if(index<bytes.length) {
							send();
						}
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
