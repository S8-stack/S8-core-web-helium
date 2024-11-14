package com.s8.core.web.helium.ssl.v1.outbound;



public class HandleNetworkBufferOverflow implements SSL_Outbound.Operation {

	@Override
	public void operate(SSL_Outbound out) {
		/**
		 * Could attempt to drain the destination (application) buffer of any already obtained data, 
		 * but we'll just increase it to the size needed.
		 */

		/**
		 * From Javadoc: 
		 * For example, unwrap() will return a SSLEngineResult.Status.BUFFER_OVERFLOW result if the engine 
		 * determines that there is not enough destination buffer space available. Applications should 
		 * call SSLSession.getApplicationBufferSize() and compare that value with the space available in 
		 * the destination buffer, enlarging the buffer if necessary
		 */
		if(out.networkBuffer.capacity() < out.engine.getSession().getPacketBufferSize()) {

			/* new capacity first guess */
			int nc = 2 * out.networkBuffer.capacity();

			/* required capacity */
			int sc = out.engine.getSession().getPacketBufferSize() + out.networkBuffer.remaining();

			while(nc < sc) { nc*=2; }

			out.increaseNetwordBufferCapacity(nc);

			out.pushOp(new Wrap());
		}
		/* application buffer is likely to be filled, so drain */
		else if(out.networkBuffer.position() > out.networkBuffer.capacity()) {
			/* nothing to do */
			out.send();
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * out.applicationBuffer.capacity();

			out.increaseApplicationBufferCapacity(nc);

			out.pushOp(new Wrap());
		
		}

	}

}
