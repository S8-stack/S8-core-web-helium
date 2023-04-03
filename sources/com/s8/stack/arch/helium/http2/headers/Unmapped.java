package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * </p>
 * @author pierre convert
 * 
 */
public class Unmapped extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0xf0,
			new String[] { "x-unmapped", "X-Unmapped" }, 
			false,
			GENERAL, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Unmapped(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Unmapped();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.unmapped;
		}
	};

	
	public String value;

	public Unmapped() {
		super();
	}
	
	public Unmapped(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.unmapped = this;
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
