# ETC Table Pairing Tool

Helps at pairing armies on tables using rules of the **European Tabletop Championship**.

Is written for the **MGWA association**.

## Usage

1. Make sure you have a *Java Runtime Environment* version 11+ installed
2. Unzip the released archive
3. Call either `start.sh` or `start.bat` from Linux/MacOS or Windows respectively

About the start scripts:

* Optionally, you may provide the path to the matrix Excel file (containing estimated scores of pairings) as the first argument of the script. 

## Contributing

In order to build the project, you will need:
* A [JDK](https://jdk.java.net/) version 11 or more
* [Apache Maven](https://maven.apache.org/) version 3.6.0 or more

Here is the command for a minimal build:

    mvn clean install

Then, in order to run the application (without packaging the project):

    cd gui && mvn javafx:run

For a run from the project's package:

1. Run `mvn package` in order to generate the package
2. Go to the `target` folder (the package is currently this folder)
3. Call either `start.sh` from Linux/Mac-OS, or `start.bat` from Windows.

## License

This project is licensed according to **CC BY-NC-SA** ( [description](https://creativecommons.org/licenses/by-nc-sa/4.0/) | [legal](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode) )
