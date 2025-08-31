// Minimal flash message handler for query-based redirects
// Shows ?error=... or ?info=... messages in dedicated containers if present.
(function () {
  try {
    var params = new URLSearchParams(window.location.search);
    var error = params.get('error');
    var info = params.get('info');

    if (error) {
      var el = document.getElementById('flash-error');
      if (el) {
        el.textContent = error;
        el.style.display = 'block';
      } else {
        alert(error);
      }
    }
    if (info) {
      var il = document.getElementById('flash-info');
      if (il) {
        il.textContent = info;
        il.style.display = 'block';
      }
    }
  } catch (e) {
    // no-op
  }
})();

