package utils

import scala.util.Either.RightProjection

object EitherUtil {
  import scala.language.implicitConversions

  implicit def eitherToRightProjection[A, B](either: Either[A, B]): RightProjection[A, B] = either.right
}
