// This file contains code related to the synchronization between the web-app and the REST API

var intervalId = null; // Interval used for API presence checks

// Actions after exit request
var afterExit = () => {
    switchSection("exited");
    if (intervalId) {
        clearInterval(intervalId);
    }
};

// API call without payload
const triggerApi = (method, path, thenFn, errorFn) => {
    abstractVoidCall(
        (getData().api_url || getApiUrl()) + path,
        method, thenFn, errorFn);
};

window.addEventListener("load", function() {
    // Checking for API presence on a regular basis
    intervalId = setInterval(() => {
        triggerApi('GET', 'app/alive',
            () => { /* Nothing to do */ },
            () => { afterExit(); })
        }, 3000); // 3 seconds delay
});

// Handling of the tab/browser closing
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
            afterExit();
            console.log("Stopping the API");
        },
        () => { // Exit fails
            afterExit();
            console.warn("Failed to ask for API stop");
        });
    /*}*/
});
