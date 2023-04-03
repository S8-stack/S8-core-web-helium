package com.s8.stack.arch.helium.http2.headers;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 *
 */
public class Origin extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x15,
			new String[] { "origin", "Origin", "ORIGIN" }, 
			false, 
			HTTP2_HeaderTarget.REQUEST, 
			HTTP2_HeaderRefresh.ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Origin(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Origin("utf8");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.origin;
		}
	};

	
	public static Origin parse(String value) {
		return new Origin(value);
	}

	public String value;

	public Origin(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.origin = this;
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
