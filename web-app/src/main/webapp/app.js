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

var resetBadges = function () {
    document.querySelectorAll('header .badge').forEach(b => {
        b.classList.remove('hidden');
    });
};

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
    const _readArmyName = (item, index) => {
        var value;
        if (typeof item === 'string') {
            value = item;
        }
        else if (item.hasOwnProperty('name')) {
            value = item.name;
        } else {
            return index.toString();
        }
        return value || index.toString();
    };
    getData().row_armies = json.row_armies.map(_readArmyName);
    getData().col_armies = json.col_armies.map(_readArmyName);
    getData().scores = json.scores;
    const tableCount = json.match.team_member_count;
    var newTables = json.hasOwnProperty('tables') ? json.tables : [];
    if (newTables.length > 0) {
        newTables = fixTablesList(newTables, tableCount);
    } else {
        newTables = newTablesList(tableCount); // No table assigned yet
    }
    getData().tables = newTables;

    // Refresh the DOM
    updateTableAssignment();
    refreshTeamChoiceNames();
    refreshMatchupForms(true);
    updateMatchupForms();
    resetBadges();
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
    var fileRemove = form.querySelector("#unselect_file");

    // Handle the states "reset with default scores" or "uploads a file"
    const resetInputs = [ rowTeamNameInput, colTeamNameInput, scoreMinInput, scoreMaxInput, teamSizeInput ];
    const _switchHidden = switchHidden;
    const _switchDisabled = switchDisabled;
    const switchFormPart = (isFromFile) => {
        _switchHidden(fileRemove, !isFromFile);
        resetInputs.forEach((input) => { _switchDisabled(input, isFromFile); });
    };
    fileInput.addEventListener("change", (event) => {
        const selectedFiles = event.target.files;
        if (selectedFiles.length > 0) {
            switchFormPart(true);
            _switchHidden(fileRemove, false);
        }
    }, false);
    fileRemove.addEventListener("click", (event) => {
        switchFormPart(false);
        _switchHidden(event.target, true);
        fileInput.value = null;
    });

    // Submit listener
    const _isFileJson = isFileJson;
    form.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();

        const defaultScore = getScoreFromInputs(scoreMinInput, scoreMaxInput);
        getData().default_score = defaultScore;

        const selectedFiles = event.target.querySelector('input[type="file"]').files;
        if (selectedFiles.length <= 0) {
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
        } else {
            const file = selectedFiles[0];
            const isFileJson = _isFileJson(file);
            if (isFileJson) {
                // Reading of the local file in offline mode
                startLoading();
                const reader = new FileReader();
                reader.onload = (e) => {
                    const newData = JSON.parse(e.target.result);
                    if (!newData) {
                        errorHandler({ cause: "Parsing", message: "Unable to parse JSON file"})
                    } else {
                        onNewState(newData);
                    }
                };
                reader.onerror = (e) => {
                    errorHandler({ cause: "Read", message: "Failed to read JSON file"})
                };
                reader.readAsText(file);
            } else if ((! getData().offline) && !isFileJson) {
                // In order to support Excel file reading
                startLoading();
                postFormCall(
                    getData().api_url + 'reset',
                    form,
                    json => onNewState(json),
                    json => errorHandler(json));
            } else if (getData().offline && !isFileJson) {
                alert("Only JSON files are supported in offline mode");
                return;
            } else {
                alert("Unexpected error");
                console.error("Unsupported");
                return;
            }
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
        // Define event listeners
        initScoreEditForm();
        initTeamNameEditForm();
        initArmyNameEditForm();
        initTableAssignmentForm();
        initMatchupForms();
        initMatchForm();
        // Towards the init screen
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
