package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class IfNoneMatch extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x38,
			new String[] { "if-none-match" }, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new IfNoneMatch(value);
		}

		@Override
		public HTTP2_Header create() {
			return new IfNoneMatch();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.ifNoneMatch;
		}
	};

	
	public String value;

	public IfNoneMatch() {
		super();
	}
	
	public IfNoneMatch(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.ifNoneMatch = this;
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
