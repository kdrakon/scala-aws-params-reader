package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersRequest, GetParametersResult}
import io.policarp.scala.aws.params.Params.ParamLikes._
import io.policarp.scala.aws.params.Params.ParamResult._
import io.policarp.scala.aws.params.Params.ParamTypes._
import io.policarp.scala.aws.params.Params._
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

import scala.collection.JavaConverters._

trait ParamReader {

  import ParamReader._
  import io.policarp.scala.aws.params.Params.ParamResult._

  implicit val client: AWSSimpleSystemsManagement

  def readMany[A](names: Seq[String], stringListSeparator: String = DefaultStringListParamSeparator, withDecryption: Boolean = true)(implicit valueWriter: ValueWriter[A]): Map[String, ParamResult[A, _ <: ParamLike[A, _ <: ParamType]]] = {
    val result = client.getParameters(prepareRequest(withDecryption, names: _*))
    validParameters[A](result, stringListSeparator, withDecryption) ++ invalidParameters[A](result)
  }

  def read[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A, Param[A]] = {
    readSingleParam(
      name,
      DefaultStringListParamSeparator,
      withDecryption = false,
      result => result.asParam
    )
  }

  def readList[A](name: String, stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): ParamResult[A, ParamList[A]] = {
    readSingleParam(
      name,
      stringListSeparator,
      withDecryption = false,
      result => result.asParamList
    )
  }

  def readSecure[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A, SecureParam[A]] = {
    readSingleParam(
      name,
      DefaultStringListParamSeparator,
      withDecryption = true,
      result => result.asSecureParam
    )
  }
}

object ParamReader {

  val DefaultStringListParamSeparator = ","

  def apply(awsSimpleSystemsManagement: AWSSimpleSystemsManagement): ParamReader = {
    new ParamReader {
      override implicit val client: AWSSimpleSystemsManagement = awsSimpleSystemsManagement
    }
  }

  private[ParamReader] def readSingleParam[A, T <: ParamLike[A, _ <: ParamType]](name: String, stringListSeparator: String, withDecryption: Boolean, toT: ParamLike[A, _ <: ParamType] => ParamResult[A, T])(implicit valueWriter: ValueWriter[A], client: AWSSimpleSystemsManagement): ParamResult[A, T] = {

    val result = client.getParameters(prepareRequest(withDecryption, name))

    type Output = ParamResult[A, T]
    lazy val whenInvalid = Invalid[A, T](InvalidParam[A](name))

    invalidParameters[A](result).get(name).fold[Output]({
      validParameters[A](result, stringListSeparator, withDecryption = false).get(name).fold[Output](whenInvalid)(validResult => {
        toT(validResult.value)
      })
    })(_ => whenInvalid)
  }

  private[ParamReader] def prepareRequest(withDecryption: Boolean, names: String*): GetParametersRequest = {
    new GetParametersRequest().withNames(names: _*).withWithDecryption(withDecryption)
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

