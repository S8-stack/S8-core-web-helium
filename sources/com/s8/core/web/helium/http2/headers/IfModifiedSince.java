package com.s8.core.web.helium.http2.headers;

import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.core.web.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class IfModifiedSince extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x37,
			new String[] { "if-modified-since"}, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new IfModifiedSince(value);
		}

		@Override
		public HTTP2_Header create() {
			return new IfModifiedSince();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.ifModifiedSince;
		}
	};

	
	public String value;

	public IfModifiedSince() {
		super();
	}
	
	public IfModifiedSince(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.ifModifiedSince = this;
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
