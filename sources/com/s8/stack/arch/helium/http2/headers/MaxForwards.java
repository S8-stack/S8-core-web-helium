package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 	MAX_FORWARDS(0x44, false, RESPONSE, , "max-forwards"),

 */
public class MaxForwards extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x44,
			new String[] { "link" }, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new MaxForwards(value);
		}

		@Override
		public HTTP2_Header create() {
			return new MaxForwards();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.maxForwards;
		}
	};

	
	public String value;

	public MaxForwards() {
		super();
	}
	
	public MaxForwards(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.maxForwards = this;
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
