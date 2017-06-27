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

    // a little black raincloud, of course
    _.display = function (displayInDivId, inputUrl) {
        let request = new XMLHttpRequest ();
        let url = (typeof (inputUrl) !== "undefined") ? inputUrl : "api?event=help";
        request.open ("GET", url, true);
        request.overrideMimeType ("application/json");
        request.onload = function () {
            // parse the data
            let db = JSON.parse (this.responseText);

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
                let eventNames = Object.keys (events).sort ();
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

                    let evenOdd = function (title, object) {
                        let odd = true;
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
                        let returnTypeName = "";
                        let returns = event.returns;
                        // return specification might be an array, indicating this event returns an
                        // array of something
                        if (Array.isArray(returns)) {
                            returnTypeName = "Array";
                            // return specification might be an empty array, or an array with a single
                            // proto object
                            if (returns.length > 0) {
                                returnTypeName + " of";
                                evenOdd("Returns Array of:" + returnTypeName, returns[0]);
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
        };
        request.send ();
    };

    return _;
} ();
