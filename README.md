# ETC Table Pairing Tool

Helps at pairing armies on tables using rules of the **European Tabletop Championship**.

Is written for the **MGWA association**.

## Usage

1. Make sure you have a *Java Runtime Environment* version 11+ installed
2. Unzip the released archive
3. Call either `start.sh` or `start.bat` from Linux/MacOS or Windows respectively

Inputs:

* Optionally, you may provide the path to the matrix Excel file (containing estimated scores of pairings) as the first argument of the script. 
* The environment variable `LANG` defines the used locale for displayed texts.

## Contributing

In order to build the project, you will need:
* A [JDK](https://jdk.java.net/) version 17 or more
* [Apache Maven](https://maven.apache.org/) version 3.6.0 or more

For a run from the project's package:

1. Run `mvn install` in order to generate the package
2. Go to the `target` folder (the package is currently this folder)
3. Call either `start.sh` from Linux/Mac-OS, or `start.bat` from Windows.

## License

This project is licensed according to **CC BY-NC-SA** ( [description](https://creativecommons.org/licenses/by-nc-sa/4.0/) | [legal](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode) )
