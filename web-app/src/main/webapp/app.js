
var defaultPort = 8000;

// Error handling related to API calls
var errorHandler = function(jsonResponse) {
    var cause = jsonResponse["cause"];
    if (cause === "network") {
        // Display error
        var section = switchSection("no_api");
        section.querySelector('#port').innerHTML = port.toFixed();
        section.querySelector('#reason').innerHTML = jsonResponse["message"]
        // Optional retry
        var hashPort = window.pairing_api.port;
        if (hashPort != defaultPort) {
            // Automatically redirect to the default port
            setTimeout(() => {
                window.location.hash = defaultPort.toFixed();
                window.location.reload(false);
            }, 3000)
        }
    }
    else {
        // TODO
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
    var apiUrlPrefix = 'http://localhost:' + port.toFixed() + '/api/';
    console.info('API URL is ' + apiUrlPrefix);
    return apiUrlPrefix;
}

// Initialization
window.addEventListener("load", function() {

        // Browser support check
        if (!window.fetch) {
			switchSection("not_supported"); // Warning about non compatible browser
            return;
		}

        // Loading screen
        switchSection("loading");

        // Getting API port
        var apiURl = getApiUrl();

        // First API call
        callGet(apiURl + 'match', json => {
			switchSection("ready");
		}, errorHandler);
        
	}, true);
