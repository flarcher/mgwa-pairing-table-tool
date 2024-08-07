// Variable that contains the application data
window.app_data = {};
var getData = function() {
    return window.app_data;
}

/*
 * Switch from a step to another
 * using <section> elements.
 */
var switchSection = function(id) {
	console.info('To step ' + id);
	document.querySelectorAll('section.current')
		.forEach(function(s) { s.classList.remove('current'); });
	var section = document.getElementById(id);
	section.classList.add('current');
	return section;
};

var _switchNavTab = function(section, tabId, sourceAnchors) {
	var targetedElement = section.querySelector('div#'+tabId);
	if (!targetedElement) {
		throw "No target element #" + tabId + " found!";
	}
	section.querySelectorAll('div.current').forEach(s => {
		s.classList.remove('current');
	});
	document.querySelectorAll('nav > a.selected').forEach(a => {
		a.classList.remove('selected');
	});
	sourceAnchors.forEach(a => a.classList.add('selected'))
	targetedElement.classList.add('current');
};

// Forces navigation using the tab ID
var switchNavTab = function(section, tabId) {
	var sourceAnchors = Array.from(document.querySelectorAll('nav > a'))
		.filter(a => { return a.dataset.target === tabId });
	_switchNavTab(section, tabId, sourceAnchors);
};

/*
 * Switch from a navigation tab to another
 * using `nav > a` anchors corresponding `div` elements.
 */
var setupNavBar = function(section) {
	document.querySelector('nav').classList.remove('hidden');
    document.querySelectorAll('nav > a').forEach(anchor => {
		anchor.addEventListener('click', e => {
			_switchNavTab(section, anchor.dataset.target, [ anchor ]);
		});
	});
};

function emptyElement(element) {
	while (element.firstElementChild) {
	   element.firstElementChild.remove();
	}
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
        element.classList.add(scoreClass);
    }
};

var getStepCounts = function(team_member_count, paired_table_count) {
	if (team_member_count < 3) {
		throw "Minimum 3 team members";
	}
	const minus = team_member_count % 2 == 0 ? 2 : 1; // Remainder count for the last step
	const totalCount = (team_member_count - minus) / 2; // 2 tables pairs at each step
	var stepCount;
	if (paired_table_count >= (team_member_count - minus - 2)) {
		stepCount = totalCount;
	}
	else {
		stepCount = paired_table_count / 2;
	}
	return [ stepCount, totalCount ];
};

// API call without payload
const abstractVoidCall = (url, method, thenFn, errorFn) => {
    fetch(new Request(url, { method: method }))
    .then((response) => {
        if (response.ok) {
            thenFn();
        } else {
            errorFn();
        }
    })
    .catch((error) => {
        errorFn();
    });
};

/*
 * Utility function related to our error handling conventions
 * Works only with JSON payloads in responses
 */
var abstractJsonCall = (fetchArg, thenFn, errorFn) => {
    fetch(fetchArg)
        .then((response) => {
            if (!response.ok) {
                response.json().then(errorFn);
            }
            else {
                response.json().then(thenFn);
            }
        })
        .catch((error) => {
            errorFn({ "cause": "network", "message": error.message });
        });
};

/*
 * Calls a GET request to the API
 */
var getCall = (url, thenFn, errorFn) => {
	abstractJsonCall(url, thenFn, errorFn);
};

/* Calls a POST request to the API */
var postCall = (url, body, thenFn, errorFn) => {
    abstractJsonCall(new Request(url, {method: "POST", body: JSON.stringify(body)}), thenFn, errorFn);
}

/*
 * Multiple calls to the API
 */
var allGetCalls = (manyUrls, thenFn, errorFn) => {
	Promise.all(manyUrls.map(url => fetch(url)))
		.then((responses) => {
			var inErrorResponses = responses.filter((rs) => !rs.ok);
			var hasAnyError = inErrorResponses.length > 0;
			Promise.all(
				(hasAnyError ? inErrorResponses : responses)
					.map(rs => rs.json()))
				.then(hasAnyError ? errorFn : thenFn);
		})
		.catch((error) => {
			// First error (fail fast)
			errorFn([ { "cause": "network", "message": error.message } ]);
		});
};

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
const getApiUrl = function(){
    var port = 8000; // Default value
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
    getData().api_url = apiUrlPrefix
    return apiUrlPrefix
};
