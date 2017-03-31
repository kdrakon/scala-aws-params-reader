package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersResult, Parameter, ParameterType}
import org.scalacheck.Gen

object GetParameterResultGen {

  def genValid(name: String): Gen[GetParametersResult] = for {
    param <- Gen.alphaStr
    paramType <- Gen.oneOf[ParameterType](ParameterType.values())
  } yield {
    new GetParametersResult()
      .withParameters(new Parameter().withName(name).withValue(param).withType(paramType))
  }

  def genInvalid(name: String): Gen[GetParametersResult] = Gen.const(new GetParametersResult().withInvalidParameters(name))

}
