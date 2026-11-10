# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

The Grinder is a Java load-testing framework (fork of the SourceForge project, maintained under `cossme`). Three process types, all JVMs:

- **Agent** (`net.grinder.Grinder`): long-running; launches/kills worker processes and caches scripts distributed from the console.
- **Worker**: spawned by an agent; runs the test script in N worker threads and reports statistics to the console.
- **Console** (`net.grinder.Console`): coordinates agents, collates stats, distributes scripts. Listens on port 6372 for agents/workers and serves the REST API and web UI on port 6373.

`README.MD` covers end-user operation (grinder.properties, starting agents/console/TCPProxy). The published user guide lives in `docs/guide/*.html` (Jekyll site for GitHub Pages).

## Build and test

Maven multi-module build, parent `pom.xml` at the root (`io.github.cossme:grinder-parent`, version 4.1.0-SNAPSHOT). The build compiles with `--release 21` and CI (`.github/workflows/build.yml`) runs `mvn -B verify` on Temurin 21 and 25. Java 21 is the floor for building and running. Maven is not on PATH in this environment by default; the wrapper distribution lives under `~/.m2/wrapper/dists/`.

```bash
mvn -B verify                                # what CI runs: build everything, run all tests, package the zip
mvn -B -DskipTests package                   # build only (keep -DskipTests, not -Dmaven.test.skip, so test-jars still build)
mvn -pl grinder-dcr-agent,grinder-test-support,grinder-core verify   # core plus what its tests need
mvn -pl grinder-dcr-agent,grinder-test-support,grinder-core verify -Dtest=TestGrinderProcess -Dsurefire.failIfNoSpecifiedTests=false
mvn -B -DskipTests package checkstyle:checkstyle   # style report per module in target/checkstyle-result.xml
```

Build gotchas:

