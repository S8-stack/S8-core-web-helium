package com.s8.stack.arch.tests.web.http2.perf;

import java.nio.ByteBuffer;

public class TestingByteArrayPerf01 {

	public static void main(String[] args) {
		Cycle cycle = new Cycle(1024, 64);

		// warming
		int nRuns = 10000;
		for(int i=0; i<100; i++) {
			cycle.round(nRuns);
		}
		
		long time, avg = 0;
		
		for(int i=0; i<100; i++) {
			time = cycle.round(nRuns);
			avg+=time;
			System.out.println("elapsed time: "+(time/nRuns));
		}
		avg/=(nRuns*100);
		System.out.println("avg time: "+avg);
		System.out.println("test var: "+cycle.var);

	}


	public static class Cycle {

		private int usage;
		
		private ByteBuffer buffer;

		private byte[] array;

		// optimization blocker
		public int index =0;
		public long var = 0;

		public byte b = 0;

		public Cycle(int capacity, int usage) {
			super();
			//this.capacity = capacity;
			this.usage = usage;
			this.buffer = ByteBuffer.allocate(capacity);
			
			buffer.clear();
			for(int k=0; k<usage; k++) {
				buffer.put((byte) b);
				b+=31;
			}
			buffer.flip();

			array = new byte[usage];
			
			
		}		

		public long round(int nRuns) {
			long time = System.nanoTime();
			for(int i=0; i<nRuns; i++) {
				
				// fill buffer
				buffer.clear();
				for(int k=0; k<usage; k++) {
					buffer.put((byte) k);
				}
				buffer.flip();
				
				// copy to array
				buffer.get(array, 0, usage);
				
				
				// optim block
				var+=array[index];
				index = (index*31+17)%usage;
			}
			return System.nanoTime()-time;
		}

	}

}
