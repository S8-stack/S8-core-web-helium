package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierreconvert
 */
public class CacheControl extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x20,
			new String[] { "cache-control"}, 
			false,
			GENERAL, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new CacheControl(value);
		}

		@Override
		public HTTP2_Header create() {
			return new CacheControl();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.cacheControl;
		}
	};

	

	public String value;

	
	public CacheControl() {
		super();
	}
	
	public CacheControl(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.cacheControl = this;
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
