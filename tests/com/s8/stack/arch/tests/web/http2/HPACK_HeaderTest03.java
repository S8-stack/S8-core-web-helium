package com.s8.stack.arch.tests.web.http2;

import com.s8.core.web.helium.http2.headers.HTTP2_Header;
import com.s8.core.web.helium.http2.hpack.HPACK_Context;
import com.s8.core.web.helium.http2.hpack.HPACK_Data;
import com.s8.core.web.helium.http2.hpack.HPACK_Decoder;
import com.s8.core.web.helium.http2.hpack.HPACK_Encoder;

public class HPACK_HeaderTest03 {

	public static void main(String[] args) {
		
		HPACK_Context context = new HPACK_Context(true);
		HTTP2_Header[] headers = new HTTP2_Header[] {

				context.createHeader(":method", "POST"),
				context.createHeader("accept-charset", "iso-UTF8; UTF16"),
				context.createHeader(":path", "go/to/my/resource1.txt"),
				context.createHeader(":path", "go/to/my/resource2.txt"),
				context.createHeader("content-length", "256"),
				context.createHeader("x-special", "special value"),
				context.createHeader("content-length", "27656")
		};

		
		int nRuns = 100;
		int nPerRun = 10000;

		TestLoop testLoop = new TestLoop(context, headers, 128);

		for(int k=0; k<20; k++) {
			testLoop.roundtrip(nPerRun);	
		}


		long avg = 0;
		long time;
		for(int k=0; k<nRuns; k++) {

			time = System.nanoTime();
			testLoop.roundtrip(nPerRun);

			time = System.nanoTime() - time;
			avg+=time;
			System.out.println("Total roundtrip time: "+time/nPerRun+" ns/roundtrip");

		}
		System.out.println("Avg Total roundtrip time: "+avg/(nRuns*nPerRun)+" ns/roundtrip");
		System.out.println("Val: "+testLoop.val+"");
	}



	public static class TestLoop {

		
		private int capacity;
		private int it = 4;
		private HTTP2_Header[] headers;
		private HPACK_Data data;
		private HPACK_Encoder encoder;
		private HPACK_Decoder decoder;
		
		
		public long val;


		public TestLoop(HPACK_Context context, HTTP2_Header[] headers, int capacity) {
			super();
			this.headers = headers;
			
			this.capacity = capacity;
			
			encoder = new HPACK_Encoder(context, 4096, true, false);
			decoder = new HPACK_Decoder(context, 4096, false);
		}


		public void roundtrip(int nPerRun) {


			for(int i=0; i<nPerRun; i++) {
				
				it=0;

				byte[] bytes = new byte[capacity];
				data = new HPACK_Data(bytes);
				data.rewind(0);
				encoder.encode(data, headers);
				
				
				data.flip(0);
				decoder.decode(data, header -> {
					if(it==4) {
						val+=Integer.valueOf(header.getValue());
					}
					it++;
				});
				
			}	
		}
	}
}
