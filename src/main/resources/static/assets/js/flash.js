// Minimal flash message handler for query-based redirects
// Shows ?error=... or ?info=... messages in dedicated containers if present.
(function () {
    try {
        const params = new URLSearchParams(window.location.search);
        const error = params.get('error');
        const info = params.get('info');

        if (error) {
            const el = document.getElementById('flash-error');
            if (el) {
                el.textContent = error;
                el.style.display = 'block';
            } else {
                alert(error);
            }
        }
        if (info) {
            const il = document.getElementById('flash-info');
            if (il) {
                il.textContent = info;
                il.style.display = 'block';
            }
        }
    } catch (e) {
        // no-op
    }
})();

