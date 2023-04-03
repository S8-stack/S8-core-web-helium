package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class IfRange extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x39,
			new String[] { "if-range"}, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new IfRange(value);
		}

		@Override
		public HTTP2_Header create() {
			return new IfRange();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.ifRange;
		}
	};

	
	public String value;

	public IfRange() {
		super();
	}
	
	public IfRange(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.ifRange = this;
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
