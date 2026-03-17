# AGENTS.md

This repository builds the `DrivenByMoss` Bitwig extension with Maven.
The codebase is predominantly Java 21 and is organized around controller-specific packages plus a shared framework layer.

## Repository Snapshot

- Build tool: Maven (`pom.xml` at repo root)
- Java version: 21 (`maven-compiler-plugin` uses `source` and `target` 21)
- Packaging: shaded JAR plus `.bwextension` copy during `install`
- Main sources: `src/main/java`
- Resources: `src/main/resources`
- Assembly descriptor: `src/assembly/dep.xml`
- Tests: no `src/test/java` directory exists at the time of writing

## Agent Priorities

- Prefer small, localized changes; many packages are controller-specific and should not be generalized casually.
- Preserve behavior across hardware integrations; subtle MIDI, USB, OSC, or display timing changes can break devices.
- Keep framework APIs stable unless the change clearly requires a coordinated refactor.
- Follow existing source style even when it differs from common Java defaults.
- Current focus area: prioritize work related to control of the Novation Launchpad Pro Mk3 unless the task clearly targets another controller or shared framework code.

## Rule Files

- No `.cursor/rules/` directory was found.
- No `.cursorrules` file was found.
- No `.github/copilot-instructions.md` file was found.
- If any of those files are added later, merge their guidance into this document and treat the repo-specific rule files as higher priority.

## Build Commands

- Compile only: `mvn compile`
- Clean compile: `mvn clean compile`
- Package artifacts: `mvn clean package -Dbitwig.extension.directory=target`
- Full install flow: `mvn clean install -Dbitwig.extension.directory=target`
- Developer quick path from README: `mvn install`
- After making changes, always run `./install-bitwig-local.sh`
- Linux release helper: `./release-linux.sh`
- macOS release helper: `./release-macos.sh`
- Windows release helper: `release-windows.cmd`

## What The Build Produces

- A shaded JAR during `package`
- `DrivenByMoss.bwextension` copied to `bitwig.extension.directory` during `install`
- A ZIP assembly via `maven-assembly-plugin`
- Third-party license metadata via `license-maven-plugin`

## Test Commands

- Run all tests: `mvn test`
- Run a single test class: `mvn -Dtest=ClassName test`
- Run a single test method: `mvn -Dtest=ClassName#methodName test`
- Run multiple tests with a pattern: `mvn -Dtest=ClassName1,ClassName2 test`

## Current Test Reality

- The repository currently has no checked-in `src/test/java` tree.
- `maven-surefire-plugin` is configured, so the single-test commands above are the expected Maven/Surefire workflow once tests exist.
- `maven-compiler-plugin` explicitly skips `testCompile` in `pom.xml`, so adding Java tests may also require updating Maven configuration before tests can compile.
- Do not claim tests passed unless you actually added test sources and verified the current Maven config supports them.

## Lint And Verification

- There is no dedicated lint plugin such as Checkstyle, Spotless, PMD, or Error Prone configured in `pom.xml`.
- In practice, the closest lightweight verification commands are:
- `mvn compile`
- `mvn test`
- `mvn clean package -Dbitwig.extension.directory=target`
- If you add a formatter or lint tool, document the exact command here and keep it aligned with existing source formatting.

## Dependency And Release Utilities

- Show outdated dependencies: `mvn versions:display-dependency-updates`
- Regenerate third-party license report: `mvn license:add-third-party`
- Linux debug launch helper: `./debug-linux.sh`

## Environment Notes

- Maven 3.8.1+ is required by `maven-enforcer-plugin`.
- Java 21 is required.
- Some release scripts set `JAVA_HOME` explicitly; follow that pattern if local Java selection matters.
- Packaging commands often expect `-Dbitwig.extension.directory=target` for local builds to avoid copying into a real Bitwig extensions directory.
- Local Bitwig control-surface API reference: `/Applications/Bitwig Studio.app/Contents/Resources/Documentation/control-surface/api/index.html`
- When working against Bitwig controller APIs, consult the local control-surface API docs above before guessing parameter order or semantics.

## Source Layout

