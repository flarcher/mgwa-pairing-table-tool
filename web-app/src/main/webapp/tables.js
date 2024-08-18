
var refreshTables = function() {
    var tablesDiv = document.getElementById("tables");
    var teamNamesRow = tablesDiv.querySelectorAll(".teams .name > span");
    if (teamNamesRow.length != 2) {
        throw "Expecting 2 teams";
    }
    teamNamesRow[0].textContent = getData().match.row_team
    teamNamesRow[1].textContent = getData().match.column_team

    // Reset
    Array.from(tablesDiv.children).forEach(e => {
        if (!e.classList.contains('teams')) {
            e.remove();
        }
    });

    // Create tables
    const memberCount = getData().match.team_member_count;
    for (let i = 0; i < memberCount; i++) {
        var tableRow = document.createElement("div");
        var tableName = document.createElement("label");
        tableName.textContent = "Table " + (i + 1).toFixed();
        tableRow.appendChild(tableName);
        var tableDiv = document.createElement("div");
        tableDiv.classList.add('table');
        // TODO: read data for table content
        tableDiv.classList.add('unassigned');
        var left = document.createElement("span");
        left.classList.add('left');
        tableDiv.appendChild(left);
        var right = document.createElement("span");
        right.classList.add('right');
        tableDiv.appendChild(right);
        tableRow.appendChild(tableDiv);
        tablesDiv.appendChild(tableRow);
    }
};
