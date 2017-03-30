package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.{ GetParametersRequest, GetParametersResult }
import io.policarp.scala.aws.params.Params.ParamResult._
import io.policarp.scala.aws.params.Params._
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

import scala.collection.JavaConverters._

trait ParamReader {
  import ParamReader._
  import io.policarp.scala.aws.params.Params.ParamResult._

  val client: AWSSimpleSystemsManagement

  def readMany[A](names: Seq[String], stringListSeparator: String = DefaultStringListParamSeparator, withDecryption: Boolean = true)(implicit valueWriter: ValueWriter[A]): Map[String, ParamResult[A, _ <: ParamLike[A, _ <: ParamType]]] = {

    val result = client.getParameters(prepareRequest(withDecryption, names:_*))
    validParameters[A](result, stringListSeparator, withDecryption) ++ invalidParameters[A](result)
  }

  def read[A](name: String, stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): ParamResult[A, Param[A]] = {

    val result = client.getParameters(prepareRequest(withDecryption = false, name))

    lazy val whenInvalid = Invalid[A, Param[A]](InvalidParam[A](name))

    invalidParameters[A](result).get(name).fold[ParamResult[A, Param[A]]]({
      validParameters[A](result, stringListSeparator, withDecryption = false).get(name).fold[ParamResult[A, Param[A]]](whenInvalid)(validResult => {
        validResult.value.asParam
      })
    })(_ => whenInvalid)
  }

  def readList[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A, ParamList[A]] = {
    ???
  }

  def readSecure[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A, SecureParam[A]] = {
    ???
  }

}

object ParamReader {

  val DefaultStringListParamSeparator = ","

  def apply(awsSimpleSystemsManagement: AWSSimpleSystemsManagement): ParamReader = {
    new ParamReader {
      override val client: AWSSimpleSystemsManagement = awsSimpleSystemsManagement
    }
  }

  private[ParamReader] def prepareRequest(withDecryption: Boolean, names: String*): GetParametersRequest = {
    new GetParametersRequest().withNames(names:_*).withWithDecryption(withDecryption)
  }

  private[ParamReader] def validParameters[A](result: GetParametersResult, stringListSeparator: String, withDecryption: Boolean)(implicit valueWriter: ValueWriter[A]): Map[String, Valid[A, ParamLike[A, _ <: ParamType]]] = {
    result.getParameters.asScala.map(p => {
      p.getType match {
        case StringParam.name =>
          p.getName -> Valid[A, Param[A]](Param[A](p.getName, valueWriter.as(p.getValue)))
        case StringListParam.name =>
          p.getName -> Valid[A, ParamList[A]](ParamList[A](p.getName, p.getValue.split(stringListSeparator).map(valueWriter.as)))
        case SecureStringParam.name =>
          p.getName -> Valid[A, SecureParam[A]](SecureParam[A](p.getName, valueWriter.as(p.getValue), !withDecryption))
      }
    }).toMap
  }

  private[ParamReader] def invalidParameters[A](result: GetParametersResult): Map[String, Invalid[A, ParamLike[A, _ <: ParamType]]] = {
    result.getInvalidParameters.asScala.map(p => p -> Invalid(InvalidParam[A](p))).toMap
  }
}

