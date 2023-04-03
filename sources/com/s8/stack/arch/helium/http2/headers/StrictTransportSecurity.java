package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class StrictTransportSecurity extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x61,
			new String[] { "strict-transport-security" }, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new StrictTransportSecurity(value);
		}

		@Override
		public HTTP2_Header create() {
			return new StrictTransportSecurity();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.strictTransportSecurity;
		}
	};

	
	public String value;

	public StrictTransportSecurity() {
		super();
	}
	
	public StrictTransportSecurity(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.strictTransportSecurity = this;
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
