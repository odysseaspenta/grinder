// Copyright (C) 2005 - 2011 Philip Aston
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
package net.grinder.scriptengine.jython.instrumentation.dcr;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import grinder.test.MyClass;
import net.grinder.common.Test;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Recorder;
import net.grinder.util.weave.Weaver.TargetSource;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;


/**
 * The Java methods behind Jython-bound Java methods and static Java methods
 * reach the DCR context as their {@code java.lang.reflect.Method}. This test
 * mocks the context, so no class is retransformed; it reads the methods out of
 * Jython's {@code PyReflectedFunction}, which Jython 2.7.4 changed.
 *
 * @author Philip Aston
 */
public class TestJython25InstrumenterJavaMethods {

  @Mock private DCRContext m_context;
  @Mock private Recorder m_recorder;
  @Mock private Test m_test;

  private final PythonInterpreter m_interpreter =
    new PythonInterpreter(null, new PySystemState());

  @Before public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @org.junit.Test public void testBoundJavaMethod() throws Exception {
    m_interpreter.exec("from grinder.test import MyClass\n" +
                       "x = MyClass(1, 2, 3)\n" +
                       "y = x.getA");

    final Jython25Instrumenter instrumenter = new Jython25Instrumenter(m_context);

    assertTrue(
      instrumenter.instrument(m_test, m_recorder, m_interpreter.get("y")));

    verify(m_context).add(m_interpreter.get("x").__tojava__(Object.class),
                          MyClass.class.getDeclaredMethod("getA"),
                          TargetSource.FIRST_PARAMETER,
                          m_recorder);
    verify(m_context).applyChanges();
    verifyNoMoreInteractions(m_context);
  }

  @org.junit.Test public void testStaticJavaMethod() throws Exception {
    m_interpreter.exec("from grinder.test import MyClass\n" +
                       "y = MyClass.staticSix");

    final Jython25Instrumenter instrumenter = new Jython25Instrumenter(m_context);

    assertTrue(
      instrumenter.instrument(m_test, m_recorder, m_interpreter.get("y")));

    verify(m_context).add(MyClass.class,
                          MyClass.class.getDeclaredMethod("staticSix"),
                          TargetSource.CLASS,
                          m_recorder);
    verify(m_context).applyChanges();
    verifyNoMoreInteractions(m_context);
  }
}
