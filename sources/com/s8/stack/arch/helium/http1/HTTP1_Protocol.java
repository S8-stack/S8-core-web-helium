package com.s8.stack.arch.helium.http1;

import java.nio.charset.StandardCharsets;

public class HTTP1_Protocol {

	/**
	 * 
	 */
	public static final String VERSION = "HTTP/1.1";

	/**
	 * from HTTP specifications (see for instance MDN)
	 */
	public static final byte[] EOL = "\r\n".getBytes(StandardCharsets.US_ASCII);
	

	/**
	 * from HTTP specifications (see for instance MDN)
	 */
	public static final byte[] PART_MODIFIER = "--".getBytes(StandardCharsets.US_ASCII);

	/**
	 * from HTTP specifications (see for instance MDN)
	 */
	public static final byte FIRST_LINE_DELIMITER = (byte) ' ';

	/**
	 * from HTTP specifications (see for instance MDN)
	 */
	public static final byte HEADER_VALUE_DELIMITER = (byte) ':';

	/**
	 * from HTTP specifications (see for instance MDN)
	 */
	public static final byte BLANK = (byte) ' ';
	
	
	/**
	 * 
	 */
	public final static int NOT_FOUND_CODE = 404;

	/**
	 * 
	 */
	public final static int OK_CODE = 200;

	/**
	 * 
	 */
	public final static int NOT_OK_CODE = 204;

	/**
	 * 
	 */
	public final static int NOT_AUTHENTICATED_CODE = 408;
	
	
	
	public static boolean isEqual(byte[] sequence0, byte[] sequence1){
		int n;
		if((n=sequence0.length)!=sequence1.length){
			return false;
		}
		for(int i=0; i<n; i++){
			if(sequence0[i]!=sequence1[i]){
				return false;
			}
		}
		return true;
	}
	
}
