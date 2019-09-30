# Test Classpath Bug Reproduction

IntelliJ IDEA's Scala Plugin miscalculates the test classpath of a multi-module SBT build. This simple example project
aims to reproduce this bug.

This project is a multi-module build, consisting of a root build, and two modules:

- The `bar` module contains a single test resource (`bar/src/test/resources/bar.conf`)
- The `foo` module contains a single test (`foo/src/test/scala/foo/FooTest.scala`), and depends on the `bar` module
- The `root` build contains nothing, and just aggregates the `foo` and `bar` modules

## Setup

More specifically, the _compile_ of the `foo` module depends on the _compile_ of the `bar` module. This is the default
when we say:

```sbt
lazy val foo = project
  .dependsOn(bar)
```

But could also be specified manually with `.dependsOn(bar % "compile->compile")` - see [SBT's documentation](https://www.scala-sbt.org/1.x/docs/Multi-Project.html#Per-configuration+classpath+dependencies)
for more information about this syntax.

Regardless, the point is that the `foo` test configuration is **not** configured to depend on the test configuration of
`bar`. Accordingly, `foo` should not be able to access `bar.conf`. You can see this by asking SBT about the classpath
of `foo`'s test configuration:

```
[IJ]sbt:test-classpath> show foo/test:fullClasspath
[info] * Attributed(~/workspace/test-classpath/foo/target/scala-2.12/test-classes)
[info] * Attributed(~/workspace/test-classpath/foo/target/scala-2.12/classes)
[info] * Attributed(~/workspace/test-classpath/bar/target/scala-2.12/classes)
[...]
```

Note that, as we'd expect, `bar/target/scala-2.12/test-classes` is **not** listed.

## Problem

### `sbt test` gets it right

```
[IJ]sbt:test-classpath> test
[info] FooTest:
[info] Test resources
[info] - should not contain resources from the subproject 'bar'
[info] Run completed in 353 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 1 s, completed 30/09/2019 4:22:51 PM
```

This is what we should expect.

### IntelliJ test runners get it wrong

Running with a ScalaTest test runner in IntelliJ gives:

```
file:~/workspace/test-classpath/bar/target/scala-2.12/test-classes/bar.conf was not null
ScalaTestFailureLocation: foo.FooTest at (FooTest.scala:7)
Expected :null
Actual   :file:~/workspace/test-classpath/bar/target/scala-2.12/test-classes/bar.conf
```

This also occurs with other test frameworks and runners (e.g. specs2). And it means that, e.g. if you're using Lightbend
Config, `reference.conf` files will be picked up from any of your source dependencies' `test/resources` directories!

Note that because the test classpath is calculated in IntelliJ, and because the SBT test classpath is ignored,
there aren't any good workarounds I know of.
