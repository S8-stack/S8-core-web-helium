package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 *
 */
public class Authority extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x18,
			new String[] { ":authority"}, 
			true,
			ENTITY, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Authority(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Authority();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.authority;
		}
	};

	

	public String value;

	
	public Authority() {
		super();
	}
	
	public Authority(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.authority = this;
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
