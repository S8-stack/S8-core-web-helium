package com.s8.core.web.helium.ssl.v1.inbound;


/**
 * 
 */
class HandleNetworkBufferUnderflow{


	
	/**
	 * 
	 * @param in
	 * @return true if receive is required
	 */
	public boolean operate(SSL_Inbound in) {
		
		/**
		 *
		 */
		if(in.networkBuffer.capacity() < in.engine.getSession().getPacketBufferSize()) {
			/* new capacity first guess */
			int nc = 2 * in.networkBuffer.capacity();

			/* required capacity */
			int sc = in.engine.getSession().getPacketBufferSize();

			while(nc < sc) { nc*=2; }

			in.increaseNetworkBufferCapacity(nc);
			
			return false;
		}
		/* network buffer is likely to need more inbound data */
		else if(in.networkBuffer.remaining() < in.networkBuffer.capacity() / 2) {
			/* nothing to do */
			
			/* in any case, need to load more inbound data in networkBuffer */
			return true;
		}
		else {
			/* new capacity first guess */
			int nc = 2 * in.networkBuffer.capacity();
			in.increaseNetworkBufferCapacity(nc);
			
			return false;
		}	
	}

}
