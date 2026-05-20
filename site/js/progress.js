/**
 * Progress Indicator
 * Shows current page number and progress bar
 */

(function() {
  'use strict';

  // Page configuration
  const PAGES = [
    { id: 'index', title: 'Home', number: 0, url: '../index.html' },
    { id: 'event-sourcing', title: 'Event Sourcing Principles', number: 1, url: 'event-sourcing.html' },
    { id: 'cqrs', title: 'CQRS Pattern', number: 2, url: 'cqrs.html' },
    { id: 'kafka-basics', title: 'Apache Kafka Fundamentals', number: 3, url: 'kafka-basics.html' },
    { id: 'spring-boot-kafka', title: 'Spring Boot + Kafka Integration', number: 4, url: 'spring-boot-kafka.html' },
    { id: 'async-communication', title: 'Asynchronous Communication', number: 5, url: 'async-communication.html' },
    { id: 'project-architecture', title: 'Project Architecture', number: 6, url: 'project-architecture.html' },
    { id: 'real-world-example', title: 'Real-World Example', number: 7, url: 'real-world-example.html' },
    { id: 'data-serialization', title: 'Data Serialization', number: 8, url: 'data-serialization.html' },
    { id: 'testing-patterns', title: 'Testing Patterns', number: 9, url: 'testing-patterns.html' },
    { id: 'docker-setup', title: 'Docker Setup', number: 10, url: 'docker-setup.html' },
    { id: 'best-practices', title: 'Best Practices', number: 11, url: 'best-practices.html' }
  ];

  const TOTAL_PAGES = 11; // Excluding index

  // Get current page info
  function getCurrentPageInfo() {
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    const currentFile = currentPath.replace('.html', '');

    const pageInfo = PAGES.find(page => page.id === currentFile);
    return pageInfo || PAGES[0];
  }

  // Update progress indicator
  function updateProgressIndicator() {
    const progressText = document.getElementById('progress-text');
    const progressBarFill = document.getElementById('progress-bar-fill');

    const currentPage = getCurrentPageInfo();

    // Don't show progress on index page
    if (currentPage.number === 0) {
      if (progressText) progressText.style.display = 'none';
      if (progressBarFill) progressBarFill.parentElement.style.display = 'none';
      return;
    }

    // Update progress text
    if (progressText) {
      progressText.textContent = `Page ${currentPage.number} of ${TOTAL_PAGES}`;
      progressText.style.display = 'block';
    }

    // Update progress bar
    if (progressBarFill) {
      const percentage = (currentPage.number / TOTAL_PAGES) * 100;
      progressBarFill.style.width = `${percentage}%`;
      progressBarFill.parentElement.style.display = 'block';
    }
  }

  // Get navigation info (previous and next pages)
  function getNavigationInfo() {
    const currentPage = getCurrentPageInfo();
    const currentIndex = PAGES.findIndex(page => page.id === currentPage.id);

    const prevPage = currentIndex > 0 ? PAGES[currentIndex - 1] : null;
    const nextPage = currentIndex < PAGES.length - 1 ? PAGES[currentIndex + 1] : null;

    return { prevPage, nextPage };
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      updateProgressIndicator();
    });
  } else {
    updateProgressIndicator();
  }

  // Export functions for use in HTML
  window.getCurrentPageInfo = getCurrentPageInfo;
  window.getNavigationInfo = getNavigationInfo;
  window.PAGES = PAGES;
})();
