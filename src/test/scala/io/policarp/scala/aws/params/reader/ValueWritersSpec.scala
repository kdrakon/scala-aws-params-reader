package io.policarp.scala.aws.params.reader

import io.policarp.scala.aws.params.Params.ParamResult._
import io.policarp.scala.aws.params.reader.ValueWriters._
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration

class ValueWritersSpec extends Properties("ValueWriters") {

  def rName: nyaya.gen.Gen[String] = nyaya.gen.Gen.alpha.string(0 to 100)

  property("StringValueWriter") = forAll { string: String =>
    StringValueWriter.as(rName.sample, string) == Valid[String](string)
  }

  property("BooleanValueWriter") = forAll { (bool: Boolean, garbage: String) =>
    val name = rName.sample()
    BooleanValueWriter.as(name, bool.toString) == Valid[Boolean](bool) && BooleanValueWriter.as(name, garbage) == Invalid[Boolean](InvalidParam(name))
  }

  property("IntValueWriter") = forAll { (int: Int, garbage: String) =>
    val name = rName.sample()
    IntValueWriter.as(name, int.toString) == Valid[Int](int) && IntValueWriter.as(name, garbage) == Invalid[Int](InvalidParam(name))
  }

  property("LongValueWriter") = forAll { (long: Long, garbage: String) =>
    val name = rName.sample()
    LongValueWriter.as(name, long.toString) == Valid[Long](long) && LongValueWriter.as(name, garbage) == Invalid[Long](InvalidParam(name))
  }

  case class DurationTuple(time: Long, unit: String) {
    def toDuration: Duration = Duration.create(s"$time $unit")
  }
  implicit val DurationTupleGen: Gen[DurationTuple] = for {
    time <- Gen.chooseNum[Long](0, 252)
    unit <- Gen.oneOf("d, day, h, hour, min, minute, s, sec, second, ms, milli, millisecond, µs, micro, microsecond, ns, nano, nanosecond".split(","))
  } yield {
    DurationTuple(time, unit)
  }

  property("DurationValueWriter") = forAll(DurationTupleGen, Gen.alphaStr) { (duration: DurationTuple, garbage: String) =>
    val name = rName.sample()
    DurationValueWriter.as(name, s"${duration.time} ${duration.unit}") == Right(duration.toDuration) &&
      DurationValueWriter.as(name, garbage) == Invalid(InvalidParam(name))
  }

}

class ListWriterSpec extends WordSpec with Matchers with GeneratorDrivenPropertyChecks {

  val someName = "blah"
  val separators = Seq(",", ";", " ")

  "The ListWriter and LongValueWriter" should {
    "parse a Long List" in {
      forAll(Gen.oneOf(separators), Gen.nonEmptyListOf(Gen.chooseNum(Long.MinValue, Long.MaxValue))) { (separator: String, numbersList: List[Long]) =>
        ListWriter[Long](LongValueWriter, separator).as(someName, numbersList.mkString(separator)) should equal(Right(numbersList))
      }
    }
  }

  "The ListWriter and StringValueWriter" should {
    "parse String List's" in {
      forAll(Gen.oneOf(separators), Gen.nonEmptyListOf(Gen.alphaStr)) { (separator: String, list: List[String]) =>
        if (list.mkString(separator) == ""){
          ListWriter[String](StringValueWriter, separator).as(someName, list.mkString(separator)) should equal(Right(List()))
        } else {
          ListWriter[String](StringValueWriter, separator).as(someName, list.mkString(separator)) should equal(Right(list))
        }
      }
    }
  }

}
