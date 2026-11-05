// Copyright (C) 2011 Philip Aston
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

package net.grinder.testutility;

import static org.mockito.ArgumentMatchers.argThat;

import java.util.Collection;
import java.util.regex.Pattern;

import org.mockito.ArgumentMatcher;


/**
 * Mockito argument matchers used across the test suites.
 *
 * @author Philip Aston
 */
public class MockingUtilities {

  /**
   * An {@link ArgumentMatcher} that tolerates arguments of the wrong type
   * instead of throwing {@link ClassCastException}.
   *
   * @param <T> The argument type.
   */
  public abstract static class TypedArgumentMatcher<T>
    implements ArgumentMatcher<T> {

    @Override public final boolean matches(T argument) {
      try {
        return argumentMatches(argument);
      }
      catch (ClassCastException e) {
        return false;
      }
    }

    protected abstract boolean argumentMatches(T t);
  }

  public static <T extends Collection<?>> T equalContents(final T t) {
    return argThat(
      new TypedArgumentMatcher<T>() {
        @Override protected boolean argumentMatches(T actual) {
          return actual != null &&
                 actual.containsAll(t) &&
                 t.containsAll(actual);
        }

        @Override public String toString() {
          return "equalContents(" + t + ")";
        }
      });
  }

  public static <T> Class<T> subclass(final Class<T> superClass) {
    return argThat(
      new TypedArgumentMatcher<Class<T>>() {
        @Override protected boolean argumentMatches(Class<T> t) {
          return superClass.isAssignableFrom(t);
        }

        @Override public String toString() {
          return "subclass(" + superClass.getName() + ")";
        }
      });
  }

  public static String containsRegex(final String regex) {
    return argThat(new TypedArgumentMatcher<String>() {

      @Override protected boolean argumentMatches(String text) {
        return text != null && Pattern.compile(regex).matcher(text).find();
      }

      @Override public String toString() {
        return "containsRegex(\"" + regex.replaceAll("\\\\", "\\\\\\\\") + "\")";
      }
    });
  }
}
