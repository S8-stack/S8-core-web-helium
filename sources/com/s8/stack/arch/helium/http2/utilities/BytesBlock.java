package com.s8.stack.arch.helium.http2.utilities;

import java.nio.ByteBuffer;

/**
 * Accumulate bytes by auto-increasing underlying byte[] array length
 * 
 * @author pc
 *
 */
public class BytesBlock {

	private byte[] bytes;

	private int index;

	private int length;

	public BytesBlock() {
		super();
		initialize(64);
	}

	public BytesBlock(int length) {
		super();
		initialize(length);
	}

	public BytesBlock(byte[] bytes) {
		super();
		length = bytes.length;
		this.bytes = bytes;
		this.index = 0;
	}


	private void initialize(int length) {
		this.length = length;
		this.bytes = new byte[length];
		this.index = 0;
	}


	/**
	 * 
	 * @param delta
	 */
	public void extend(int delta) {
		int extendedLength = length+delta;
		byte[] extendedArray = new byte[extendedLength];
		for(int i=0; i<length; i++) {
			extendedArray[i] = bytes[i];
		}
		bytes = extendedArray;
		length = extendedLength;
	}


	public void push(byte b) {
		bytes[index] = b;
		index++;
	}


	/**
	 * 
	 * @param buffer
	 */
	public boolean pullFromBuffer(ByteBuffer buffer) {
		int nPushableBytes = Math.min(buffer.remaining(), length-index);
		buffer.get(bytes, index, nPushableBytes);
		index+=nPushableBytes;
		return index == length;
	}


	/**
	 * <h2> /!\ No capacity checking before pushing </h2>
	 * Insert the bytes in the builder
	 * @param bytes
	 * @return
	 */
	public void push(byte[] bytes) {
		int nb = bytes.length;
		for(int i=0; i<nb; i++) {
			bytes[index+i] = bytes[i];
		}
		index+=nb;
	}


	/**
	 * write as much bytes as possible in the buffer 
	 * @param buffer
	 */
	public boolean pushToBuffer(ByteBuffer buffer) {
		int n = Math.min(buffer.remaining(), length-index);
		buffer.put(bytes, index, n);
		index+=n;
		return index==length;
	}


	public boolean isFilled() {
		return length==index;
	}



	public int getUInt31(int index) {
		/*
		 * When packing signed bytes into an int, each byte needs to be masked off
		 * because it is sign-extended to 32 bits (rather than zero-extended) due to the
		 * arithmetic promotion rule (described in JLS, Conversions and Promotions).
		 */
		return (int) (
				(bytes[index+0] & 0x7f) << 24 | 
				(bytes[index+1] & 0xff) << 16 | 
				(bytes[index+2] & 0xff) << 8 | 
				(bytes[index+3] & 0xff));
	}


	public void setUInt31(int index, int value) {
		bytes[index+0] = (byte) (0x7f & (value >> 24)); // high byte
		bytes[index+1] = (byte) (0xff & (value >> 16));
		bytes[index+2] = (byte) (0xff & (value >> 8));
		bytes[index+3] = (byte) (0xff & value); // low byte
	}


	public int getUInt24(int index) {
		return (int) (
				(bytes[index+0] & 0xff) << 16 | 
				(bytes[index+1] & 0xff) << 8 | 
				(bytes[index+2] & 0xff));
	}

	public void setUInt24(int index, int value) {
		bytes[index+0] = (byte) (0xff & (value >> 16));
		bytes[index+1] = (byte) (0xff & (value >> 8));
		bytes[index+2] = (byte) (0xff & value); // low byte
	}


	public short getUInt15(int index) {
		return (short) (
				(bytes[index+0] & 0x7f) << 8 | 
				(bytes[index+1] & 0xff));
	}


	public void setUInt15(int index, int value) {
		bytes[index+0] = (byte) (0x7f & (value >> 8));
		bytes[index+1] = (byte) (0xff & value); // low byte
	}

	public int getUInt8(int index) {
		return (0xff & bytes[index]);
	}


	public void setUInt8(int index, int value) {
		bytes[index] = (byte) (value & 0xff);
	}


	public byte getByte(int index) {
		return bytes[index];
	}

	public void setByte(int index, byte b) {
		bytes[index] = b;
	}

	public void setBytes(int index, byte[] bytes, int offset, int length) {
		for(int i=0; i<length; i++) {
			this.bytes[index+i] = bytes[offset+i];
		}
	}


	public byte[] getBytes() {
		return bytes;
	}

	
	public byte[] trim(int index) {
		int length = bytes.length-index;
		byte[] trimmedBytes = new byte[length];
		for(int i=0; i<length; i++) {
			trimmedBytes[i] = bytes[i+index];
		}
		return trimmedBytes;
	}
	
	public byte[] trim(int index, int length) {
		byte[] trimmedBytes = new byte[length];
		for(int i=0; i<length; i++) {
			trimmedBytes[i] = bytes[i+index];
		}
		return trimmedBytes;
	}



}
