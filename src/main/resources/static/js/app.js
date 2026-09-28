document.body.addEventListener('htmx:configRequest', function (e) {
  var m = document.querySelector('meta[name="csrf"]');
  if (m) e.detail.headers['X-CSRF-TOKEN'] = m.content;
});
