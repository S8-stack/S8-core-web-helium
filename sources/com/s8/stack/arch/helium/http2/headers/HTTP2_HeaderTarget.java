package com.s8.stack.arch.helium.http2.headers;


/**
 * The type of target header can be associated with.
 * 
 * @author pierreconvert
 *
 */
public enum HTTP2_HeaderTarget {

	
	/**
	 * Request headers contain more information about the resource to be fetched, or
	 * about the client requesting the resource.
	 */
	REQUEST, 
	
	/**
	 * Response headers hold additional information about the response, like its
	 * location or about the server providing it.
	 */
	RESPONSE, 
	
	
	/**
	 * Entity headers contain information about the body of the resource, like its
	 * content length or MIME type.
	 */
	ENTITY, 
	
	
	/**
	 * General headers apply to both requests and responses, but with no relation to
	 * the data transmitted in the body.
	 */
	GENERAL;
}
