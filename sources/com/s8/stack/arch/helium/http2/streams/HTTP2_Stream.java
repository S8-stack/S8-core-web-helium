package com.s8.stack.arch.helium.http2.streams;

import java.util.Iterator;

import com.s8.io.bytes.linked.LinkedBytes;
import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.frames.HTTP2_CONTINUATION_Frame;
import com.s8.stack.arch.helium.http2.frames.HTTP2_DATA_Frame;
import com.s8.stack.arch.helium.http2.frames.HTTP2_HEADERS_Frame;
import com.s8.stack.arch.helium.http2.headers.ContentLength;
import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Data;
import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;
import com.s8.stack.arch.helium.http2.settings.HTTP2_Settings;

public class HTTP2_Stream {
	
	private int identifier;

	private HTTP2_StreamState state;

	public HTTP2_Connection endpoint;

	public HTTP2_Stream previous;

	public HTTP2_Stream next;

	/**
	 * message in the process of being received
	 */
	private HTTP2_Message receivedMessage;


	private int awaitedDataLength;

	public boolean isVerbose;

	/**
	 * Status flag:
	 * <ul>
	 * <li><b>true</b>: wait for headers</li>
	 * <li><b>false</b>: wait for data</li>
	 * </ul>
	 * 
	 */
	private boolean isWaitingForHeaders;

	public HTTP2_Stream(HTTP2_Connection endpoint, int identifier, boolean isVerbose) {
		super();
		this.endpoint = endpoint;
		this.identifier = identifier;
		this.isVerbose = isVerbose;

		/* 
		 * [RFC-7540] All streams start in the "idle" state.
		 */
		state = HTTP2_StreamState.IDLE;
		isWaitingForHeaders = true;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setState(HTTP2_StreamState state) {
		this.state = state;
	}

	public HTTP2_StreamState getState() {
		return state;
	}

	
	
	public HTTP2_Error pushHeaders(HPACK_Data data, boolean isEndOfHeaders) {

		if(!isWaitingForHeaders) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		if(receivedMessage==null) {
			receivedMessage = new HTTP2_Message(endpoint.HPACK_getContext(), this);
		}

		// decode and push headers
		endpoint.getDecoder().decode(data, header -> {

			/*
			if(isVerbose) {
				System.out.println("[HTTP2_HEADERS_Frame] > "+header);	
			}
			*/

			if(header.getPrototype() == ContentLength.PROTOTYPE) {
				awaitedDataLength = ((ContentLength) header).value;
			}

			receivedMessage.setHeader(header);	
		});

		if(isEndOfHeaders) {
			// do not wait for data to release message
			if(!receivedMessage.isWaitingForData()) {
				notifyMessage();
			}
			else {
				isWaitingForHeaders = false;
			}
		}
		return HTTP2_Error.NO_ERROR;
	}

	public HTTP2_Error pushData(LinkedBytes fragment) {
		if(receivedMessage==null || isWaitingForHeaders) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}
		receivedMessage.appendDataFragment(fragment);
		awaitedDataLength-=fragment.length;

		// fire message
		if(awaitedDataLength==0) {
			notifyMessage();
		}
		return HTTP2_Error.NO_ERROR;
	}


	private void notifyMessage() {
		endpoint.HTTP2_onMessageReceived(receivedMessage);
		receivedMessage = null;
		isWaitingForHeaders = true;
	}





	public boolean isClosed() {
		return state == HTTP2_StreamState.CLOSED;
	}

	public void close() {

	}

	public void remoteClose() {
		state = HTTP2_StreamState.REMOTE_HALF_CLOSED;
	}


	public final static int OUTFLOW_DATA_BUFFER_SIZE = 16384;


	public void sendMessage(HTTP2_Message message) {


		/* <headers> */

		boolean isVerbose = endpoint.HTTP2_isVerbose();

		boolean isSending = true;

		/**
		 * Ensure headers frame are sent contiguously
		 */

		boolean isContinuation = false;
		Iterator<HTTP2_Header> i = message.iterator();
		
		/** push all headers to HPACK data */
		while(i.hasNext()) {
			HPACK_Data data = new HPACK_Data(new byte[OUTFLOW_DATA_BUFFER_SIZE]);
		
			/* bundle as much as possible */
			boolean isBundling = true;
			while(isBundling && (isSending = i.hasNext())) {
				HTTP2_Header header = i.next();
				if(header!=null) {
					if(isVerbose) {
						System.out.println("[HTTP2_Stream] pushing header to data: "+header);
					}
					endpoint.getEncoder().encode(data, header);
					isBundling = data.getIndex()<OUTFLOW_DATA_BUFFER_SIZE/2;	
				}
			}
			
			if(!isContinuation) { // first headers frame
				HTTP2_HEADERS_Frame frame = new HTTP2_HEADERS_Frame();
				frame.streamIdentifier = identifier;
				frame.setPadding(4);
				frame.headersFragment = data;
				frame.isEndOfHeaders = !isSending;
				endpoint.send(frame);
				isContinuation = true;
			}
			else { // next headers frame
				HTTP2_CONTINUATION_Frame frame = new HTTP2_CONTINUATION_Frame();
				frame.streamIdentifier = identifier;
				frame.headersFragment = data;
				frame.isEndOfHeaders = !isSending;
				endpoint.send(frame);
			}
		}

		/* </headers> */

		/* <payload> */
		HTTP2_Settings settings = endpoint.getSettings();
		LinkedBytes fragment = message.getDataFragmentHead();
		
		// recut to match max frame size (with safety margin)
		fragment = fragment.recut(settings.maxFrameSize-64);
		
		while(fragment!=null) {
			HTTP2_DATA_Frame frame = new HTTP2_DATA_Frame();
			frame.streamIdentifier = identifier;
			frame.fragment = fragment;
			frame.isEndOfStream = !(fragment.next!=null);
			endpoint.send(frame);
			fragment = fragment.next;
		}
		/* </payload> */
	}


	public void setPriority(boolean isExclusive, int streamDependency, int weight) {
		// TODO implement priority queue in HTTP2_Outbound and use priority information
	}

}
