package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersResult, Parameter, ParameterType}
import nyaya.gen.Gen


object GetParameterResultGen {

  def genValid(name: String): Gen[GetParametersResult] = for {
    param <- Gen.alpha.string(0 to 100)
    paramType <- Gen.chooseArray_![ParameterType](ParameterType.values())
  } yield {
    new GetParametersResult()
      .withParameters(new Parameter().withName(name).withValue(param).withType(paramType))
  }

  def genInvalid(name: String): Gen[GetParametersResult] = Gen.pure(new GetParametersResult().withInvalidParameters(name))

}
