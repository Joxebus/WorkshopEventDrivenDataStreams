/**
 * Theme Toggle Functionality
 * Manages light/dark theme switching with localStorage persistence
 */

(function() {
  'use strict';

  const THEME_KEY = 'theme';
  const THEME_LIGHT = 'light';
  const THEME_DARK = 'dark';

  // Initialize theme on page load
  function initTheme() {
    // Remove no-transition class after page load to enable smooth transitions
    document.documentElement.classList.add('no-transition');

    // Get saved theme or default to light
    const savedTheme = localStorage.getItem(THEME_KEY) || THEME_LIGHT;
    applyTheme(savedTheme);

    // Remove no-transition class after a brief delay
    setTimeout(() => {
      document.documentElement.classList.remove('no-transition');
    }, 100);
  }

  // Apply theme to document
  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);
    updateThemeIcon(theme);
    updateThemedImages(theme);
  }

  // Update theme toggle button icon
  function updateThemeIcon(theme) {
    const toggleButton = document.getElementById('theme-toggle');
    if (!toggleButton) return;

    const sunIcon = toggleButton.querySelector('.icon-sun');
    const moonIcon = toggleButton.querySelector('.icon-moon');

    if (theme === THEME_DARK) {
      sunIcon.style.display = 'block';
      moonIcon.style.display = 'none';
      toggleButton.setAttribute('aria-label', 'Switch to light theme');
    } else {
      sunIcon.style.display = 'none';
      moonIcon.style.display = 'block';
      toggleButton.setAttribute('aria-label', 'Switch to dark theme');
    }
  }

  // Update all themed images on the page
  function updateThemedImages(theme) {
    const themedImages = document.querySelectorAll('[data-image-base]');

    themedImages.forEach(img => {
      const baseName = img.getAttribute('data-image-base');
      const newSrc = `../assets/img/${baseName}_${theme}.jpg`;
      img.src = newSrc;
    });
  }

  // Toggle between light and dark theme
  function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || THEME_LIGHT;
    const newTheme = currentTheme === THEME_LIGHT ? THEME_DARK : THEME_LIGHT;
    applyTheme(newTheme);
  }

  // Setup theme toggle button
  function setupThemeToggle() {
    const toggleButton = document.getElementById('theme-toggle');
    if (!toggleButton) return;

    toggleButton.addEventListener('click', toggleTheme);

    // Keyboard support
    toggleButton.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        toggleTheme();
      }
    });
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initTheme();
      setupThemeToggle();
    });
  } else {
    initTheme();
    setupThemeToggle();
  }

  // Export for manual theme setting if needed
  window.setTheme = applyTheme;
  window.getCurrentTheme = () => {
    return document.documentElement.getAttribute('data-theme') || THEME_LIGHT;
  };
})();
