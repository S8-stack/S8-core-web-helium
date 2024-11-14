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

public class TestRx04 {

	public static void main(String[] args) throws Exception {

		RxWebConfiguration webConfig = new RxWebConfiguration();
		webConfig.port = 1336;
		
		SiliconConfiguration appConfig = new SiliconConfiguration();
		
		SiliconEngine ng = new SiliconEngine(appConfig);
		ng.start();

		RxServer server = new RxServer() {
			
			@Override
			public SiliconEngine getSiliconEngine() {
				return ng;
			}

			@Override
			public RxConnection open(SocketChannel socketChannel) throws IOException {

				return new RxConnection_Impl01(this, socketChannel,
						new RxInbound_Impl01("server", webConfig) {

							@Override
							public void onRxReceived() {
								
								int length = networkBuffer.remaining();
								byte[] bytes = new byte[length];
								networkBuffer.get(bytes);
								System.out.println("Server > "+new String(bytes));
								receive(); // always reading
							}

						}, 
						new RxOutbound_Impl01("server", webConfig) {

							@Override
							public void onPreRxSending() {
							}
						});
			}

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return webConfig;
			}
		};
		server.start();

		int nClients = 8;
		TestClient[] clients = new TestClient[nClients];
		for(int i=0; i<nClients; i++) {


			RxWebConfiguration clientConfig = new RxWebConfiguration();
			clientConfig.port = 1336;
			clientConfig.hostname = "localhost";

			TestClient client = new TestClient(ng, clientConfig, "client"+i);
			client.start();
			clients[i]=client;
		}

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" ---> fire");
					for(TestClient client : clients) {
						client.fire();	
					}
				}
			}
		});
		thread.start();

	}


	private static class TestClient extends RxClient {

		public final SiliconEngine ng;
		private String name;

		private Queue<byte[]> queue;

		private byte[] bytes;

		private int index = 0;
		
		private RxWebConfiguration config;

		public TestClient(SiliconEngine ng, RxWebConfiguration config, String name) {
			super();
			this.ng = ng;
			this.name = name;
			queue = new ConcurrentLinkedQueue<byte[]>();
			this.config = config;
		}

		public void fire() {
			byte[] bytes = (name+"> Hi this is the client speaking now!!!gabuzomeuh-meuhç!èàç!à!").getBytes();
			queue.add(bytes);
			send();
		}


		private void pull() {
			bytes = queue.poll();
			index = 0;
		}

		@Override
		public RxConnection open(Selector selector, SocketChannel socketChannel) throws IOException {
			return new RxConnection_Impl01(this, socketChannel, 
					new RxInbound_Impl01("client", config) {

						@Override
						public void onRxReceived() {	
						}
					}, 
					new RxOutbound_Impl01("client", config) {

						@Override
						public void onPreRxSending() {
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
					});
		}

		@Override
		public RxWebConfiguration getWebConfiguration() {
			return config;
		}

		@Override
		public void stop() throws Exception {
		}

		@Override
		public SiliconEngine getSiliconEngine() {
			return ng;
		}
	}

}
