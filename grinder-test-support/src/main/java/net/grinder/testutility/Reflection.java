// Copyright (C) 2026 The Grinder contributors
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

import java.lang.reflect.Field;


/**
 * Set private fields from tests. Replaces Mockito 1.x's internal
 * {@code Whitebox}, which Mockito 2 removed.
 */
public final class Reflection {

  private Reflection() {
  }

  /**
   * Set a field, walking up the class hierarchy to find it.
   *
   * @param target An instance, or a {@link Class} for a static field.
   * @param fieldName The field name.
   * @param value The new value.
   */
  public static void setField(Object target, String fieldName, Object value) {
    final boolean isStatic = target instanceof Class<?>;
    Class<?> c = isStatic ? (Class<?>) target : target.getClass();

    while (c != null) {
      try {
        final Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(isStatic ? null : target, value);
        return;
      }
      catch (NoSuchFieldException e) {
        c = c.getSuperclass();
      }
      catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

    throw new IllegalArgumentException(
      "No field '" + fieldName + "' on " + target);
  }
}
