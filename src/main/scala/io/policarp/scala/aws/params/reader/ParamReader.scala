package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersRequest, GetParametersResult}
import io.policarp.scala.aws.params.Params.ParamResult._
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

import scala.collection.JavaConverters._

trait ParamReader {

  import ParamReader._

  implicit val client: AWSSimpleSystemsManagement

  def read[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {
    readSingleParam(
      name,
      DefaultStringListParamSeparator, // unused
      withDecryption = false
    )
  }

  def readList[A](name: String, stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): ParamResult[Seq[A]] = {
    readSingleParam(
      name,
      stringListSeparator,
      withDecryption = false
    )(ListWriter(valueWriter, stringListSeparator), client)
  }

  def readSecure[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {
    readSingleParam(
      name,
      DefaultStringListParamSeparator, // unused
      withDecryption = true
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

  private[ParamReader] def readSingleParam[A](name: String, stringListSeparator: String, withDecryption: Boolean)(implicit valueWriter: ValueWriter[A], client: AWSSimpleSystemsManagement): ParamResult[A] = {

    val result = client.getParameters(prepareRequest(withDecryption, name))

    type Output = ParamResult[A]
    lazy val whenInvalid = Invalid[A](InvalidParam[A](name))

    invalidParameters[A](result).get(name).fold[Output]({
      validParameters(result).get(name).fold[Output](whenInvalid)(validResult => valueWriter.as(name, validResult))
    })(_ => whenInvalid)
  }

  private[ParamReader] def prepareRequest(withDecryption: Boolean, names: String*): GetParametersRequest = {
    new GetParametersRequest().withNames(names: _*).withWithDecryption(withDecryption)
  }

  private[ParamReader] def validParameters(result: GetParametersResult): Map[String, String] = {
    result.getParameters.asScala.map(p => p.getName -> p.getValue).toMap
  }

  private[ParamReader] def invalidParameters[A](result: GetParametersResult): Map[String, Invalid[A]] = {
    result.getInvalidParameters.asScala.map(p => p -> Invalid(InvalidParam[A](p))).toMap
  }
}

