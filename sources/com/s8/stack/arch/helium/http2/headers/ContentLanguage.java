package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierre convert
 */
public class ContentLanguage extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x24,
			new String[] { "content-language"}, 
			false,
			ENTITY, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ContentLanguage(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ContentLanguage();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.contentLanguage;
		}
	};

	

	public String value;

	
	public ContentLanguage() {
		super();
	}
	
	public ContentLanguage(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.contentLanguage = this;
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
