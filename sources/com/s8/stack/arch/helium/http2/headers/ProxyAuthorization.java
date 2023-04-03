package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class ProxyAuthorization extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x48,
			new String[] { "proxy-authorization" }, 
			false,
			RESPONSE, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ProxyAuthorization(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ProxyAuthorization();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.proxyAuthorization;
		}
	};

	
	public String value;

	public ProxyAuthorization() {
		super();
	}
	
	public ProxyAuthorization(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.proxyAuthorization = this;
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
