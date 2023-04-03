package com.s8.stack.arch.helium.rx;


/**
 * 
 * @author pierreconvert
 *
 */
class Pool {


	public final static int DEFAULT_CAPACITY = 0xffff+1;


	/**
	 * 
	 * @author pierreconvert
	 *
	 */
	private static class Bucket {

		public final static int INITIAL_CAPACITY = 2;

		/**
		 * Allow fast traversing of all endpoints
		 */
		private RxConnection[] slots;


		public Bucket() {
			super();
			this.slots = new RxConnection[INITIAL_CAPACITY];
		}

		public void pullInterestOps() {
			/* <update> */

			// update observed operations flags
			int nSlots = slots.length;

			RxConnection connection;
			for(int index=0; index<nSlots; index++) {
				if((connection = slots[index])!=null) {
					connection.pullInterestOps(); 
				}
			}
			/* </update> */
		}

		/**
		 * 
		 * @param connection
		 */
		public void add(RxConnection connection) {
			int nSlots = slots.length;
			int index=0;
			boolean isAdded = false;
			while(!isAdded && index<nSlots) {
				if(slots[index]==null) {
					slots[index] = connection;
					isAdded = true;
				}
				index++;
			}

			// no available slots found
			if(!isAdded) {
				// extend
				RxConnection[] extendedSlots = new RxConnection[2*nSlots];
				for(int i=0; i<nSlots; i++) {
					extendedSlots[i] = slots[i];
				}
				extendedSlots[index] = connection;
			}
		}

		/**
		 * 
		 * @param slot
		 */
		public void remove(long id) {
			int nSlots = slots.length;
			int index=0;
			boolean isRemoved = false;
			RxConnection slot;
			while(!isRemoved && index<nSlots) {
				slot = slots[index];
				if(slot!=null && slot.id==id) {
					// remove connection from slot
					slots[index] = null;		
					isRemoved = true;
				}
			}
		}

		public RxConnection get(long id) {
			int nSlots = slots.length;
			RxConnection slot;
			for(int index=0; index<nSlots; index++) {
				slot = slots[index];
				if(slot!=null && slot.id==id) {
					return slot;
				}
			}
			// not found
			return null;
		}


	}


	private Bucket[] buckets;

	private long nextAvailableConnectionId = 2L;

	public Pool(int capacity) {
		super();

		buckets = new Bucket[capacity];

	}






	private static int bucketIndex(long id) {
		return (((int) id) & 0xffff);
	}

	public synchronized void pullInterestOps() {
		/* <update> */

		Bucket bucket;
		// update observed operations flags
		int nBuckets = buckets.length;
		for(int index=0; index<nBuckets; index++) {
			if((bucket = buckets[index])!=null) {
				bucket.pullInterestOps(); 
			}
		}
		/* </update> */
	}


	public synchronized RxConnection get(long id) {
		int bucketIndex = (((int) id) & 0xffff);
		Bucket bucket = buckets[bucketIndex];
		if(bucket!=null) {
			return bucket.get(id);
		}
		else {
			return null;
		}
	}

	public synchronized void put(RxConnection connection) {

		// linking
		long id = nextAvailableConnectionId++;
		connection.id = id;
		connection.pool = this;

		int bucketIndex = bucketIndex(id);
		Bucket bucket = buckets[bucketIndex];
		if(bucket==null) {
			bucket = new Bucket();
			buckets[bucketIndex] = bucket;
		}
		bucket.add(connection);
	}


	public synchronized void remove(long id) {
		int bucketIndex = bucketIndex(id);

		Bucket bucket = buckets[bucketIndex];
		if(bucket!=null) {
			bucket.remove(id);
		}
	}
}
