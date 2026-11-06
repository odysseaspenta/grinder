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

package net.grinder.engine.process;

import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * {@link Logger} implementation for external consumption. Delegates to
 * another {@code Logger}, adding the calling thread's context marker to the
 * calls that have none. Calls that carry their own marker pass it through.
 *
 * <p>Implements {@code Logger} directly rather than through SLF4J's
 * {@code AbstractLogger}, which would funnel every call through a level check
 * and a normalised form and so change which delegate method is called.</p>
 *
 * @author Philip Aston
 */
final class ExternalLogger implements Logger {

  private final Logger m_delegate;
  private final ThreadContextLocator m_threadContextLocator;

  public ExternalLogger(Logger processLogger,
                        ThreadContextLocator threadContextLocator) {

    m_delegate = processLogger;
    m_threadContextLocator = threadContextLocator;
  }

  private Marker getMarker() {
    final ThreadContext threadContext = m_threadContextLocator.get();

    if (threadContext != null) {
      return threadContext.getLogMarker();
    }

    return null;
  }

  @Override public String getName() {
    return m_delegate.getName();
  }

  @Override public boolean isTraceEnabled() {
    return m_delegate.isTraceEnabled(getMarker());
  }

  @Override public boolean isTraceEnabled(Marker marker) {
    return m_delegate.isTraceEnabled(marker);
  }

  @Override public void trace(String msg) {
    m_delegate.trace(getMarker(), msg);
  }

  @Override public void trace(String format, Object arg) {
    m_delegate.trace(getMarker(), format, arg);
  }

  @Override public void trace(String format, Object arg1, Object arg2) {
    m_delegate.trace(getMarker(), format, arg1, arg2);
  }

  @Override public void trace(String format, Object... arguments) {
    m_delegate.trace(getMarker(), format, arguments);
  }

  @Override public void trace(String msg, Throwable t) {
    m_delegate.trace(getMarker(), msg, t);
  }

  @Override public void trace(Marker marker, String msg) {
    m_delegate.trace(marker, msg);
  }

  @Override public void trace(Marker marker, String format, Object arg) {
    m_delegate.trace(marker, format, arg);
  }

  @Override public void trace(Marker marker, String format, Object arg1,
                            Object arg2) {
    m_delegate.trace(marker, format, arg1, arg2);
  }

  @Override public void trace(Marker marker, String format,
                            Object... arguments) {
    m_delegate.trace(marker, format, arguments);
  }

  @Override public void trace(Marker marker, String msg, Throwable t) {
    m_delegate.trace(marker, msg, t);
  }

  @Override public boolean isDebugEnabled() {
    return m_delegate.isDebugEnabled(getMarker());
  }

  @Override public boolean isDebugEnabled(Marker marker) {
    return m_delegate.isDebugEnabled(marker);
  }

  @Override public void debug(String msg) {
    m_delegate.debug(getMarker(), msg);
  }

  @Override public void debug(String format, Object arg) {
    m_delegate.debug(getMarker(), format, arg);
  }

  @Override public void debug(String format, Object arg1, Object arg2) {
    m_delegate.debug(getMarker(), format, arg1, arg2);
  }

  @Override public void debug(String format, Object... arguments) {
    m_delegate.debug(getMarker(), format, arguments);
  }

  @Override public void debug(String msg, Throwable t) {
    m_delegate.debug(getMarker(), msg, t);
  }

  @Override public void debug(Marker marker, String msg) {
    m_delegate.debug(marker, msg);
  }

  @Override public void debug(Marker marker, String format, Object arg) {
    m_delegate.debug(marker, format, arg);
  }

  @Override public void debug(Marker marker, String format, Object arg1,
                            Object arg2) {
    m_delegate.debug(marker, format, arg1, arg2);
  }

  @Override public void debug(Marker marker, String format,
                            Object... arguments) {
    m_delegate.debug(marker, format, arguments);
  }

  @Override public void debug(Marker marker, String msg, Throwable t) {
    m_delegate.debug(marker, msg, t);
  }

  @Override public boolean isInfoEnabled() {
    return m_delegate.isInfoEnabled(getMarker());
  }

