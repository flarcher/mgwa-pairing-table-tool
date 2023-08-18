
var defaultPort = 8000;

// Error handling related to API calls
var errorHandler = function(jsonResponse) {
    var cause = jsonResponse["cause"];
    var message = jsonResponse["message"] || "?"
    if (cause === "network") {
        var port = getData().port;
        // Display error
        var section = switchSection("no_api");
        section.querySelector('#port').innerHTML = port.toFixed();
        section.querySelector('#reason').innerHTML = message
    }
    else {
        var section = switchSection("api_error");
        section.querySelector('#status').innerHTML = (jsonResponse["status"] || 0).toFixed();
        section.querySelector('#message').innerHTML = message
    }
}

// URL hash reading
var getApiUrl = function() {
    var port = defaultPort;
    var query = window.location.hash;
    if (query) {
        var begin = query.indexOf('#');
        if (begin >= 0) {
            port = parseInt(query.substring(begin + 1));
        }
    }
    getData().port = port;
    var apiUrlPrefix = 'http://localhost:' + port.toFixed() + '/api/';
    console.info('API URL is ' + apiUrlPrefix);
    return apiUrlPrefix;
}

var addTableCornerCell = function(row) {
    var cornerCell = document.createElement("td");
    cornerCell.classList.add('corner');
    row.appendChild(cornerCell);
};

var setupScoreElement = function(element, score) {
    element.classList.add('score');
    element.textContent = score.min.toFixed() + "-" + score.max.toFixed();
    var scoreClass
    if (score.min > score.max) {
        console.warn("Invalid score " + score);
    }
    if (score.min < 3 && score.max > 17) {
        scoreClass = 'game';
    } else if (score.max < 5) {
        scoreClass = 'bad';
    } else if (score.min > 15) {
        scoreClass = 'good';
    } else if (score.min < 10) {
        scoreClass = 'above';
    } else if (score.max < 10) {
        scoreClass = 'below';
    } else if (score.min > 7 && score.max < 13) {
        scoreClass = 'middle';
    }
    element.classList.add(scoreClass);
};

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

// Initialization
window.addEventListener("load", function() {

        // Browser support check
        if (!window.fetch || !Promise.all) {
			switchSection("not_supported"); // Warning about non compatible browser
            return;
		}

        // Loading screen
        switchSection("loading");

        // Getting API URL
        var apiURl = getApiUrl();

        // API calls
        allGetCalls([
                apiURl + 'match',
                apiURl + 'match/rows',
                apiURl + 'match/cols',
                apiURl + 'scores'
            ],
            jsonResults => {
                // Assign data
                getData().match      = jsonResults[0];
                getData().row_armies = jsonResults[1];
                getData().col_armies = jsonResults[2];
                getData().scores     = jsonResults[3];

                // Refresh the DOM
                refreshMatrix();

                // Display relevant DOM elements
                var section = switchSection("ready");
                setupNavBar(section);
                switchNavTab(section, 'matrix');
            },
            errors => {
                errors.forEach(error => console.error("API call error: " + error));
                errorHandler(errors[0]); // We only display the first error for simplicity
            });
        
	}, true);
