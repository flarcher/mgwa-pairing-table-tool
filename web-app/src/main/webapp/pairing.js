
// Returns indexes of armies that were not assigned to a table
var getRemainingArmies = function (isRow) {
    let data = getData();
    const allArmies = integerRange(0, data.match.team_member_count);
    const assignedList = data.tables
        ? data.tables.map(t => isRow ? t.row_army : t.col_army)
        : undefined;
    return assignedList
        ? allArmies.filter(armyIndex => assignedList.find(assigned => assigned == armyIndex) == undefined)
        : [];
};

var getRemainingArmyNames = function (isRow) {
    const allArmyNames = isRow ? data.row_armies : data.col_armies;
    const remainingIndexes = getRemainingArmies(isRow);
    return remainingIndexes.map(index => allArmyNames[index]);
};

var checkMatchSetup = function () {
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

var _getMatchupFormContainer = () => {
    return document.getElementById('assign');
};

var _getTeamChoice = () => {
    return _getMatchupFormContainer().querySelector("#team_choice");
};

var _getSubForm = (subId) => {
    return document.getElementById("assign_" + subId);
};

var _isRow = (teamChoice) => {
    let select = teamChoice || _getTeamChoice();
    switch (select.value) {
        case "row": return true;
        case "col": return false;
    }
    throw "Unexpected value"; // Should never happen
};

var refreshTeamChoiceNames = function () {
    var teamChoice = _getTeamChoice();
    var data = getData();
    emptyElement(teamChoice);
    [data.match.row_team, data.match.column_team].forEach((name, index) => {
        let option = document.createElement('option');
        option.value = index == 0 ? "row" : "col";
        option.textContent = name;
        teamChoice.appendChild(option);
    });
    return teamChoice;
};

const UNDEFINED_OPTION_VALUE = '=';
const UNDEFINED_OPTION_NAME = '- Select -';

var _defaultSelectOption = (select) => {
    let option = document.createElement('option');
    option.value = UNDEFINED_OPTION_VALUE;
    option.textContent = UNDEFINED_OPTION_NAME;
    if (select) {
        select.appendChild(option);
        select.value = UNDEFINED_OPTION_VALUE;
    }
    return option;
};

var _initSelect = (thisSelect, othersSelect) => {
    thisSelect.addEventListener("change", event => {
        const thisValue = event.target.value;
        if (thisValue != UNDEFINED_OPTION_VALUE) {
            othersSelect.forEach((other) => {
                if (other.value == thisValue) {
                    other.value = UNDEFINED_OPTION_VALUE;
                }
            });
            if (event.target.validity.customError) {
                event.target.setCustomValidity(""); // Reset custom error
            }
        } else {
            event.target.setCustomValidity("Please choose");
        }
    });
};

var _initSelects = (selects) => {
    selects.forEach((select, i, array) => {
        let others = array.filter(item => item != select);
        _initSelect(select, others);
    });
};

var _getArmyName = (isRow, index) => {
    const data = getData();
    const army_names = isRow ? data.row_armies : data.col_armies;
    return army_names[index];
};

var _initAttackerChoice = (isYours, selects, radioInputs) => {
    if (selects.length != radioInputs.length) {
        throw "Same length required";
    }
    selects.forEach((select, index) => {
        const radioInput = radioInputs[index];
        const radioDiv = radioInput.parentElement;
        const radioLabel = radioDiv.querySelector('label');
        const _isRow = isYours ? _isRow() : !_isRow();
        const updateRadioLabel = (selectValue) => {
            if (selectValue != UNDEFINED_OPTION_VALUE) {
                radioDiv.classList.remove('hidden');
                const armyIndex = parseInt(selectValue);
                radioLabel.textContent = _getArmyName(_isRow, armyIndex);
            } else {
                radioDiv.classList.add('hidden');
            }
        };
        select.addEventListener("change", event => {
            updateRadioLabel(event.target.value);
        });
        updateRadioLabel(select.value);
    });
};

var _assignMatchup = (tableIndex, rowIndex, colIndex) => {
    assignTable(tableIndex, rowIndex, colIndex); // Updates data and matrix
    refreshMatchupForms(); // Refresh the forms
};

var _isFirstAttackerSelected = (radioInputs) => {
    return radioInputs.find(input => input.checked).value == "one"; // See HTML
};

var _initMatchupSubmit = (formElement, isYourDefender, tableSelect, defenderSelect, attackersSelects, radioInputs) => {
    if (attackersSelects.length != 2) {
        throw "Must have 2 attackers select";
    }
    formElement.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();

        const _isRowDefender = isYourDefender ? _isRow() : !_isRow();
        const _isFirst = _isFirstAttackerSelected(radioInputs);
        const attackerSelect = attackersSelects[_isFirst ? 0 : 1];
        _assignMatchup(
            parseInt(tableSelect.value),
            parseInt(_isRowDefender ? defenderSelect.value : attackerSelect.value),
            parseInt(_isRowDefender ? attackerSelect.value : defenderSelect.value)
        );
    });
};

