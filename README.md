# Scala AWS Params Reader
[![Build Status](https://travis-ci.org/kdrakon/scala-aws-params-reader.svg?branch=master)](https://travis-ci.org/kdrakon/scala-aws-params-reader)
### _...work in progress_

## What?
Let's you read data from AWS' _EC2 Systems Manager Parameter Store_ in a Scala-friendly way.

## Huh?
- [AWS Systems Manager Parameter Store](http://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html)
- [Simple Secrets Management via AWS’ EC2 Parameter Store](https://medium.com/@mda590/simple-secrets-management-via-aws-ec2-parameter-store-737477e19450)

## Example
```scala
  
package io.policarp.scala

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import io.policarp.scala.aws.params.reader.ParamReader
import io.policarp.scala.aws.params.reader.ListWriter._

object Test extends App {

  val client = AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build()
  val params = ParamReader(client)
  
  println(params.read[Long]("length"))
  // Right(42)
  
  println(params.readList[Long]("length"))
  // Left(InvalidParam(length)) because 'length' is actually a single parameter
  
  println(params.readList[String]("names"))
  // Right(List(Grayson,Jemma)))
  
  println(params.readList[String]("emails", listSeparator = Semicolon))
  // Right(List(alice@somemail.com,bob@anothermail.com)))  
  
  println(params.readSecure[String]("mysecret"))
  // Right(hunter2)
  
  println(params.readSecure[String]("yoursecret"))
  // Left(InvalidParam(yoursecret)) because 'yoursecret' does not exist
}

```