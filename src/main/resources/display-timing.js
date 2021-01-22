// origin: https://github.com/sunnylost/navigation-timing
(function() {
  var rtmpl = /\$\{([^}]+)\}/g;

  var tmpl = {
    base: '<ol>${content}</ol>',

    item: '<li>\
            <span class="title">${name}</span>\
            <div class="bar"><i style="width:${width}%;left:${left}%;background-color:#${color};">${value}ms</i></div>\
          </li>'
  };

  var phases = [
    {
      name: 'Redirect',
      start: 'redirectStart',
      end: 'redirectEnd',
      index: 0
    }, {
      name: 'App cache',
      start: 'fetchStart',
      end: 'domainLookupStart',
      index: 1
    }, {
      name: 'DNS',
      start: 'domainLookupStart',
      end: 'domainLookupEnd',
      index: 2
    }, {
      name: 'TCP',
      start: 'connectStart',
      end: 'connectEnd',
      index: 3
    }, {
      name: 'Request',
      start: 'requestStart',
      end: 'responseStart',
      index: 4
    }, {
      name: 'Response',
      start: 'responseStart',
      end: 'responseEnd',
      index: 5
    }, {
      name: 'Processing',
      start: 'domLoading',
      end: 'domComplete',
      index: 6
    }, {
      name: 'onLoad',
      start: 'loadEventStart',
      end: 'loadEventEnd',
      index: 7
    }
  ];

  var colors = [
    'f60', 'f90', 'fc3', '06c', '09c', '360', '690', '7c3'
  ];

  var totalCost = 0;
  var t = performance.timing;

  function render(content) {
    var div = document.createElement('div');
    div.id = 'navigation-timing-chart';

    var content = [],
        left = 0;
    phases.forEach(function(v) {
      v.left = left;
      left += +v.width;
      content.push(tmpl.item.replace(rtmpl, function(_, key) {
        return v[key];
      }));
    })

    div.innerHTML = tmpl.base.replace(rtmpl, content.join(''));
    document.body.appendChild(div);
  }

  function compute() {
    setTimeout(function() {
      phases.forEach(function(v) {
        var start = t[v.start],
            end = t[v.end];
        totalCost += (v.value = (start == 0 ? 0 : (end - start)));
      })

      phases.sort(function(a, b) {
        return b.value - a.value;
      })

      phases.forEach(function(v, i) {
        v.color = colors[i];
        v.width = (100 * v.value / totalCost).toFixed(3);
      })

      phases.sort(function(a, b) {
        return a.index - b.index;
      })

      render();
    }, 0)
  }

  window.addEventListener('load', compute);
}())

/**
 * unload
 *   unloadEventStart
 *   unloadEventEnd
 *
 * navigationStart
 *
 * redirect
 *   redirectStart
 *   redirectEnd
 *
 * App cache
 *   fetchStart
 *
 * DNS
 *   domainLookupStart
 *   domainLookupEnd
 *
 * TCP
 *   connectStart
 *     secureConnectionStart
 *   connectEnd
 *
 * Request
 *   requestStart
 *
 * Response
 *   responseStart
 *   responseEnd
 *
 * Processing
 *   domLoading
 *   domInteractive
 *
 *   domContentLoadedEventStart
 *   domContentLoadedEventEnd
 *
 *   domComplete
 *
 * onLoad
 *   loadEventStart
 *   loadEventEnd
 *
 */
