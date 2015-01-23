// Any customized settings must be written here, i.e. after 'assemblySettings' above.
// See https://github.com/sbt/sbt-assembly for available parameters.

// We do not want to run the test when assembling because we prefer to chain the various build steps manually, e.g.
// via `./sbt clean test scoverage:test package packageDoc packageSrc doc assembly`.  Because, in this scenario, we
// have already run the tests before reaching the assembly step, we do not re-run the tests again.
//
// Comment the following line if you do want to (re-)run all the tests before building assembly.
test in assembly := {}
