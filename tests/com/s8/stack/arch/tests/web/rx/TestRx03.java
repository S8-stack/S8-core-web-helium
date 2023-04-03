package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.s8.stack.arch.helium.rx.NetworkBufferResizer;
import com.s8.stack.arch.helium.rx.RxClient;
import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxServer;
import com.s8.stack.arch.helium.rx.RxWebConfiguration;
import com.s8.stack.arch.silicon.SiliconConfiguration;
import com.s8.stack.arch.silicon.SiliconEngine;

public class TestRx03 {

	public static void main(String[] args) throws Exception {

		SiliconConfiguration appConfig = new SiliconConfiguration();
		
		SiliconEngine app = new SiliconEngine(appConfig);
		app.start();
		
		RxWebConfiguration serverConfig = new RxWebConfiguration();
		serverConfig.port = 1336;

		RxServer server = new RxServer() {
			
			@Override
			public SiliconEngine getEngine() {
				return app;
			}

			@Override
			public RxWebConfiguration getWebConfiguration() {
				return serverConfig;
			}

			@Override
			public RxConnection open(SocketChannel channel) throws IOException {

				return new RxConnection_Impl01(this, channel, 
						new RxInbound_Impl01(1024, serverConfig) {
					@Override
					public void onRxReceived(ByteBuffer buffer, NetworkBufferResizer resizer) {
						int length = buffer.remaining();
						byte[] bytes = new byte[length];
						buffer.get(bytes);
						System.out.println("Server > "+new String(bytes));
						receive(); // always reading
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
						new RxOutbound_Impl01(1024, serverConfig) {

							@Override
							public void onRxSending(ByteBuffer networkBuffer, NetworkBufferResizer resizer) {
								// TODO Auto-generated method stub
								
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
		};
		server.start();


		RxWebConfiguration clientConfig = new RxWebConfiguration();
		clientConfig.port = 1336;
		clientConfig.hostname = "localhost";

		TestClient client = new TestClient(clientConfig);
		client.start();
		while(true) {
			Thread.sleep(500);
			client.fire();
		}
	}


	private static class TestClient extends RxClient {

		private Queue<byte[]> queue;

		private byte[] bytes;

		private int index = 0;

		private RxWebConfiguration config;

		public TestClient(RxWebConfiguration config) {
			super();
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

		@Override
		public RxConnection open(Selector selector, SocketChannel channel) throws IOException {
			return new RxConnection_Impl01(this, channel, 
					new RxInbound_Impl01(1024, config) {

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
					new RxOutbound_Impl01(1024, config) {

				@Override
				public void onRxSending(ByteBuffer outbound, NetworkBufferResizer resizer) {
					if(bytes==null) {
						pull();

					}

					if(bytes!=null) {
						int n = Math.min(outbound.remaining(), bytes.length-index);
						outbound.put(bytes, index, n);
						index+=n;
						if(index==bytes.length) {
							pull();
						}
						send(); // keep writing until all bytes written
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
		public void stop() throws Exception {
		}
	}

}
