name := "test-classpath"

lazy val root = (project in file("."))
  .aggregate(foo, bar)
  .dependsOn(foo, bar)

lazy val foo = project
  .dependsOn(bar)
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test)

lazy val bar = project
