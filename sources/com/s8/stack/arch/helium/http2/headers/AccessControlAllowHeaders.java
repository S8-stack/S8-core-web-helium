package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 *
 */
public class AccessControlAllowHeaders extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x15,
			new String[] { "access-control-allow-headers", "Access-Control-Allow-Headers"}, 
			false, 
			RESPONSE, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new AccessControlAllowHeaders(value);
		}

		@Override
		public HTTP2_Header create() {
			return new AccessControlAllowHeaders("utf8");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.accessControlAllowHeaders;
		}
	};

	
	public static AccessControlAllowHeaders parse(String value) {
		return new AccessControlAllowHeaders(value);
	}

	public String value;

	public AccessControlAllowHeaders(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.accessControlAllowHeaders = this;
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
