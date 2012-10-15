package net.beaconcontroller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a custom implementation of Hopscotch Hashing as initially written:
 * Maurice Herlihy, Nir Shavit, and Moran Tzafrir. 2008. Hopscotch
 * Hashing. In Proceedings of the 22nd international symposium on Distributed
 * Computing (DISC '08).
 *
 * This Map supports only a key of primitive long type, with value of primitive
 * short type. Internally it is a single array of longs, with an overhead of 6
 * bytes per entry, which is much less than the observed HashMap overhead per
 * entry.
 *
 * Internal array layout [key 8 bytes][value 2 bytes | pad 2 bytes | hop_info 4 bytes],
 *                       [key 8 bytes][value 2 bytes | pad 2 bytes | hop_info 4 bytes]..
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class LongShortHopscotchHashMap {
    protected static Logger log = LoggerFactory.getLogger(LongShortHopscotchHashMap.class);
    protected static final int HOP_RANGE = 32;
    protected static final int ADD_RANGE = 256;
    protected static int MAX_SEGMENTS = 1024;
    protected static final long VALUE_MASK = 0x0000ffffffffffffl;
    protected static final long HOPINFO_MASK = 0x00000000ffffffffl;

    protected int num_segments = 1;
    protected int segment_mask = num_segments-1;
    protected final int num_buckets = 65536;
    protected int bucket_mask = num_buckets-1;
    protected int segment_shift = 16;
    protected long[] segments;
    protected short defaultValue = -1;
    protected int[] keyInts;
    protected final static int[] hopHelper;
    protected long keys = 0;

    static {
        hopHelper = new int[32];
        for (int i = 0; i < 32; ++i) {
            hopHelper[i] = Integer.MIN_VALUE >>> i;
        }
    }

    public LongShortHopscotchHashMap() {
        segments = new long[num_segments*num_buckets*2];
        keyInts = new int[2];
    }

    public short put(long key, short value) {
        int hash = hash(key);
        int iSegment = (hash & segment_mask) >> segment_shift;
        int iBucket = hash & bucket_mask;
            
        // Does the key already exist? if so swap and return
        int index = ((iSegment * num_buckets) + iBucket)*2;
        int hop_info = (int) segments[index+1];
        if (segments[index] == key)
            return swap(index, value);

        for (int i = 1; i < HOP_RANGE; ++i) {
            if ((hopHelper[i] & hop_info) == hopHelper[i]) {
                index = ((iSegment * num_buckets) + ((iBucket + i) % num_buckets))*2;
                if (segments[index] == key) {
                    return swap(index, value);
                }
            }
        }

        // Find a new slot to place it
        int i = 0;
        for (; i < ADD_RANGE; ++i) {
            index = ((iSegment * num_buckets) + ((iBucket + i) % num_buckets))*2;
            if (segments[index] == 0) {
                break;
            }
        }
        if (i < ADD_RANGE) {
            do {
                if (i < HOP_RANGE) {
                    index = ((iSegment * num_buckets) + ((iBucket + i) % num_buckets))*2;
                    segments[index] = key;
                    segments[index+1] = (segments[index+1] & VALUE_MASK) | (((long)value) << 48);
                    int sourceIndex = ((iSegment * num_buckets) + iBucket)*2;
                    segments[sourceIndex+1] |= ((long)hopHelper[i]) & 0xffffffffL;
                    ++keys;
                    return defaultValue;
                }
                // find closer free bucket
                i = findCloserBucket(iSegment, iBucket, i);
            } while (i != -1);
        }

        //log.debug("segment_mask {} bucket_mask {}", toBinary(segment_mask), toBinary(bucket_mask));
        //double ratio = (double)(keys)/(double)(num_segments*num_buckets);
        resize();
        //log.debug("Ratio {} Size {} Trigger {}", new Object[] {ratio, num_segments*num_buckets, key});
        return put(key, value);
    }

    /**
     * 
     * @param iSegment segment the key hashed to
     * @param iBucket bucket the key hashed to
     * @param i first free offset from iBucket open for writing
     * @return a value that is strictly < i if possible, otherwise -1 
     */
    protected int findCloserBucket(int iSegment, int iBucket, int i) {
        int j = i - (HOP_RANGE - 1);
        int targetIndex = ((iSegment * num_buckets) + ((iBucket + i) % num_buckets))*2;
        for (; j < i; ++j) {
            int hopInfoIndex = ((iSegment * num_buckets) + ((iBucket + j) % num_buckets))*2;
            int hopInfo = (int) (segments[hopInfoIndex+1] & 0xffffffff);
            for (int k = 0; j+k < i; ++k) {
                if ((hopHelper[k] & hopInfo) == hopHelper[k]) {
                    int sourceIndex = ((iSegment * num_buckets) + ((iBucket + j + k) % num_buckets))*2;
                    // copy key
                    segments[targetIndex] = segments[sourceIndex];
                    // copy value
                    segments[targetIndex+1] = (segments[targetIndex+1] & VALUE_MASK) | (segments[sourceIndex+1] & ~VALUE_MASK);
                    // update source hop info
                    hopInfo |= hopHelper[i-j];
                    hopInfo &= ~hopHelper[k];
                    segments[hopInfoIndex+1] = (segments[hopInfoIndex+1] & ~HOPINFO_MASK) | ((long)hopInfo & HOPINFO_MASK);
                    // clear host key and value
                    segments[sourceIndex] = 0L;
                    segments[sourceIndex+1] &= VALUE_MASK;
                    segments[sourceIndex+1] |= (long)defaultValue << 48;
                    return j+k;
                }
            }
        }
        return -1;
    }

    protected void resize() {
        num_segments *= 2;
        segment_mask = (num_segments-1) << segment_shift;
        long[] oldSegments = segments;
        segments = new long[num_segments*num_buckets*2];
        keys = 0;
        for (int i = 0; i < oldSegments.length; i += 2) {
            long key = oldSegments[i];
            short value = (short) (oldSegments[i+1] >> 48);
            if (key != 0)
                put(key, value);
        }
    }

    protected short swap(int index, short value) {
        short retval = (short) ((segments[index+1] >> 48) & 0xffff);
        long newval = ((long)value & 0xffff) << 48;
        segments[index+1] = (segments[index+1] & VALUE_MASK) | newval;
        return retval;
    }

    /**
     * Currently uses the murmur hash 2.0
     * @param key
     * @return
     */
    protected int hash(long key) {
        keyInts[0] = (int) (key);
        keyInts[1] = (int) (key >>> 32);

        return murmur32(keyInts, 283349958);
    }

    public boolean contains(long key) {
        int hash = hash(key);
        int iSegment = (hash & segment_mask) >> segment_shift;
        int iBucket = hash & bucket_mask;

        int index = ((iSegment * num_buckets) + iBucket)*2;
        int hop_info = (int) (segments[index+1] & HOPINFO_MASK);
        if (segments[index] == key)
            return true;

        int baseIndex = iSegment * num_buckets * 2;
        for (int i = 1; i < HOP_RANGE; ++i) {
            if ((hopHelper[i] & hop_info) == hopHelper[i]) {
                index = baseIndex + (((iBucket + i) % num_buckets) << 1);
                if (segments[index] == key) {
                    return true;
                }
            }
        }

        return false;
    }

    public short get(long key) {
        int hash = hash(key);
        int iSegment = (hash & segment_mask) >> segment_shift;
        int iBucket = hash & bucket_mask;

        int index = ((iSegment * num_buckets) + iBucket)*2;
        int hop_info = (int) (segments[index+1] & HOPINFO_MASK);
        if (segments[index] == key)
            return (short) ((segments[index+1] >> 48) & 0xffffL);

        int baseIndex = iSegment * num_buckets * 2;
        for (int i = 1; i < HOP_RANGE; ++i) {
            if ((hopHelper[i] & hop_info) == hopHelper[i]) {
                index = baseIndex + (((iBucket + i) % num_buckets) << 1);
                if (segments[index] == key) {
                    return (short) (segments[index+1] >>> 48);
                }
            }
        }
        return defaultValue;
    }

    public String toBinary(int value) {
        StringBuffer sb = new StringBuffer();
        for (int i = 31; i >= 0; --i) {
            if ((value & (1 << i)) != 0) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    protected boolean verifySegments() {
        // Verify hopinfo
        for (int i = 0; i < segments.length; i += 2) {
            int iSegment = (i/2) / num_buckets;
            int iBucket = (i/2) - (iSegment * num_buckets);
            int hopInfo = (int) (segments[i+1] & 0xffffffffL);
            for (int j = 0; j < HOP_RANGE; ++j) {
                if ((hopInfo & (1 << (HOP_RANGE-1-j))) != 0) {
                    int index = ((iSegment*num_buckets) + ((iBucket + j) % num_buckets))*2;
                    assert(segments[index] != 0);
                    int hash = hash(segments[index]);
                    assert(iSegment == ((hash & segment_mask) >> segment_shift));
                    assert(iBucket == (hash & bucket_mask));
                }
            }
        }

        // Verify every key
        for (int i = 0; i < segments.length; i += 2) {
            if (segments[i] != 0) {
                int hash = hash(segments[i]);
                int iSegment = (hash & segment_mask) >> segment_shift;
                int iBucket = hash & bucket_mask;

                // index to original segment/bucket this key hashed to
                int index = ((iSegment*num_buckets) + (iBucket))*2;
                int hopInfo = (int) (segments[index+1] & 0xffffffffL);
                // the bucket i refers to at the moment
                int currentBucket = (i/2) - (iSegment * num_buckets);
                int j = (currentBucket - iBucket) % num_buckets;
                assert((hopInfo & (1 << (HOP_RANGE-1-j))) != 0);
            }
        }
        return true;
    }

    /**
     * Modified version of the Murmur hash 2.0.  Originally retrieved from
     * http://d3s.mff.cuni.cz/~holub/sw/javamurmurhash/, modified by David
     * Erickson. Note this modified version is hard coded to accept two
     * integers in an array, ie a long converted to two ints.
     * Original comments below:
     * 
     * The murmur hash is a relative fast hash function from
     * http://murmurhash.googlepages.com/ for platforms with efficient
     * multiplication.
     * 
     * This is a re-implementation of the original C code plus some
     * additional features.
     * 
     * Public domain.
     * 
     * @author David Erickson
     * @author Viliam Holub
     * @version 1.0.2
     * @param data
     * @param seed
     * @return
     */
    public static int murmur32(final int[] data, int seed) {
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;
        // Initialize the hash to a random value
        int h = seed^8;

        for (int i=0; i<2; i++) {
            int k = data[i];
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

    /**
     * @return the defaultValue
     */
    public short getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(short defaultValue) {
        this.defaultValue = defaultValue;
    }
}
