package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.s8.core.arch.silicon.SiliconConfiguration;
import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.web.helium.rx.RxClient;
import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxServer;
import com.s8.core.web.helium.rx.RxWebConfiguration;

public class TestRx03 {

	public static void main(String[] args) throws Exception {

		SiliconConfiguration appConfig = new SiliconConfiguration();
		
		SiliconEngine ng = new SiliconEngine(appConfig);
		ng.start();
		
		RxWebConfiguration serverConfig = new RxWebConfiguration();
		serverConfig.port = 1336;

		RxServer server = new RxServer() {
			
			@Override
			public SiliconEngine getSiliconEngine() {
				return ng;
			}

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return serverConfig;
			}

			@Override
			public RxConnection open(SocketChannel channel) throws IOException {

				return new RxConnection_Impl01(this, channel, 
						new RxInbound_Impl01("server", 1024, serverConfig) {
					@Override
					public void onRxReceived() {
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
					
				}, 
						new RxOutbound_Impl01("server", 1024, serverConfig) {

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
		server.start();


		RxWebConfiguration clientConfig = new RxWebConfiguration();
		clientConfig.port = 1336;
		clientConfig.hostname = "localhost";

		TestClient client = new TestClient(ng, clientConfig);
		client.start();
		while(true) {
			Thread.sleep(500);
			client.fire();
		}
	}


	private static class TestClient extends RxClient {

		private final SiliconEngine ng;
		
		private Queue<byte[]> queue;

		private byte[] bytes;

		private int index = 0;

		private RxWebConfiguration config;

		public TestClient(SiliconEngine ng, RxWebConfiguration config) {
			super();
			this.ng = ng;
			queue = new ConcurrentLinkedQueue<byte[]>();
			this.config = config;
		}

		public void fire() {
			byte[] bytes = "Hi this is the client speaking now!!!zeoinzoicnpioz".getBytes();
			queue.add(bytes);
			send();
		}


		private void pull() {
			bytes = queue.poll();
			index = 0;
		}

		@Override
		public RxWebConfiguration getWebConfiguration() {
			return config;
		}
		
		public @Override SiliconEngine getSiliconEngine() { return ng; }

		@Override
		public RxConnection open(Selector selector, SocketChannel channel) throws IOException {
			return new RxConnection_Impl01(this, channel, 
					new RxInbound_Impl01("client", 1024, config) {

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
					new RxOutbound_Impl01("client", 1024, config) {

				@Override
				public void onRxSending() {
					if(bytes==null) {
						pull();
					}

					if(bytes!=null) {
						int n = Math.min(networkBuffer.remaining(), bytes.length-index);
						networkBuffer.put(bytes, index, n);
						index+=n;
						if(index==bytes.length) {
							pull();
						}
						send(); // keep writing until all bytes written
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
		public void stop() throws Exception {
		}
	}

}
