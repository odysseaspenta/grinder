// Copyright (C) 2004 - 2011 Philip Aston
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

package net.grinder.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.grinder.util.JVM.VersionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for {@link JVM}.
 *
 * @author Philip Aston
 */
public class TestJVM {

  private String m_originalVersion;

  @Before public void saveVersion() {
    m_originalVersion = System.getProperty("java.version");
  }

  @After public void restoreVersion() {
    System.setProperty("java.version", m_originalVersion);
  }

  private static JVM withVersion(String version) {
    System.setProperty("java.version", version);
    return JVM.getInstance();
  }

  @Test public void testJava8VersionScheme() throws Exception {
    final JVM jvm = withVersion("1.8.0_292");

    assertTrue(jvm.isAtLeastVersion(1, 6));
    assertTrue(jvm.isAtLeastVersion(1, 8));
    assertFalse(jvm.isAtLeastVersion(1, 9));
    assertFalse(jvm.isAtLeastVersion(21, 0));
  }

  @Test public void testJava9VersionScheme() throws Exception {
    final JVM jvm = withVersion("21.0.12.1");

    assertTrue(jvm.isAtLeastVersion(1, 6));
    assertTrue(jvm.isAtLeastVersion(17, 0));
    assertTrue(jvm.isAtLeastVersion(21, 0));
    assertFalse(jvm.isAtLeastVersion(21, 1));
    assertFalse(jvm.isAtLeastVersion(22, 0));
  }

  @Test public void testSingleComponentVersion() throws Exception {
    final JVM jvm = withVersion("17");

    assertTrue(jvm.isAtLeastVersion(1, 6));
    assertTrue(jvm.isAtLeastVersion(17, 0));
    assertFalse(jvm.isAtLeastVersion(17, 1));
  }

  @Test public void testRunningJVMHasRequisites() throws Exception {
    System.setProperty("java.version", m_originalVersion);
    assertTrue(JVM.getInstance().isAtLeastVersion(1, 6));
  }

  @Test public void testBadVersions() throws Exception {
    final String[] badVersions = { "", "x", "1.x", ".", "a.b" };

    for (final String badVersion : badVersions) {
      final JVM jvm = withVersion(badVersion);

      try {
        jvm.isAtLeastVersion(1, 3);
        fail("Expected VersionException for \"" + badVersion + "\"");
      }
      catch (VersionException e) {
        assertTrue(e.getMessage().contains("Could not parse"));
      }
    }
  }

  @Test public void testToString() throws Exception {
    final String s = JVM.getInstance().toString();
    assertTrue(s.contains(System.getProperty("java.vm.name")));
    assertEquals(s, JVM.getInstance().toString());
  }
}
