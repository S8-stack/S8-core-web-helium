package com.s8.stack.arch.tests.web.footprint;

import com.s8.core.web.helium.http2.messages.HTTP2_Message;

public class TestingManyMessages {

	public final static int MAX_NB_INSTANCES = 65536;
	
	public static void main(String[] args) throws InterruptedException {
		HTTP2_Message[] messages = new HTTP2_Message[MAX_NB_INSTANCES];
		for(int i=0; i<MAX_NB_INSTANCES; i++) {
			messages[i] = new HTTP2_Message(null, null);
		}
		Thread.sleep(20000);
	}

}
