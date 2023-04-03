package com.s8.stack.arch.tests.web.http2;

import com.s8.stack.arch.helium.http2.hpack.HPACK_Context;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Data;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Decoder;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Encoder;

public class HPACK_HeaderTest02 {

	public static void main(String[] args) {
		
		HPACK_Context context = new HPACK_Context(true);

		int capacity = 1024;
		byte[] bytes = new byte[capacity];
		HPACK_Data data = new HPACK_Data(bytes);
		

		HPACK_Encoder encoder = new HPACK_Encoder(context, 4096, true, true);
		HPACK_Decoder decoder = new HPACK_Decoder(context, 4096, true);


		for(int i=0; i<2; i++) {
			
			data.rewind(0);
			encoder.encode(
					data,
					context.createHeader(":method", "POST"),
					context.createHeader("accept-charset", "iso-UTF8; UTF16"),
					context.createHeader(":path", "go/to/my/resource1.txt"),
					context.createHeader(":path", "go/to/my/resource2.txt"),
					context.createHeader("content-length", "256"),
					context.createHeader("x-special", "special value"),
					context.createHeader("content-length", "27656"));

			
			data.flip(0);
			
			decoder.decode(data, header -> {
				System.out.println("decoded: "+header);
			});
			
		}
	
	}

}
