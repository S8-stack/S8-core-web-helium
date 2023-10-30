package com.s8.core.web.helium.http2.headers;


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
