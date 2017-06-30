let ServiceBase = function () {
    let _ = Object.create (null);

    // Helper functions for emitting HTML from Javascript
    let valid = function (value) {
        return (typeof (value) != "undefined") && (value !== null);
    };

    let block = function (block, attributes, content) {
        let result = "<" + block;
        if (valid (attributes)) {
            let attributeNames = Object.keys (attributes);
            for (let attributeName of attributeNames) {
                if (valid (attributes[attributeName])) {
                    result += " " + attributeName + "=\"" + attributes[attributeName] + "\"";
                }
            }
        }
        return result + (valid (content) ? (">" + content + "</" + block + ">") : ("/>"));
    };

    let div = function (cssClass, content) {
        return block ("div", { "class": cssClass }, content);
    };

    let a = function (cssClass, href, content) {
        return block ("a", { "class": cssClass, "href": href, "target": "_top" }, content);
    };

    _.get = function (queryString, onSuccess) {
        let request = new XMLHttpRequest ();
        request.overrideMimeType ("application/json");
        request.open ("GET", queryString, true);
        request.onload = function (event) {
            if (request.status === 200) {
                let response = JSON.parse (this.responseText);
                onSuccess (response);
            }
        };
        request.send ();
    };

    // a little black raincloud, of course
    _.display = function (displayInDivId, inputUrl) {
        let url = (typeof (inputUrl) !== "undefined") ? inputUrl : "api?event=help";
        _.get (url, function (db) {
            // if we retrieved the api.json from the service base, get the actual response
            if (typeof (inputUrl) === "undefined") { db = db.response; }

            // start with an empty build
            let innerHTML = "";

            if ("description" in db) {
                innerHTML += block ("h2", {}, "Description") + div ("description-div", db.description);
            }

            if ("events" in db) {
                innerHTML += block ("h2", {}, "Events");
                let events = db.events;
                let eventNames = Object.keys (events);
                let eventsHTML = "";
                for (let eventName of eventNames) {
                    let event = events[eventName];
                    let eventHTML = "";

                    // if there is an example
                    if ("example" in event) {
                        let url = "api?event=" + eventName;
                        let example = event.example;
                        let exampleKeys = Object.keys (example).sort ();
                        for (let exampleKey of exampleKeys) {
                            url += "&" + exampleKey + "=" + example[exampleKey];
                        }
                        //eventHTML = a ("try-it", url, " (" + url.replace (/&/g, "&amp;") + ")");
                        eventHTML = a ("try-it", url, "[example]");
                    }
                    eventHTML = div ("event-name", eventName + eventHTML);

                    // if there is a description
                    if ("description" in event) {
                        eventHTML += div ("event-description", event.description);
                    }

                    let odd;
                    let evenOdd = function (title, object) {
                        odd = true;
                        let names = Object.keys (object);
                        if (names.length > 0) {
                            eventHTML += div ("even-odd-title", title);
                            for (let name of names) {
                                let element = object[name];
                                let required = ("required" in element) ? element.required : false;
                                eventHTML += div ("even-odd-div" + (odd ? " odd" : ""),
                                    div ("even-odd-name", name) +
                                    div ("even-odd-required", required ? "REQUIRED" : "OPTIONAL") +
                                    div ("even-odd-description", element.description));
                                odd = !odd;
                            }
                        }
                        return odd;
                    };

                    if ("parameters" in event) {
                        evenOdd ("Parameters:", event.parameters);
                    }

                    if (("strict" in event) && (event.strict == "false")) {
                        eventHTML += div ("even-odd-div" + (odd ? " odd" : ""),
                            div ("even-odd-name", "(any)") +
                            div ("even-odd-required", "OPTIONAL") +
                            div ("even-odd-description", "Event allows unspecified parameters."));
                    }

                    if ("returns" in event) {
                        let returns = event.returns;
                        // return specification might be an array, indicating this event returns an
                        // array of something
                        if (Array.isArray(returns)) {
                            // return specification might be an empty array, or an array with a single
                            // proto object
                            if (returns.length > 0) {
                                evenOdd("Returns Array of:", returns[0]);
                            } else {
                                eventHTML += div ("even-odd-title", "Returns: Array");
                            }
                        } else {
                            evenOdd("Returns:", returns);
                        }
                    }

                    eventsHTML += div ("event-div", eventHTML);
                }
                innerHTML += div("events-div", eventsHTML);
            }

            if ("name" in db) {
                document.title = db.name;
                innerHTML = block ("h1", {}, db.name) + div("container-div", innerHTML);
            }
            innerHTML += div ("content-center footer", "Built with " + a ("footer-link", "http://bag-service-base.brettonw.com", "brettonw/BagServiceBase"));

            document.getElementById(displayInDivId).innerHTML = innerHTML;
        });
    };

    _.api = function (onSuccess, baseUrl, apiSource) {
        // convert the names into camel-case names (dashes are removed and the following character is uppercased)
        let makeName = function (input) {
            return input.replace (/-([^-])/g, function replacer (match, p1, offset, string) {
                return p1.toUpperCase();
            });
        };

        // condition the inputs
        baseUrl = (typeof baseUrl !== "undefined") ? baseUrl : "";
        baseUrl = baseUrl.replace (/\/$/g, "");

        // get the api
        let url = (typeof (apiSource) !== "undefined") ? apiSource : (baseUrl + "api?event=help");
        _.get (url, function (db) {
            // if we retrieved the api.json from the service base, get the actual response
            if (typeof (apiSource) === "undefined") { db = db.response; }

            // start with an empty build
            let api = Object.create (null);

            // check that we got a response with events
            if ("events" in db) {
                let events = db.events;
                let eventNames = Object.keys (events);
                for (let eventName of eventNames) {
                    let event = events[eventName];

                    // set up the function name and an empty parameter list
                    let functionName = makeName (eventName);
                    let functionParameters = "(";
                    let functionBody = '\tlet url = "' + baseUrl + '/api?event=' + eventName + '";\n';

                    // if there are parameters, add them
                    let first = true;
                    if ("parameters" in event) {
                        let names = Object.keys (event.parameters);
                        if (names.length > 0) {
                            for (let name of names) {
                                let parameterName = makeName (name);
                                functionParameters += ((first !== true) ? ", " : "") + parameterName;
                                functionBody += '\turl += "' + name + '=" + ' + parameterName + ';\n';
                                first = false;
                            }
                        }
                    }
                    functionParameters += ((first !== true) ? ", " : "") + "onSuccess";
                    functionBody += "\tServiceBase.get (url, onSuccess);\n";
                    functionParameters += ")";

                    console.log (functionName + " " + functionParameters + ";\n");

                    let functionString = "return function " + functionParameters + " {\n" +functionBody + "};\n";
                    api[functionName] = new Function (functionString) ();
                }
            }

            // call the completion routine
            onSuccess (api);
        });
    };

    return _;
} ();
