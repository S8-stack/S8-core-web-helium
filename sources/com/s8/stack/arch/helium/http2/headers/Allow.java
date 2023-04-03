package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.ENTITY;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * The Allow header lists the set of methods supported by a resource.
 * 
 * This header must be sent if the server responds with a 405 Method Not Allowed
 * status code to indicate which request methods can be used. An empty Allow
 * header indicates that the resource allows no request methods, which might
 * occur temporarily for a given resource, for example.
 * </p>
 * 
 * ALLOW(0x17, false, ENTITY, FEW_STATES, "allow")
 * 
 * @author pierreconvert
 *
 */
public class Allow extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x17,
			new String[] { "allow" }, 
			false,
			ENTITY, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Allow(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Allow("none");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.allow;
		}
	};

	
	public static Allow parse(String value) {
		return new Allow(value);
	}

	public String value;

	public Allow(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.allow = this;
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
