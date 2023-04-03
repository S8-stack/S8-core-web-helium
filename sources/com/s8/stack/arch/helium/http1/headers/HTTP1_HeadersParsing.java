package com.s8.stack.arch.helium.http1.headers;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.HTTP1_Protocol;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;

/**
 * 
 * @author pc
 *
 */
public class HTTP1_HeadersParsing implements HTTP1_IOReactive {

	public final static byte[] ENTRY_DEF = " : ".getBytes(StandardCharsets.US_ASCII);


	public void append(HTTP1_Header header){
		header.getPrototype().set(request, header);
	}


	public String getParsingErrorMessage(){
		return errorMessage;
	}



	private enum ParsingState {
		CLOSING_LINE,
		READING_HEADER_NAME,
		READING_HEADER_VALUE,
		CLOSING_HEADER;
	}


	private boolean isVerbose;
	private boolean isTerminated = false;
	private ParsingState state = ParsingState.READING_HEADER_NAME;
	private StringBuilder builder = new StringBuilder();
	private String headerName=null, headerValue;



	private String errorMessage;

	private HTTP1_Request request;

	public HTTP1_HeadersParsing(HTTP1_Request request, boolean isVerbose) {
		super();
		this.request = request;
		this.isVerbose = isVerbose;
	}

	/**
	 * For the request line specifically, the method name and HTTP version are going
	 * to be ASCII characters only, but it's possible that the URL itself could
	 * include non-ASCII characters. But if you look at RFC 2396, it says that.
	 * 
	 * 
	 * @param readable
	 * @param SSL_isVerbose
	 * @return
	 */
	@Override
	public Result onBytes(ByteBuffer readable){

		char c;

		while(!isTerminated){

			if(!readable.hasRemaining()){
				if(isVerbose){
					System.out.println("[HTTP_Headers]: Unexpected end of input stream");
				}
				return Result.NEED_MORE_BYTES;
			}


			/**
			 * For the request line specifically, the method name and HTTP version are going
			 * to be ASCII characters only, but it's possible that the URL itself could
			 * include non-ASCII characters. But if you look at RFC 2396, it says that.
			 */
			c = (char) readable.get();

			if(isVerbose){
				System.out.print(c);	
			}
			switch(state){

			case CLOSING_LINE:
				if(c==HTTP1_Protocol.EOL[1]){
					state = ParsingState.READING_HEADER_NAME;
				}
				else{
					if(isVerbose){
						System.out.println("[HTTP_Headers]: Ill formed request header");
					}
					return Result.ERROR;
				}
				break;

			case READING_HEADER_NAME:
				if(c==HTTP1_Protocol.EOL[0]){
					state = ParsingState.CLOSING_HEADER;
				}
				else if(c==HTTP1_Protocol.HEADER_VALUE_DELIMITER){
					headerName = builder.toString();
					builder = new StringBuilder();
					state = ParsingState.READING_HEADER_VALUE;
				}
				else if(c!=HTTP1_Protocol.BLANK){
					builder.append(c);
				}
				break;

			case READING_HEADER_VALUE:
				if(c==HTTP1_Protocol.EOL[0]){
					headerValue = builder.toString();
					HTTP1_Header.Prototype headerPrototype = HTTP1_Header.get(headerName);
					if(headerPrototype==null){
						if(isVerbose){
							System.out.println("[HTTP_Headers]: Unknown header type: "+headerName+". Skipped.");	
						}
					}
					else{
						HTTP1_Header header = headerPrototype.create();
						header.parse(headerValue);
						append(header);	
					}
					builder = new StringBuilder();
					state = ParsingState.CLOSING_LINE;
				}
				else if(c!=HTTP1_Protocol.BLANK){
					builder.append(c);
				}
				break;

			case CLOSING_HEADER:
				if(c==HTTP1_Protocol.EOL[1]){
					isTerminated = true;
				}
				else{
					if(isVerbose){
						System.out.println("[HTTP_Headers]: Ill formed HTTP request");
					}
					return Result.ERROR;
				}
				break;
			}
		}
		return Result.OK;
	}

}
