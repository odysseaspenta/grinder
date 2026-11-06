// Copyright (C) 2009 - 2011 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process.dcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * Unit tests for {@link WeakIdentityConcurrentMap}.
 *
 * @author Philip Aston
 */
public class TestWeakIdentityConcurrentMap {

  /** A key whose equals and hashCode must never be called. */
  private static final class Hostile {
    @Override public int hashCode() {
      throw new AssertionError("hashCode called");
    }

    @Override public boolean equals(Object o) {
      throw new AssertionError("equals called");
    }
  }

  private final WeakIdentityConcurrentMap<String> m_map =
    new WeakIdentityConcurrentMap<String>();

  @Test public void testKeysAreComparedByIdentityWithoutCallingThem() {
    final Hostile key = new Hostile();
    final Hostile other = new Hostile();

    assertNull(m_map.putIfAbsent(key, "one"));
    assertEquals("one", m_map.get(key));
    assertNull(m_map.get(other));
    assertEquals("one", m_map.putIfAbsent(key, "two"));
    assertEquals("one", m_map.get(key));

    assertNull(m_map.putIfAbsent(other, "two"));
    assertEquals("two", m_map.get(other));
    assertEquals(2, m_map.size());
  }

  @Test public void testEqualButDistinctKeysAreSeparate() {
    final String key = new String("key");
    final String key2 = new String("key");
    assertEquals(key, key2);

    assertNull(m_map.putIfAbsent(key, "one"));
    assertNull(m_map.putIfAbsent(key2, "two"));
    assertSame("one", m_map.get(key));
    assertSame("two", m_map.get(key2));
  }

  @Test(expected = NullPointerException.class)
  public void testNullKeyIsRejected() {
    m_map.putIfAbsent(null, "value");
  }

  // In a separate method so that no interpreter frame keeps the key
  // reachable after the call.
  private void putThrowawayKey() {
    m_map.putIfAbsent(new Object(), "value");
  }

  @Test public void testEntryGoesWhenKeyIsCollected() throws Exception {
    putThrowawayKey();
    assertEquals(1, m_map.size());

    awaitSize(0);
  }

  @Test public void testValueThatReachesItsKeyKeepsTheEntry()
    throws Exception {
    // Documents the limitation: values are strong.
    final WeakIdentityConcurrentMap<Object[]> map =
      new WeakIdentityConcurrentMap<Object[]>();
    final Object[] holder = new Object[1];
    holder[0] = new Object();
    map.putIfAbsent(holder[0], holder);

    for (int i = 0; i < 5; ++i) {
      System.gc();
      Thread.sleep(20);
    }

    assertEquals(1, map.size());
    assertNotNull(map.get(holder[0]));
  }

  @Test public void testClear() {
    m_map.putIfAbsent(new Object(), "value");
    m_map.clear();
    assertEquals(0, m_map.size());
  }

  private void awaitSize(int expected) throws InterruptedException {
    final long deadline = System.currentTimeMillis() + 5000;

    while (m_map.size() != expected) {
      if (System.currentTimeMillis() > deadline) {
        fail("size is " + m_map.size() + ", expected " + expected);
      }

      System.gc();
      Thread.sleep(20);
    }
  }
}
