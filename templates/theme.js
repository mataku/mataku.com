(function() {
  var theme = localStorage.getItem('theme');
  if (theme) {
    document.documentElement.setAttribute('data-theme', theme);
  }

  function getEffectiveTheme() {
    var stored = localStorage.getItem('theme');
    if (stored) return stored;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  function updateThemeColor(theme) {
    var color = theme === 'dark' ? '#222831' : '#F2EDE5';
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute('content', color);
  }

  updateThemeColor(getEffectiveTheme());

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function() {
    if (!localStorage.getItem('theme')) {
      updateThemeColor(getEffectiveTheme());
    }
  });

  document.addEventListener('DOMContentLoaded', function() {
    var toggle = document.getElementById('theme-toggle');
    if (!toggle) return;

    toggle.addEventListener('click', function() {
      var current = getEffectiveTheme();
      var next = current === 'dark' ? 'light' : 'dark';
      localStorage.setItem('theme', next);
      document.documentElement.setAttribute('data-theme', next);
      updateThemeColor(next);
    });
  });
})();
