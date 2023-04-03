package com.s8.stack.arch.helium.http2.settings;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.frames.HTTP2_SETTINGS_Frame;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * 
 * [RFC 7540 / 6.5.2. Defined SETTINGS Parameters]
 * <p>
 * The payload of a SETTINGS frame consists of zero or more parameters, each consisting 
 * of an unsigned 16-bit setting identifier and an unsigned 32-bit value.
 * </p>
 * @author pc
 *
 */
public class HTTP2_Settings {

	public static class Entry {
		
		/**
		 * /!\ Implementation restriction on the norm: only support 15-bits int rather
		 * than uint16 as specified. Since settings code do not exceed 8-bits capabilities,
		 * not a problem.
		 */
		public short code;
		

		/**
		 * /!\ Implementation restriction on the norm: only support 31-bits int rather
		 * than uint32 as specified. Since settings values tends do not exceed 16-bits capabilities,
		 * not seen as a problem.
		 */
		public int value;
		
		public Entry() {
		}
		
		public Entry(short code, int value) {
			super();
			this.code = code;
			this.value = value;
		}
		
		public Entry(int code, int value) {
			super();
			this.code = (short) code;
			this.value = value;
		}
		
		public void parse(BytesBlock bytes, int offset) {
			code = bytes.getUInt15(offset);
			value = bytes.getUInt31(offset+2);
		}
		
		public void compose(BytesBlock bytes, int offset) {
			bytes.setUInt15(offset, code);
			bytes.setUInt31(offset+2, value);
		}
		
	}
	
	/**
	 * <h1><b>HEADER_TABLE_SIZE</b> code: (0x1)</h1>
	 * <p>
	 * Allows the sender to inform the remote endpoint of the maximum 
	 * size of the header compression table used to decode header blocks, 
	 * in octets. The encoder can select any size equal to or less than 
	 * this value by using signaling specific to the header compression 
	 * format inside a header block (see [COMPRESSION]). The initial 
	 * value is 4,096 octets.
	 * </p>
	 */
	public int headerTableSize = 4096;


	/**
	 * <h1><b>ENABLE_PUSH</b> code: (0x2)</h1>
	 * <p>
	 * This setting can be used to disable server push (Section 8.2). 
	 * An endpoint MUST NOT send a PUSH_PROMISE frame if it receives 
	 * this parameter set to a value of 0. An endpoint that has both 
	 * set this parameter to 0 and had it acknowledged MUST treat the 
	 * receipt of a PUSH_PROMISE frame as a connection error 
	 * (Section 5.4.1) of type PROTOCOL_ERROR. The initial value is 1, 
	 * which indicates that server push is permitted. Any value other than 0 or 1 MUST be treated as a
	 * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 * </p>
	 */
	public int enablePush = 1;


	/**
	 * <h1><b>MAX_CONCURRENT_STREAMS</b>, code: (0x3)</h1>
	 * <p>
	 * Indicates the maximum number of concurrent streams that the sender will allow. 
	 * This limit is directional: it applies to the number of streams that the sender
	 * permits the receiver to create. Initially, there is no limit to this value. 
	 * It is recommended that this value be no smaller than 100, so as to not 
	 * unnecessarily limit parallelism. 
	 * A value of 0 for SETTINGS_MAX_CONCURRENT_STREAMS SHOULD NOT be treated as 
	 * special by endpoints. A zero value does prevent the creation of new streams; 
	 * however, this can also happen for any limit that is exhausted with active 
	 * streams. Servers SHOULD only set a zero value for short durations; 
	 * if a server does not wish to accept requests, 
	 * closing the connection is more appropriate.
	 * </p>
	 */
	public int maxConcurrentStreams = 128;


	/**
	 * <h1><b>INITIAL_WINDOW_SIZE</b>, code: (0x4)</h1>
	 * <p>
	 * Indicates the sender’s initial 
	 * window size (in octets) for stream-level flow control. The initial value is 
	 * 2^16-1 (65,535) octets. This setting affects the window size of all 
	 * streams (see Section 6.9.2).
	 * </p>
	 */
	public int initialWindowSize = 65535;



