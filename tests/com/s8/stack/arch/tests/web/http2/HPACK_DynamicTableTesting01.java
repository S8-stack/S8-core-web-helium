package com.s8.stack.arch.tests.web.http2;

import com.s8.core.web.helium.http2.headers.HTTP2_HeaderMapping;
import com.s8.core.web.helium.http2.hpack.HPACK_DynamicTable;
import com.s8.core.web.helium.http2.hpack.HPACK_HeaderEntry;

public class HPACK_DynamicTableTesting01 {

	public static void main(String[] args) {

		HPACK_DynamicTable dynamicTable = new HPACK_DynamicTable(4096) {

			@Override
			public void onClearEntries() {
				System.out.println("[callback] clear entries");
			}

			@Override
			public void onEntryInserted(HPACK_HeaderEntry entry) {
				System.out.println("[callback] entry inserted: "+entry.getHeader());	
			}

			@Override
			public void onEntryDropped(HPACK_HeaderEntry entry) {
				System.out.println("[callback] entry dropped: "+entry.getHeader());	
			}
		};
		
		HTTP2_HeaderMapping mapping = new HTTP2_HeaderMapping(true);
		for(int i=0; i<256; i++) {
			dynamicTable.add(mapping.createHeader("content-type", Integer.toString(i)));
		}
		System.out.println(dynamicTable.get(255).getValue());
		System.out.println(dynamicTable.get(128).getValue());
		System.out.println(dynamicTable.get(0).getValue());
	}

}
