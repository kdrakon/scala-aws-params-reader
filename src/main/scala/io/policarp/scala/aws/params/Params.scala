package io.policarp.scala.aws.params

object Params {

  object ParamTypes {

    sealed abstract class ParamType(val name: String)

    class StringParam extends ParamType("String")
    class StringListParam extends ParamType("StringList")
    class SecureStringParam extends ParamType("SecureString")
    class UnknownParam extends ParamType("")

    object StringParam extends StringParam
    object StringListParam extends StringListParam
    object SecureStringParam extends SecureStringParam
  }

  object ParamResult {

    case class InvalidParam[A](name: String)

    type ParamResult[A] = Either[InvalidParam[A], A]
    type Valid[A] = Right[InvalidParam[A], A]
    type Invalid[A] = Left[InvalidParam[A], A]

    def Valid[A](p: A): Valid[A] = {
      Right[InvalidParam[A], A](p)
    }

    def Invalid[A](p: InvalidParam[A]): Invalid[A] = {
      Left[InvalidParam[A], A](p)
    }
  }
}

