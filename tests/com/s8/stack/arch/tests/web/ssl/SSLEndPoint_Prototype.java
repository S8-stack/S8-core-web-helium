package com.s8.stack.arch.tests.web.ssl;

import java.nio.ByteBuffer;

public abstract class SSLEndPoint_Prototype {

	public abstract boolean onReceiving(ByteBuffer buffer);
	
	public abstract boolean onSending(ByteBuffer buffer);
	
	public abstract void resumeReceiving();

	public abstract void resumeSending();
}
