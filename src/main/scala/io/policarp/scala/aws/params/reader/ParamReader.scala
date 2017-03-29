package io.policarp.scala.aws.params.reader

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import com.amazonaws.services.simplesystemsmanagement.{ AWSSimpleSystemsManagement, AWSSimpleSystemsManagementClientBuilder }
import io.policarp.scala.aws.params.Params
import io.policarp.scala.aws.params.Params.ParamResult.ParamResult
import io.policarp.scala.aws.params.Params._
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

import scala.collection.JavaConverters._

trait ParamReader {
  import ParamReader._
  import io.policarp.scala.aws.params.Params.ParamResult._

  val client: AWSSimpleSystemsManagement

  def read[A](name: String, stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = {
    readMany(Seq(name), stringListSeparator)(valueWriter).get(name) match {
      case Some(p) => p
      case None => Invalid(InvalidParam[A](name))
    }
  }

  def readMany[A](names: Seq[String], stringListSeparator: String = DefaultStringListParamSeparator)(implicit valueWriter: ValueWriter[A]): Map[String, ParamResult[A]] = {

    val result = client.getParameters {
      new GetParametersRequest()
        .withNames(names: _*)
        .withWithDecryption(true)
    }

    val valid: Map[String, Right[InvalidParam[A], _ <: ParamLike[A, _ <: ParamType]]] = result.getParameters.asScala.map(p => {
      p.getType match {
        case StringParam.name => p.getName -> Valid(Param[A](p.getName, valueWriter.as(p.getValue)))
        case StringListParam.name => p.getName -> Valid(ParamList[A](p.getName, p.getValue.split(stringListSeparator).map(valueWriter.as)))
        case SecureStringParam.name => p.getName -> Valid(SecureParam[A](p.getName, valueWriter.as(p.getValue)))
      }
    }).toMap

    val invalid: Map[String, Left[InvalidParam[A], _ <: ParamLike[A, _ <: ParamType]]] =
      result.getInvalidParameters.asScala.map(p => p -> Invalid(InvalidParam[A](p))).toMap

    valid ++ invalid
  }
}

object ParamReader {

  val DefaultStringListParamSeparator = ","

  def apply(awsSimpleSystemsManagement: AWSSimpleSystemsManagement): ParamReader = {
    new ParamReader {
      override val client: AWSSimpleSystemsManagement = awsSimpleSystemsManagement
    }
  }
}

object Test extends App {

  import Params._

  val client = AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build()
  val x = ParamReader(client)

  val y: ParamResult[Long] = x.read("sean-testing-parameter-store")

  y match {
    case Right(v: Param[_]) =>
      println(v.value); println(v.wasSecured);
    case Right(v: ParamList[_]) =>
      println(v.value.mkString(" and ")); println(v.wasSecured)
    case Right(v: SecureParam[_]) =>
      println(v.value); println(v.wasSecured)
    case Left(i) => println(s"Nope $i")
  }

}
