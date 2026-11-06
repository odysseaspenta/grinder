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

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Concurrent map whose keys are compared by identity and held weakly.
 *
 * <p>Never calls {@code equals} or {@code hashCode} on a key: the keys are
 * script objects (Jython ones run Python code from both). {@link #get} is a
 * plain {@code ConcurrentHashMap} lookup with a short-lived lookup key; it
 * takes no lock and allocates nothing once C2 has inlined it.</p>
 *
 * <p>Entries whose key has been collected are removed by a {@link Cleaner}
 * thread, so the map cleans itself without further writes, as the
 * extra166y map it replaces did. Values are strong: a value that reaches its
 * own key keeps the entry alive.</p>
 *
 * <p>This class must stay under {@code net.grinder.engine.process}, which
 * {@code DCRContextImplementation} excludes from instrumentation; a woven
 * lookup would call {@code RecorderLocator.enter} from inside itself.</p>
 *
 * @param <V> Value type.
 * @author Philip Aston
 */
final class WeakIdentityConcurrentMap<V> {

  private static final Cleaner CLEANER = Cleaner.create();

  private final ConcurrentMap<Object, V> m_map =
    new ConcurrentHashMap<Object, V>();

  /**
   * Look up a key by identity.
   *
   * @param key The key.
   * @return The value, or {@code null}.
   */
  V get(Object key) {
    return m_map.get(new LookupKey(key));
  }

  /**
   * Add a value for a key unless the key already has one.
   *
   * @param key The key, not {@code null}.
   * @param value The value.
   * @return The existing value, or {@code null} if {@code value} was added.
   */
  V putIfAbsent(Object key, V value) {
    Objects.requireNonNull(key);

    final V existing = get(key);

    if (existing != null) {
      return existing;
    }

    final WeakKey weakKey = new WeakKey(key);
    final V previous = m_map.putIfAbsent(weakKey, value);

    if (previous == null) {
      // The action must not capture the key.
      CLEANER.register(key, new Removal(m_map, weakKey));
    }

    return previous;
  }

  /**
   * The number of entries, including any whose key has been collected but
   * not yet removed.
   *
   * @return The size.
   */
  int size() {
    return m_map.size();
  }

  /**
   * Remove every entry.
   */
  void clear() {
    m_map.clear();
  }

  private static final class Removal implements Runnable {
    private final ConcurrentMap<Object, ?> m_target;
    private final WeakKey m_key;

    Removal(ConcurrentMap<Object, ?> target, WeakKey key) {
      m_target = target;
      m_key = key;
    }

    @Override public void run() {
      m_target.remove(m_key);
    }
  }

  /**
   * Hot-path key: holds the referent strongly for the duration of one call.
   */
  private static final class LookupKey {
    private final Object m_referent;

    LookupKey(Object referent) {
      m_referent = referent;
    }

    @Override public int hashCode() {
      return System.identityHashCode(m_referent);
    }

    @Override public boolean equals(Object o) {
      return o instanceof WeakKey && ((WeakKey) o).get() == m_referent;
    }
  }

  /**
   * Stored key. Equal to a lookup key or another stored key for the same live
   * referent, and to itself even once cleared, so {@link Removal} finds it.
   */
  private static final class WeakKey extends WeakReference<Object> {
    private final int m_hash;

    WeakKey(Object referent) {
      super(referent);
      m_hash = System.identityHashCode(referent);
    }

    @Override public int hashCode() {
      return m_hash;
    }

    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }

      final Object mine = get();

      if (mine == null) {
        return false;
      }

      if (o instanceof LookupKey) {
        return ((LookupKey) o).m_referent == mine;
      }

      return o instanceof WeakKey && ((WeakKey) o).get() == mine;
    }
  }
}
