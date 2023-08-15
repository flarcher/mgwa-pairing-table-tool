
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
 * Calls a GET request to the API
 */
var callGet = (url, thenFn, errorFn) => {
	fetch(url)
		.then((response) => {
			if (!response.ok) {
				console.error('GET call failed with status ' + response.status);
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
