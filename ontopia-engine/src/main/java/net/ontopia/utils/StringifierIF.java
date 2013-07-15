
package net.ontopia.utils;

/**
 * PUBLIC: Stringifies objects. The stringifier interface consists of
 * one method called toString which takes a single Object
 * argument. The return value is a string that is the string
 * representation of that object.</p>
 */

public interface StringifierIF<T> {

  /**
   * Returns a stringified version of the object, i.e. a string
   * representation of that object.
   *
   * @param object the object that is to be made a string
   * representation of.
   * @return a string representation of the <code>object</code>
   * argument.
   */
  public String toString(T object);

}




