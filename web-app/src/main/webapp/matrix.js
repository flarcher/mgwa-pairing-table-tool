
const SCORE_CLASSES = [ 'game', 'bad', 'good', 'above', 'middle', 'below' ];

var setupScoreElement = function(element, score) {
    element.classList.add('score');
    element.textContent = score.min.toFixed() + "-" + score.max.toFixed();
    var scoreClass
    if (score.min > score.max || score.min < 0 || score.max > 20) {
        console.warn("Invalid score " + score);
        return;
    }
    if (score.min < 3 && score.max > 17) {
        scoreClass = 'game';
    } else if (score.max < 5) {
        scoreClass = 'bad';
    } else if (score.min > 15) {
        scoreClass = 'good';
    }

    if (!scoreClass) {
        const average = (score.min + score.max) / 2;
        if (average > 10) {
            scoreClass = 'above';
        } else if (average < 10) {
            scoreClass = 'below';
        } else if (average == 10) {
            scoreClass = 'middle';
        }
    }

    if (scoreClass) {
        SCORE_CLASSES.forEach(c => element.classList.remove(c)); // Clean-up
        element.classList.add(scoreClass); // Adds appropriate class
    }
};

const _getScoreEditFormElement = function() {
    return document.getElementById("edit_score").querySelector('form');
};

const getScoreFromInputs = function(scoreMinInput, scoreMaxInput) {
    return {
        min: parseInt(scoreMinInput.value),
        max: parseInt(scoreMaxInput.value)
    };
};

const setupScoreBasedOnInputs = function(scoreMinInput, scoreMaxInput, scoreElement) {
    var nextScore = getScoreFromInputs(scoreMinInput, scoreMaxInput);
    if ((nextScore.min != NaN) && (nextScore.max != NaN)) {
        setupScoreElement(scoreElement, nextScore);
    }
};

const initScoreInputs = function(scoreMinInput, scoreMaxInput, scoreElement) {
    const valuesRequirement = () => {
        var minimum = scoreMinInput.value;
        var maximum = scoreMaxInput.value;
        if (!minimum || !maximum) { return true; } // Robustness
        return parseInt(minimum) <= parseInt(maximum);
    };
    [ scoreMinInput, scoreMaxInput ].forEach(input => {
        input.addEventListener("change", event => {
            // Refresh the next score element
            setupScoreBasedOnInputs(scoreMinInput, scoreMaxInput, scoreElement);
            // Handle form validation
            var thisInput = event.target;
            var constraintFine = valuesRequirement();
            var validityState = thisInput.validity;
            if (validityState.customError || validityState.valid /* Built-in validation check */) {
                if (validityState.customError && constraintFine) {
                    thisInput.setCustomValidity(""); // Reset custom error
                } else if (validityState.valid && !constraintFine) {
                    thisInput.setCustomValidity("The minimum value must be less or equal than the maximum value"); // Add custom error
                }    
            }
            // Force values comparison
            var thisValue = parseInt(thisInput.value);
            var otherInput = [ scoreMinInput, scoreMaxInput ].find(c => c.id != thisInput.id);
            var otherValue = parseInt(otherInput.value);
            if (thisValue && otherValue && !constraintFine) {
                otherInput.value = thisValue;
            }
        });
    });
};

const initScoreEditForm = function() {
    var formElement = _getScoreEditFormElement();

    // Handling of score display/inputs
    var scoreMinInput = formElement.querySelector("#new_score_min");
    var scoreMaxInput = formElement.querySelector("#new_score_max");
    var nextScoreElement = formElement.querySelector("#score_next");
    initScoreInputs(scoreMinInput, scoreMaxInput, nextScoreElement);

    // Submission of the form
    formElement.addEventListener("submit", event => {
        if (!formElement.checkValidity()) {
            return;
        }
        event.preventDefault();
        startLoading();
        var row = formElement.dataset.row;
        var col = formElement.dataset.col;
        var tableCell = findScoreCell(row, col);
        var newScore = {
            min: parseInt(scoreMinInput.value),
            max: parseInt(scoreMaxInput.value)
        };
        getData().scores[parseInt(row)][parseInt(col)] = newScore;
        setupScoreElement(tableCell, newScore);
        endLoading('matrix');
    });
}

const refreshScoreEditForm = function(row, col) {
    const score = getData().scores[row][col];
    console.log("Current score is: " + score.min.toFixed() + "-" + score.max.toFixed());
    
    var formElement = _getScoreEditFormElement();

    // Apply data context
    formElement.dataset.row = row;
    formElement.dataset.col = col;

    // Display competitors
    var rowNameElement = formElement.querySelector("#row_team");
    rowNameElement.textContent = getData().match.row_team;
    var rowArmyElement = formElement.querySelector("#row_army");
    rowArmyElement.textContent = getData().row_armies[row];
    var colNameElement = formElement.querySelector("#col_team");
    colNameElement.textContent = getData().match.column_team;
    var colArmyElement = formElement.querySelector("#col_army");
    colArmyElement.textContent = getData().col_armies[col];

    // Display scores
    var prevScoreElement = formElement.querySelector("#score_prev");
    setupScoreElement(prevScoreElement, score);
    var nextScoreElement = formElement.querySelector("#score_next");
    setupScoreElement(nextScoreElement, score); // Previous values are default values

    // Previous values are default values
    var scoreMinInput = formElement.querySelector("#new_score_min");
    scoreMinInput.value = score.min;
    var scoreMaxInput = formElement.querySelector("#new_score_max");
    scoreMaxInput.value = score.max;
};

