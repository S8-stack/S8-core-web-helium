package com.s8.stack.arch.helium.http2.hpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class HPACK_Data {

	public static final Charset CHARSET = Charset.forName("ISO-8859-1");

	
	/**
	 * position among the HPACK_Data bytes
	 */
	private int index;
	

	/**
	 * 
	 */
	private int limit;
	
	/**
	 * the bytes of the data (per frame)
	 */
	private byte[] bytes;


	public HPACK_Data(byte[] bytes) {
		super();
		this.bytes = bytes;
		this.limit = bytes.length;
		this.index = 0;
	}
	
	public HPACK_Data(byte[] bytes, int offset, int length) {
		super();
		this.bytes = bytes;
		this.index = offset;
		this.limit = offset+length;
	}
	
	
	
	public int getIndex() {
		return index;
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	
	public void rewind(int offset) {
		index = offset;
	}
	
	
	public boolean isLimitNotReached() {
		return index<limit;
	}

	
	public void setCurrentIndexAsLimit() {
		limit = index;
	}
	
	public void flip(int offset) {
		limit = index;
		index = offset;
	}

	/**
	 * return the byte at current position, WITHOUT incrementing position
	 * @return
	 */
	public byte getByte() {
		return bytes[index++];
	}
	
	
	public int getLimit() {
		return limit;
	}
	
	public boolean putByte(byte b) {
		if(index>=limit) return false;
		bytes[index++] = b;
		return true;
	}




	/**
	 * 
	 * @author pc
	 * 
	 * <h1>source: RFC-7541</h1>
	 * <h2>5.1. Integer Representation</h2>
	 * <p>
	 * An integer is represented in two parts: a prefix that fills the
	 * current octet and an optional list of octets that are used if the
	 * integer value does not fit within the prefix. The number of bits of
	 * the prefix (called N) is a parameter of the integer representation.
	 * If the integer value is small enough, i.e., strictly less than 2^N-1,
	 * it is encoded within the N-bit prefix.
	 * </p>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | ? | ? | ? | Value             |
	 * +---+---+---+-------------------+
	 * Figure 2: Integer Value Encoded within the Prefix (Shown for N = 5)
	 * </pre>
	 * <p>
	 * Otherwise, all the bits of the prefix are set to 1, and the value,
	 * decreased by 2^N-1, is encoded using a list of one or more octets.
	 * The most significant bit of each octet is used as a continuation
	 * flag: its value is set to 1 except for the last octet in the list.
	 * The remaining bits of the octets are used to encode the decreased
	 * value.
	 * </p>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | ? | ? | ? | 1   1   1   1   1 |
	 * +---+---+---+-------------------+
	 * | 1 | Value-(2^N-1) LSB         |
	 * +---+---------------------------+
	 *  ...
	 * +---+---------------------------+
	 * | 0 | Value-(2^N-1) MSB         |
	 * +---+---------------------------+
	 * </pre>
	 * <p>
	 * Figure 3: Integer Value Encoded after the Prefix (Shown for N = 5)
	 * Decoding the integer value from the list of octets starts by
	 * reversing the order of the octets in the list. Then, for each octet,
	 * its most significant bit is removed. The remaining bits of the
	 * octets are concatenated, and the resulting value is increased by
	 * 2^N-1 to obtain the integer value.
	 * </p>
	 * 
	 * <p><b>index-friendly</b>, always exit with incremented index, so bytes[i] 
	 * is truly the next byte to read</p>
	 * @param b : the current byte
	 * @param prefixLength
	 * @param maxNbBits : the max nb of bits
	 *
	 */
	public int getInteger(byte b, int prefixLength, int maxNbBits) {


		// current byte (already been read by previous method)

		/* <prefix> */
		int mask = 0xff >> (8-prefixLength);
		int prefix = b & mask;

		/* we have entirely consumed this byte, but we have
		 * already moved to next when calling method getByte.
		 * (see byte-friendly policy)
		 */

		if(prefix < mask) {	
			return prefix; 
		}

		/* </prefix> */

		int shift = 0;
		int result = 0;
		while(shift<maxNbBits) {

			// read current byte and increment i
			b = bytes[index++];


			result |= (b & 0x7F) << shift;

			if((b & 0x80) == 0) {
				return prefix+result;
			}
			else {
				shift+=7;
			}
		}
		throw new ArrayIndexOutOfBoundsException(shift);
	}



	/**
	 * <p><b>index-friendly</b>, always exit with incremented index, so bytes[i] 
	 * 
	 * @param b
	 * @param prefixLength
	 * @param value
	 * @return is Writing successful
	 */
	public void putInteger(byte b, int prefixLength, int value) {

		/* <prefix> */
		int mask = (0xff >> (8-prefixLength));

		
		if(value<mask) {
			b|=value;
			bytes[index++] = b;
			
		}
		else {
			b|=mask;
			value-=mask;
			bytes[index++] = b;
			/* </prefix> */	

			while(value>0x7f) {
				bytes[index++] = (byte) ((value & 0x7f) | 0x80);
				value = value >> 7;	
			}
			bytes[index++] = (byte) value;
		}
	}




	/**
	 * <h1>RFC 7541</h1>
	 * <h2>5.2. String Literal Representation</h2>
	 * <p>
	 * Header field names and header field values can be represented as
	 * string literals. A string literal is encoded as a sequence of
	 * octets, either by directly encoding the string literal’s octets or by
	 * using a Huffman code (see [HUFFMAN]).
	 * </p>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | H | String Length (7+)        |
	 * +---+---------------------------+
	 * | String Data (Length octets)   |
	 * +-------------------------------+
	 * Figure 4: String Literal Representation
	 * </pre>
	 * <p>
	 * A string literal representation contains the following fields:
	 * <ul>
	 * <li>H: A one-bit flag, H, indicating whether or not the octets of the
	 * string are Huffman encoded.</li>
	 * <li>String Length: The number of octets used to encode the string
	 * literal, encoded as an integer with a 7-bit prefix (see Section 5.1).
	 * </li>
	 * <li>String Data: The encoded data of the string literal. If H is ’0’, 
	 * then the encoded data is the raw octets of the string literal. If
	 * H is ’1’, then the encoded data is the Huffman encoding of the string literal.
	 * </li>
	 * </ul>
	 * <p>
	 * String literals that use Huffman encoding are encoded with the
	 * Huffman code defined in Appendix B (see examples for requests in
	 * Appendix C.4 and for responses in Appendix C.6). The encoded data is
	 * the bitwise concatenation of the codes corresponding to each octet of
	 * the string literal.
	 * As the Huffman-encoded data doesn’t always end at an octet boundary,
	 * some padding is inserted after it, up to the next octet boundary. To
	 * prevent this padding from being misinterpreted as part of the string
	 * literal, the most significant bits of the code corresponding to the
	 * EOS (end-of-string) symbol are used.
	 * </p>
	 * <p>
	 * Upon decoding, an incomplete code at the end of the encoded data is
	 * to be considered as padding and discarded. A padding strictly longer
	 * than 7 bits MUST be treated as a decoding error. A padding not
	 * corresponding to the most significant bits of the code for the EOS
	 * symbol MUST be treated as a decoding error. A Huffman-encoded string
	 * literal containing the EOS symbol MUST be treated as a decoding
	 * error.
	 * </p>
	 * <p><b>index-friendly</b>, always exit with incremented index, so bytes[i] 
	 * is truly the next byte to read</p>
	 * @author pc
	 *
	 */

	public final static int MAX_STRING_LENGTH = 1024;

	public String getString() {
		/* <prefix> */

		// read byte
		byte b = bytes[index++];

		boolean isHuffmanEncoded = (b & 0x80)==0x80;
		int length = getInteger(b, 7, 28);

		// boundary checking
		if(length>MAX_STRING_LENGTH) {
			throw new RuntimeException("String length too large");
		}

		/* </prefix> */

		/* <data> */

		// move to next byte

		// consuming String data
		String str = null;
		if(isHuffmanEncoded) {
			try {
				ByteArrayOutputStream decodedStringOutputStream = new ByteArrayOutputStream();
				Huffman.decode(bytes, index, length, decodedStringOutputStream);
				str = decodedStringOutputStream.toString();
				decodedStringOutputStream.close(); // no effect, but pretty...
			}
			catch (IOException e) {
				throw new RuntimeException("Huffman.decode failed");
			}
		}
		else {
			str = new String(bytes, index, length, CHARSET);
		}

		// update position
		index+=length;

		/* string read successfully, see what's next by returning to callback */
		return str;
	}

	
	
	

	/**
	 * 
	 * @param str
	 * @param isHuffmanEncoded
	 * @return a flag indicating if the writing has been successful
	 */
	public void putString(String str, boolean isHuffmanEncoded) {
		
		if(str==null) {
			throw new RuntimeException("[HPACH_Data] null string");
		}
		byte[] strBytes = str.getBytes(CHARSET);
		if(strBytes.length==0) {
			throw new RuntimeException("[HPACH_Data] empty string");
		}

		if(isHuffmanEncoded) {
			try {
				ByteArrayOutputStream encodedOutputStream = new ByteArrayOutputStream();	
				Huffman.encode(strBytes, encodedOutputStream);
				strBytes = encodedOutputStream.toByteArray();
			} 
			catch (IOException e) {
				throw new RuntimeException("[HPACH_Data] Huffman compression failed");
			}
		}
		int length = strBytes.length;
		
		/* <prefix> */
		byte b = (byte) (isHuffmanEncoded ? 0x80 : 0x00);
		
		// try to write integer
		putInteger(b, 7, length);
		
		// push
		for(int j=0; j<length; j++) {
			bytes[index++] = strBytes[j];
		}
	}
}
