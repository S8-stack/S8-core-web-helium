package com.s8.stack.arch.helium.http2.headers;


/**
 * the type of refresh behavior of headers.
 * 
 * @author pierreconvert
 *
 */
public enum HTTP2_HeaderRefresh {
	
	STATIC_OVER_CONNECTION,
	FEW_STATES,
	ALWAYS_RENEWED;

}
