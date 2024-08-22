
// Returns indexes of armies that were not assigned to a table
var getRemainingArmies = function(isRow) {
    let   data         = getData();
    const allArmies    = integerRange(0, data.match.team_member_count);
    const assignedList = data.tables
        ? data.tables.map(t => isRow ? t.row_army : t.col_army)
        : undefined;
    return assignedList
        ? allArmies.filter(armyIndex => assignedList.find(assigned => assigned == armyIndex) == undefined)
        : [];
};

var getRemainingArmyNames = function(isRow) {
    const allArmyNames = isRow ? data.row_armies : data.col_armies;
    const remainingIndexes = getRemainingArmies(isRow);
    return remainingIndexes.map(index => allArmyNames[index]);
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