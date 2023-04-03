package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierreconvert
 */
public class ContentLocation extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x26,
			new String[] { "content-location"}, 
			false,
			ENTITY, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ContentLocation(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ContentLocation();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.contentLocation;
		}
	};

	

	public String value;

	
	public ContentLocation() {
		super();
	}
	
	public ContentLocation(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.contentLocation = this;
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
