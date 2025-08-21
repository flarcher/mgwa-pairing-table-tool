
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

// Updates an army name
var refreshAssignmentArmyName = (isRow, index, newName) => {
    var youAreRows = _isRow();
    var isYou = (youAreRows && isRow) || (!youAreRows && !isRow);
    const indexAsStr = index.toString();
    const _getLabel = (radio) => radio.parentElement.querySelector('label');
    const _updateSelect = (select) => {
        Array.from(select.querySelectorAll('option'))
            .filter(option => option.value == indexAsStr)
            .forEach(option => {
                option.textContent = newName;
            });
    };
    const _updateChoice = (radioInput, selectInput) => {
        if (selectInput.value == indexAsStr) {
            _updateAttackerChoiceLabel(_getLabel(radioInput), selectInput, isRow);
        }
    };
    if (isYou) {
        const yourDefenderSelect = container.querySelector("#your_defender");
        const yourAttackSelect1 = container.querySelector("#your_attacker_1");
        const yourAttackSelect2 = container.querySelector("#your_attacker_2");
        [yourDefenderSelect, yourAttackSelect1, yourAttackSelect2].forEach(_updateSelect);
        const radioTheirChoice1 = container.querySelector('#their_attacker_choice_1');
        const radioTheirChoice2 = container.querySelector('#their_attacker_choice_2');
        _updateChoice(radioTheirChoice1, yourAttackSelect1, isRow);
        _updateChoice(radioTheirChoice2, yourAttackSelect2, isRow);
    } else {
        const theirDefenderSelect = container.querySelector("#their_defender");
        const theirAttackSelect1 = container.querySelector("#their_attacker_1");
        const theirAttackSelect2 = container.querySelector("#their_attacker_2");
        [theirDefenderSelect, theirAttackSelect1, theirAttackSelect2].forEach(_updateSelect);
        const radioYourChoice1 = container.querySelector('#your_attacker_choice_1');
        const radioYourChoice2 = container.querySelector('#your_attacker_choice_2');
        _updateChoice(radioYourChoice1, theirAttackSelect1, isRow);
        _updateChoice(radioYourChoice2, theirAttackSelect2, isRow);
    }
};

const UNDEFINED_OPTION_VALUE = '=';
const UNDEFINED_OPTION_NAME = '- Select -';
const UNDEFINED_VALIDITY_MESSAGE = "Please choose";

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
            event.target.setCustomValidity(UNDEFINED_VALIDITY_MESSAGE);
        }
    });
    if (thisSelect.value == UNDEFINED_OPTION_VALUE) {
        thisSelect.setCustomValidity(UNDEFINED_VALIDITY_MESSAGE);
    }
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

var _updateAttackerChoiceLabel = (radioLabel, armyIndexValue, isRow) => {
    if (armyIndexValue != UNDEFINED_OPTION_VALUE) {
        radioLabel.textContent = _getArmyName(isRow, parseInt(armyIndexValue));
    } else {
        radioLabel.textContent = UNDEFINED_OPTION_NAME;
    }
};

var _initAttackerChoice = (isYours, selects, radioInputs, hintElement) => {
    if (selects.length != radioInputs.length) {
        throw "Same length required";
    }
    const scope = this;
    selects.forEach((select, index, all) => {
        const radioInput = radioInputs[index];
        const radioDiv = radioInput.parentElement;
        const radioLabel = radioDiv.querySelector('label');
        const updateRadioLabel = (selectValue) => {
            if (selectValue != UNDEFINED_OPTION_VALUE) {
                if (radioInputs.filter((e, i) => i != index).every(e => !e.checked)) {
                    radioInput.checked = true;
                }
            }
            var yourAreRows = scope._isRow();
            var _isRow = isYours ? yourAreRows : !yourAreRows;
            _updateAttackerChoiceLabel(radioLabel, selectValue, _isRow);
            switchHidden(radioDiv, selectValue == UNDEFINED_OPTION_VALUE);
            switchHidden(hintElement, !all.every(s => s.value == UNDEFINED_OPTION_VALUE));
        };
        select.addEventListener("change", event => {
            updateRadioLabel(event.target.value);
        });
        updateRadioLabel(select.value);
    });
};

