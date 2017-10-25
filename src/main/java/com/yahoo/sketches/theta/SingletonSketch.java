/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.theta;

import static com.yahoo.sketches.Util.DEFAULT_UPDATE_SEED;
import static com.yahoo.sketches.Util.checkSeedHashes;
import static com.yahoo.sketches.Util.computeSeedHash;
import static com.yahoo.sketches.hash.MurmurHash3.hash;
import static com.yahoo.sketches.theta.PreambleUtil.COMPACT_FLAG_MASK;
import static com.yahoo.sketches.theta.PreambleUtil.ORDERED_FLAG_MASK;
import static com.yahoo.sketches.theta.PreambleUtil.READ_ONLY_FLAG_MASK;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.yahoo.memory.Memory;
import com.yahoo.memory.WritableMemory;
import com.yahoo.sketches.Family;
import com.yahoo.sketches.SketchesArgumentException;

/**
 * @author Lee Rhodes
 */
public class SingletonSketch extends CompactSketch {
  static final long defaultPreamble;
  private long[] arr = new long[] {defaultPreamble, 0L };
  private WritableMemory wmem = WritableMemory.wrap(arr);

  static {
    final byte[] sArr = new byte[8];
    final WritableMemory smem = WritableMemory.wrap(sArr);
    smem.putByte(0, (byte) 1); //preLongs
    smem.putByte(1, (byte) 3); //serVer
    smem.putByte(2, (byte) 3); //FamilyID
    final byte flags = (byte) (READ_ONLY_FLAG_MASK | COMPACT_FLAG_MASK  | ORDERED_FLAG_MASK);
    smem.putByte(5, flags);
    smem.putShort(6, computeSeedHash(DEFAULT_UPDATE_SEED));
    defaultPreamble = smem.getLong(0);
  }

  SingletonSketch(final long hash) {
    arr[1] = hash;
  }

  SingletonSketch(final long hash, final long seed) {
    arr[1] = hash;
    wmem.putShort(6, computeSeedHash(seed));
  }

  /**
   * Creates a SingletonSketch on the heap given a Memory
   * @param mem the Memory to be heapified.  It must be a least 16 bytes.
   * @return a SingletonSketch
   */
  public static SingletonSketch heapify(final Memory mem) {
    final long memPre0 = mem.getLong(0);
    checkDefaultPre0to7(memPre0);
    return new SingletonSketch(mem.getLong(8));
  }

  /**
   * Creates a SingletonSketch on the heap given a Memory.
   * Checks the seed hash of the given Memory against a hash of the given seed.
   * @param mem the Memory to be heapified
   * @param seed a given hash update seed
   * @return a SingletonSketch
   */
  public static SingletonSketch heapify(final Memory mem, final long seed) {
    final long memPre0 = mem.getLong(0);
    checkDefaultPre0to5(memPre0);
    final short seedHashIn = mem.getShort(6);
    final short seedHashCk = computeSeedHash(seed);
    checkSeedHashes(seedHashIn, seedHashCk);
    return new SingletonSketch(mem.getLong(8), seed);
  }

  /**
   * Present this sketch with a long.
   *
   * @param datum The given long datum.
   * @return a SingletonSketch
   */
  public static SingletonSketch update(final long datum) {
    final long[] data = { datum };
    return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
  }

/**
 * Present this sketch with the given double (or float) datum.
 * The double will be converted to a long using Double.doubleToLongBits(datum),
 * which normalizes all NaN values to a single NaN representation.
 * Plus and minus zero will be normalized to plus zero.
 * The special floating-point values NaN and +/- Infinity are treated as distinct.
 *
 * @param datum The given double datum.
 * @return a SingletonSketch
 */
public static SingletonSketch update(final double datum) {
  final double d = (datum == 0.0) ? 0.0 : datum; // canonicalize -0.0, 0.0
  final long[] data = { Double.doubleToLongBits(d) };// canonicalize all NaN forms
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

/**
 * Present this sketch with the given String.
 * The string is converted to a byte array using UTF8 encoding.
 * If the string is null or empty no update attempt is made and the method returns.
 *
 * <p>Note: this will not produce the same output hash values as the {@link #update(char[])}
 * method and will generally be a little slower depending on the complexity of the UTF8 encoding.
 * </p>
 *
 * @param datum The given String.
 * @return a SingletonSketch or null
 */
public static SingletonSketch update(final String datum) {
  if ((datum == null) || datum.isEmpty()) { return null; }
  final byte[] data = datum.getBytes(UTF_8);
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

/**
 * Present this sketch with the given byte array.
 * If the byte array is null or empty no update attempt is made and the method returns.
 *
 * @param data The given byte array.
 * @return a SingletonSketch or null
 */
public static SingletonSketch update(final byte[] data) {
  if ((data == null) || (data.length == 0)) { return null; }
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

/**
 * Present this sketch with the given char array.
 * If the char array is null or empty no update attempt is made and the method returns.
 *
 * <p>Note: this will not produce the same output hash values as the {@link #update(String)}
 * method but will be a little faster as it avoids the complexity of the UTF8 encoding.</p>
 *
 * @param data The given char array.
 * @return a SingletonSketch or null
 */
public static SingletonSketch update(final char[] data) {
  if ((data == null) || (data.length == 0)) { return null; }
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

/**
 * Present this sketch with the given integer array.
 * If the integer array is null or empty no update attempt is made and the method returns.
 *
 * @param data The given int array.
 * @return a SingletonSketch or null
 */
public static SingletonSketch update(final int[] data) {
  if ((data == null) || (data.length == 0)) { return null; }
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

/**
 * Present this sketch with the given long array.
 * If the long array is null or empty no update attempt is made and the method returns.
 *
 * @param data The given long array.
 * @return a SingletonSketch or null
 */
public static SingletonSketch update(final long[] data) {
  if ((data == null) || (data.length == 0)) { return null; }
  return new SingletonSketch(hash(data, DEFAULT_UPDATE_SEED)[0] >>> 1);
}

  //Sketch

  @Override
  public int getCurrentBytes(final boolean compact) {
    return 16;
  }

  @Override
  public Family getFamily() {
    return Family.COMPACT;
  }

  @Override
  public int getRetainedEntries(final boolean valid) {
    return 1;
  }

  @Override
  public boolean isCompact() {
    return true;
  }

  @Override
  public boolean isDirect() {
    return wmem.isDirect();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isOrdered() {
    return true;
  }

  @Override
  public byte[] toByteArray() {
    final byte[] out = new byte[16];
    wmem.getByteArray(0, out, 0, 16);
    return out;
  }

  //restricted methods

  @Override
  long[] getCache() {
    return new long[] { wmem.getLong(8) };
  }

  @Override
  int getCurrentPreambleLongs(final boolean compact) {
    return 1;
  }

  @Override
  Memory getMemory() {
    return wmem;
  }

  @Override
  short getSeedHash() {
    return wmem.getShort(6);
  }

  @Override
  long getThetaLong() {
    return Long.MAX_VALUE;
  }

  static void checkDefaultPre0to7(final long memPre0) {
    if (memPre0 != defaultPreamble) {
      throw new SketchesArgumentException(
        "Input Memory does not match defualt Preamble bytes 0 through 7.");
    }
  }

  static void checkDefaultPre0to5(final long memPre0) {
    final long mask = (1L << 48) - 1L;
    if ((memPre0 & mask) != (defaultPreamble & mask)) {
      throw new SketchesArgumentException(
        "Input Memory does not match defualt Preamble bytes 0 through 5.");
    }
  }

}
