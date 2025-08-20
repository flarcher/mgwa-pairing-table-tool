//==--- Links from HTML ---==//

// Download link: JSON
const downloadJsonState = function () {
    const link = document.getElementById('export').querySelector('.download.type-json');
    //downloadJSON(getData(), link.download);
    downloadURL(getData().api_url + 'download/json', link.download);
};
// Download link: Excel
const downloadXlsxState = function () {
    const link = document.getElementById('export').querySelector('.download.type-xlsx');
    downloadURL(getData().api_url + 'download/xlsx', link.download);
};
// Download link: JSON of Web-App state
const downloadWebState = function () {
    const link = document.getElementById('export').querySelector('.download.type-js');
    downloadJSON(getData(), link.download);
}

const toExportSection = function () {
    var section = document.getElementById('ready');
    switchNavTab(section, 'export');
};
const toInitSection = function () {
    var section = document.getElementById('ready');
    switchNavTab(section, 'init');
};

//==--- Application logic ---==//

var refreshBadges = function () {
    const memberCount = getData().match.team_member_count;
    const pairCount = getPairCount(getData().tables);
    const stepCounts = getStepCounts(memberCount, pairCount);

    const stepsLabel = stepCounts[0].toFixed() + "/" + stepCounts[1].toFixed();
    const pairsLabel = pairCount.toFixed() + "/" + memberCount.toFixed();

    var header = document.querySelector('header');
    header.querySelector('#steps').textContent = stepsLabel;
    header.querySelector('#pairs').textContent = pairsLabel;
};

var isBrowserSupported = function () {
    // Checking the presence of recent Javascript functions that are used
    return window.fetch && Promise.all && window.structuredClone;
};

var initBadges = function () {
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
        getData().tables     = newTablesList(jsonResults[0].team_member_count); // No table assigned yet
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

var teamNameInputValidation = function (thisInput, otherInput) {
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
var onNewState = function (json) {
    // Refresh the state
    getData().match = json.match;
    getData().row_armies = json.row_armies.map(a => a.name);
    getData().col_armies = json.col_armies.map(a => a.name);
    getData().scores = json.scores;
    getData().tables = newTablesList(json.match.team_member_count); // No table assigned yet
    getData().attackers = {
        "rows": [],
        "columns": []
    }; // Not assignment yet

    // Initialize the DOM
    initBadges();
    initScoreEditForm();
    initTeamNameEditForm();
    initArmyNameEditForm();
    initTableAssignmentForm();
    initMatchupForms();
    // Refresh the DOM
    refreshMatchForm();
    refreshMatrix();
    refreshTables();
    refreshBadges();
    // Display relevant DOM elements
    var shownSection = switchSection("ready");
    setupNavBar(shownSection);
    switchNavTab(shownSection, 'matrix');
};

var refreshMatchForm = function () {
    var matchForm = document.querySelector("#init > form");
    var rowTeamNameInput = matchForm.querySelector("#team_row_name");
    var colTeamNameInput = matchForm.querySelector("#team_col_name");
    var teamSizeInput = matchForm.querySelector("#team_size");

    var matchData = getData().match;
    rowTeamNameInput.value = matchData.row_team;
    colTeamNameInput.value = matchData.column_team;
    teamSizeInput.value = matchData.team_member_count;
};

var initMatchForm = function () {
    // Team names input validation
    var form = document.querySelector("#init > form");
    var rowTeamNameInput = form.querySelector("#team_row_name");
    var colTeamNameInput = form.querySelector("#team_col_name");
    teamNameInputValidation(rowTeamNameInput, colTeamNameInput);
    teamNameInputValidation(colTeamNameInput, rowTeamNameInput);

    // Default score inputs
    var scoreMinInput = form.querySelector("#def_score_min");
    var scoreMaxInput = form.querySelector("#def_score_max");
    var scoreDefSpan = form.querySelector("#score_def");
    initScoreInputs(scoreMinInput, scoreMaxInput, scoreDefSpan);
    setupScoreBasedOnInputs(scoreMinInput, scoreMaxInput, scoreDefSpan);

    // Others inputs
    var teamSizeInput = form.querySelector("#team_size");
    var fileInput = form.querySelector("#matrix_file");

    // Submit listener
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();

        const defaultScore = getScoreFromInputs(scoreMinInput, scoreMaxInput);
        getData().default_score = defaultScore;
        const hasFile = fileInput.value.length > 0;
        if (getData().offline || (!hasFile)) {
            const armyCount = parseInt(teamSizeInput.value);
            const _getDefaultArmies = (isRow) => Array.from({ length: armyCount }, (x, i) => {
                return { name: (i + 1).toString(), is_row: isRow, index: i };
            });
            onNewState({
                match: {
                    row_team: rowTeamNameInput.value.trim(),
                    column_team: colTeamNameInput.value.trim(),
                    team_member_count: armyCount,
                },
                row_armies: _getDefaultArmies(true),
                col_armies: _getDefaultArmies(false),
                scores: Array.from({ length: armyCount }, () => Array.from({ length: armyCount }, () => defaultScore))
            });
        } else if (getData().offline && hasFile) {
            alert("The reading of a file is not supported in offline mode");
        } else if (! getData().offline) {
            // In order to support Excel file reading
            startLoading();
            postFormCall(
                getData().api_url + 'reset',
                form,
                json => onNewState(json),
                json => errorHandler(json));
        } else {
            console.error("Unsupported");
        }
    });
};

// Initialization
window.addEventListener("load", function () {

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

    // Checking API status
    const _start = () => {
        // Towards the init screen
        initMatchForm();
        var shownSection = switchSection("ready");
        switchNavTab(shownSection, 'init');
    };
    watchForStatus(() => { // On start
        // Towards the init screen
        _start();
    }, () => { // On stop
        switchSection("exited");
    }, () => { // When no API is available
        console.info("No API available: falling back to offline mode");
        _start();
    });

}, true);
