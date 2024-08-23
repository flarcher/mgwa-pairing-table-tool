const NO_ARMY_INDEX = -1; // Must be below 0

const newTablesList = (tableCount) => Array.from(new Array(tableCount),
    (x, i) => {return { index: i, row_army: NO_ARMY_INDEX, col_army: NO_ARMY_INDEX };});

const getPairCount = (tables) => (tables || getData().tables).filter(_isAssigned).length;

const _isAssigned = (t) => t.row_army > NO_ARMY_INDEX && t.col_army > NO_ARMY_INDEX;

const _getTableOf = (tables, index) => (tables || getData().tables).find(t => t.index == index);

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

const updateTableAssignmentTeamNames = (parentSection, rowTeamName, colTeamName) => {
    let section = parentSection || document.getElementById('assign_table');
    section.querySelector('#row_team').textContent = rowTeamName || getData().match.row_team;
    section.querySelector('#col_team').textContent = colTeamName || getData().match.column_team;
};

const updateTableAssignmentArmyName = (parentSection, isRow, index, newName) => {
    let section     = parentSection || document.getElementById('assign_table');
    let armySelect  = section.querySelector(isRow ? '#row_select' : '#col_select');
    Array.from(armySelect.querySelectorAll('option'))
        .filter(option => option.value == index)
        .forEach(matchOption => { matchOption.textContent = newName; });
};

const _updateTableAssignmentOptions = (selectElement, isRow) => {
    emptyElement(selectElement);
    let remainingIndexes = getRemainingArmies(isRow);
    let armyNames = isRow ? getData().row_armies : getData().col_armies;
    remainingIndexes.forEach(index => {
        let option = document.createElement('option');
        option.value = index;
        option.textContent = armyNames[index];
        selectElement.appendChild(option);
    });
};

const _displayAssignmentScore = (rowArmySelect, colArmySelect, scoreElement = null) => {
    let row = parseInt(rowArmySelect.value);
    let col = parseInt(colArmySelect.value);
    let tableScoreSpan = scoreElement || document.getElementById('score_table');
    if (row == NaN || col == NaN) {
        console.warn('No row/col for assignment');
        tableScoreSpan.textContent = '?';
    } else {
        let score = getScoreData(row, col);
        setupScoreElement(tableScoreSpan, score);
    }
};

const updateTableAssignmentOptions = (rowArmySelect, colArmySelect, scoreElement = null, tableIndex = NO_ARMY_INDEX) => {
    _updateTableAssignmentOptions(rowArmySelect, true);  // TODO: add its current selection
    _updateTableAssignmentOptions(colArmySelect, false); // TODO: add its current selection
    _displayAssignmentScore(rowArmySelect, colArmySelect, scoreElement);
};

const _refreshTableContent = (table) => {
    let isAssigned = _isAssigned(table);
    let armyNames  = isAssigned ? _getArmyNames(table) : [ '' , '' ];
    let tableIndex = table.index.toFixed();
    Array.from(document.getElementById('tables').querySelectorAll('.table'))
        .filter(div => div.parentElement.dataset.index === tableIndex)
        .forEach(div => {
            switchClass(div, 'unassigned', !isAssigned);
            div.querySelector('.left').textContent  = armyNames[0]; // Row
            div.querySelector('.right').textContent = armyNames[1]; // Column
        });
};

// Assignment logic (All arguments are strings)
const _assignTable = (tableIndex, rowArmyIndex, colArmyIndex) => {
    // Update data
    let table = _getTableOf(getData().tables, parseInt(tableIndex));
    let oldTable = structuredClone(table);
    table.row_army = parseInt(rowArmyIndex);
    table.col_army = parseInt(colArmyIndex);
    let newTable = structuredClone(table);
    // Update badges
    refreshBadges();
    // Updates corresponding elements in <div id="matrix">
    assignMatrixScore(_isAssigned(oldTable) ? oldTable : null, newTable);
    // Updates corresponding elements in <div id="tables">
    _refreshTableContent(newTable);
};

// Unassignment logic (All arguments are strings)
const _unassignTable = (tableIndex) => {
    let table = _getTableOf(getData().tables, parseInt(tableIndex));
    if (_isAssigned(table)) {
        if (window.confirm("Are you sure you want to free the table "+ displayTableIndex(tableIndex) + "?")) {
            table.row_army = NO_ARMY_INDEX;
            table.col_army = NO_ARMY_INDEX;
            // Update badges
            refreshBadges();
            // Updates corresponding elements in <div id="matrix">
            assignMatrixScore(table, null);
            // Updates corresponding elements in <div id="tables">
            _refreshTableContent(table);
        }
    } else {
        window.alert("The table " + displayTableIndex(tableIndex) + " is already free");
    }
};

const initTableAssignmentForm = () => {
    let section        = document.getElementById('assign_table');
    let sectionForm    = section.querySelector('form');
    let tableScoreSpan = section.querySelector('#score_table');
    let rowArmySelect  = section.querySelector('#row_select');
    let colArmySelect  = section.querySelector('#col_select');

    updateTableAssignmentTeamNames(sectionForm);
    updateTableAssignmentOptions(rowArmySelect, colArmySelect);

    // Update the score display after each 
    [ rowArmySelect, colArmySelect ].forEach(select => {
        select.addEventListener("change", (e) => {
            _displayAssignmentScore(rowArmySelect, colArmySelect, tableScoreSpan);
        });
    });

    sectionForm.addEventListener("submit", event => {
        if (!sectionForm.checkValidity()) {
            return;
        }
        event.preventDefault();
        _assignTable(sectionForm.dataset.table, rowArmySelect.value, colArmySelect.value);
        switchNavTab(null, 'tables');
    });
};

const displayTableIndex = (indexAsStr) => (parseInt(indexAsStr) + 1).toFixed();

const _assignTableClickListener = (e) => {
    let section        = switchSection('assign_table');
    let sectionForm    = section.querySelector('form');
    let tableIndexSpan = section.querySelector('#table_index');
    let tableScoreSpan = section.querySelector('#score_table');
    let rowArmySelect  = section.querySelector('#row_select');
    let colArmySelect  = section.querySelector('#col_select');

    let tableIndex = getParentData(e.target.parentElement, 'index');
    if (!tableIndex) { throw 'No table index found'; }
    sectionForm.dataset.table  = tableIndex;
    tableIndexSpan.textContent = displayTableIndex(tableIndex);
    updateTableAssignmentOptions(rowArmySelect, colArmySelect, tableScoreSpan, parseInt(tableIndex));
};

const _unassignTableClickListener = (e) => {
    let tableIndex = getParentData(e.target.parentElement, 'index');
    if (!tableIndex) { throw 'No table index found'; }
    _unassignTable(tableIndex);
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

        // Actions button
        var actionsDiv = document.createElement("div");
        actionsDiv.classList.add('actions');
        var assignLink = document.createElement("a");
        assignLink.classList.add('assign');
        assignLink.classList.add('submit');
        assignLink.href = "#";
        assignLink.addEventListener('click', _assignTableClickListener);
        actionsDiv.appendChild(assignLink);
        var unassignLink = document.createElement("a");
        unassignLink.classList.add('unassign');
        unassignLink.classList.add('submit');
        unassignLink.href = "#";
        unassignLink.addEventListener('click', _unassignTableClickListener);
        actionsDiv.appendChild(unassignLink);
        tableRow.appendChild(actionsDiv);

        tablesDiv.appendChild(tableRow);
    });
};
