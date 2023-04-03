package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class IfUnmodifiedSince extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x40,
			new String[] { "if-unmodified-since"}, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new IfUnmodifiedSince(value);
		}

		@Override
		public HTTP2_Header create() {
			return new IfUnmodifiedSince();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.ifUnmodifiedSince;
		}
	};

	
	public String value;

	public IfUnmodifiedSince() {
		super();
	}
	
	public IfUnmodifiedSince(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.ifUnmodifiedSince = this;
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
