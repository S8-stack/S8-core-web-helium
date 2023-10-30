package com.s8.stack.arch.tests.web.http2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.s8.core.web.helium.http2.hpack.Huffman;


public class HuffmanTest {

	public static void main(String[] args) throws IOException {

		String text1 = "Hi! This is a fu***cking wEird Texté\"è!§é!§èà!&èà with pretty much "+
				"all type os ym01987387647859>%%*ökzeijkzoiejcoijzieKJHLKJN>/POIZ09";

		byte[] textBytes = text1.getBytes();

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		Huffman.encode(textBytes, out1);
		byte[] bytes2 = out1.toByteArray();
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream(32);
		Huffman.decode(bytes2, 0, bytes2.length, out2);
		String text2 = out2.toString();

		System.out.println(text1);
		System.out.println(text2);
		System.out.println(text2.equals(text1));
	}

}