const _getTeamNameEditForm = () => document.getElementById('edit_team').querySelector('form');

const initTeamLink = function(spanElement, isRow = true) {
    spanElement.dataset.is_row = _boolToStr(isRow);
    var form = _getTeamNameEditForm();
    spanElement.addEventListener("click", (e) => {
        e.preventDefault();
        const isRow = _strToBool(e.target.dataset.is_row);
        switchSection('edit_team');
        // Refresh form values
        form.dataset.is_row = _boolToStr(isRow);
        var teamName = isRow ? getData().match.row_team : getData().match.column_team;
        form.querySelector("#current_team_name").textContent = teamName;
        form.querySelector('#new_team_name').value = teamName; // default
    });
};
const initTeamNameEditForm = function() {
    var form = _getTeamNameEditForm();
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();

        const isRow = _strToBool(form.dataset.is_row);
        const newName = form.querySelector('#new_team_name').value;
        const matchAttr = isRow ? 'row_team' : 'column_team';
        getData().match[matchAttr] = newName;

        // Update team names
        findTeamNameElement(isRow).textContent = newName;
        _getScoreEditFormElement().querySelector(isRow ? '#row_team' : '#col_team').textContent = newName;
        updateTablesTeamNames(null /* Use default */, newMatch.row_team, newMatch.column_team);
        updateTableAssignmentTeamNames(null /* Use default */, newMatch.row_team, newMatch.column_team);
        refreshTeamChoiceNames();
        // TODO: update other places

    });
};

const _getArmyNameEditForm = () => document.getElementById('edit_army').querySelector('form');

const initArmyLink = function(spanElement, isRow, index) {
    spanElement.dataset.is_row = _boolToStr(isRow);
    spanElement.dataset.index  = index.toFixed();
    var form = _getArmyNameEditForm();
    spanElement.addEventListener("click", (e) => {
        e.preventDefault();
        const isRow = _strToBool(e.target.dataset.is_row);
        const index = parseInt(e.target.dataset.index);
        switchSection('edit_army');
        // Refresh form values
        form.dataset.is_row = _boolToStr(isRow);
        form.dataset.index  = index.toFixed();
        var teamName = isRow ? getData().match.row_team : getData().match.column_team;
        form.querySelector("#team_name").textContent = teamName;
        var armyName = isRow ? getData().row_armies[index] : getData().col_armies[index];
        form.querySelector('#army_name').textContent = armyName;
        form.querySelector('#new_army_name').value = armyName; // default
    });
};
const initArmyNameEditForm = function() {
    var form = _getArmyNameEditForm();
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();
        startLoading();
        const isRow = _strToBool(form.dataset.is_row);
        const index = parseInt(form.dataset.index); // Army index
        var newName = form.querySelector('#new_army_name').value;
        if (isRow) {
            getData().row_armies[index] = newName;
        } else {
            getData().col_armies[index] = newName;
        }
        findArmyNameCell(isRow, index).textContent = newName;
        updateTableAssignmentArmyName(null /* default */, isRow, index, newName);
        // TODO: update other places
        endLoading('matrix');
    });
};

const _highlightNamesListener = (enable, e) => {
    const row = e.target.dataset.row;
    const col = e.target.dataset.col;
    var colArmyCell = findArmyNameCell(false, parseInt(col));
    var rowArmyCell = findArmyNameCell(true,  parseInt(row));
    [ colArmyCell, rowArmyCell ].forEach(cell => {
        if (enable) {
            cell.classList.add('light');
        } else {
            cell.classList.remove('light');
        }
    });
};

// Initializes the edition of a score from the matrix table
const initScoreLink = function(tableCell, i, j) {
    tableCell.dataset.row = i.toFixed();
    tableCell.dataset.col = j.toFixed();
    tableCell.addEventListener("mouseenter", (e) => {
        _highlightNamesListener(true, e);
    });
    tableCell.addEventListener("mouseleave", (e) => {
        _highlightNamesListener(false, e);
    });
    tableCell.addEventListener("click", (e) => {
        e.preventDefault();
        const row = e.target.dataset.row;
        const col = e.target.dataset.col;
        refreshScoreEditForm(row, col);
        switchSection('edit_score');
    });
};

var _getScoreTableElement = function() {
    return document.querySelector("#matrix > table > tbody");
};

var findTeamNameElement = function(isRow) {
    var matrixTableBody = _getScoreTableElement();
    var teamNameTableCell;
    if (isRow) {
        teamNameTableCell = matrixTableBody.querySelector('tr > td.name:first-child');
    } else {
        teamNameTableCell = matrixTableBody.querySelector('tr:first-child td.name');
    }
    return teamNameTableCell.querySelector('span');
};

