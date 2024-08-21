const newTablesList = (tableCount) => Array.from(new Array(tableCount),
    (x, i) => {return { index: i, row_army: -1, col_army: -1 };});

const getPairCount = (tables) => (tables || getData().tables).filter(_isAssigned).length;

const _isAssigned = (t) => t.row_army >= 0 && t.col_army >= 0;

const _getArmyNames = (t) => [
        t.row_army >= 0 ? getData().row_armies[t.row_army] : '',
        t.col_army >= 0 ? getData().col_armies[t.col_army] : '',
    ];

var updateTablesTeamNames = function(tablesElement, rowTeamName, colTeamName) {
    var tablesDiv = tablesElement || document.getElementById("tables");
    var teamNamesRow = tablesDiv.querySelectorAll(".teams .name > span");
    if (teamNamesRow.length != 2) {
        throw "Expecting 2 teams";
    }
    teamNamesRow[0].textContent = rowTeamName || getData().match.row_team;
    teamNamesRow[1].textContent = colTeamName || getData().match.column_team;
};

const _assignTableClickListener = (e) => {
    let tableIndex = e.target.parentElement.dataset.index;
    console.log("Assigning table " + tableIndex);
};

var refreshTables = function() {
    var tablesDiv = document.getElementById("tables");
    updateTablesTeamNames(tablesDiv);

    // Reset
    Array.from(tablesDiv.children).forEach(e => {
        if (!e.classList.contains('teams')) {
            e.remove();
        }
    });

    // Create tables
    const memberCount = getData().match.team_member_count;
    const tables = getData().tables;
    if (memberCount != tables.length) {
        console.error("Team member count is different than the table count");
    }
    tables.forEach(table => {

        let assigned = _isAssigned(table);
        let armies = _getArmyNames(table);

        var tableRow = document.createElement("div");
        tableRow.dataset.index = table.index.toFixed();

        // Table name
        var tableName = document.createElement("label");
        tableName.textContent = "Table " + (table.index + 1).toFixed();
        tableRow.appendChild(tableName);
        // Table assignment
        var tableDiv = document.createElement("div");
        tableDiv.classList.add('table');
        if (!assigned) {
            tableDiv.classList.add('unassigned');
        }
        var left = document.createElement("span");
        left.classList.add('left');
        left.textContent = armies[0] || '';
        tableDiv.appendChild(left);
        var right = document.createElement("span");
        right.classList.add('right');
        right.textContent = armies[1] || '';
        tableDiv.appendChild(right);
        tableRow.appendChild(tableDiv);
        // Assignment button
        var assignLink = document.createElement("a");
        assignLink.classList.add('assign');
        assignLink.classList.add('submit');
        assignLink.href = "#";
        assignLink.addEventListener('click', _assignTableClickListener);
        tableRow.appendChild(assignLink);

        tablesDiv.appendChild(tableRow);
    });
};
