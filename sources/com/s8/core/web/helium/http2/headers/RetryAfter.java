package com.s8.core.web.helium.http2.headers;

import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.core.web.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class RetryAfter extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x53,
			new String[] { "retry-after" }, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new RetryAfter(value);
		}

		@Override
		public HTTP2_Header create() {
			return new RetryAfter();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.retryAfter;
		}
	};

	
	public String value;

	public RetryAfter() {
		super();
	}
	
	public RetryAfter(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.retryAfter = this;
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
