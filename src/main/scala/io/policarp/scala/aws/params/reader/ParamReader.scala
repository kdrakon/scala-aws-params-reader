package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersRequest, GetParametersResult, Parameter}
import io.policarp.scala.aws.params.Params.ParamResult._
import io.policarp.scala.aws.params.Params.ParamTypes.{ParamType, SecureStringParam, StringListParam, StringParam}
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

import scala.collection.JavaConverters._

trait ParamReader {

  import ParamReader._

  val client: AWSSimpleSystemsManagement

  def read[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {
    readSingleParam(
      name,
      client.getParameters(prepareRequest(withDecryption = false, name)),
      StringParam
    )
  }

  def readList[A](name: String, stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): ParamResult[Seq[A]] = {
    readSingleParam(
      name,
      client.getParameters(prepareRequest(withDecryption = false, name)),
      StringListParam
    )(ListWriter(valueWriter, stringListSeparator))
  }

  def readSecure[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {
    readSingleParam(
      name,
      client.getParameters(prepareRequest(withDecryption = true, name)),
      SecureStringParam
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

  private[reader] def readSingleParam[A](name: String, result: GetParametersResult, parameterType: ParamType)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {

    type Output = ParamResult[A]
    lazy val whenInvalid = Invalid[A](InvalidParam[A](name))

    invalidParameters[A](result).get(name).fold[Output]({
      validParameters(result).get(name).fold[Output](whenInvalid)(validParameter => {
        if (ParamType(validParameter.getType) == parameterType) {
          valueWriter.as(validParameter.getName, validParameter.getValue)
        } else {
          whenInvalid
        }
      })
    })(_ => whenInvalid)
  }

  private[reader] def prepareRequest(withDecryption: Boolean, names: String*): GetParametersRequest = {
    new GetParametersRequest().withNames(names: _*).withWithDecryption(withDecryption)
  }

  private[reader] def validParameters(result: GetParametersResult): Map[String, Parameter] = {
    result.getParameters.asScala.map(p => p.getName -> p).toMap
  }

  private[reader] def invalidParameters[A](result: GetParametersResult): Map[String, Invalid[A]] = {
    result.getInvalidParameters.asScala.map(p => p -> Invalid(InvalidParam[A](p))).toMap
  }
}

