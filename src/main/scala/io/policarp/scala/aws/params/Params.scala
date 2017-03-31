package io.policarp.scala.aws.params

import com.amazonaws.services.simplesystemsmanagement.model.ParameterType

object Params {

  object ParamTypes {

    sealed case class ParamType(name: String)

    object StringParam extends ParamType(ParameterType.String.toString)
    object StringListParam extends ParamType(ParameterType.StringList.toString)
    object SecureStringParam extends ParamType(ParameterType.SecureString.toString)
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

