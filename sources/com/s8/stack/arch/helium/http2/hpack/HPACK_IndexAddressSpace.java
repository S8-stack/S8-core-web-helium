package com.s8.stack.arch.helium.http2.hpack;

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
public class HPACK_IndexAddressSpace {

	
	
}
