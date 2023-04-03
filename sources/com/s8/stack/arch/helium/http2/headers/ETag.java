package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class ETag extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x31,
			new String[] { "etag"}, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ETag(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ETag();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return null;
		}
	};

	
	public String value;

	public ETag() {
		super();
	}
	
	public ETag(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.eTag = this;
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