- `grinder-core` and `grinder-http` tests require the `grinder-dcr-agent` jar to already be built: surefire runs with `-javaagent:target/grinder-dcr-agent.jar`, copied in by maven-dependency-plugin during `generate-test-resources`. Always build `grinder-dcr-agent` first (or use `-am`). Every module's surefire also loads Mockito's inline mock maker as a `-javaagent`, through the `${org.mockito:mockito-core:jar}` property that `dependency:properties` sets in the parent; a module that overrides `argLine` must keep that entry.
- `mvn test` on `grinder-core` alone fails in `maven-dependency-plugin` (MDEP-187); use the `verify` phase with the module list above.
- Tests expect `org.springframework.boot.logging.LoggingSystem=none` in `grinder-console-service` (set in its surefire argLine) so Spring Boot's logging system does not reconfigure the Grinder's Logback setup; `SpringConsoleFoundation` sets the same property at runtime. Logback is 1.5.x and SLF4J 2.0.x, the same line Spring Boot 4 manages.
- Module order matters for `-pl`: dcr-agent → test-support → core → httpclient / xmlbeans → http → console-service → web-console / swing-console → grinder (assembly).
- `JythonVersionRunner` runs the Jython suites against the shipped Jython when no alternative install is configured; it no longer skips silently.
- javac runs with `-Xlint:removal -Werror` (parent pom) and the tree compiles with zero javac warnings. Any new warning fails the build, including a plain deprecation from a dependency bump; fix it at the use site, or suppress it there with the reason in a comment, as `RestControllerTest` (SpringRunner) and `Cookie.setAttribute` do. HTTPClient has no finalizers; `grinder-http`'s surefire JVM runs with `--finalization=disabled` so a test cannot pass by relying on one.
- `etc/checkstyle.xml` and `etc/checkstyle-suppressions.xml` are the style rules (Checkstyle 9 syntax, `m_` field prefix, `s_` static prefix, 2-space indent, braces on their own line for `RightCurly`). The report is informational; it does not fail the build. `docs/development/contributing.html` describes the style in prose.
- The DCR weaver (`net.grinder.util.weave.j2se6.ASMTransformerFactory`) is on ASM 9. Any class file javac 21 produces contains `invokedynamic`; ASM 3.2 could not read those, which is why the ASM dependency cannot go back.
- `grinder-core` depends on Spring Boot because `SpringConsoleFoundation` (which boots the console's Spring context) lives there. `grinder-console-service` and `grinder-web-console` depend on it too. One `spring-boot.version` property in the parent pins all three starters.
- `grinder-console-service` uses non-standard source dirs: `src/`, `test/`, `resources/` (not `src/main/java`).
- The `grinder` module produces no code; it assembles the binary distribution zip (`grinder/src/main/assembly/assembly.xml`) with `lib/grinder.jar` (manifest classpath pointing at sibling jars), `examples/`, `contrib/` and unpacked `.xsl`/`.xsd` files in `etc/`. Jython example scripts live in `grinder/examples/`.
- `docker/Dockerfile` builds the console image from a distribution zip copied into `docker/` (the header says how); CI does not build or publish it. Nothing publishes to Maven Central (no signing or Central Portal profile); the `io.github.cossme` groupId is a namespace this fork cannot publish under.

## Module map and how the pieces connect

| Module | Role |
|---|---|
| `grinder-dcr-agent` | Tiny `-javaagent` jar (`Premain-Class: net.grinder.util.weave.agent.ExposeInstrumentation`) exposing `java.lang.instrument.Instrumentation`. Required at runtime for DCR ("dynamic code rewriting") instrumentation of tests. |
| `grinder-test-support` | Shared JUnit helpers: `AbstractFileTestCase`, `RandomStubFactory`, `CallRecorder`, `AssertUtilities`, `TemporaryDirectory`, etc. Used by every module's tests. |
| `grinder-core` | Nearly everything: engine, console model, communication, statistics, script engines. No shading; the DCR dispatch table (`engine/process/dcr/RecorderLocator`) uses its own `WeakIdentityConcurrentMap` (weak identity keys over `ConcurrentHashMap`, a `Cleaner` removes entries for collected targets). That class must stay under `net.grinder.engine.process`, the package `DCRContextImplementation` excludes from weaving. |
| `grinder-httpclient` | Vendored, LGPL `HTTPClient.*` library (not Apache HttpClient) used by the HTTP plugin. |
| `grinder-xmlbeans` | XMLBeans-generated classes from the HTTP recording XSDs (codegen at build time via xmlbeans-maven-plugin). |
| `grinder-http` | HTTP plugin (`net.grinder.plugin.http`), TCPProxy (`net.grinder.tools.tcpproxy`) and the recording filters that turn proxied traffic into scripts (`tcpproxyfilter`, uses grinder-xmlbeans + XSL). |
| `grinder-console-service` | REST API for the console (`net.grinder.console.service.RestController`: `/properties`, `/agents/*`, `/files/*`, `/recording/*`) plus `Bootstrap`, which is the bridge from the console's core components to Spring. |
| `grinder-web-console` | Spring Boot web UI (`net.grinder.console.webui.WebConsoleEndPoint`, static assets under `src/main/resources/static`). Default UI. |
| `grinder-swing-console` | Legacy Swing UI (`net.grinder.console.swingui`), used with `java net.grinder.Console -classic`. |
| `grinder` | Distribution assembly only. |

### Plugin discovery via META-INF service files

Two classpath-scanning extension points drive the wiring; know them before adding a component:

- `META-INF/net.grinder.scriptengine` (in grinder-core): ordered list of `ScriptEngineService` implementations. Order matters: Jython, then Clojure, then Java. A script engine is chosen per `grinder.script` by asking each service in turn.
- `META-INF/net.grinder.console` (in grinder-console-service): classes to add to the console's PicoContainer. `ConsoleFoundation` reads every such file on the classpath. The first `UI` implementation found becomes the UI (unless `-headless`, which forces `TextUI`); other classes are registered as components and instantiated eagerly if `Startable`. This is how `Bootstrap` gets the `SampleModel`, `ProcessControl`, `FileDistribution`, etc. injected by constructor.

### Console startup sequence

`Console.main` → `ConsoleFoundation` builds a PicoContainer with the core console components (`ConsoleCommunicationImplementation`, `ProcessControlImplementation`, `FileDistributionImplementation`, `SampleModelImplementation`, …) → loads dynamic components from `META-INF/net.grinder.console` → `run()` starts the container, then calls `SpringConsoleFoundation.initSpring()` (a `@SpringBootApplication` scanning `net.grinder.console`), then loops on `communication.processOneMessage()`. Spring beans (`RestController`, `WebConsoleEndPoint`) reach the console internals through the static `Bootstrap.getInstance()`, not through Spring injection. `ServletConfig` sets the embedded servlet port from `ConsoleProperties.getHttpPort()` (`grinder.console.httpPort`, default 6373).

Console settings persist in `~/.grinder_console` (`ConsoleProperties`). `grinder.startEmbeddedAgent=true` makes the web console start an agent in-process.

### Engine side (grinder-core)

- `net.grinder.engine.agent`: `AgentImplementation` connects to the console (or runs standalone if `grinder.useConsole=false`), receives `StartGrinderMessage`, and launches workers via `WorkerLauncher` / `ProcessWorkerFactory` (separate JVM) or `DebugThreadWorkerFactory` (in-process, `grinder.debug.singleprocess`). `FileStore` is the agent's local cache of distributed files.
- `net.grinder.engine.process`: `WorkerProcessEntryPoint` → `GrinderProcess` creates `GrinderThread`s, each with a `ThreadContext`. `ScriptEngineContainer` picks the script engine; `MasterInstrumenter` composes the engine's `Instrumenter`s so `Test.wrap()`/`record()` can instrument script objects. DCR instrumentation is in `engine/process/dcr` and `scriptengine/*/instrumentation`, built on `net.grinder.util.weave` (ASM).
- `net.grinder.script`: the public scripting API (`Grinder.grinder`, `Test`, `Statistics`, `SSLControl`, `Barrier`). Treat this package as a published API; scripts in the wild depend on it.
- `net.grinder.communication` / `net.grinder.messages`: the serialized-object protocol between agents, workers and console (`Sender`/`Receiver`, `MessageDispatchRegistry`). Message classes are `Serializable`; changing fields breaks wire compatibility with older agents.
- `net.grinder.statistics`: `StatisticsSet`, `TestStatisticsMap`, `StatisticsIndexMap`, expression views. Console graphs and the worker summary table are both built on these.
- `net.grinder.synchronisation`: distributed barriers (`grinder.barrier`) spanning worker threads, processes and the console.

### Logging

The console `logback.xml` is auto-loaded first in every process with root level `${grinder.logLevel:-warn}`; `GrinderProcess.configureLogging` layers `logback-worker.xml` on top without resetting the context, so the `worker` logger carries its own level (`${grinder.logLevel:-info}`) or it would inherit warn and the worker log would hold only warnings, as it did in 4.0.x. The per-line thread/run/test context in that log comes from `ThreadLogMarker` (`%marker`), not from Logback's own marker nesting, which SLF4J 2 deprecated. Logback 1.5 rejects an `<appender>` nested inside a `<logger>` and dumps its status list to the console on any configuration warning, so keep appenders top-level and referenced. Workers use `logback-worker.xml` (per-worker `host-n.log` and `host-n-data.log` under `grinder.logDirectory`, rolled by `RollOnStartUp`); agents and console use `logback.xml`. The web console's `/logs` endpoints read these files directly from disk.

## Tests

JUnit 4 + Mockito 5 (`mockito-core`, inline mock maker loaded as a Java agent by surefire). Test classes are named `Test*.java` and mirror main packages. Core tests often use the `grinder-test-support` stub/recording utilities rather than Mockito. Swing tests check `Boolean.getBoolean("build.travis")` and skip graphics when set, and the TCPProxy console test runs headless. `grinder-console-service` tests live in `test/` and use `spring-boot-starter-test` plus `spring-boot-starter-webmvc-test`. `tools/perf/helloworld-throughput.sh <dist root> [script] [runs] [threads]` runs one standalone worker from an unzipped distribution and prints wall-clock seconds and mean test time; `tools/perf/javacall.py` is a probe with one instrumented Java call per run, and `tools/perf/httpget.py` does one HTTP GET per run against a local server on port 7001 (`python3 -m http.server -p HTTP/1.1 7001`).

## Docs

- `docs/`: Jekyll GitHub Pages site (user guide, `docs/guide/*.html`). Edit these when changing user-visible behaviour (properties, REST API, console UI).
- `documentation/`: legacy Apache Forrest source for the old SourceForge site; effectively frozen.
- `grinder/CHANGES` is the release changelog shipped in the binary zip; `grinder/TODO.MD` is the original maintainer's wish list.
