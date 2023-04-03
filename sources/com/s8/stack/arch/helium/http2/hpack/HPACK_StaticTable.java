package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;
import com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderMapping;

/**
 * 
 * @author pc
 *
 */
public abstract class HPACK_StaticTable {

	public class Entry extends HPACK_HeaderEntry {

		private int index;
		
		private HTTP2_Header header;
		
		public Entry(int index, HTTP2_Header header) {
			super();
			this.index = index;
			this.header = header;
		}
		
		@Override
		public int getIndex() {
			return index;
		}
		
		@Override
		public HTTP2_Header getHeader() {
			return header;
		}
	}
	
	public final static int OFFSET = 62;
	
	private Entry[] entries;
	
	
	/**
	 * 
	 * @param mapping
	 */
	public HPACK_StaticTable(HTTP2_HeaderMapping mapping) {
		super();
		
		// build headers
		HTTP2_Header[] headers = buildHeaders(mapping);
		int nHeaders = headers.length;
		
		// build entries
		entries = new Entry[OFFSET];
		int index;
		Entry entry;
		for(int i=0; i<nHeaders; i++) {
			index = i+1;
			entry = new Entry(index, headers[i]);
			entries[index] = entry;
			onEntryInserted(entry);
		}
	}
	
	public abstract void onEntryInserted(HPACK_HeaderEntry entry);
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public HTTP2_Header get(int index) {
		return entries[index].getHeader();
	}
	
	
	/**
	 * Static table for HPACK header, as defined in RFC 7541, Appendix A. Static Table Definition
	 */
	private static HTTP2_Header[] buildHeaders(HTTP2_HeaderMapping mapping) {
		
		return new HTTP2_Header[] {
				/* 01 */ mapping.createHeader(":authority"), 
				/* 02 */ mapping.createHeader(":method", "GET"),
				/* 03 */ mapping.createHeader(":method", "POST"),
				/* 04 */ mapping.createHeader(":path", "/"),
				/* 05 */ mapping.createHeader(":path", "/index.html"),
				/* 06 */ mapping.createHeader(":scheme", "HTTP"),
				/* 07 */ mapping.createHeader(":scheme", "HTTPS"),
				/* 08 */ mapping.createHeader(":status", "200"),
				/* 09 */ mapping.createHeader(":status", "204"),
				/* 10 */ mapping.createHeader(":status", "206"),
				/* 11 */ mapping.createHeader(":status", "304"),
				/* 12 */ mapping.createHeader(":status", "400"),
				/* 13 */ mapping.createHeader(":status", "404"),
				/* 14 */ mapping.createHeader(":status", "500"),
				/* 15 */ mapping.createHeader("accept-charset"),
				/* 16 */ mapping.createHeader("accept-encoding"),
				/* 17 */ mapping.createHeader("accept-language"),
				/* 18 */ mapping.createHeader("accept-ranges"),
				/* 19 */ mapping.createHeader("accept"),
				/* 20 */ mapping.createHeader("access-control-allow-origin"),
				/* 21 */ mapping.createHeader("age"),
				/* 22 */ mapping.createHeader("allow"),
				/* 23 */ mapping.createHeader("authorization"),
				/* 24 */ mapping.createHeader("cache-control"),
				/* 25 */ mapping.createHeader("content-disposition"),
				/* 26 */ mapping.createHeader("content-encoding"),
				/* 27 */ mapping.createHeader("content-language"),
				/* 28 */ mapping.createHeader("content-length"),
				/* 29 */ mapping.createHeader("content-location"),
				/* 30 */ mapping.createHeader("content-range"),
				/* 31 */ mapping.createHeader("content-type"),
				/* 32 */ mapping.createHeader("cookie"),
				/* 33 */ mapping.createHeader("date"),
				/* 34 */ mapping.createHeader("etag"),
				/* 35 */ mapping.createHeader("expect"),
				/* 36 */ mapping.createHeader("expires"),
				/* 37 */ mapping.createHeader("from"),
				/* 38 */ mapping.createHeader("host"),
				/* 39 */ mapping.createHeader("if-match"),
				/* 40 */ mapping.createHeader("if-modified-since"),
				/* 41 */ mapping.createHeader("if-none-match"),
				/* 42 */ mapping.createHeader("if-range"),
				/* 43 */ mapping.createHeader("if-unmodified-since"),
				/* 44 */ mapping.createHeader("last-modified"),
				/* 45 */ mapping.createHeader("link"),
				/* 46 */ mapping.createHeader("location"),
				/* 47 */ mapping.createHeader("max-forwards"),
				/* 48 */ mapping.createHeader("proxy-authenticate"),
				/* 49 */ mapping.createHeader("proxy-authorization"),
				/* 50 */ mapping.createHeader("range"),
				/* 51 */ mapping.createHeader("referer"),
				/* 52 */ mapping.createHeader("refresh"),
				/* 53 */ mapping.createHeader("retry-after"),
				/* 54 */ mapping.createHeader("server"),
				/* 55 */ mapping.createHeader("set-cookie"),
				/* 56 */ mapping.createHeader("strict-transport-security"),
				/* 57 */ mapping.createHeader("transfer-encoding"),
				/* 58 */ mapping.createHeader("user-agent"),
				/* 59 */ mapping.createHeader("vary"),
				/* 60 */ mapping.createHeader("via"),
				/* 61 */ mapping.createHeader("www-authenticate")
		};
	}

}
