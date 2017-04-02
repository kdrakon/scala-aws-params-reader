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
    StringValueWriter.as(rName.sample, string) == Valid[String](string.trim)
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

  import ListWriter._

  val someName = "blah"
  val separators = Seq(Comma, Semicolon, Space)

  "The ListWriter and LongValueWriter" should {
    "parse a Long List" in {
      forAll(Gen.oneOf(separators), Gen.nonEmptyListOf(Gen.chooseNum(Long.MinValue, Long.MaxValue))) { (listSeparator: ListSeparator, numbersList: List[Long]) =>
        ListWriter[Long](LongValueWriter, listSeparator).as(someName, numbersList.mkString(listSeparator.separator)) should equal(Right(numbersList))
      }
    }
  }

  def RandomList(listSeparator: ListSeparator): Gen[String] = for {
    list <- Gen.listOf(Gen.alphaStr.suchThat(!_.contains(listSeparator.separator)))
  } yield {
    list.mkString(listSeparator.separator)
  }

  "The ListWriter and StringValueWriter" should {
    "parse String List's" in {
      forAll(Gen.oneOf(separators)) { (listSeparator: ListSeparator) =>
        val list = RandomList(listSeparator).sample.get
        val output: List[String] = list.split(listSeparator.separator).map(_.trim).toList
        if (list.trim.isEmpty) {
          ListWriter[String](StringValueWriter, listSeparator).as(someName, list) should equal(Right(List[String]()))
        } else {
          ListWriter[String](StringValueWriter, listSeparator).as(someName, list) should equal(Right(output))
        }
      }
    }
  }

}
