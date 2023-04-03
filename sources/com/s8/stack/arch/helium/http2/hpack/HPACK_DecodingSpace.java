package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;

/**
 * <p>
 * HPACK uses two tables for associating header fields to indexes. The static
 * table (see Section 2.3.1) is predefined and contains common header fields
 * (most of them with an empty value). The dynamic table (see Section 2.3.2) is
 * dynamic and can be used by the encoder to index header fields repeated in the
 * encoded header lists.
 * </p>
 * <p>
 * These two tables are combined into a single address space for defining index
 * values (see Section 2.3.3).
 * </p>
 * 
 * @author pc
 *
 */
public class HPACK_DecodingSpace {


	private HPACK_StaticTable staticTable;

	private HPACK_DynamicTable dynamicTable;

	public HPACK_DecodingSpace(HPACK_Context context, int dynamicTableMaxSize) {
		super();
		
		staticTable = new HPACK_StaticTable(context.mapping) {
			public @Override void onEntryInserted(HPACK_HeaderEntry entry) {
			}
		};
		
		dynamicTable = new HPACK_DynamicTable(dynamicTableMaxSize) {
			public @Override void onClearEntries() { /* do nothing */ }
			public @Override void onEntryInserted(HPACK_HeaderEntry entry) { /* do nothing */ }
			public @Override void onEntryDropped(HPACK_HeaderEntry entry) { /* do nothing */ }
		};
		
	}

	public void addHeader(HTTP2_Header header) {
		dynamicTable.add(header);
	}

	public HTTP2_Header getHeader(int index) {
		if(index<HPACK_StaticTable.OFFSET) {
			return staticTable.get(index);
		}
		else {
			return dynamicTable.get(index-HPACK_StaticTable.OFFSET);
		}
	}


	public void resize(int dynamicTableMaxSize) {
		dynamicTable.resize(dynamicTableMaxSize);
	}
}
