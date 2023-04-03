package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;

/**
 * 
 * @author pc
 *
 */
public class HPACK_Decoder extends HPACK_DecodingSpace {
	
	protected boolean isVerbose;
	
	private HPACK_Context context;
	

	/**
	 * 
	 * @param request: The process currently built and on which decoded header fields are appended
	 */
	public HPACK_Decoder(HPACK_Context context, int dynamicTableMaxSize, boolean isVerbose) {
		super(context, dynamicTableMaxSize);
		this.context = context;
		this.isVerbose = isVerbose;
	}

	public interface Callback {
		
		public void onDecoded(HTTP2_Header header);
	}

	
	public void decode(HPACK_Data data, Callback callback) {
		while(data.isLimitNotReached()) {
			forkHeader(data, callback);
		}
	}

	private void forkHeader(HPACK_Data data, Callback callback) {

		byte b = data.getByte();

		if((b & 0x80)==0x80) { // bits-pattern: 1*******
			decodeIndexedHeader(b, data, callback);
		}
		else if((b & 0xc0)==0x40) { // bits-pattern: 01******
			decodeIncrementalIndexingLiteralHeader(b, data, callback);
		}
		else if((b & 0xf0)==0x00) { // bits-pattern: 0000****
			decodeWithoutIndexingLiteralHeader(b, data, callback);
		}
		else if((b & 0xf0)==0x10) { // bits-pattern: 0001****
			decodeNeverIndexedLiteralHeader(b, data, callback);
		}
		else if((b & 0xe0)==0x20) { // bits-pattern: 001*****
			onTableSizeUpdate(b, data);
		}
		else {
			throw new RuntimeException("[HPACK_Decoder] Unsupported bit-pattern at forking");
		}
	}	




	/**
	 * 
	 * <p>
	 * An indexed header field representation identifies an entry in either the static 
	 * table or the dynamic table (see Section 2.3). An indexed header field 
	 * representation causes a header field to be added to the decoded header 
	 * list, as described in Section 3.2.
	 * </p>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | 1 | Index (7+)                |
	 * +---+---------------------------+
	 *  Figure 5: Indexed Header Field
	 * </pre>
	 * <p>
	 * An indexed header field starts with the ’1’ 1-bit pattern, followed
	 * by the index of the matching header field, represented as an integer 
	 * with a 7-bit prefix (see Section 5.1). 
	 * The index value of 0 is not used. It MUST be treated as a decoding
	 * error if found in an indexed header field representation.
	 * </p>
	 * @author pc
	 *
	 */
	private void decodeIndexedHeader(byte b, HPACK_Data data, Callback callback) {
		int index = data.getInteger(b, 7, 28);
		HTTP2_Header header = getHeader(index);
		callback.onDecoded(header);
	}


	/**
	 * <h1> [RFC-7541/6.2.]  Literal Header Field Representation </h1>
	 * <p>
	 * A literal header field representation contains a literal header field value. 
	 * Header field names are provided either as a literal or by reference to an 
	 * existing table entry, either from the static table or the dynamic table 
	 * (see Section 2.3).
	 * This specification defines three forms of literal header field 
	 * representations: with indexing, without indexing, and never indexed.
	 * </p>
	 */

	private HTTP2_Header decodeLiteralHeader(byte b, int prefixLength, HPACK_Data data){

		int index = data.getInteger(b, prefixLength, 28);

		// build header from index/name
		HTTP2_Header header = null;

		/* Figure 7: Literal Header Field with Incremental Indexing -- New Name */
		if(index==0) {

			// reading name literal
			String name = data.getString();
			header = context.getPrototype(name).create();	
			
		}
		/* Figure 6: Literal Header Field with Incremental Indexing -- Indexed Name */
		else {
			HTTP2_Header referencedHeader = getHeader(index);
			header = referencedHeader.getPrototype().create();
		}

		// set header value
		header.setValue(data.getString());

		return header;
	}


