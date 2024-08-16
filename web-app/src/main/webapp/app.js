
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

// Applying the result of a new pairing configuration
var onNewState = function(json) {
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
    initScoreEditForm();
    initAnalysisForm();
    initTeamNameEditForm();
    initArmyNameEditForm();
    // Refresh the DOM
    refreshMatchForm();
    refreshMatrix();
    refreshTables();
    refreshBadges();
    refreshAssignmentForm();
    refreshAnalysisForm();
    // Display relevant DOM elements
    var shownSection = switchSection("ready");
    setupNavBar(shownSection);
    switchNavTab(shownSection, 'matrix');
};

var refreshMatchForm = function() {
    var matchForm = document.querySelector("#init > form");
    var rowTeamNameInput = matchForm.querySelector("#team_row_name");
    var colTeamNameInput = matchForm.querySelector("#team_col_name");
    var teamSizeInput    = matchForm.querySelector("#team_size");

    var matchData = getData().match;
    rowTeamNameInput.value = matchData.row_team;
    colTeamNameInput.value = matchData.column_team;
    teamSizeInput.value    = matchData.team_member_count;
};

var initMatchForm = function() {
    // Team names input validation
    var form = document.querySelector("#init > form");
    var rowTeamNameInput = form.querySelector("#team_row_name");
    var colTeamNameInput = form.querySelector("#team_col_name");
    teamNameInputValidation(rowTeamNameInput, colTeamNameInput);
    teamNameInputValidation(colTeamNameInput, rowTeamNameInput);

    // Submit listener
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();
        startLoading();
        postFormCall(
            getData().api_url + 'reset',
            form,
            json => onNewState(json),
            json => errorHandler(json));
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
        startLoading();
        // See status.js about the wait for the API readyness

        // Getting API URL
        getApiUrl(); // Store the URL in `getData().api_url`

        watchForStatus(() => { // On start
                // Towards the init screen
                initMatchForm();
                var shownSection = switchSection("ready");
                switchNavTab(shownSection, 'init');
            }, () => { // On stop
                switchSection("exited");
            });
        
	}, true);
