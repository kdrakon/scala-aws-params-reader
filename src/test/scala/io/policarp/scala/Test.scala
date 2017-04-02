package io.policarp.scala

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import io.policarp.scala.aws.params.reader.ParamReader

object Test extends App {

  val client = AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build()
  val params = ParamReader(client)

  println(params.read[String]("sean-testing-parameter-store"))
  println(params.readList[String]("sean-testing-parameter-store"))
  println(params.readSecure[String]("sean-testing-parameter-store"))
}
