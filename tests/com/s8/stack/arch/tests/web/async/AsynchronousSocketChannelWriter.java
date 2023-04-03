package com.s8.stack.arch.tests.web.async;




import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;


/**
 *
 *
 */
public class AsynchronousSocketChannelWriter {
	

	/**
	 * max number of retries
	 */
	private int maxNumberofRetries = 1024;

	/**
	 * 
	 */
	private int consecutiveFailuresCount;

	/**
	 * The underlying socket channel
	 */
	private final AsynchronousSocketChannel channel;


	/**
	 * timeout in ms
	 */
	private long timeout;

	
	/**
	 * Will contain this peer's encrypted data, that will be generated after {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)}
	 * is applied on {@link AsynchronousWriter#applicationOutputBuffer}. It should be initialized using {@link SSLSession#getPacketBufferSize()},
	 * which returns the size up to which, SSL/TLS packets will be generated from the engine under a session.
	 * All SSLEngine network buffers should be sized at least this large to avoid insufficient space problems when performing wrap and unwrap calls.
	 */
	private ByteBuffer buffer;

	
	/**
	 * Queue
	 */
	private Queue<AsynchronousComposer> queue;


	/**
	 * 
	 * @param channel
	 * @param bufferCapacity
	 * @param timeout
	 * @throws IOException
	 */
	public AsynchronousSocketChannelWriter(
			AsynchronousSocketChannel channel, 
			int bufferCapacity, 
			long timeout) {
		super();
		
		if(channel == null) {
			throw new IllegalArgumentException("InputSocketChannel must not be null" );
		}
		this.channel = channel;

		this.buffer = ByteBuffer.allocate(bufferCapacity);
		this.timeout = timeout;
		this.queue = new LinkedList<AsynchronousComposer>();
		this.consecutiveFailuresCount = 0;
	}


	public synchronized void push(AsynchronousComposer writable) {
		queue.add(writable);
	}
	
	private synchronized AsynchronousComposer peek() {
		return queue.peek();
	}
	
	private synchronized void pop() {
		queue.poll();
	}
	

	/**
	 * 
	 */
	private void writeNext() {

		AsynchronousComposer composable = peek();
		if(composable!=null) {
			
			buffer.clear();
			boolean hasNext = composable.compose(buffer);
			if(!hasNext) {
				pop();
			}
			
			buffer.flip();
			
			writeBuffer();
		}
	}

	
	/**
	 * 
	 */
	private void writeBuffer() {
	
		channel.write(buffer, timeout, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Void>() {

			@Override
			public void completed(Integer result, Void attachment) {

				// no success in pushing
				if(result==0) {
					consecutiveFailuresCount++;
				}
				else { // if push has been successful, set count to zero
					consecutiveFailuresCount = 0;
				}

				if(consecutiveFailuresCount<maxNumberofRetries) {
					if(buffer.hasRemaining()) {
						writeBuffer();
					}
					else {
						writeNext();
					}	
				}
				else {
					abort();
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				abort();
			}
		});
	}
	

	private void abort() {
		try {
			channel.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}