package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class Link extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x42,
			new String[] { "link" }, 
			false,
			RESPONSE, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Link(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Link();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.link;
		}
	};

	
	public String value;

	public Link() {
		super();
	}
	
	public Link(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.link = this;
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
