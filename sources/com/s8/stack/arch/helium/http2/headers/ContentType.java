package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;
import com.s8.stack.arch.helium.mime.MIME_Type;

/**
 * 
 * @author pierreconvert
 */
public class ContentType extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x28,
			new String[] { "content-type"}, 
			false,
			ENTITY, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ContentType(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ContentType();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.contentType;
		}
	};

	
	
	public MIME_Type type;

	public String template;

	
	public ContentType() {
		super();
	}
	
	public ContentType(String template) {
		super();
		this.template = template;
		this.type = MIME_Type.get(template);	
	}
	
	public ContentType(MIME_Type type) {
		super();
		this.template = type.template;
		this.type = type;	
	}
	
	
	public void set(MIME_Type type) {
		this.type = type;
		this.template = type.template;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.contentType = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return template;
	}
	
	@Override
	public void setValue(String template) {
		this.template = template;
		this.type = MIME_Type.get(template);
	}
}
