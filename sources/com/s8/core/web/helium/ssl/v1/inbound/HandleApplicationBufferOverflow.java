package com.s8.core.web.helium.ssl.v1.inbound;


/**
 * 
 */
class HandleApplicationBufferOverflow implements Operation {


	@Override
	public boolean operate(SSL_Inbound in) {

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
		if(in.applicationBuffer.capacity() < in.engine.getSession().getApplicationBufferSize()) {

			/* new capacity first guess */
			int nc = 2 * in.applicationBuffer.capacity();

			/* required capacity */
			int sc = in.engine.getSession().getApplicationBufferSize() + in.applicationBuffer.remaining();

			while(nc < sc) { nc*=2; }

			in.increaseApplicationBufferCapacity(nc);
		}
		/* application buffer is likely to be filled, so drain */
		else if(in.applicationBuffer.position() > in.applicationBuffer.capacity()) {
			
			/* should be drained by next unwrap call */
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * in.applicationBuffer.capacity();

			in.increaseApplicationBufferCapacity(nc);
		}

		/* since no I/O involved, we can immediately retry */
		in.pushOp(new Unwrap());
		return true; // continue
	}

}
