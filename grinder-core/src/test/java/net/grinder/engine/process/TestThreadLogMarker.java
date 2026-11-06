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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


/**
 * Unit tests for {@link ThreadLogMarker}.
 *
 * @author Philip Aston
 */
public class TestThreadLogMarker {

  private final Marker m_run = MarkerFactory.getDetachedMarker("run-2");
  private final Marker m_test = MarkerFactory.getDetachedMarker("test-3");

  @Test public void testRendersLikeANestedMarker() {
    final ThreadLogMarker marker = new ThreadLogMarker("thread-1");
    assertEquals("thread-1", marker.toString());

    marker.setRun(m_run);
    assertEquals("thread-1 [ run-2 ]", marker.toString());

    marker.setTest(m_test);
    assertEquals("thread-1 [ run-2, test-3 ]", marker.toString());

    marker.setRun(null);
    assertEquals("thread-1 [ test-3 ]", marker.toString());
  }

  @Test public void testEqualityIsByName() {
    final ThreadLogMarker marker = new ThreadLogMarker("thread-1");
    assertEquals(MarkerFactory.getDetachedMarker("thread-1"), marker);
    assertEquals(marker, MarkerFactory.getDetachedMarker("thread-1"));
    assertEquals(MarkerFactory.getDetachedMarker("thread-1").hashCode(),
                 marker.hashCode());
    assertNotEquals(new ThreadLogMarker("thread-2"), marker);
  }

  @SuppressWarnings("deprecation") // The reference walk SLF4J 2 deprecates.
  @Test public void testReferencesForCodeThatStillWalksThem() {
    final ThreadLogMarker marker = new ThreadLogMarker("thread-1");
    assertFalse(marker.hasReferences());
    assertFalse(marker.iterator().hasNext());
    assertTrue(marker.contains("thread-1"));
    assertFalse(marker.contains("run-2"));

    marker.setRun(m_run);
    marker.setTest(m_test);
    assertTrue(marker.hasChildren());
    assertTrue(marker.contains(m_run));
    assertTrue(marker.contains("test-3"));

    final Iterator<Marker> references = marker.iterator();
    assertEquals(m_run, references.next());
    assertEquals(m_test, references.next());
    assertFalse(references.hasNext());
  }

  @SuppressWarnings("deprecation") // The reference walk SLF4J 2 deprecates.
  @Test(expected = UnsupportedOperationException.class)
  public void testChildrenAreNotAddedThroughTheMarkerAPI() {
    new ThreadLogMarker("thread-1").add(m_run);
  }
}
