# Plexus Interpolation

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-interpolation.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.plexus/plexus-interpolation)
[![GitHub CI](https://github.com/codehaus-plexus/plexus-interpolation/actions/workflows/maven.yml/badge.svg)](https://github.com/codehaus-plexus/plexus-interpolation/actions)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/codehaus/plexus/plexus-interpolation/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/codehaus/plexus/plexus-interpolation/README.md)

Resolves `${...}` expressions in strings. This is the engine behind POM interpolation — when Maven turns
`${project.version}` in your POM into an actual version, this is what does it.

Expression sources are stackable, so you can resolve against a model object, then system properties, then
the environment, in a defined order; cycle detection and synonym prefixes (`${pom.x}` and `${project.x}`)
are built in.

It began as a package inside `plexus-utils` and was split out so the two could version independently.

## Status

Maintained, conservatively. The API is stable and very widely depended on transitively — changes are
mostly bug fixes.

## Using it

```xml
<dependency>
  <groupId>org.codehaus.plexus</groupId>
  <artifactId>plexus-interpolation</artifactId>
</dependency>
```

Check the badge above for the current version.

## Requirements

Java 8 or later.

## Documentation

- [Project site](https://codehaus-plexus.github.io/plexus-interpolation/) — includes a worked example of the configuration Maven uses
- [Javadoc](https://javadoc.io/doc/org.codehaus.plexus/plexus-interpolation)
- [Release notes](https://github.com/codehaus-plexus/plexus-interpolation/releases)

## Contributing

See [CONTRIBUTING.md](https://github.com/codehaus-plexus/.github/blob/master/CONTRIBUTING.md). In short:
`mvn verify` builds, and run `mvn spotless:apply` before pushing or CI will fail on formatting.

Please report security vulnerabilities privately — see
[SECURITY.md](https://github.com/codehaus-plexus/.github/blob/master/SECURITY.md), not a public issue.
