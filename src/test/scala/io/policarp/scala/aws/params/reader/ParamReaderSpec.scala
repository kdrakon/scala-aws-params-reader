package io.policarp.scala.aws.params.reader

import io.policarp.scala.aws.params.Params.ParamResult.InvalidParam
import io.policarp.scala.aws.params.Params.ParamTypes._
import io.policarp.scala.aws.params.reader.ValueWriters.StringValueWriter
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

import scala.collection.JavaConverters._

class ParamReaderSpec extends Properties(classOf[ParamReader].getTypeName) {

  property("prepareRequest") = forAll { (withDecryption: Boolean, names: Seq[String]) =>
    ParamReader.prepareRequest(withDecryption, names: _*).getWithDecryption == withDecryption &&
    ParamReader.prepareRequest(withDecryption, names: _*).getNames.asScala == names
  }

  property("readSingleParam") = forAll(Gen.oneOf(Seq(StringParam, StringListParam, SecureStringParam))) { paramType =>

    forAll { name: String =>
      val validResult = GetParameterResultGen.genValid(name).sample
      val param = ParamReader.readSingleParam(name, validResult, paramType)
      if (paramType.name == validResult.getParameters.asScala.head.getType) {
        param == Right(validResult.getParameters.asScala.head.getValue)
      } else {
        param == Left(InvalidParam[String](name))
      }
    }

    forAll { name: String =>
      val invalidResult = GetParameterResultGen.genInvalid(name).sample
      ParamReader.readSingleParam(name, invalidResult, paramType) == Left(InvalidParam[String](name))
    }

  }
}
