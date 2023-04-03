package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;

/**
 * <p>
 * SETTINGS_HEADER_TABLE_SIZE (0x1): Allows the sender to inform the remote
 * endpoint of the maximum size of the header compression table used to decode
 * header blocks, in octets. The encoder can select any size equal to or less
 * than this value by using signaling specific to the header compression format
 * inside a header block (see [COMPRESSION]). The initial value is 4,096 octets.
 * <p>
 * 
 * @author pc
 *
 */
public abstract class HPACK_DynamicTable {



	/**
	 * RFC 7541 (HPACK)/4.1
	 * <p>
	 * The size of an entry is the sum of its name's length in octets (as 
	 * defined in Section 5.2), its value's length in octets, and 32.
	 * </p>
	 * <p>
	 * The size of an entry is calculated using the length of its name and 
	 * value without any Huffman encoding applied.
	 * </p>
	 * <p>
	 * Note: The additional 32 octets account for an estimated overhead 
	 * associated with an entry.  For example, an entry structure using 
	 * two 64-bit pointers to reference the name and the value of the 
	 * entry and two 64-bit integers for counting the number of 
	 * references to the name and value would have 32 octets of overhead.
	 * </p>
	 * 
	 */
	private class Entry extends HPACK_HeaderEntry {

		private int size;
		
		private int position;

		private HTTP2_Header header;

		public Entry(HTTP2_Header header) {
			super();
			this.header = header;
			this.size = header.getName().length()+header.getValue().length()+32;
		}

		@Override
		public int getIndex() {
			int index = insert-position;
			if(index<0) {
				// take in account the circular buffer effect
				index+=capacity;
			}
			return index+HPACK_StaticTable.OFFSET;
		}
		
		@Override
		public HTTP2_Header getHeader() {
			return header;
		}
	}


	/**
	 * Typical min size for an header, calculated so that there is no collisions.
	 */
	public final static int BASE_HEADER_SIZE = 4;


	/**
	 * circular buffer
	 */
	private Entry[] entries;



	/**
	 * maximum size for the dynamicTable
	 */
	private int maximumSize;


	/**
	 * length of the underlying table
	 */
	private int capacity;

	/**
	 * number of headers within the underlying array
	 */
	private int length;


	/**
	 * Size of the table, according to RFC 7541 (HPACK)/4.1
	 */
	private int size;

	/**
	 * Tells us whetether the table is empty or not
	 */
	private boolean isEmpty;

	/**
	 * index at which occurred last insertion
	 */
	private int insert;


	/**
	 * index at which will occur next deletion
	 */
	private int drop;


	/**
	 * 
	 * @param mapping
	 */
	public HPACK_DynamicTable(int maximumSize) {
		super();
		
		this.maximumSize = maximumSize;

		capacity = maximumSize/BASE_HEADER_SIZE;
		entries = new Entry[capacity];

		size = 0;
		length = 0;
		isEmpty = true;

	}


	public HTTP2_Header get(int index) {
		if(!isEmpty) {
			int i = (insert-index)%capacity;
			return entries[i].header;
		}
		else {
			return null;
		}
	}

	public void add(HTTP2_Header header) {

		Entry entry = new Entry(header);
		size+=entry.size;
		length+=1;
		
		
		if(!isEmpty) {
			// entry eviction until table size becomes valid
			Entry droppedEntry;
			while(size>maximumSize) {
				droppedEntry = entries[drop];
				size-=droppedEntry.size;
				onEntryDropped(droppedEntry);
				length-=1;
				drop = (drop+1)%capacity;
			}
			
			// eventually add new header
			insert = (insert+1)%capacity;
			entries[insert] = entry;
			entry.position = insert;
			onEntryInserted(entry);
			
		}
		else { // isEmpty
			insert = 0;
			drop = 0;
			entries[insert] = entry;
			onEntryInserted(entry);
			isEmpty = false;
		}
	}


	public void resize(int newMaximumSize) {
		int resizedCapacity = newMaximumSize/BASE_HEADER_SIZE;
		Entry[] resizedEntries = new Entry[resizedCapacity];
		if(!isEmpty) {
			onClearEntries();
			
			int resizedLength = length<resizedCapacity?length:resizedCapacity;
			
			int resizedInsert = resizedLength-1;
			int resizedDrop = (resizedInsert-resizedLength-1)%resizedCapacity;
			
			int i, ri;
			Entry entry;
			for(int k=0; k<resizedLength; k++) {
				i = (insert-k)%capacity;
				ri = (resizedInsert-k)%resizedCapacity;
				entry = entries[i];
				resizedEntries[ri] = entry;
				onEntryInserted(entry);
			}
			
			// swap
			
			length = resizedLength;
			insert = resizedInsert;
			drop = resizedDrop;
			isEmpty = length>0;
		}
		entries = resizedEntries;
		capacity = resizedCapacity;
	}
	
	public abstract void onClearEntries();
	
	public abstract void onEntryInserted(HPACK_HeaderEntry entry);

	public abstract void onEntryDropped(HPACK_HeaderEntry entry);

}
