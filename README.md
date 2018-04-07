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


This is a lot easier than processing the full SeleniumDriver log: 
```java
ChromeOptions options = new ChromeOptions();
LoggingPreferences loggingPreferences = new LoggingPreferences();
loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
capabilities.setCapability(CapabilityType.LOGGING_PREFS,
    loggingPreferences);
  driver = new ChromeDriver(capabilities);

LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
```
which looks like

```javascript
...
{
    "message": {
        "method": "Page.loadEventFired",
        "params": {
            "timestamp": 534304.116728
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Page.frameStoppedLoading",
        "params": {
            "frameId": "2C55735DE539FCE8BEA1A6CA827A511"
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Page.domContentEventFired",
        "params": {
            "timestamp": 534304.117791
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Page.frameStartedLoading",
        "params": {
            "frameId": "2C55735DE539FCE8BEA1A6CA827A511"
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Network.requestWillBeSent",
        "params": {
            "documentURL": "https://www.priceline.com/",
            "frameId": "2C55735DE539FCE8BEA1A6CA827A511",
            "initiator": {
                "type": "other"
            },
            "loaderId": "14DFFA25E9B5979541C96D5481B309AA",
            "request": {
                "headers": {
                    "Upgrade-Insecure-Requests": "1",
                    "User-Agent": "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/65.0.3325.181 Safari/537.36"
                },
                "initialPriority": "VeryHigh",
                "method": "GET",
                "mixedContentType": "none",
                "referrerPolicy": "no-referrer-when-downgrade",
                "url": "https://www.priceline.com/"
            },
            "requestId": "14DFFA25E9B5979541C96D5481B309AA",
            "timestamp": 534305.113269,
            "type": "Document",
            "wallTime": 1523124859.15939
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Network.responseReceived",
        "params": {
            "frameId": "2C55735DE539FCE8BEA1A6CA827A511",
            "loaderId": "14DFFA25E9B5979541C96D5481B309AA",
            "requestId": "14DFFA25E9B5979541C96D5481B309AA",
            "response": {
                "connectionId": 9,
                "connectionReused": false,
                "encodedDataLength": 711,
                "fromDiskCache": false,
                "fromServiceWorker": false,
                "headers": {
                    "accept-ranges": "bytes\nbytes",
                    "content-encoding": "gzip",
                    "content-type": "text/html; charset=utf-8",
                    "date": "Sat, 07 Apr 2018 18:14:21 GMT",
                    "etag": "W/\"4f6f6-OEvAhjqSi26QYORsGBqJx8NiXSI\"",
                    "server": "nginx",
                    "set-cookie": "SITESERVER=ID=2201b110990e4a5e879513e0bd7a5b1b; Domain=.priceline.com; Path=/; Expires=Mon, 30 Mar 2048 18:14:20 GMT; Secure\nvid=v2018040718142064586c21; Domain=.priceline.com; Path=/; HttpOnly; Secure\nReferral=CLICKID=&WEBENTRYTIME=4%2F7%2F2018%2014%3A14%3A20&ID=DIRECT&PRODUCTID=&SOURCEID=DT; Domain=.priceline.com; Path=/; Expires=Mon, 07 May 2018 18:14:20 GMT; HttpOnly; Secure\npclnguidse=52a268c6b2019fe7ab28a88500e6e1968b93f219;Path=/;Domain=.priceline.com\npclnguidpe=52a268c6b2019fe7ab28a88500e6e1968b93f219;Path=/;Domain=.priceline.com;Expires=Tue, 04-Apr-2028 18:14:21 GMT",
                    "status": "200",
                    "vary": "Accept-Encoding",
                    "via": "1.1 varnish\n1.1 varnish",
                    "wsheader": "ws=fMIA/fIAD/ny-home001 D=0.437 NT=448",
                    "x-cache": "MISS, MISS",
                    "x-cache-hits": "0, 0",
                    "x-served-by": "cache-iad2127-IAD, cache-mia17629-MIA",
                    "x-timer": "S1523124861.619507,VS0,VE484"
                },
                "mimeType": "text/html",
                "protocol": "h2",
                "remoteIPAddress": "151.101.2.186",
                "remotePort": 443,
                "requestHeaders": {
                    ":authority": "www.priceline.com",
                    ":method": "GET",
                    ":path": "/",
                    ":scheme": "https",
                    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
                    "accept-encoding": "gzip, deflate",
                    "upgrade-insecure-requests": "1",
                    "user-agent": "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/65.0.3325.181 Safari/537.36"
                },
                "securityDetails": {
                    "certificateId": 0,
                    "cipher": "AES_128_GCM",
                    "issuer": "GlobalSign CloudSSL CA - SHA256 - G3",
                    "keyExchange": "ECDHE_RSA",
                    "keyExchangeGroup": "X25519",
                    "protocol": "TLS 1.2",
                    "sanList": ["priceline.map.fastly.net", "*.pclncdn.com", "*.priceline.com", "mobileimg.priceline.com", "pclncdn.com", "tickets.priceline.com", "travel.priceline.com", "www.priceline.com"],
                    "signedCertificateTimestampList": [{
                        "hashAlgorithm": "SHA-256",
                        "logDescription": "Symantec log",
                        "logId": "DDEB1D2B7A0D4FA6208B81AD8168707E2E8E9D01D55C888D3D11C4CDB6ECBECC",
                        "origin": "Embedded in certificate",
                        "signatureAlgorithm": "ECDSA",
                        "signatureData": "3046022100EF2C99BC7629C684FDF08D939F2AD190D767DA05308DD170FC5F9543BDD7A28D0221009DC0CAC7EF21919E7D3F699961E51D4312E8022FAB904959FF8BE973EFC6F578",
                        "status": "Verified",
                        "timestamp": 1516741627217.0
                    }, {
                        "hashAlgorithm": "SHA-256",
                        "logDescription": "Google 'Skydiver' log",
                        "logId": "BBD9DFBC1F8A71B593942397AA927B473857950AAB52E81A909664368E1ED185",
                        "origin": "Embedded in certificate",
                        "signatureAlgorithm": "ECDSA",
                        "signatureData": "304402205B7043E321863ABDC7CDCA3828E36E3CE7D25661E3E508B3A67C499A6F32A6FA022037E03E4F8A57A3E71E8F5ABB0043EDD8502AAF67845B7BB4054BD7526CC99C18",
                        "status": "Verified",
                        "timestamp": 1516741627363.0
                    }, {
                        "hashAlgorithm": "SHA-256",
                        "logDescription": "DigiCert Log Server",
                        "logId": "5614069A2FD7C2ECD3F5E1BD44B23EC74676B9BC99115CC0EF949855D689D0DD",
                        "origin": "Embedded in certificate",
                        "signatureAlgorithm": "ECDSA",
                        "signatureData": "3046022100D5B55C035E35C5DC9EC5BBD954F5C985799F31861D0E20E837412BCAE186BB1C0221008393F5E08D549877235006A5F276A9CD326AEE37EA67937E6162B2560CFE8B94",
                        "status": "Verified",
                        "timestamp": 1516741627307.0
                    }, {
                        "hashAlgorithm": "SHA-256",
                        "logDescription": "Google 'Rocketeer' log",
                        "logId": "EE4BBDB775CE60BAE142691FABE19E66A30F7E5FB072D88300C47B897AA8FDCB",
                        "origin": "Embedded in certificate",
                        "signatureAlgorithm": "ECDSA",
                        "signatureData": "304402203B3F86F6E547BF5F6E14E218A7EFBD09DDA0F617BBA2B3CE0A8B1F1198236083022048F02E2079AEF019969E54D47E954E09F960A655AC5FA967C5805288C71374AD",
                        "status": "Verified",
                        "timestamp": 1516741627937.0
                    }],
                    "subjectName": "priceline.map.fastly.net",
                    "validFrom": 1516741624,
                    "validTo": 1536238759
                },
                "securityState": "secure",
                "status": 200,
                "statusText": "",
                "timing": {
                    "connectEnd": 307.547000003979,
                    "connectStart": 138.16199998837,
                    "dnsEnd": 138.16199998837,
                    "dnsStart": 129.266000003554,
                    "proxyEnd": 126.842000056058,
                    "proxyStart": 114.427000051364,
                    "pushEnd": 0,
                    "pushStart": 0,
                    "receiveHeadersEnd": 812.544999993406,
                    "requestTime": 534304.292439,
                    "sendEnd": 309.232000028715,
                    "sendStart": 308.51000000257,
                    "sslEnd": 307.429000036791,
                    "sslStart": 176.882000057958,
                    "workerReady": -1,
                    "workerStart": -1
                },
                "url": "https://www.priceline.com/"
            },
            "timestamp": 534305.117249,
            "type": "Document"
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Network.dataReceived",
        "params": {
            "dataLength": 16862,
            "encodedDataLength": 0,
            "requestId": "14DFFA25E9B5979541C96D5481B309AA",
            "timestamp": 534305.117938
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Page.frameNavigated",
        "params": {
            "frame": {
                "id": "2C55735DE539FCE8BEA1A6CA827A511",
                "loaderId": "14DFFA25E9B5979541C96D5481B309AA",
                "mimeType": "text/html",
                "securityOrigin": "https://www.priceline.com",
                "url": "https://www.priceline.com/"
            }
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Network.dataReceived",
        "params": {
            "dataLength": 48674,
            "encodedDataLength": 0,
            "requestId": "14DFFA25E9B5979541C96D5481B309AA",
            "timestamp": 534305.151364
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
}, {
    "message": {
        "method": "Network.dataReceived",
        "params": {
            "dataLength": 65536,
            "encodedDataLength": 0,
            "requestId": "14DFFA25E9B5979541C96D5481B309AA",
            "timestamp": 534305.176181
        }
    },
    "webview": "2C55735DE539FCE8BEA1A6CA827A511"
},
...

```

Loading results into ELK is a work in progress.

### See Also

 * https://github.com/sirensolutions/kibi/tree/master/native-bindings
 * https://www.elastic.co/blog/logstash-jdbc-input-plugin
 * https://www.elastic.co/guide/en/logstash/current/plugins-inputs-sqlite.html
 * https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/
 * https://developers.google.com/web/tools/chrome-devtools/network-performance/understanding-resource-timing
 * https://chrome.google.com/webstore/detail/web-performance-timing-ap/nllipdabkglnhmanndddgcihbcmjpfej
 * https://github.com/addyosmani/timing.js/blob/master/timing.js
 * https://github.com/ChrisLMerrill/muse-webperformance-graphite
 * https://stackoverflow.com/questions/32219113/how-to-extract-network-tab-contents-of-chrome-developer-tools-via-json/37133568#37133568

#### Note

The project was earlier develped inside a repository [sergueik/selenium_java](https://github.com/sergueik/selenium_java.git) - check the old past histories there.

### License
This project is licensed under the terms of the MIT license.

### Author

[Serguei Kouzmine](kouzmine_serguei@yahoo.com)
