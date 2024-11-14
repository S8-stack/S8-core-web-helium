package com.s8.core.web.helium.ssl.v1.inbound;


/**
 * 
 */
class HandleNetworkBufferUnderflow implements SSL_Inbound.Operation {


	@Override
	public void operate(SSL_Inbound in) {
		
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
		}
		/* network buffer is likely to need more inbound data */
		else if(in.networkBuffer.remaining() < in.networkBuffer.capacity() / 2) {
			/* nothing to do */
		}
		else {
			/* new capacity first guess */
			int nc = 2 * in.networkBuffer.capacity();
			in.increaseNetworkBufferCapacity(nc);
		}

		/* in any case, need to load more inbound data in networkBuffer */
		in.receive();
	}

	

}
