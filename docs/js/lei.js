hljs.initHighlightingOnLoad();

window.addEventListener('DOMContentLoaded', function() {
  Array.from(document.querySelectorAll("[data-nav-tab]"))
    .forEach(function(tab) {
      tab.addEventListener('click', function() {
        var initialOffset = tab.offsetTop - tab.getBoundingClientRect().top; 
        window.scrollTo(0, initialOffset);
      });
    });
});