var _emptyAttackerChoice = (radioInput) => {
    const fieldset = radioInput.parentElement.parentElement;
    Array.from(fieldset.querySelectorAll('label'))
        .forEach(label => {
            label.textContent = UNDEFINED_OPTION_NAME;
            switchHidden(label.parentElement, true);
            label.parentElement.querySelector('input').checked = false;
        });
    switchHidden(fieldset.querySelector("div.hint"), false);
};

var _assignMatchup = (tableIndex, rowIndex, colIndex) => {
    assignTable(tableIndex, rowIndex, colIndex); // Updates data and matrix
    refreshMatchupForms(); // Refresh the forms
    alert("Table " + tableIndex + " has been assigned to " + _getArmyName(true, rowIndex) + " and " + _getArmyName(false, colIndex));
};

var _isFirstAttackerSelected = (radioInputs) => {
    return radioInputs.find(input => input.checked).value == "one"; // See HTML
};

var _initMatchupSubmit = (formElement, isYourDefender, tableSelect, defenderSelect, attackersSelects, radioInputs) => {
    if (attackersSelects.length != 2) {
        throw "Must have 2 attackers select";
    }
    const scope = this;
    addSubmitListener(formElement, () => {
        const _yourAreRows = scope._isRow();
        const _isRowDefender = isYourDefender ? _yourAreRows : !_yourAreRows;
        const _isFirst = scope._isFirstAttackerSelected(radioInputs);
        const attackerSelect = attackersSelects[_isFirst ? 0 : 1];
        if ([attackerSelect, defenderSelect, tableSelect].filter(select => {
            if (select.value == UNDEFINED_OPTION_VALUE) {
                select.setCustomValidity(UNDEFINED_VALIDITY_MESSAGE);
                return true;
            } else { return false; }
        }).length > 0) {
            return; // Invalid form
        };
        scope._assignMatchup(
            parseInt(tableSelect.value),
            parseInt(_isRowDefender ? defenderSelect.value : attackerSelect.value),
            parseInt(_isRowDefender ? attackerSelect.value : defenderSelect.value)
        );
    });
};