	/** 
	 * <h1>  Literal Header Field with Incremental Indexing </h1>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | 0 | 1 | Index (6+)            |
	 * +---+---+-----------------------+
	 * | H | Value Length (7+)         |
	 * +---+---------------------------+
	 * | Value String (Length octets)  |
	 * +-------------------------------+
	 * Figure 6: Literal Header Field with Incremental Indexing -- Indexed Name
	 * </pre> 
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | 0 | 1 | 0                     |
	 * +---+---+-----------------------+
	 * | H | Name Length (7+)          |
	 * +---+---------------------------+
	 * | Name String (Length octets)   |
	 * +---+---------------------------+
	 * | H | Value Length (7+)         |
	 * +---+---------------------------+
	 * | Value String (Length octets)  |
	 * +-------------------------------+
	 * Figure 7: Literal Header Field with Incremental Indexing -- New Name
	 * </pre>
	 * <p>
	 * If the header field name matches the header field name of an entry 
	 * stored in the static table or the dynamic table, the header field 
	 * name can be represented using the index of that entry. In this case, 
	 * the index of the entry is represented as an integer with a 6-bit 
	 * prefix (see Section 5.1). This value is always non-zero.
	 * </p>
	 * <p>
	 * Otherwise, the header field name is represented as a string literal 
	 * (see Section 5.2). A value 0 is used in place of the 6-bit index, 
	 * followed by the header field name.
	 * </p>
	 * <p>
	 * Either form of header field name representation is followed by the 
	 * header field value represented as a string literal (see Section 5.2).
	 * </p>
	 * @author pc
	 *
	 * @param b : forking byte
	 * @param data : headers data
	 */
	private void decodeIncrementalIndexingLiteralHeader(byte b, HPACK_Data data, Callback callback) {

		HTTP2_Header header = decodeLiteralHeader(b, 6, data);
		
		if(header!=null) {
			// store in the space
			addHeader(header);

			callback.onDecoded(header);	
		}		
	}


	/**
	 * <h1>[RFC-7541/6.2.2.] Literal Header Field without Indexing</h1>
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | 0 | 0 | 0 | 0 |  Index (4+)   |
	 * +---+---+-----------------------+
	 * | H |     Value Length (7+)     |
	 * +---+---------------------------+
	 * | Value String (Length octets)  |
	 * +-------------------------------+
	 * Figure 8: Literal Header Field without Indexing -- Indexed Name
	 * </pre>
	 * 
	 * <pre>
	 *   0   1   2   3   4   5   6   7
	 * +---+---+---+---+---+---+---+---+
	 * | 0 | 0 | 0 | 0 |       0       |
	 * +---+---+-----------------------+
	 * | H |     Name Length (7+)      |
	 * +---+---------------------------+
	 * |  Name String (Length octets)  |
	 * +---+---------------------------+
	 * | H |     Value Length (7+)     |
	 * +---+---------------------------+
	 * | Value String (Length octets)  |
	 * +-------------------------------+
	 * Figure 9: Literal Header Field without Indexing -- New Name
	 * </pre>
	 * <p>
	 * A literal header field without indexing representation starts with the '0000'
	 * 4-bit pattern.
	 * </p>
	 * <p>
	 * If the header field name matches the header field name of an entry stored in
	 * the static table or the dynamic table, the header field name can be
	 * represented using the index of that entry. In this case, the index of the
	 * entry is represented as an integer with a 4-bit prefix (see Section 5.1).
	 * This value is always non-zero.
	 * </p>
	 * <p>
	 * Otherwise, the header field name is represented as a string literal (see
	 * Section 5.2). A value 0 is used in place of the 4-bit index, followed by the
	 * header field name. Either form of header field name representation is followed 
	 * by the header field value represented as a string literal (see Section 5.2).
	 * </p>
	 *
	 * @param b : forking byte
	 * @param data : headers data
	 */
	private void decodeWithoutIndexingLiteralHeader(byte b, HPACK_Data data, Callback callback) {

		HTTP2_Header header = decodeLiteralHeader(b, 4, data);

		// no store in the context

		callback.onDecoded(header);
	}

	/**
	 * <h1>[RFC-7541/6.2.3.]  Literal Header Field Never Indexed</h1>
	 * <p>
	 * A literal header field never-indexed representation starts with the
	 * '0001' 4-bit pattern. .
	 * </p>
	 * @see WithoutIndexing
	 * @author pc
	 *
	 *
	 * @param b : forking byte
	 * @param data : headers data
	 */
	private void decodeNeverIndexedLiteralHeader(byte b, HPACK_Data data, Callback callback) {

		HTTP2_Header header = decodeLiteralHeader(b, 4, data);

		// no store in the context

		callback.onDecoded(header);
	}


	/**
	 * 
	 * @param b
	 * @param data
	 */
	private void onTableSizeUpdate(byte b, HPACK_Data data) {
		int newSize = data.getInteger(b, 5, 28);
		// TODO do sthg with size
		if(isVerbose) {
			System.out.println("[HPACK_Decoder]: table size update request to "+newSize+", performed");	
		}
		resize(newSize);
	}
	
}
