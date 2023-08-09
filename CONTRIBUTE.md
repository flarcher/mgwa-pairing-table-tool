# How to contribute

This article is about contributing to the project.

## Requirements

In order to build the project, you will need:
* A [JDK](https://jdk.java.net/) version 17 or more
* [Apache Maven](https://maven.apache.org/) version 3.8.6 or more

## Fast run

In the development process, we can quickly run the application with the following steps:

1. Run `mvn install` (if not already done)
2. Go to the `gui` folder
3. Run `mvn javafx:run`

## Fat JAR packaging 

In order to run the project using a _fat JAR file_:

1. Run `mvn install -Ppack` in order to generate a _fat JAR file_ and run unit-tests.
2. Go to the `target` folder
3. Call either `start.sh` from Linux/Mac-OS, or `start.bat` from Windows.

## Creating an installer

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
