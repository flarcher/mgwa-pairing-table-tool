
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

const initScoreEdit = function() {
    var formElement = _getScoreEditFormElement();
    var scoreMinInput = formElement.querySelector("#new_score_min");
    var scoreMaxInput = formElement.querySelector("#new_score_max");
    var nextScoreElement = formElement.querySelector("#score_next");

    const valuesRequirement = () => {
        var minimum = scoreMinInput.value;
        var maximum = scoreMaxInput.value;
        if (!minimum || !maximum) { return true; } // Robustness
        return parseInt(minimum) <= parseInt(maximum);
    };
    [ scoreMinInput, scoreMaxInput ].forEach(input => {
        input.addEventListener("change", event => {
            // Refresh the next score element
            var nextScore = {
                min: parseInt(scoreMinInput.value), 
                max: parseInt(scoreMaxInput.value)
            };
            if ((nextScore.min != NaN) && (nextScore.max != NaN)) {
                setupScoreElement(nextScoreElement, nextScore);
            }
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
        postFormCall(
            getData().api_url + 'update/' + row + '/' + col,
            formElement,
            newScore => {
                getData().scores[parseInt(row)][parseInt(col)] = newScore;
                setupScoreElement(tableCell, newScore);
                endLoading('matrix');
            },
            json => errorHandler(json),
            false); // Not multi-part
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
    var rowNameElement = formElement.querySelector("#row_name");
    rowNameElement.textContent = getData().match.row_team + " / " + getData().row_armies[row];
    var colNameElement = formElement.querySelector("#col_name");
    colNameElement.textContent = getData().match.column_team + " / " + getData().col_armies[col];

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

// Initializes the edition of a score from the matrix table
const initScoreLink = function(tableCell, i, j) {
    tableCell.dataset.row = i.toFixed();
    tableCell.dataset.col = j.toFixed();
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

var findScoreCell = function(row, col) { // Arguments must be strings
    var matrixTableBody = _getScoreTableElement();
    return Array.from(matrixTableBody.querySelectorAll('tr'))
        .filter(tr => !tr.classList.contains('corner') && !tr.classList.contains('name'))
        .flatMap(tr => {
            var match = Array.from(tr.querySelectorAll('td'))
                .find(td => td.dataset.col === col );
            return match ? [ match ] : [];
        })
        .filter(td => !td.classList.contains('corner') && !td.classList.contains('name'))
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
            newRow.appendChild(rowTeamCell);
        }
        let rowArmyCell = document.createElement("td");
        rowArmyCell.classList.add('name');
        rowArmyCell.textContent = rowArmies[i];
        newRow.appendChild(rowArmyCell);
        for (let j = 0; j < columnArmies.length; j++) {
            let scoreCell = document.createElement("td");
            setupScoreElement(scoreCell, scores[i][j]);
            initScoreLink(scoreCell, i, j);
            newRow.appendChild(scoreCell);
        }
    }

};