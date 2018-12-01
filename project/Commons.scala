import sbt.Keys._
import sbt._

object Commons {
  val settings: Seq[Def.Setting[_]] = Seq(
    organization := "deontaljaard",
    scalaVersion := "2.12.7",
    resolvers := Resolvers.resolvers,
    scalacOptions += "-Ypartial-unification"
  )
}
