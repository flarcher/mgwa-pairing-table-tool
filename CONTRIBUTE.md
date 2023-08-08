# How to contribute

## Development requirements

In order to build the project, you will need:
* A [JDK](https://jdk.java.net/) version 17 or more
* [Apache Maven](https://maven.apache.org/) version 3.8.6 or more

## Test run

In order to run the project using a _fat JAR file_:

1. Run `mvn install` in order to generate a _fat JAR file_ and run unit-tests.
2. Go to the `target` folder
3. Call either `start.sh` from Linux/Mac-OS, or `start.bat` from Windows.

## Packaging

In order to package the project on Linux, you will also need:

* The package `fakeroot`, that is required by **jpackage**.
* The package `binutils`, since this is requirement for **jlink**. If not present, it would prompt an error message containing `Cannot run program "objcopy"`.

Here is an example of a command that packages the project: `mvn package -pl package -Ppack`

* It creates the installation package only for the current OS
* It can take more than one minute for the processing
* It relies on the Maven profile `pack` that is not enabled by default