- `de.mossgrabers.framework`: reusable framework abstractions and shared logic
- `de.mossgrabers.bitwig.framework`: Bitwig-specific implementations of framework interfaces
- `de.mossgrabers.controller...`: controller integrations, commands, views, setup, and device logic
- `de.mossgrabers.bitwig.controller...`: extension-definition entry points for Bitwig discovery

## Style Overview

- Java source uses 4-space indentation.
- Opening braces go on the next line, not the same line.
- There is a deliberate space before method-call and declaration parentheses, e.g. `foo ()`, `if (x)`, `new HashMap<> ()`.
- Keep the project's spacing style even if your formatter would normally remove that extra space.
- Files typically begin with the existing copyright/license header.
- Javadoc is common on public types and public methods; preserve it and extend it when adding public API.

## Imports

- Do not use wildcard imports.
- Group imports by origin with blank lines between groups.
- Common order is JDK imports first, third-party imports next, project imports last.
- Remove unused imports promptly.

## Formatting Conventions

- Use one declaration per line.
- Keep blank lines between logical sections of fields and methods.
- Short single-line `if` statements without braces are used in places; do not mechanically rewrite them unless touching the code for clarity.
- Align wrapped declarations and field blocks consistently with nearby code rather than reformatting whole files.
- Preserve existing enum and constant alignment when editing dense declarations.

## Types And Language Features

- Use explicit types by default.
- `final` is used heavily for method parameters and many local variables; keep that style.
- Records are used selectively for tiny immutable wrappers; do not convert ordinary classes to records without a clear benefit.
- Interfaces commonly use the `I` prefix, e.g. `IModel`, `IHost`, `IValueChanger`.
- Exceptions use the `*Exception` suffix and are often thin wrappers with descriptive messages.

## Naming

- Packages are all lowercase and deeply hierarchical by device/framework area.
- Classes use PascalCase.
- Methods and fields use camelCase.
- Constants use `UPPER_SNAKE_CASE`.
- Enum constants use `UPPER_SNAKE_CASE`.
- Use descriptive names tied to controller/domain concepts rather than generic names.
- For device-specific code, prefer names that include the hardware family or protocol when helpful, e.g. `GamepadConfiguration`, `GenericFlexiControlSurface`.

## Object And API Conventions

- Prefer `this.` when reading or writing instance fields; it is used consistently.
- Constructors often just assign dependencies and initialize helper objects; keep them straightforward.
- Static utility state is acceptable where the existing design uses it, but avoid introducing new global state casually.
- Match surrounding collection choices (`EnumMap`, `ArrayList`, `HashMap`, `HashSet`) instead of abstractly refactoring them.

## Error Handling

- Catch narrow exception types where practical.
- When reporting failures, the common pattern is `this.host.error ("message", ex)` or `this.host.error ("message")`.
- Error messages are concise and operational; keep them direct.
- Avoid swallowing exceptions silently.
- Prefer recovering gracefully for device-access errors and logging through the host instead of crashing extension logic.

## Control Flow Preferences

- Early returns are common and preferred for guard conditions.
- `switch` statements are used for command and mode dispatch; preserve that style where it fits.
- Avoid introducing deep nesting when a guard clause would do.
- Be careful with timing-sensitive loops, executors, and I/O interactions in controller/display code.

## Comments And Documentation

- Keep existing file headers intact.
- Use Javadoc for public APIs, constructors, and non-obvious behavior.
- Inline comments are acceptable for hardware quirks, protocol details, or UI edge cases.
- Do not add commentary for obvious code.

## When Editing Maven Config

- Do not remove the Java 21 requirement.
- Be careful with `bitwig.extension.directory`; it controls where the extension file is copied.
- If you enable tests, review the custom `default-testCompile` execution that currently sets `<skip>true</skip>`.
- Keep plugin versions explicit, as the existing `pom.xml` does.

## Safe Change Strategy For Agents

- Read neighboring controller/framework classes before changing shared abstractions.
- Assume similar hardware integrations may copy or mirror patterns from one another.
- Prefer adding narrowly-scoped behavior over broad cleanup passes.
- Avoid repository-wide formatting churn.
- If you add tests later, document the exact command you ran and whether Maven config had to change to make tests compile.
