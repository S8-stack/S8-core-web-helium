package com.s8.stack.arch.tests.web.async;

import java.nio.ByteBuffer;


/**
 * 
 * @author pc
 *
 */
public interface AsynchronousComposer {

	/**
	 * <p>
	 * Sequence is supposed to run like this:
	 * <ul>
	 * <li>Channel internal control calls <code>ChannelWritable.write(ByteBuffer buffer)</code> (note that
	 * <code>ByteBuffer buffer</code> is a private internal object of the channel) to push some data to the buffer.
	 * Note also that <code>ChannelWritable</code> is responsible for cleaning the buffer when full to write further
	 * data. Also, the returned flag indicate if it the last buffer writing required.
	 * </li>
	 * <li>Then Channel internal control starts a new loop to transmit the newly added data over the socket. It retries 
	 * -every time completion handler is called back- to write the remaining buffer content to the channel, 
	 * until successfully transmitted or max_nb_retires exceeded. BufferWritable is supposed to be stateful and recall
	 * of its current writing needs every time write is called.
	 * </li>
	 * <li>
	 * If this is the last buffer writing required, the channel will move to the next BufferWritable in queue.
	 * </li>
	 * </ul>
	 * </p>
	 * @param buffer
	 * @return
	 */
	public boolean compose(ByteBuffer buffer);
	
	
	
}
