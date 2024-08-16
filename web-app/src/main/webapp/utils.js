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
    switchSection(section.id);
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

// Show the loading section
const startLoading = function() {
    document.querySelector('nav').classList.add('hidden');
    switchSection("loading");
};
// Comes back to the given tab, after loading
const endLoading = function(tabId) {
    var section = switchSection("ready");
    switchNavTab(section, tabId);
    document.querySelector('nav').classList.remove('hidden');
};

const _boolToStr = (bool) => bool ? 'true' : 'false';
const _strToBool = (str)  => str === 'true';

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
 * Calls a POST request expecting JSON as request and in response
 */
var postJsonCall = (url, jsonRequest, thenFn, errorFn) => {
    abstractJsonCall(
        new Request(url, {
            method: "POST",
            body: JSON.stringify(jsonRequest),
            headers: {
                "Content-Type": "application/json",
            }}),
        thenFn, errorFn);
};

/*
 * Calls a POST request expecting JSON in response with a form as an input
 * Inputs are sent using either "Multipart Form Data" or "URL Encoding".
 */
var postFormCall = (url, formElement, thenFn, errorFn, multipart = true) => {
    var submitInput = formElement.querySelector("input[type = 'submit']");
    if (!submitInput) {
        console.error("No submit input found in " + formElement);
        return;
    }
    var encoding = multipart ? "multipart/form-data" : "application/x-www-form-urlencoded";
    formElement.enctype = encoding;
    submitInput.formenctype =  encoding;
    var formData = new FormData(formElement);
    var body = multipart ? formData : new URLSearchParams(formData);
    abstractJsonCall(
        new Request(url, {
            method: "POST",
            body: body,
            //headers: { "Content-Type": "..." } // DO NOT PROVIDE THE CONTENT-TYPE
        }),
        thenFn, errorFn);
};

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

