# Work in progress

# What?
Let's you read AWS EC2 system parameters (_parameter store_) in a scala-friendly way.

## Example
```scala
  
package io.policarp.scala

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import io.policarp.scala.aws.params.reader.ParamReader

object Test extends App {

  val client = AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build()
  val params = ParamReader(client)
  
  println(params.read[Long]("length"))
  // Right(Param(length, 42))
  
  println(params.readList[Long]("length"))
  // Left(InvalidParam(length)) because 'length' is actually a single parameter
  
  println(params.readList[String]("names"))
  // Right(ParamList(names,ArraySeq(Grayson, Jemma)))
  
  println(params.readSecure[String]("mysecret"))
  // Right(SecureParam(mysecret, hunter2, false)) where 'false' indicates it's no longer encrypted
  
  println(params.readSecure[String]("yoursecret"))
  // Left(InvalidParam(yoursecret)) because 'yoursecret' does not exist
  
  println(params.readMany[String](Seq("length", "names", "mysecret", "yoursecret"), withDecryption = false))
  /** 
   * Map(
   *   length -> Right(Param(length, 42)), 
   *   names -> Right(ParamList(names,ArraySeq(Grayson, Jemma))), 
   *   mysecret -> Right(SecureParam(mysecret, AQECAHjeg2OD8fUwNXm64gLTT8aT3PYKEp+Dt0mF4db2bb4ioAAAAM, true)),
   *   yoursecret -> Left(InvalidParam(yoursecret))
   * )
   */
}

```