package com.s8.stack.arch.helium.http2.streams;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;

/**
 * 
 * @author pc
 *
 */
public class HTTP2_StreamMapping {

	/**
	 * Bucket of the internal map
	 * @author pc
	 *
	 */
	private class Bucket {
		
		private HTTP2_Stream head;
		
		public HTTP2_Stream get(int identifier) {
			if(head!=null) {
				
				// start with head
				HTTP2_Stream stream = head;
				
				/*
				 * remove all closed streams at the beginning
				 * of the list
				 */
				boolean isMostRecentReached = false;
				while(!isMostRecentReached) {
					if(stream.getIdentifier()==identifier) {
						return stream;
					}
					else if(stream.isClosed()) { // remove if closed
						if(stream.previous==null) { // is head
							head = stream.next;
						}
						else {
							stream.previous.next = stream.next;
						}
						if(stream.next!=null) {
							stream.next.previous = stream.previous;
						}
					}
					
					if(stream.next!=null) {
						stream = stream.next;
					}
					else {
						isMostRecentReached = true;
					}
				}
				
				// not found, so create it:

				// check if violating ever increasing rule for stream ID
				/*
				if(identifier<=highestIdentifier) {
					return null;
				}
				*/
				
				HTTP2_Stream newStream = new HTTP2_Stream(endpoint, identifier, isVerbose);
				stream.next = newStream;
				newStream.previous = stream;
				return newStream;
				
			}
			else {

				// check if violating ever increasing rule for stream ID
				/*
				if(identifier<=highestIdentifier) {
					return null;
				}
				*/
				
				return (head = new HTTP2_Stream(endpoint, identifier, isVerbose));
			}
		}
	}
	
	

	private HTTP2_Connection endpoint;

	

	/**
	 * hard-coded max (so ignoring settings which is default to advertising of 128
	 * ) for the sake of simplicity (binary operation enabled)
	 */
	public final static int NB_BUCKETS = 256;
	
	
	private Bucket[] buckets = new Bucket[NB_BUCKETS];
	
	/**
	 * highest strem identifier so far
	 */
	private int highestIdentifier;
	
	
	private boolean isVerbose;
	
	public HTTP2_StreamMapping(HTTP2_Connection endpoint, boolean isVerbose) {
		super();
		this.endpoint = endpoint;
		this.isVerbose = isVerbose;
	}
	
	/**
	 * 
	 * <h1>5.1.1. Stream Identifiers</h1>
	 * <p>
	 * Streams are identified with an unsigned 31-bit integer. Streams initiated by
	 * a client MUST use odd-numbered stream identifiers; those initiated by the
	 * server MUST use even-numbered stream identifiers. A stream identifier of zero
	 * (0x0) is used for connection control messages; the stream identifier of zero
	 * cannot be used to establish a new stream.
	 * 
	 * HTTP/1.1 requests that are upgraded to HTTP/2 (see Section 3.2) are responded
	 * to with a stream identifier of one (0x1). After the upgrade completes, stream
	 * 0x1 is "half-closed (local)" to the client. Therefore, stream 0x1 cannot be
	 * selected as a new stream identifier by a client that upgrades from HTTP/1.1.
	 * 
	 * The identifier of a newly established stream MUST be numerically greater than
	 * all streams that the initiating endpoint has opened or reserved. This governs
	 * streams that are opened using a HEADERS frame and streams that are reserved
	 * using PUSH_PROMISE. An endpoint that receives an unexpected stream identifier
	 * MUST respond with a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 * 
	 * The first use of a new stream identifier implicitly closes all streams in the
	 * "idle" state that might have been initiated by that peer with a lower-valued
	 * stream identifier. For example, if a client sends a HEADERS frame on stream 7
	 * without ever sending a frame on stream 5, then stream 5 transitions to the
	 * "closed" state when the first frame for stream 7 is sent or received.
	 * 
	 * Stream identifiers cannot be reused. Long-lived connections can result in an
	 * endpoint exhausting the available range of stream identifiers. A client that
	 * is unable to establish a new stream identifier can establish a new connection
	 * for new streams. A server that is unable to establish a new stream identifier
	 * can send a GOAWAY frame so that the client is forced to open a new connection
	 * for new streams.
	 * </p>
	 * @param identifer
	 * @return
	 * @throws HTTP2_ProtocolException 
	 */
	public HTTP2_Stream get(int identifier) {
		
		
		int i = identifier & 0xff;
		Bucket bucket = buckets[i];
		if(bucket==null) {
			bucket = new Bucket();
			buckets[i] = bucket;
		}
		
	
		HTTP2_Stream stream = bucket.get(identifier);
		
		// update highest identifier
		highestIdentifier = Math.max(identifier, highestIdentifier);
		return stream;
	}
	
}
