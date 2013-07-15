
package net.ontopia.utils;

/**
 * INTERNAL: Stringifier that calls the toString method on the object.
 */

public class DefaultStringifier<T> implements StringifierIF<T> {

  public String toString(T object) {
    if (object == null) return "null";
    return object.toString();
  }

}




