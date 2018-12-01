package server

import io.circe.Json
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import service.ReactiveMongoRegistry

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object GraphQLUtil {

  def executeGraphQL(query: Document, operationName: Option[String], vars: Json) =
    Executor.execute(SchemaDefinition.schema, query,
      userContext = new ReactiveMongoRegistry,
      variables = if (vars.isNull) Json.obj() else vars,
      operationName = operationName,
      exceptionHandler = exceptionHandler)

  def executeAndPrintGraphQL(query: String) =
    QueryParser.parse(query) match {
      case Success(doc) ⇒
        println(Await.result(executeGraphQL(doc, None, Json.obj()), 10 seconds).spaces2)
      case Failure(error) ⇒
        Console.err.print(error.getMessage)
    }

  val exceptionHandler = ExceptionHandler {
    case (_, e) ⇒ HandledException(e.getMessage)
  }

}
