/*
 * Rerun the current test support  
 */

// The appendText method encodes '&' as '&amp;' so need to decode it in Javascript before navigate to URL
function decodeEntities(encodedString) {
	var textArea = document.createElement('textarea');
	textArea.innerHTML = encodedString;
	return textArea.value;
}

function runtest(jobUrl, rerunUrl) {
	console.log('Running test: ' + decodeEntities(rerunUrl));
	var xmlHttp;

	// Start test
	if (window.XMLHttpRequest) {
		xmlHttp=new XMLHttpRequest();
	} else {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
	}

	xmlHttp.onreadystatechange = function() {
		if (xmlHttp.readyState == 4) {
			if (parseInt(xmlHttp.status / 100) != 2) {
				alert('Unable to start Jenkins job ' + decodeEntities(rerunUrl));
				return;
			}
		}
	}
	
	try {
		xmlHttp.open('GET', decodeEntities(rerunUrl), true);
		xmlHttp.send(null);
	} catch (e) {
		alert('Error sending HTTP GET request: ' + e.message);
		return false;
	}
	
	// View test - can't put in onreadystatechange as then browser prevents popups :-(
	window.open(decodeEntities(jobUrl), '_blank');
}
