package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.HTTP2_Status;
import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class Status extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x60,
			new String[] { ":status"}, 
			true,
			RESPONSE, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Status(Integer.valueOf(value));
		}

		@Override
		public HTTP2_Header create() {
			return new Status();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.status;
		}
	};

	

	public HTTP2_Status value;

	
	/**
	 * 
	 */
	public Status() {
		super();
	}
	
	
	/**
	 * 
	 * @param value
	 */
	public Status(HTTP2_Status value) {
		super();
		this.value = value;
	}
	
	
	/**
	 * 
	 * @param pathname
	 */
	public Status(int code) {
		super();
		this.value = HTTP2_Status.byCode(code);
	}

	
	@Override
	public void bind(HTTP2_Message message) {
		message.status = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value.name();
	}
	
	@Override
	public void setValue(String value) {
		this.value = HTTP2_Status.valueOf(value);
	}
	
}
