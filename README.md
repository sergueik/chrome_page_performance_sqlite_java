### Info ###

The __Java Chrome Page Element Load Timing Collector__ project is based on [Szada30/Java-Selenium-LoadTime](https://github.com/Szada30/Java-Selenium-LoadTime) project and [.Net version of the Chrome Page Element Load Timing Collector](https://github.com/sergueik/chrome_page_performance_sqlite).

Selenium driver is used to launch the Chrome browser (alternatively [Chome Devkit Protocol driver](https://github.com/ChromeDevTools/awesome-chrome-devtools) may be used)
and load the target page then collects the page performance timings offered by Chrome browser:
```javascript
var performance = window.performance;
var timings = performance.timing;
return timings;
```
Alternaively the more advanced `timing.js` script from [Addy Osmani's repository](https://github.com/addyosmani/timing.js/blob/master/timing.js
) is called, and the results are deserialized into a `Map<String, Double>` either implicitly by deserializing the string value produced by `JSON.stringify()` method with [one of many available](https://stleary.github.io/JSON-java/) __JSON__ parsing libraries or by extracting the object via home-brewed parser of the value returned by Javascript API, as shown below.


See [https://github.com/addyosmani/timing.js/blob/master/timing.js](https://github.com/addyosmani/timing.js/blob/master/timing.js) for details of how the `timings.loadTime`,`timings.domReadyTime`  etc. are calculated):
```javascript
(function(window) {
    'use strict';
    window.timing = window.timing ||
        {
            getTimes: function(opt) {
                var performance = window.performance ||
                    window.webkitPerformance || window.msPerformance ||
                    window.mozPerformance;
                if (performance === undefined) {
                    return '';
                }
                var timings = performance.timing || {};
                NOTE: legacy conversion
                if (opt && opt['stringify']){
                    return JSON.stringify(timings);
                } else {
                	return timings;
                }
            },
            getNetwork: function(opt) {
                var network = performance.getEntries() || {};
                if (opt && opt['stringify']){
                	return JSON.stringify(network);
                } else {
                	return network;
                }
            }
        }
})(typeof window !== 'undefined' ? window : {});
return window.timing.getTimes();
return window.timing.getNetwork({stringify:true});
```
Running this  Javascript in the browser will return
```javascript
{
  unloadEventEnd = 1512339255280,
  responseEnd = 1512339255305,
  responseStart = 1512339255277,
  domInteractive = 1512339256932,
  domainLookupEnd = 1512339255033,
  unloadEventStart = 1512339255279,
  domComplete = 1512339263380,
  domContentLoadedEventStart = 1512339256932,
  domainLookupStart = 1512339255033,
  redirectEnd = 0,
  redirectStart = 0,
  connectEnd = 1512339255033,
  toJSON = {},
  connectStart = 1512339255033,
  loadEventStart = 1512339263380,
  navigationStart = 1512339255032,
  requestStart = 1512339255035,
  secureConnectionStart = 0,
  fetchStart = 1512339255033,
  domContentLoadedEventEnd = 1512339256934,
  domLoading = 1512339255302,
  loadEventEnd = 1512339263471
}
```
of (when `opt[stringify]` is set):
```javascript
{
  "navigationStart": 1512338852987,
  "unloadEventStart": 1512338855099,
  "unloadEventEnd": 1512338855100,
  "redirectStart": 0,
  "redirectEnd": 0,
  "fetchStart": 1512338852988,
  "domainLookupStart": 1512338852988,
  "domainLookupEnd": 1512338852988,
  "connectStart": 1512338852988,
  "connectEnd": 1512338852988,
  "secureConnectionStart": 0,
  "requestStart": 1512338852990,
  "responseStart": 1512338855090,
  "responseEnd": 1512338855130,
  "domLoading": 1512338855135,
  "domInteractive": 1512338856653,
  "domContentLoadedEventStart": 1512338856655,
  "domContentLoadedEventEnd": 1512338856658,
  "domComplete": 1512338862728,
  "loadEventStart": 1512338862728,
  "loadEventEnd": 1512338862864
}
```
The Java application subsequently dumps the results ino SQLite database using [JDBC](https://www.tutorialspoint.com/sqlite/sqlite_java.htm)

Loading the results into ElasticSearch is a Work in progress.
### See Also

https://github.com/sirensolutions/kibi/tree/master/native-bindings
https://www.elastic.co/blog/logstash-jdbc-input-plugin
https://www.elastic.co/guide/en/logstash/current/plugins-inputs-sqlite.html
https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/
https://developers.google.com/web/tools/chrome-devtools/network-performance/understanding-resource-timing
https://chrome.google.com/webstore/detail/web-performance-timing-ap/nllipdabkglnhmanndddgcihbcmjpfej
https://github.com/addyosmani/timing.js/blob/master/timing.js

#### Note

The project was earlier develped inside a repository [sergueik/selenium_java](https://github.com/sergueik/selenium_java.git) - check the old past histories there.

### License
This project is licensed under the terms of the MIT license.

### Author

[Serguei Kouzmine](kouzmine_serguei@yahoo.com)
