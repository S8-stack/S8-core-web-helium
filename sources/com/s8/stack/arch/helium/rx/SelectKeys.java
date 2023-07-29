package com.s8.stack.arch.helium.rx;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.s8.arch.silicon.watch.WatchTask;


/**
 * 
 * @author pierreconvert
 *
 */
public class SelectKeys implements WatchTask {


	/**
	 * We limits at 2^16 the number keys per select
	 */
	public final static int MAX_KEYCOUNT_PER_SELECT = 65536;
	
	
	private final RxServer server;
	
	
	public SelectKeys(RxServer server) {
		this.server = server;
	}
	
	@Override
	public WatchTask run() {

		try {

			if(server.isRxVerbose) {
				System.out.println("\t->server loop++ (Selecting new keys)");	
			}

			// update observables
			server.pool.pullInterestOps();

			// blocking
			Set<SelectionKey> selectedKeys = server.selectKeys();

			Iterator<SelectionKey> iterator = selectedKeys.iterator();

			/* keep track of number of inserted keys */
			long keycount = 0;
			boolean hasReachedThresholds = false;
			while (!hasReachedThresholds && iterator.hasNext()) {

				SelectionKey key = iterator.next();

				// filter OP_ACCEPT
				if(key.isValid()) {
					
					/**
					 * new incoming connection request is yet to be accepted, then fully connected
					 */
					if (key.isAcceptable()) {

						SocketChannel socketChannel;

						socketChannel = server.serverSocketChannel.accept();


						if(socketChannel!=null) {

							// open endpoint
							RxConnection connection = server.open(socketChannel);

							// setup id
							server.pool.put(connection);						

							// open for reception
							connection.receive();

						}
					}
					/*
					 *  perform other types of operations
					 *  (the connection has already been created)
					 */
					else { 
						server.getEngine().pushAsyncTask(new ProcessKey(key));
						
					}
				}
				keycount++;
				
				if(keycount > MAX_KEYCOUNT_PER_SELECT) {
					hasReachedThresholds = true;
				}

				/* </connection-IO> */


				/*
				 * Remove this key from selection set. Because the Selector never does that, it
				 * only adds to the set, so if you don't do it you will reprocess the event
				 * yourself next time the Selector returns.
				 * 
				 * I just recently learned that my foreach loop over the selected keys set is
				 * bad. foreach uses the set's iterator. Modifying a collection directly (not
				 * via the iterator's methods) while iterating over it may result in
				 * "arbitrary, undeterministic" behavior.
				 * 
				 * The selected keys set may provide a fail-fast iterator. Fail-fast iterators
				 * detect such modifications and throw a ConcurrentModificationException upon
				 * the next iteration. So modifying the set in a foreach either risks
				 * undeterministic behavior or may cause exceptions - depending on the iterator
				 * implementation.
				 * 
				 * Solution: don't use foreach. Use the iterator and remove the key via
				 * iterator.remove().
				 */
				iterator.remove();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		
		/*
		 * WHATEVER happened, we push a new SelectKeys task to re-iterate
		 */
		return new SelectKeys(server);
	}

	

	@Override
	public String describe() {
		return "(Rx) SELECT_KEYS";
	}
}
