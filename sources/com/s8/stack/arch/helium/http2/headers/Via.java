package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class Via extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x65,
			new String[] { "via" }, 
			true,
			GENERAL, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Via(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Via();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.via;
		}
	};

	

	public String value;

	
	public Via() {
		super();
	}
	
	public Via(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.via = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
}
