package utils

/**
  * Created by jesus on 16/04/17.
  */
object Utils {
  def optionBy[T, U](value: T, predicate: T => Boolean, mapper: T => U): Option[U] =
    if (predicate(value)) Some(mapper(value)) else None
  def optionBy[T](value: T, predicate: T => Boolean): Option[T] = optionBy(value, predicate, identity[T])
}
