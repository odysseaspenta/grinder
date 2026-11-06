// Copyright (C) 2012 Philip Aston
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

package net.grinder.util.logback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;


/**
 * Unit tests for {@link EchoMessageEncoder}.
 *
 * @author Philip Aston
 */
public class TestEchoMessageEncoder {

  private final EchoMessageEncoder m_encoder = new EchoMessageEncoder();

  @Mock private ILoggingEvent m_event;

  @Before public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test public void testEncodeWritesOnlyTheMessage() {
    m_encoder.start();

    when(m_event.getMessage()).thenReturn("hello");
    assertEquals("hello" + CoreConstants.LINE_SEPARATOR,
                 new String(m_encoder.encode(m_event), StandardCharsets.UTF_8));

    when(m_event.getMessage()).thenReturn("w\u00f6rld");
    assertEquals("w\u00f6rld" + CoreConstants.LINE_SEPARATOR,
                 new String(m_encoder.encode(m_event), StandardCharsets.UTF_8));
  }
}
