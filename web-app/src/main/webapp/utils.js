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

/*
 * Switch from a navigation tab to another
 * using `nav > a` anchors corresponding `div` elements.
 */
var setupNavBar = function(section) {
	document.querySelector('nav').classList.remove('hidden');
    document.querySelectorAll('nav > a').forEach(anchor => {
            var linkTarget = anchor.dataset.target;
            var targetedElement = section.querySelector('div#'+linkTarget);
            if (!targetedElement) {
                throw "No target element #" + linkTarget + " found!";
            }
			anchor.addEventListener('click', e => {
					section.querySelectorAll('div.current').forEach(s => {
						s.classList.remove('current');
					});
					document.querySelectorAll('nav > a.selected').forEach(a => {
						a.classList.remove('selected');
					});
					anchor.classList.add('selected');
                    targetedElement.classList.add('current');
                });
        });
}

function emptyElement(element) {
	while (element.firstElementChild) {
	   element.firstElementChild.remove();
	}
}

/*
 * Calls a GET request to the API
 */
var getCall = (url, thenFn, errorFn) => {
	fetch(url)
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
