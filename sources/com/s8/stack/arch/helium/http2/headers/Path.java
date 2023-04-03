package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class Path extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x46,
			new String[] { ":path"}, 
			true,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Path(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Path();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.path;
		}
	};

	

	public String pathname;

	
	public Path() {
		super();
	}
	
	public Path(String value) {
		super();
		this.pathname = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.path = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return pathname;
	}
	
	@Override
	public void setValue(String value) {
		this.pathname = value;
	}
	
}
