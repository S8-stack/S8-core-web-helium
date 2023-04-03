package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierreconvert
 * 
 * ENTITY
 */
public class ContentLength extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x25,
			new String[] { "content-length"}, 
			false,
			ENTITY, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ContentLength(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ContentLength();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.contentLength;
		}
	};

	

	public int value;

	
	public ContentLength() {
		super();
	}
	
	public ContentLength(String arg) {
		super();
		this.value = Integer.valueOf(arg);
	}

	public ContentLength(int length) {
		super();
		this.value = length;
	}

	
	@Override
	public void bind(HTTP2_Message message) {
		message.contentLength = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return Integer.toString(value);
	}
	
	@Override
	public void setValue(String value) {
		this.value = Integer.valueOf(value);
	}
}