var initMatchupForms = () => {
    // Team side
    let teamChoice = refreshTeamChoiceNames();
    const scope = this;
    teamChoice.addEventListener("change", event => {
        let _isRow = scope._isRow(event.target);
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
    const defenseForm = _getSubForm('defense');
    const defenseRadioHintElement = defenseForm.querySelector("div.hint");
    _initAttackerChoice(false, theirAttackSelects, radioYourChoices, defenseRadioHintElement);
    const yourAttackSelects = [yourAttackSelect1, yourAttackSelect2];
    const radioTheirChoice1 = container.querySelector('#their_attacker_choice_1');
    const radioTheirChoice2 = container.querySelector('#their_attacker_choice_2');
    const radioTheirChoices = [radioTheirChoice1, radioTheirChoice2];
    const attackForm = _getSubForm('attack');
    const attackRadioHintElement = attackForm.querySelector("div.hint");
    _initAttackerChoice(true, yourAttackSelects, radioTheirChoices, attackRadioHintElement);

    // Submits
    _initMatchupSubmit(defenseForm, true, yourTable, yourDefenderSelect, theirAttackSelects, radioYourChoices);
    _initMatchupSubmit(attackForm, false, theirTable, theirDefenderSelect, yourAttackSelects, radioTheirChoices);
    const remainForm = _getSubForm('remaining');
    addSubmitListener(remainForm, () => {
        const remainingRowArmies = getRemainingArmies(true);
        const remainingColArmies = getRemainingArmies(false);
        var remainingTables = scope.getRemainingTables();
        if (remainingTables.length == 1) {
            // Obvious case: we take the last possibility only
            scope._assignMatchup(remainingTables[0].index, remainingRowArmies[0], remainingColArmies[0]);
        } else if (remainingTables.length > 1) {
            // Assign the rejected attacker
            const _yourAreRows = scope._isRow();
            const isTheirFirstAttacker = scope._isFirstAttackerSelected(radioYourChoices);
            const theirAttackerSelect = theirAttackSelects[isTheirFirstAttacker ? 1 : 0];
            const isYourFirstAttacker = scope._isFirstAttackerSelected(radioTheirChoices);
            const yourAttackerSelect = yourAttackSelects[isYourFirstAttacker ? 1 : 0];
            if ([rejectedTable, theirAttackerSelect, yourAttackerSelect].filter(select => {
                if (select.value == UNDEFINED_OPTION_VALUE) {
                    select.setCustomValidity(UNDEFINED_VALIDITY_MESSAGE);
                    return true;
                } else { return false; }
            }).length > 0) {
                return; // Invalid form
            };
            const tableIndex = parseInt(rejectedTable.value);
            const rowArmyIndex = parseInt(_yourAreRows ? yourAttackerSelect.value : theirAttackerSelect.value);
            const colArmyIndex = parseInt(_yourAreRows ? theirAttackerSelect.value : yourAttackerSelect.value);
            scope._assignMatchup(tableIndex, rowArmyIndex, colArmyIndex);
            remainingTables = remainingTables.filter(t => t.index != tableIndex);
            if (remainingTables.length == 1) {
                // Also assign the last table automatically
                let remainingRowArmyIndex = remainingRowArmies.find(a => a != rowArmyIndex);
                let remainingColArmyIndex = remainingColArmies.find(a => a != colArmyIndex);
                scope._assignMatchup(remainingTables[0].index, remainingRowArmyIndex, remainingColArmyIndex);
            }
        } else {
            console.error("No remaining table?");
        }
    });

    // last table label
    const lastTableLabel = remainForm.querySelector("#last_table");
    const tableSelects = [yourTable, theirTable, rejectedTable];
    tableSelects.forEach((select) => {
        select.addEventListener("change", () => {
            const allTables = getRemainingTables().map(t => t.index);
            if (allTables.length == 4 && tableSelects.every(select => select.value != UNDEFINED_OPTION_VALUE)) {
                const selectedTables = tableSelects.map(select => parseInt(select.value));
                const remainingTableIndex = allTables.find(t => !selectedTables.includes(t));
                lastTableLabel.textContent = "Table " + remainingTableIndex.toString();
            } else {
                lastTableLabel.textContent = UNDEFINED_OPTION_NAME;
            }
        });
    });
};

var _refreshArmiesSelect = (select, isRow, indexes) => {
    let remainingIndexes = indexes || getRemainingArmies(isRow);
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
    switchHidden(container.querySelector("#assign_" + subId), isHidden);
};

var refreshMatchupForms = function (isRow) {
    var _isRowInternal = isRow == undefined ? _isRow() : isRow;

    const container = _getMatchupFormContainer();

    const yourArmies = getRemainingArmies(_isRowInternal);
    const theirArmies = getRemainingArmies(!_isRowInternal);
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
        _refreshArmiesSelect(container.querySelector("#your_defender"), _isRowInternal, yourArmies);
        _refreshArmiesSelect(container.querySelector("#their_attacker_1"), !_isRowInternal, theirArmies);
        _refreshArmiesSelect(container.querySelector("#their_attacker_2"), !_isRowInternal, theirArmies);
        _refreshArmiesSelect(container.querySelector("#their_defender"), !_isRowInternal, theirArmies);
        _refreshArmiesSelect(container.querySelector("#your_attacker_1"), _isRowInternal, yourArmies);
        _refreshArmiesSelect(container.querySelector("#your_attacker_2"), _isRowInternal, yourArmies);
        _refreshTableSelect(container.querySelector("#your_table"), remainingTables);
        _refreshTableSelect(container.querySelector("#their_table"), remainingTables);
        _emptyAttackerChoice(container.querySelector("#your_attacker_choice_1"));
        _emptyAttackerChoice(container.querySelector("#their_attacker_choice_1"));
    }
    if (showRemaining) {
        _refreshTableSelect(container.querySelector("#rejected_attackers_table"), remainingTables);
    }
    _switchSubForm(container, "defense", !showPairing);
    _switchSubForm(container, "attack", !showPairing);
    _switchSubForm(container, "remaining", !showRemaining);
};
