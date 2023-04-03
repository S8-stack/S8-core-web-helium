package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public class ContentType extends HTTP1_Header {

	public final static Prototype PROTOTYPE = new Prototype(0x04, "Content-Type"){
		
		@Override
		public HTTP1_Header create() {
			return new ContentType();
		}

		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.contentType = (ContentType) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.contentType;
		}
	};
	

	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	
	public MIME_Type type = MIME_Type.TEXT;
	
	public String boundary;

	
	public ContentType() {
		super();
	}
	
	public ContentType(MIME_Type type){
		super();
		this.type = type;
	}
	
	public ContentType(MIME_Type type, String boundary){
		super();
		this.type = type;
		this.boundary = boundary;
	}
	
	@Override
	public void parse(String value) {
		String[] parameters = value.split(";");
		type = MIME_Type.getByKeyword(parameters[0]);
		if(type==MIME_Type.MULTIPART_FORM_DATA){
			boundary = parameters[1].split("=")[1];
		}
	}

	@Override
	public String compose() throws IOException {
		if(type!=null){
			if(type==MIME_Type.MULTIPART_FORM_DATA){
				return type.keyword+";boundary="+boundary;
			}
			else{
				return type.keyword;	
			}
		}
		throw new IOException("no type defined");
	}
	
	public static ContentType from(MIME_Type type){
		ContentType contentType = new ContentType();
		contentType.type = type;
		return contentType;
	}
}
