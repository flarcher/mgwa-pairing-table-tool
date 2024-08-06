# Web application module

The web application source code is located in [src/main/webapp](./src/main/webapp) and is bundled in a `web-app` folder inside the application archive.

There is a [TODO list](TODO.md).

## Javascript data variables

Here is a description of attributes of the result of the `getData()` function, to be used from the [app.js](src/main/webapp/app.js) file:

* `match` contains:

    - `team_member_count` : member count per team
    - `row_team` : name of the team displayed on rows (by default)
    - `column_team` : name of the team displayed on columns (by default)

* `row_armies` is a list of the names of  team displayed on rows (by default)
* `col_armies` is a list of the names of  team displayed on rows (by default)
* `scores` is a list of lists of scores. Each score is composed of:

    - `min` : minimum estimated score (between 0 and 20)
    - `max` : maximum estimated score (between 0 and 20)

* `tables` is a list of table assignment. Each table is described with:

    - `row_army`: Name of the army on the _row_ side
    - `col_army`: Name of the army on the _column_ side

* `defenders` stores the defender army attributes:

    - `row`: the _row_ army index
    - `column` : the _column_ army index

* `attackers` stores the attacker candidates about the current assignment step. It contains:

    - `rows`: is a list of 2 _row_ army indexes.
    - `columns`: is a list of 2 _column_ army indexes.