var findArmyNameCell = function(isRow, index) {
    var matrixTableBody = _getScoreTableElement();
    var isRowMatch =_boolToStr(isRow);
    var indexMatch = index.toFixed();
    return Array.from(matrixTableBody.querySelectorAll('tr'))
        .flatMap(tr => {
            var match = Array.from(tr.querySelectorAll('td'))
                .filter(td => td.classList.contains('name'))
                .find(td => td.dataset.is_row === isRowMatch && td.dataset.index  === indexMatch);
            return match ? [ match ] : [];
        })
        .find(() => true);
};

var findScoreCell = function(row, col) { // Arguments must be strings
    var matrixTableBody = _getScoreTableElement();
    return Array.from(matrixTableBody.querySelectorAll('tr'))
        .flatMap(tr => {
            var match = Array.from(tr.querySelectorAll('td'))
                .filter(td => !td.classList.contains('corner') && !td.classList.contains('name'))
                .find(td => td.dataset.col === col );
            return match ? [ match ] : [];
        })
        .find(td => td.dataset.row === row );
};

// Refresh the DOM part dedicated to the matrix, using getData()
var refreshMatrix = function() {
    var matrixTableBody = _getScoreTableElement();

    emptyElement(matrixTableBody); // reset

    const memberCount = getData().match.team_member_count;
    var newRow = document.createElement("tr");
    matrixTableBody.appendChild(newRow);
    addTableCornerCell(newRow);
    addTableCornerCell(newRow);
    const columnTeamCell = document.createElement("td");
    columnTeamCell.classList.add('name');
    columnTeamCell.setAttribute("colspan", memberCount.toFixed());
    const columnTeamSpan = document.createElement("span");
    columnTeamSpan.textContent = getData().match.column_team;
    initTeamLink(columnTeamSpan, false);
    columnTeamCell.appendChild(columnTeamSpan);
    newRow.appendChild(columnTeamCell);

    newRow = document.createElement("tr");
    matrixTableBody.appendChild(newRow);
    addTableCornerCell(newRow);
    addTableCornerCell(newRow);

    var columnArmies = getData().col_armies; // Column armies names
    var rowArmies    = getData().row_armies; // Row armies names
    if (memberCount != rowArmies.length || memberCount != columnArmies.length) {
        throw "Inconsistent matrix size"
    }

    for (let i = 0; i < columnArmies.length; i++) {
        let colArmyCell = document.createElement("td");
        colArmyCell.classList.add('name');
        colArmyCell.textContent = columnArmies[i];
        initArmyLink(colArmyCell, false, i);
        newRow.appendChild(colArmyCell);
    }

    var scores = getData().scores;     // Matrix values
    for (let i = 0; i < rowArmies.length; i++) {
        newRow = document.createElement("tr");
        matrixTableBody.appendChild(newRow);
        if (i == 0) { // Adds the 'row' team name (once)
            const rowTeamCell = document.createElement("td");
            rowTeamCell.setAttribute("rowspan", memberCount.toFixed());
            rowTeamCell.classList.add('name');
            const rowTeamSpan = document.createElement("span");
            rowTeamSpan.textContent = getData().match.row_team;
            rowTeamCell.appendChild(rowTeamSpan);
            initTeamLink(rowTeamCell, true);
            newRow.appendChild(rowTeamCell);
        }
        let rowArmyCell = document.createElement("td");
        rowArmyCell.classList.add('name');
        rowArmyCell.textContent = rowArmies[i];
        initArmyLink(rowArmyCell, true, i);
        newRow.appendChild(rowArmyCell);
        for (let j = 0; j < columnArmies.length; j++) {
            let scoreCell = document.createElement("td");
            setupScoreElement(scoreCell, scores[i][j]);
            initScoreLink(scoreCell, i, j);
            newRow.appendChild(scoreCell);
        }
    }

};

const _assignMatrixScore = (matrixBody, table, enable) => {
    if (! table) {
        return; // Robustness
    }
    let rowIndex = table.row_army.toFixed();
    let colIndex = table.col_army.toFixed();
    Array.from(matrixBody.querySelectorAll('tr'))
        .flatMap(tr => {
            let match = [];
            let allRowCells = Array.from(tr.querySelectorAll('td'));
            // Score cells
            match = match.concat(allRowCells.filter(td =>
                td.classList.contains('score') &&
                    (td.dataset.col === colIndex || td.dataset.row === rowIndex)
                )
            );
            // Name cells
            match = match.concat(allRowCells.filter(td => {
                    if (!td.classList.contains('name')) { return false; }
                    let isRow = td.dataset['is_row'] === 'true';
                    return (isRow && td.dataset.index === rowIndex) ||
                        (!isRow && td.dataset.index === colIndex);
                })
            );
            return match;
        })
        .forEach(td => enable ? td.classList.add('assign') : td.classList.remove('assign'));
};

var assignMatrixScore = (oldTable, newTable) => {
    var matrixBody = _getScoreTableElement();
    // Remove the class on old table assignment
    _assignMatrixScore(matrixBody, oldTable, false);
    // Add the class on new table assignment
    _assignMatrixScore(matrixBody, newTable, true);
};
