package com.s8.core.web.helium.ssl.v1.outbound;



/**
 * 
 */
public class HandleApplicationBufferUnderflow implements SSL_Outbound.Operation {

	@Override
	public void operate(SSL_Outbound out) {
		
		/**
		 *
		 */
		if(out.applicationBuffer.capacity() < out.engine.getSession().getApplicationBufferSize()) {
			/* new capacity first guess */
			int nc = 2 * out.applicationBuffer.capacity();

			/* required capacity */
			int sc = out.engine.getSession().getPacketBufferSize();

			while(nc < sc) { nc*=2; }

			out.increaseApplicationBufferCapacity(nc);
		}
		/* application buffer is likely to need more incoming data */
		else if(out.applicationBuffer.position() < out.applicationBuffer.capacity() / 2) {
			/* nothing to do */
		}
		else {
			/* new capacity first guess */
			int nc = 2 * out.networkBuffer.capacity();
			out.increaseApplicationBufferCapacity(nc);
		}

		out.pump();

		out.pushOp(new Wrap());
	}

}
