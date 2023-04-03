package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class IfMatch extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x36,
			new String[] { "if-match" }, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new IfMatch(value);
		}

		@Override
		public HTTP2_Header create() {
			return new IfMatch();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.ifMatch;
		}
	};

	
	public String value;

	public IfMatch() {
		super();
	}
	
	public IfMatch(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.ifMatch = this;
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