  @Override public boolean isInfoEnabled(Marker marker) {
    return m_delegate.isInfoEnabled(marker);
  }

  @Override public void info(String msg) {
    m_delegate.info(getMarker(), msg);
  }

  @Override public void info(String format, Object arg) {
    m_delegate.info(getMarker(), format, arg);
  }

  @Override public void info(String format, Object arg1, Object arg2) {
    m_delegate.info(getMarker(), format, arg1, arg2);
  }

  @Override public void info(String format, Object... arguments) {
    m_delegate.info(getMarker(), format, arguments);
  }

  @Override public void info(String msg, Throwable t) {
    m_delegate.info(getMarker(), msg, t);
  }

  @Override public void info(Marker marker, String msg) {
    m_delegate.info(marker, msg);
  }

  @Override public void info(Marker marker, String format, Object arg) {
    m_delegate.info(marker, format, arg);
  }

  @Override public void info(Marker marker, String format, Object arg1,
                            Object arg2) {
    m_delegate.info(marker, format, arg1, arg2);
  }

  @Override public void info(Marker marker, String format,
                            Object... arguments) {
    m_delegate.info(marker, format, arguments);
  }

  @Override public void info(Marker marker, String msg, Throwable t) {
    m_delegate.info(marker, msg, t);
  }

  @Override public boolean isWarnEnabled() {
    return m_delegate.isWarnEnabled(getMarker());
  }

  @Override public boolean isWarnEnabled(Marker marker) {
    return m_delegate.isWarnEnabled(marker);
  }

  @Override public void warn(String msg) {
    m_delegate.warn(getMarker(), msg);
  }

  @Override public void warn(String format, Object arg) {
    m_delegate.warn(getMarker(), format, arg);
  }

  @Override public void warn(String format, Object arg1, Object arg2) {
    m_delegate.warn(getMarker(), format, arg1, arg2);
  }

  @Override public void warn(String format, Object... arguments) {
    m_delegate.warn(getMarker(), format, arguments);
  }

  @Override public void warn(String msg, Throwable t) {
    m_delegate.warn(getMarker(), msg, t);
  }

  @Override public void warn(Marker marker, String msg) {
    m_delegate.warn(marker, msg);
  }

  @Override public void warn(Marker marker, String format, Object arg) {
    m_delegate.warn(marker, format, arg);
  }

  @Override public void warn(Marker marker, String format, Object arg1,
                            Object arg2) {
    m_delegate.warn(marker, format, arg1, arg2);
  }

  @Override public void warn(Marker marker, String format,
                            Object... arguments) {
    m_delegate.warn(marker, format, arguments);
  }

  @Override public void warn(Marker marker, String msg, Throwable t) {
    m_delegate.warn(marker, msg, t);
  }

  @Override public boolean isErrorEnabled() {
    return m_delegate.isErrorEnabled(getMarker());
  }

  @Override public boolean isErrorEnabled(Marker marker) {
    return m_delegate.isErrorEnabled(marker);
  }

  @Override public void error(String msg) {
    m_delegate.error(getMarker(), msg);
  }

  @Override public void error(String format, Object arg) {
    m_delegate.error(getMarker(), format, arg);
  }

  @Override public void error(String format, Object arg1, Object arg2) {
    m_delegate.error(getMarker(), format, arg1, arg2);
  }

  @Override public void error(String format, Object... arguments) {
    m_delegate.error(getMarker(), format, arguments);
  }

  @Override public void error(String msg, Throwable t) {
    m_delegate.error(getMarker(), msg, t);
  }

  @Override public void error(Marker marker, String msg) {
    m_delegate.error(marker, msg);
  }

  @Override public void error(Marker marker, String format, Object arg) {
    m_delegate.error(marker, format, arg);
  }

  @Override public void error(Marker marker, String format, Object arg1,
                            Object arg2) {
    m_delegate.error(marker, format, arg1, arg2);
  }

  @Override public void error(Marker marker, String format,
                            Object... arguments) {
    m_delegate.error(marker, format, arguments);
  }

  @Override public void error(Marker marker, String msg, Throwable t) {
    m_delegate.error(marker, msg, t);
  }
}
