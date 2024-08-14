// This file contains code related to the synchronization between the web-app and the REST API

var intervalId = null; // Interval used for API presence checks

// Statuses
var started = false;
var exited = false;

// API call without payload
const triggerApi = (method, path, thenFn, errorFn) => {
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
        console.error("API call error");
        errorFn();
    });
};

const watchForStatus = function(onStart, onExit) {
    // onExit wrapper
    const exiting = () => {
        if (!exited) {
            exited = true;
            if (intervalId) {
                clearInterval(intervalId);
            }
            onExit();
        }
    };

    // Preparing exit path with handling of the tab/browser closing
    window.addEventListener('beforeunload', function (e) {
        e.preventDefault(); // Will prompt the confirmation window
        e.returnValue = false; // Legacy browser support

        /*
        var confirmed = confirm("Are you sure to stop the application?");
        if (confirmed) {
        */
        // Request to stop the API server
        triggerApi('POST', 'app/exit',
            {}, // No request body
            () => { // Exit ok
                exiting();
                console.log("Stopped the API");
            },
            () => { // Exit fails
                exiting();
                console.warn("Failed to ask for API stop");
            });
        /*}*/
    });

    // Checking for API presence on a regular basis
    intervalId = setInterval(() => {
        triggerApi('GET', 'app/alive',
            (response) => {
                var status = response.headers.get("X-Status");
                if (!status) {
                    console.error("No status header found in response");
                } else {
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
                            exiting();
                            break;
                    }
                }
            },
            (response) => { exiting(); })
        }, 5000); // Delay in milliseconds
};

