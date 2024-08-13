
// Refresh the DOM part dedicated to the matrix, using getData()
var refreshMatrix = function() {
    var matrixTableBody = document.querySelector("#matrix > table > tbody");

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
        if (i == 0) {
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
            newRow.appendChild(scoreCell);
        }
    }

};

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

var refreshBadges = function() {
    const memberCount = getData().match.team_member_count;
    const pairCount = getData().tables.length;
    const stepCounts = getStepCounts(memberCount, pairCount)

    const stepsLabel = stepCounts[0].toFixed() + "/" + stepCounts[1].toFixed();
    const pairsLabel = pairCount.toFixed() + "/" + memberCount.toFixed();

    var header = document.querySelector('header');
    header.querySelector('#steps').textContent = stepsLabel;
    header.querySelector('#pairs').textContent = pairsLabel;
};

var refreshAssignmentForm = function() {
    var tablesDiv = document.getElementById("assign");
    var teamNamesRow = tablesDiv.querySelectorAll(".teams .name > span");
    if (teamNamesRow.length != 2) {
        throw "Expecting 2 teams";
    }
    teamNamesRow[0].textContent = getData().match.row_team
    teamNamesRow[1].textContent = getData().match.column_team

    // TODO
};

var getRemainingArmies = function(isRowArmy) {
    const data = getData();
    const allArmies = isRowArmy ? data.row_armies : data.col_armies;
    const assignedList = data.tables
        ? data.tables.map(t => isRowArmy ? t.row_army : t.col_army)
        : undefined;
    return assignedList
        ? allArmies.filter(armyName => ! assignedList.find(assigned => assigned === armyName))
        : [];
};

var triggerAnalysis = function(isRowArmy, armyName) {
    const waitingLoop = document.getElementById('analysis').querySelector('.waiting');
    waitingLoop.classList.remove('hidden');

    // TODO: API call
}

var refreshArmyList = function(isRowArmy, parentDiv) {
    var filteredList = getRemainingArmies(isRowArmy);
    emptyElement(parentDiv);
    filteredList.forEach(armyName => {
        var candidateElement = document.createElement('span');
        candidateElement.classList.add('army');
        candidateElement.textContent = armyName;
        parentDiv.appendChild(candidateElement);
        candidateElement.addEventListener('click', e => {
            triggerAnalysis(isRowArmy, armyName);
        });
    });
};

var initAnalysisForm = function() {
    const section = document.getElementById('analysis');
    const armyDiv = section.querySelector('.selector > div');
    section.querySelectorAll('.teams input').forEach(radioInput => {
        if (radioInput.checked) {
            refreshArmyList(radioInput.value === "row", armyDiv); // Otherwise "column"
        }
        radioInput.addEventListener('click', e => {
            refreshArmyList(e.target.value === "row", armyDiv); // Otherwise "column"
        });
    });
};

var refreshAnalysisForm = function() {
    var formDiv = document.getElementById("analysis");
    var teamNamesRow = formDiv.querySelectorAll("fieldset.teams label");
    if (teamNamesRow.length != 2) {
        throw "Expecting 2 teams";
    }
    teamNamesRow[0].textContent = getData().match.row_team
    teamNamesRow[1].textContent = getData().match.column_team

    // TODO
};

var isBrowserSupported = function() {
    // Checking the presence of recent Javascript functions that are used
    return window.fetch && Promise.all;
};

var checkMatchSetup = function() {
    var team_size = getData().match.team_member_count;
    if (team_size < 3) {
        return "Not enough member per team";
    }
    else if (team_size > 10) {
        return "Too much member per team";
    }

    const teamNames = new Set();
    getData().row_armies.forEach(army => teamNames.add(army));
    if (teamNames.size != getData().row_armies.length) {
        return "Redundant row army name";
    }
    teamNames.clear();
    getData().col_armies.forEach(army => teamNames.add(army));
    if (teamNames.size != getData().col_armies.length) {
        return "Redundant row army name";
    }

    return null; // No result = OK
};

var initBadges = function() {
    document.querySelectorAll('header .badge').forEach(b => {
        b.classList.remove('hidden');
    });
};

// Triggered as soon as the teams changed
/*var onNewTeams = function() {
    allGetCalls([
        apiUrl + 'match',
        apiUrl + 'match/rows',
        apiUrl + 'match/cols',
        apiUrl + 'scores'
    ],
    jsonResults => {

        // Assign data
        getData().match      = jsonResults[0];
        getData().row_armies = jsonResults[1];
        getData().col_armies = jsonResults[2];
        getData().scores     = jsonResults[3];
        getData().tables     = []; // No table assigned yet
        getData().attackers  = {
                "rows"   : [],
                "columns": []
            }; // Not assigning yet
        // ...
    },
    errors => {
        errors.forEach(error => console.error("API call error: " + error));
        errorHandler(errors[0]); // We only display the first error for simplicity
    });
};*/

var teamNameInputValidation = function(thisInput, otherInput) {
    thisInput.addEventListener("change", event => {
        var validityState = event.target.validity;
        if (validityState.customError || validityState.valid /* Built-in validation check */) {
            // Custom validation
            var thisValue = event.target.value;
            var otherValue = otherInput.value;
            var areSimilar = thisValue && otherValue && thisValue.trim().toUpperCase() == otherValue.trim().toUpperCase();
            if (validityState.customError && !areSimilar) {
                event.target.setCustomValidity(""); // Reset custom error
            } else if (validityState.valid && areSimilar) {
                event.target.setCustomValidity("Must be different"); // Add custom error
            }
        }
    });
};

var initMatrixForm = function() {
    // Input validation
    var form = document.querySelector("#init > form");
    var rowTeamNameInput = form.querySelector("#team_row_name");
    var colTeamNameInput = form.querySelector("#team_col_name");
    teamNameInputValidation(rowTeamNameInput, colTeamNameInput);
    teamNameInputValidation(colTeamNameInput, rowTeamNameInput);
    // Form submission
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();
        switchSection("loading");
        postFormCall(
            getData().api_url + 'reset',
            form,
            json => {
                // Refresh the state
                getData().match      = json.match;
                getData().row_armies = json.row_armies.map(a => a.name);
                getData().col_armies = json.col_armies.map(a => a.name);
                getData().scores     = json.scores;
                getData().tables     = []; // No table assigned yet
                getData().attackers  = {
                        "rows"   : [],
                        "columns": []
                    }; // Not assignment yet

                // Initialize the DOM
                initBadges();
                initAnalysisForm();
                // Refresh the DOM
                refreshMatrix();
                refreshTables();
                refreshBadges();
                refreshAssignmentForm();
                refreshAnalysisForm();
                // Display relevant DOM elements
                var shownSection = switchSection("ready");
                setupNavBar(shownSection);
                switchNavTab(shownSection, 'matrix');
            },
            json => {
                errorHandler(json);
            });
    });
};

// Initialization
window.addEventListener("load", function() {

        // Browser support check
        if (!isBrowserSupported()) {
			switchSection("not_supported"); // Warning about non compatible browser
            return;
		}

        // Loading screen
        switchSection("loading");
        // See status.js about the wait for the API readyness

        // Getting API URL
        getApiUrl(); // Store the URL in `getData().api_url`

        watchForStatus(() => { // On start
                // Towards the init screen
                initMatrixForm();
                var shownSection = switchSection("ready");
                switchNavTab(shownSection, 'init');
            }, () => { // On stop
                switchSection("exited");
            });
        
	}, true);