var initMatchupForms = () => {
    // Team side
    let teamChoice = refreshTeamChoiceNames();
    teamChoice.addEventListener("change", event => {
        let _isRow = _isRow(event.target);
        refreshMatchupForms(_isRow);
    });
    const initialIsRow = true;
    refreshMatchupForms(initialIsRow);

    let container = _getMatchupFormContainer();

    // Your armies
    const yourDefenderSelect = container.querySelector("#your_defender");
    const yourAttackSelect1 = container.querySelector("#your_attacker_1");
    const yourAttackSelect2 = container.querySelector("#your_attacker_2");
    _initSelects([yourDefenderSelect, yourAttackSelect1, yourAttackSelect2]);

    // Theirs armies
    const theirDefenderSelect = container.querySelector("#their_defender");
    const theirAttackSelect1 = container.querySelector("#their_attacker_1");
    const theirAttackSelect2 = container.querySelector("#their_attacker_2");
    _initSelects([theirDefenderSelect, theirAttackSelect1, theirAttackSelect2]);

    // Tables
    const yourTable = container.querySelector("#your_table");
    const theirTable = container.querySelector("#their_table");
    const rejectedTable = container.querySelector("#rejected_attackers_table");
    _initSelects([yourTable, theirTable, rejectedTable])

    // Radio buttons
    const theirAttackSelects = [theirAttackSelect1, theirAttackSelect2];
    const radioYourChoice1 = container.querySelector('#your_attacker_choice_1');
    const radioYourChoice2 = container.querySelector('#your_attacker_choice_2');
    const radioYourChoices = [radioYourChoice1, radioYourChoice2];
    _initAttackerChoice(false, theirAttackSelects, radioYourChoices)
    const yourAttackSelects = [yourAttackSelect1, yourAttackSelect2];
    const radioTheirChoice1 = container.querySelector('#your_attacker_choice_1');
    const radioTheirChoice2 = container.querySelector('#your_attacker_choice_2');
    const radioTheirChoices = [radioTheirChoice1, radioTheirChoice2];
    _initAttackerChoice(true, yourAttackSelects, radioTheirChoices)

    // Submits
    const defenseForm = _getSubForm('defense');
    _initMatchupSubmit(defenseForm, true, yourTable, theirAttackSelects, radioYourChoices);
    const attackForm = _getSubForm('attack');
    _initMatchupSubmit(attackForm, false, theirTable, yourAttackSelects, radioTheirChoices);
    const remainForm = _getSubForm('remaining');
    remainForm.addEventListener("submit", event => {
        if (!form.checkValidity()) {
            return;
        }
        event.preventDefault();

        const remainingRowArmies = getRemainingArmies(true);
        const remainingColArmies = getRemainingArmies(false);
        var remainingTables = getRemainingTables();
        if (remainingTables.length == 1) {
            // Obvious case: we take the last possibility only
            _assignMatchup(remainingTables[0].index, remainingRowArmies[0], remainingColArmies[0]);
        } else if (remainingTables.length > 1) {
            // Assign the rejected attacker
            const _yourAreRows = _isRow();
            const isTheirFirstAttacker = _isFirstAttackerSelected(radioYourChoices);
            const theirAttackerIndex = theirAttackSelects[isTheirFirstAttacker ? 1 : 0].value;
            const isYourFirstAttacker = _isFirstAttackerSelected(radioTheirChoices);
            const yourAttackerIndex = yourAttackSelects[isYourFirstAttacker ? 1 : 0].value;
            if (rejectedTable.value == UNDEFINED_OPTION_VALUE) {
                throw "Undefined table index but valid form?"
            }
            const tableIndex = parseInt(rejectedTable.value);
            const rowArmyIndex = _yourAreRows ? yourAttackerIndex : theirAttackerIndex;
            const colArmyIndex = _yourAreRows ? theirAttackerIndex : yourAttackerIndex;
            _assignMatchup(tableIndex, rowArmyIndex, colArmyIndex);
            remainingTables = remainingTables.filter(t => t.index != tableIndex);
            if (remainingTables.length == 1) {
                // Also assign the last table automatically
                let remainingRowArmyIndex = remainingRowArmies.find(a => a != rowArmyIndex);
                let remainingColArmyIndex = remainingColArmies.find(a => a != colArmyIndex);
                _assignMatchup(remainingTables[0].index, remainingRowArmyIndex, remainingColArmyIndex);
            }
        } else {
            console.error("No remaining table?");
        }
    });

    // last table label
    const lastTableLabel = remainForm.querySelector("#last_table");
    const tableSelects = [yourTable, theirTable, rejectedTable];
    tableSelects.forEach( (select) => {
        select.addEventListener("change", () => {
            const allTables = getRemainingTables().map(t => t.index);
            if (allTables.length  == 4 && tableSelects.every(select => select.value != UNDEFINED_OPTION_VALUE)) {
                const selectedTables = tableSelects.map(select => parseInt(select.value));
                const remainingTableIndex = allTables.find(t => ! selectedTables.includes(t));
                lastTableLabel.textContent = "Table " + remainingTableIndex.toString();
            } else {
                lastTableLabel.textContent = UNDEFINED_OPTION_NAME;
            }
        });
    });
};

