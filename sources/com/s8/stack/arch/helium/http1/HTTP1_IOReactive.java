package com.s8.stack.arch.helium.http1;

import java.nio.ByteBuffer;

public interface HTTP1_IOReactive {
	
	public static enum Result {

		/**
		 * Successfully parsed or compose message
		 */
		OK,
		
		
		/**
		 * 
		 */
		NEED_MORE_BYTES,
		
		/**
		 * 
		 */
		ERROR;
	}

	public Result onBytes(ByteBuffer buffer);
	

	public static class Compound implements HTTP1_IOReactive {

		private int i=0;

		private HTTP1_IOReactive[] parsables;

		public Compound(HTTP1_IOReactive[] parsables) {
			super();
			this.parsables = parsables;
			this.i = 0;
		}

		@Override
		public Result onBytes(ByteBuffer buffer) {
			int n = parsables.length;
			Result result = Result.OK;
			while(result==Result.OK && i<n) {
				result = parsables[i].onBytes(buffer);
				if(result==Result.OK) {
					i++;
				}
			}
			return result;
		}
	}
}
