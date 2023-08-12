# How to contribute

This article is about contributing to the project.

## Design

The entrypoint is located in [the Main class in the main module](main/src/main/java/org/mgwa/w40k/pairing/Main.java).

Here is a description of the Maven modules:

| Module     | Description                                                                                             |
|------------|---------------------------------------------------------------------------------------------------------|
| main       | Entrypoint of the program. It handles arguments and launches both the UI and the HTTP API.              |
| gui        | The JavaFX based heavy-weight _User Interface_                                                          |
| http-api   | The HTTP API that would be used by HTML pages.                                                          |
| web-app    | The HTML pages. They are included in the package, but have not direct dependency with any other module. |
| core       | It contains the pairing algorithms, the shared model and utilities.                                     |
| matrix-xls | It implements the reading of a score matrix from an Excel file.                                         |
| package    | Technical module that cares about the packaging of the project.                                         |

## Requirements

In order to build the project, you will need:
* A [JDK](https://jdk.java.net/) version 17 or more
* [Apache Maven](https://maven.apache.org/) version 3.8.6 or more

## Fast run

In the development process, we can quickly run the application with the following steps:

1. Run `mvn install` (if not already done)
2. Go to the `main` folder
3. Run `mvn javafx:run`

The last two steps can be replaced with the launch of the `launch.sh` script from the root folder.

## Fat JAR packaging 

In order to run the project using a _fat JAR file_:

1. Run `mvn package -Ppack` in order to generate a _fat JAR file_ and run unit-tests.
2. Go to the `target` folder
3. Call either `start.sh` from Linux/Mac-OS, or `start.bat` from Windows.

## <a name="installer"></a> Creating an installer

In order to generate an installer on Linux, you will also need:

* The package `fakeroot`, that is required by **jpackage**.
* The package `binutils`, since this is requirement for **jlink**. If not present, it would prompt an error message containing `Cannot run program "objcopy"`.

Here is an example of a command that packages the project, where `__` must be replaced by the path to the local JDK:

```
mvn package -Dmake.installer -Djdk.home=__
```

* The installer is generated inside the `target` folder
* The installer is only compatible with the current OS
* It can take more than one minute for the processing

## Release process

In the process below, we should write `__` instead of the tag name that is also the semantic version of the release (e.g. `0.2`).

1. Prepare the release with `mvn release:prepare`
2. Answer questions; provide the semantic version `__`
3. Push the Git commits with `git push`
4. Push the Git tag with `git push origin __`
5. Checkout the code-base from the tag with `git checkout __`
6. Generate the installers like explained in [the corresponding section above](#installer), for each OS
7. Create a [new GitHub release](https://github.com/flarcher/mgwa-pairing-table-tool/releases/new) with a description, and artifacts. The artifacts are the _fat JAR file_ and the installers (packages) to be uploaded.
8. Communicate about the new release in the chat channels
