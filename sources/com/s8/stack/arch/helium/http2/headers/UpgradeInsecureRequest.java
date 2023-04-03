package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class UpgradeInsecureRequest extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0xa0,
			new String[] { "upgrade-insecure-requests", "Upgrade-Insecure-Requests" }, 
			false,
			REQUEST, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new UpgradeInsecureRequest(value);
		}

		@Override
		public HTTP2_Header create() {
			return new UpgradeInsecureRequest();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.upgradeInsecureRequest;
		}
	};

	

	public String value;

	
	public UpgradeInsecureRequest() {
		super();
	}
	
	public UpgradeInsecureRequest(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.upgradeInsecureRequest = this;
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
