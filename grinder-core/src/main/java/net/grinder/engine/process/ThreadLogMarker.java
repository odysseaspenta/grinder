// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000 - 2012 Philip Aston
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

package net.grinder.engine.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Marker;


/**
 * The log marker of a worker thread: its name plus the current run and test
 * markers, rendered by the worker log's {@code %marker} pattern as
 * {@code thread-1 [ run-2, test-3 ]}.
 *
 * <p>SLF4J 2 deprecated marker nesting ("markers are now immutable"), so this
 * class owns the nesting instead of building on {@code BasicMarker}. The
 * deprecated {@link Marker} methods are implemented for anything that still
 * walks a marker's references, such as a Logback marker filter.</p>
 *
 * @author Philip Aston
 */
final class ThreadLogMarker implements Marker {

  private static final long serialVersionUID = 1L;

  private final String m_name;
  private volatile Marker m_run;
  private volatile Marker m_test;

  ThreadLogMarker(String name) {
    m_name = name;
  }

  /**
   * Set the current run marker, or {@code null} between runs.
   */
  void setRun(Marker run) {
    m_run = run;
  }

  /**
   * Set the current test marker, or {@code null} outside a test.
   */
  void setTest(Marker test) {
    m_test = test;
  }

  private List<Marker> references() {
    final List<Marker> result = new ArrayList<Marker>(2);
    final Marker run = m_run;
    final Marker test = m_test;

    if (run != null) {
      result.add(run);
    }

    if (test != null) {
      result.add(test);
    }

    return result;
  }

  @Override public String getName() {
    return m_name;
  }

  @Override public String toString() {
    final List<Marker> references = references();

    if (references.isEmpty()) {
      return m_name;
    }

    final StringBuilder result = new StringBuilder(m_name).append(" [ ");

    for (int i = 0; i < references.size(); ++i) {
      if (i > 0) {
        result.append(", ");
      }

      result.append(references.get(i).getName());
    }

    return result.append(" ]").toString();
  }

  @Override public boolean equals(Object o) {
    return o instanceof Marker && m_name.equals(((Marker) o).getName());
  }

  @Override public int hashCode() {
    return m_name.hashCode();
  }

  @Override @Deprecated public void add(Marker reference) {
    throw new UnsupportedOperationException(
      "Use setRun or setTest; a ThreadLogMarker has fixed children");
  }

  @Override @Deprecated public boolean remove(Marker reference) {
    throw new UnsupportedOperationException(
      "Use setRun or setTest; a ThreadLogMarker has fixed children");
  }

  @Override @Deprecated public boolean hasChildren() {
    return hasReferences();
  }

  @Override @Deprecated public boolean hasReferences() {
    return m_run != null || m_test != null;
  }

  @Override @Deprecated public Iterator<Marker> iterator() {
    return references().iterator();
  }

  @Override @Deprecated public boolean contains(Marker other) {
    if (other == null) {
      throw new IllegalArgumentException("Other cannot be null");
    }

    return equals(other) || references().contains(other);
  }

  @Override @Deprecated public boolean contains(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Other cannot be null");
    }

    if (m_name.equals(name)) {
      return true;
    }

    for (Marker reference : references()) {
      if (reference.getName().equals(name)) {
        return true;
      }
    }

    return false;
  }
}
