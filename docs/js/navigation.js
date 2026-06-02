/**
 * Navigation Menu Functionality
 * Manages mobile menu toggle and active page highlighting
 */

(function() {
  'use strict';

  // Setup navigation menu toggle
  function setupNavToggle() {
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    const navOverlay = document.getElementById('nav-overlay');

    if (!navToggle || !navMenu || !navOverlay) return;

    // Toggle menu on button click
    navToggle.addEventListener('click', () => {
      toggleMenu();
    });

    // Close menu when overlay is clicked
    navOverlay.addEventListener('click', () => {
      closeMenu();
    });

    // Close menu when a link is clicked
    const navLinks = navMenu.querySelectorAll('.nav-menu-link');
    navLinks.forEach(link => {
      link.addEventListener('click', () => {
        closeMenu();
      });
    });

    // Close menu on Escape key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && navMenu.classList.contains('active')) {
        closeMenu();
      }
    });
  }

  // Toggle menu open/closed
  function toggleMenu() {
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    const navOverlay = document.getElementById('nav-overlay');

    const isActive = navMenu.classList.contains('active');

    if (isActive) {
      closeMenu();
    } else {
      openMenu();
    }
  }

  // Open menu
  function openMenu() {
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    const navOverlay = document.getElementById('nav-overlay');

    navToggle.classList.add('active');
    navMenu.classList.add('active');
    navOverlay.classList.add('active');
    document.body.style.overflow = 'hidden'; // Prevent body scroll when menu is open

    navToggle.setAttribute('aria-expanded', 'true');
  }

  // Close menu
  function closeMenu() {
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    const navOverlay = document.getElementById('nav-overlay');

    navToggle.classList.remove('active');
    navMenu.classList.remove('active');
    navOverlay.classList.remove('active');
    document.body.style.overflow = ''; // Restore body scroll

    navToggle.setAttribute('aria-expanded', 'false');
  }

  // Highlight active page in navigation
  function highlightActivePage() {
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const navLinks = document.querySelectorAll('.nav-menu-link');

    navLinks.forEach(link => {
      const linkPage = link.getAttribute('href');
      const linkPageName = linkPage ? linkPage.split('/').pop() : '';

      if (linkPageName === currentPage) {
        link.classList.add('active');
        link.setAttribute('aria-current', 'page');
      } else {
        link.classList.remove('active');
        link.removeAttribute('aria-current');
      }
    });
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      setupNavToggle();
      highlightActivePage();
    });
  } else {
    setupNavToggle();
    highlightActivePage();
  }
})();