	/**
	 * <h1><b>MAX_FRAME_SIZE</b>, code: (0x5)</h1>
	 * <p>
	 * Indicates the size of the largest frame payload that the sender 
	 * is willing to receive, in octets. The initial value is 2^14 (16,384) 
	 * octets.
	 * </p>
	 */
	public int maxFrameSize = 16384;



	/**
	 * <h1>code: (0x6)</h1>
	 * <p>
	 * This advisory setting informs a peer of the maximum size of header 
	 * list that the sender is prepared to accept, in octets. The value is 
	 * based on the uncompressed size of header fields, including the 
	 * length of the name and value in octets plus an overhead of 32 
	 * octets for each header field. For any given request, a lower 
	 * limit than what is advertised MAY be enforced. The initial value of 
	 * this setting is unlimited.
	 * </p>
	 * <p>
	 * Coder's edit: We don't like unlimited settings for protocol (security), so 
	 * initial value intentionally set to quite low figure.
	 * </p>
	 */
	public int MAX_HEADER_LIST_SIZE = 1024;
	
	
	
	public HTTP2_Error set(Entry entry, boolean isVerbose) {
		return set(entry.code, entry.value, isVerbose);
	}

	public HTTP2_Error set(short code, int value, boolean isVerbose) {

		switch(code) {

		case 0x1: 
			if(value<32) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] HEADER_TABLE_SIZE is now set to "+value);
			}
			headerTableSize = value;
			return HTTP2_Error.NO_ERROR;

			
		case 0x2: 
			if(value!=0 && value !=1) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] ENABLE_PUSH is now set to "+value);
			}
			enablePush = value;
			return HTTP2_Error.NO_ERROR;

			
		case 0x3: 
			if(value<2|| value>1024) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] MAX_CONCURRENT_STREAMS is now set to "+value);
			}
			maxConcurrentStreams = value;
			return HTTP2_Error.NO_ERROR;

			
		case 0x4: 
			/* Values above the maximum flow-control window size of 2^31-1 MUST 
			 * be treated as a connection error (Section 5.4.1) of type FLOW_CONTROL_ERROR.
			 */
			if(value>2147483647) { // 2^31-1
				return HTTP2_Error.FLOW_CONTROL_ERROR;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] INITIAL_WINDOW_SIZE is now set to "+value);
			}
			initialWindowSize = value; 
			return HTTP2_Error.NO_ERROR;

			
		case 0x5: 
			/* The value advertised by an endpoint MUST be between this initial value 
			 * and the maximum allowed frame size (2^24-1 or 16,777,215 octets), 
			 * inclusive. Values outside this range MUST be treated as a connection 
			 * error (Section 5.4.1) of type PROTOCOL_ERROR.
			 */
			if(value<16384 || value>16777215) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] MAX_FRAME_SIZE is now set to "+value);
			}
			maxFrameSize = value;
			return HTTP2_Error.NO_ERROR;

			
		case 0x6:
			if(isVerbose) {
				System.out.println("[HTTP2_Settings] MAX_HEADER_LIST_SIZE is now set to "+value);
			}
			MAX_HEADER_LIST_SIZE = value;
			return HTTP2_Error.NO_ERROR;

			
		default:
			/* An endpoint that receives a SETTINGS frame with any unknown or
			 * unsupported identifier MUST ignore that setting.
			 */
			return HTTP2_Error.NO_ERROR;
		}
	}
	
	
	
	
	public HTTP2_SETTINGS_Frame getFrame() {
		/*
		 * Mac browser is behaving like this: 
		 * INITIAL_WINDOW_SIZE is now set to 65535
		 * MAX_CONCURRENT_STREAMS is now set to 100
		 */
		HTTP2_Settings.Entry[] entries = new HTTP2_Settings.Entry[] {
				new Entry(0x4, initialWindowSize),
				new Entry(0x3, maxConcurrentStreams)
		};
		return new HTTP2_SETTINGS_Frame(entries);
	}
}
