package com.s8.stack.arch.tests.web.http2.perf;

public class TestingByteArrayPerf02 {

	public static void main(String[] args) {
		
		byte[] bytes = new byte[4];
		bytes[0] = 2;
		bytes[1] = 4;
		bytes[2] = 8;
		bytes[3] = 16;
		
		int i=1;
		System.out.println(bytes[++i]);
		
	}
}