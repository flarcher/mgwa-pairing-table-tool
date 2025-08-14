// This file contains code related to the synchronization between the web-app and the REST API

var intervalId = null; // Interval used for API presence checks

// Statuses
var offline = true;
var started = false;
var exited = false;

// API call without payload
const _triggerApi = (method, path, thenFn, errorFn) => {
    fetch(new Request(
        (getData().api_url || getApiUrl()) + path,
        { method: method })
    ).then((response) => {
        if (response.ok) {
            thenFn(response);
        } else {
            errorFn();
        }
    })
    .catch((error) => {
        console.warn("API call error: " + error);
        errorFn();
    });
};

const _setServerMode = (is_offline) => {
    offline = is_offline;
    getData().offline = is_offline;
}
_setServerMode(true);

const watchForStatus = function(onStart, onExit, onError) {

    // clear of interval
    const _clear = () => {
        if (!exited) {
            exited = true;
            if (intervalId) {
                clearInterval(intervalId);
            }
        }
    };

    // Preparing exit path with handling of the tab/browser closing
    window.addEventListener('beforeunload', function (e) {
        e.preventDefault(); // Will prompt the confirmation window
        e.returnValue = false; // Legacy browser support

        if (!confirm("Are you sure to stop the application?")) {
            return; // Nothing to do
        }

        _clear();

        if (offline) {
            console.info("Closing in offline mode");
            return; // Nothing to do
        } else {
            _setServerMode(true);
        }

        // Request to stop the API server
        _triggerApi('POST', 'app/exit',
            {}, // No request body
            () => { // Exit ok
                onExit();
                console.log("Stopped the API");
            },
            () => { // Exit fails
                onExit();
                console.warn("Failed to ask for API stop");
            });
    });

    // Checking for API presence on a regular basis
    intervalId = setInterval(() => {
        _triggerApi('GET', 'app/alive',
            (response) => {
                var status = response.headers.get("X-Status");
                if (!status) {
                    console.error("No status header found in response");
                } else {
                    _setServerMode(false);
                    switch (status) {
                        case "initializing":
                            console.log("Not ready yet ...")
                            break; // We should still wait
                        case "running":
                            if (!started) {
                                started = true;
                                onStart();
                            }
                            break;
                        case "exiting":
                            _clear();
                            _setServerMode(true);
                            onExit();
                            break;
                    }
                }
            },
            (error) => { 
                _clear();
                _setServerMode(true);
                if (!offline) {
                    // Here, the server was probably shut down: -> clean exit
                    onExit();
                } else {
                    // Occurs when no server is available
                    onError();
                }
            })
        }, 2500); // Delay in milliseconds
};