var _refreshArmiesSelect = (select, isRow, indexes) => {
    let _isRow = isRow || _isRow();
    let remainingIndexes = indexes || getRemainingArmies(_isRow);
    let armyNames = isRow ? getData().row_armies : getData().col_armies;
    emptyElement(select);
    remainingIndexes.forEach((index) => {
        let option = document.createElement('option');
        option.value = index.toString();
        option.textContent = armyNames[index];
        select.appendChild(option);
    });
    _defaultSelectOption(select);
};

var _refreshTableSelect = (select, tables) => {
    const _tables = tables || getRemainingTables();
    emptyElement(select);
    _tables.forEach(table => {
        let option = document.createElement('option');
        option.value = table.index.toString();
        option.textContent = "Table " + table.index.toString();
        select.appendChild(option);
    });
    _defaultSelectOption(select);
};

var _switchSubForm = (container, subId, isHidden) => {
    let _element = container.querySelector("#assign_" + subId);
    if (isHidden) {
        _element.classList.add('hidden');
    } else {
        _element.classList.remove('hidden');
    }
};

var refreshMatchupForms = function (isRow) {
    const _isRow = isRow || _isRow();

    const container = _getMatchupFormContainer();

    const yourArmies = getRemainingArmies(_isRow);
    const theirArmies = getRemainingArmies(!_isRow);
    const remainingTables = getRemainingTables();
    if (yourArmies.length != theirArmies.length) {
        throw "Different army counts";
    } else if (
        (remainingTables.length != yourArmies.length) ||
        (remainingTables.length != theirArmies.length)
    ) {
        throw "Remaining table count do not match the army count";
    }

    const showPairing = remainingTables.length >= 4
    const showRemaining = remainingTables.length <= 4;

    if (showPairing) {
        _refreshArmiesSelect(container.querySelector("#your_defender"), _isRow, yourArmies);
        _refreshArmiesSelect(container.querySelector("#their_attacker_1"), !_isRow, theirArmies);
        _refreshArmiesSelect(container.querySelector("#their_attacker_2"), !_isRow, theirArmies);
        _refreshArmiesSelect(container.querySelector("#their_defender"), !_isRow, theirArmies);
        _refreshArmiesSelect(container.querySelector("#your_attacker_1"), _isRow, yourArmies);
        _refreshArmiesSelect(container.querySelector("#your_attacker_2"), _isRow, yourArmies);
        _refreshTableSelect(container.querySelector("#your_table"), remainingTables);
        _refreshTableSelect(container.querySelector("#their_table"), remainingTables);
    }
    if (showRemaining) {
        _refreshTableSelect(container.querySelector("#rejected_attackers_table"), remainingTables);
    }
    _switchSubForm(container, "defense", !showPairing);
    _switchSubForm(container, "attack", !showPairing);
    _switchSubForm(container, "remaining", !showRemaining);
};
