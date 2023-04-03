package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.HTTP1_Protocol;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

/**
 * 
 * @author pc
 *
 */
public class HTTP1_HeadersComposing implements HTTP1_IOReactive {

	public final static byte[] ENTRY_DEF = " : ".getBytes(StandardCharsets.US_ASCII);

	private String errorMessage;

	private HTTP1_Response response;

	public HTTP1_HeadersComposing(HTTP1_Response response){
		super();
		this.response = response;
	}


	public String getParsingErrorMessage(){
		return errorMessage;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");

		for(HTTP1_Header.Prototype prototype : HTTP1_Header.PROTOTYPES) {
			HTTP1_Header header = prototype.get(response);
			if(header!=null) {
				builder.append("\n");
				header.printSnaphsot(builder);
			}
		}
		return builder.toString();
	}


	public Result onBytes(ByteBuffer writable) {

		for(HTTP1_Header.Prototype prototype : HTTP1_Header.PROTOTYPES) {
			HTTP1_Header header = prototype.get(response);
			if(header!=null){
				writable.put(header.getPrototype().getBytes());
				writable.put(ENTRY_DEF);
				try {
					writable.put(header.compose().getBytes(StandardCharsets.US_ASCII));
				} catch (IOException e) {
					return Result.ERROR;
				}
				writable.put(HTTP1_Protocol.EOL);	
			}
		}

		// end of headers
		writable.put(HTTP1_Protocol.EOL);

		/**
		 * should be checking that remaining is OK in buffer every time, but since we take
		 * a sufficiently large buffer every time...
		 */
		return Result.OK;
	}

}
