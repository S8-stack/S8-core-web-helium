package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.HTTP2_Method;
import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class Method extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x45,
			new String[] { ":method"}, 
			true,
			REQUEST, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Method(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Method();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return null;
		}
	};

	

	public HTTP2_Method value;

	
	public Method() {
		super();
	}
	
	public Method(String value) {
		super();
		this.value = HTTP2_Method.get(value);
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.method = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value.name();
	}
	
	@Override
	public void setValue(String value) {
		this.value = HTTP2_Method.get(value);
	}
}
