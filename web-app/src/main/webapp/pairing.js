
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