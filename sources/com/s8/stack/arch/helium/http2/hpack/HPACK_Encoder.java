package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;

public class HPACK_Encoder extends HPACK_EncodingSpace {
	

	private boolean isVerbose;

	private boolean isHuffmanEncoding = true;

	

	public HPACK_Encoder(HPACK_Context context, int dynamicTableMaxSize, boolean isHuffmanEncoding, boolean isVerbose) {
		super(context, dynamicTableMaxSize);
		
		this.isVerbose = isVerbose;
		this.isHuffmanEncoding = isHuffmanEncoding;
	}

	public void encode(HPACK_Data data, HTTP2_Header... headers) {
		for(HTTP2_Header header : headers) {
			encode(data, header);
		}
	}

	/**
	 * Find best possible encoding
	 * @param header
	 * @return is writing successful
	 */
	public boolean encode(HPACK_Data data, HTTP2_Header header) {

		if(isVerbose) {
			System.out.println("[HPACK] EncodingTable: start to encode "+header+" ...");
		}
		
		HTTP2_Header.Prototype type = header.getPrototype();
		

		/* most favorable: direct index */
		switch(type.behavior) {
		case STATIC_OVER_CONNECTION:
		case FEW_STATES:

			int index = findMatching(header);
			if(index>=0) {
				if(isVerbose) {
					System.out.println("\tchoose IndexedHeader with index = "+index);
				}
				return encodeIndexedHeader(index, data);
			}

		case ALWAYS_RENEWED:
		default:

			/* At this point, we can only go for literal encoding */			
			return forkLiteralEncoding(header, data);
			
		}
	}

	


	private boolean encodeIndexedHeader(int index, HPACK_Data data) {
		data.putInteger((byte) 0x80, 7, index);
		return true;
	}


	private boolean forkLiteralEncoding(HTTP2_Header header, HPACK_Data data) {

		switch(header.getPrototype().behavior) {
		case STATIC_OVER_CONNECTION:
		case FEW_STATES:
			if(isVerbose) {
				System.out.print("\tchoose IncrementalIndexing with ");
			}
			
			// encode
			encodeLiteralHeader((byte) 0x40, 6, header, data);
			
			// THEN store (order is significant)
			addHeader(header);
			return true;
			
		case ALWAYS_RENEWED:
			if(isVerbose) {
				System.out.print("\tchoose NeverIndexed with ");
			}
			encodeLiteralHeader((byte) 0x10, 4, header, data);
			return true;
		}
		return false;
	}




	/**
	 * 
	 * @param header
	 * @param data
	 */
	private void encodeLiteralHeader(byte b, int prefixLength, HTTP2_Header header, HPACK_Data data) {

	
		int index = findName(header);

		/* <name> */
		if(index>=0) {
			if(isVerbose) {
				System.out.print("[HPACK_Encoding] indexed name with index="+index+"\n");
			}
			/* intermediate : only sharing header's name index */
			data.putInteger(b, prefixLength, index);
		}
		else {
			if(isVerbose) {
				System.out.print("[HPACK_Encoding] new name \n");
			}
			/* least favourable: complete encoding */
			data.putByte(b);
			data.putString(header.getName(), isHuffmanEncoding);
		}
		/* </name> */
		/* <value> */

		data.putString(header.getValue(), isHuffmanEncoding);

		/* </value> */
	}

	
	
	
}
