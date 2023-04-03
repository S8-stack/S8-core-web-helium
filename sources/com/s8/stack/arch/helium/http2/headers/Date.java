package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 * 
 */
public class Date extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x30,
			new String[] { "date"}, 
			false,
			GENERAL, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Date(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Date();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.date;
		}
	};

	
	public String value;

	public Date() {
		super();
	}
	
	public Date(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.date = this;
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
