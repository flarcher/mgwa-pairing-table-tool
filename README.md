# ETC Table Pairing Tool

Helps at pairing armies on tables using rules of the **European Tabletop Championship**.

Is written for the **MGWA association**.

## Usage

Install the application using the provided installer.

Inputs:

* Optionally, you may provide the path to the matrix Excel file (containing estimated scores of pairings) as the first argument of the script. See [this file as an example](matrix-xls/src/test/resources/example.xlsx).
* The environment variable `LANG` defines the used locale for displayed texts.
* The environment variable `API_SERVER_FILE` can define the location of an alternative DropWizard (HTTP API) configuration file
* The environment variable `WEBAPP_TMP_FOLDER` defines an alternative temporary folder where web files will be placed. This folder will be deleted after execution.

## Contributing

See [this page about contributing to this project](CONTRIBUTE.md).

## License

This project is licensed according to **CC BY-NC-SA** ( [description](https://creativecommons.org/licenses/by-nc-sa/4.0/) | [legal](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode) )
