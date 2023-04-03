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
public class HPACK_EncodingSpace {


	public static class Bucket {

		public HPACK_HeaderEntry head;

		public HPACK_HeaderEntry tail;

		public void append(HPACK_HeaderEntry entry) {
			if(tail!=null) {
				tail.next = entry;
				entry.previous = tail;
				tail = entry;
			}
			else {
				head = entry;
				tail = entry;
			}
		}

		public void remove(HPACK_HeaderEntry entry) {
			entry.relink();

			if(entry.previous==null) { // entry is the current head
				head = entry.next;
			}
			if(entry.next==null) { // entry is the current tail
				tail = entry.previous;
			}
		}
		
		public int getFirstIndex() {
			if(head!=null) {
				return head.getIndex();
			}
			else {
				return -1;
			}
		}
		
		public int getMatchingIndex(String value) {
			HPACK_HeaderEntry entry = head;
			while(entry!=null) {
				if(entry.getHeader().isValueEqualTo(value)) {
					return entry.getIndex();
				}
				entry = entry.next;
			}
			return -1;
		}
	}



	public final static int NB_MAP_BUCKETS = 256;
	

	private HPACK_StaticTable staticTable;

	private HPACK_DynamicTable dynamicTable;

	private Bucket[] buckets;

	public HPACK_EncodingSpace(HPACK_Context context, int dynamicTableMaxSize) {
		super();
		
		clear();
		
		staticTable = new HPACK_StaticTable(context.mapping) {
			public @Override void onEntryInserted(HPACK_HeaderEntry entry) {
				append(entry);
			}
		};
		
		dynamicTable = new HPACK_DynamicTable(dynamicTableMaxSize) {
			public @Override void onClearEntries() {
				clear();
			}

			public @Override void onEntryInserted(HPACK_HeaderEntry entry) {
				append(entry);
			}

			public @Override void onEntryDropped(HPACK_HeaderEntry entry) {
				remove(entry);
			}
			
		};		
	}

	
	public void addHeader(HTTP2_Header header) {
		dynamicTable.add(header);
	}

	
	/**
	 * Not used (for legacy)
	 * 
	 * @param index
	 * @return
	 */
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

	
	private void append(HPACK_HeaderEntry entry) {
		Bucket bucket = buckets[entry.getCode()];
		bucket.append(entry);
	}

	private void remove(HPACK_HeaderEntry entry) {
		Bucket bucket = buckets[entry.getCode()];
		bucket.remove(entry);
	}
	
	
	public int findMatching(HTTP2_Header header) {
		Bucket bucket = buckets[header.getCode()];
		return bucket.getMatchingIndex(header.getValue());
	}
	
	public int findName(HTTP2_Header header) {
		Bucket bucket = buckets[header.getCode()];
		return bucket.getFirstIndex();
	}

	
	/**
	 * Clear internal map by rebuilding buckets array
	 * 
	 */
	private void clear() {
		buckets = new Bucket[NB_MAP_BUCKETS];
		for(int i=0; i<NB_MAP_BUCKETS; i++) {
			buckets[i] = new Bucket();
		}
	}
}
