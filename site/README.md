# Event-Driven Architecture Workshop Website

This is a static HTML website for the "Event-Driven Architecture and Data Streaming" workshop, designed to replace traditional slide presentations.

## Structure

```
site/
├── index.html                      # Landing page with workshop overview
├── pages/                          # Topic pages (11 sections)
│   ├── event-sourcing.html        # Event Sourcing Principles
│   ├── cqrs.html                  # CQRS Pattern  
│   ├── kafka-basics.html          # Apache Kafka Fundamentals
│   ├── spring-boot-kafka.html     # Spring Boot + Kafka Integration
│   ├── async-communication.html   # Asynchronous Communication
│   ├── project-architecture.html  # Project Architecture
│   ├── real-world-example.html    # Real-World Order Processing Example
│   ├── data-serialization.html    # Data Serialization (JSON to Avro)
│   ├── testing-patterns.html      # Testing with Spock Framework
│   ├── docker-setup.html          # Docker Compose Setup
│   └── best-practices.html        # Production Best Practices
├── css/                            # Stylesheets
│   ├── theme.css                  # Light/dark theme variables
│   ├── main.css                   # Core typography and layout
│   └── components.css             # UI components (cards, nav, buttons)
├── js/                             # JavaScript functionality
│   ├── theme-toggle.js            # Theme switching logic
│   ├── navigation.js              # Mobile menu and page highlighting
│   └── progress.js                # Progress tracking across pages
└── assets/
    └── img/                        # Architecture diagrams (future)
```

## Features

- **Dark/Light Theme Toggle**: Persistent theme selection with smooth transitions
- **Responsive Design**: Mobile-first approach with breakpoints at 640px and 1024px
- **Progress Tracking**: Visual progress bar showing current position in workshop
- **Mobile Navigation**: Slide-out navigation menu with hamburger icon
- **Accessibility**: Semantic HTML5, ARIA labels, keyboard navigation support
- **No Dependencies**: Pure HTML/CSS/JS with no build system required

## Technology Stack

- **HTML5**: Semantic markup with accessibility features
- **CSS3**: Custom properties for theming, Flexbox and Grid layouts
- **JavaScript**: Vanilla ES6+ with localStorage for persistence
- **Google Fonts**: Inter font family for modern typography

## Usage

### Local Development

Simply open `index.html` in a web browser:

```bash
open site/index.html
```

Or use a simple HTTP server:

```bash
cd site
python3 -m http.server 8000
# Visit http://localhost:8000
```

### Theme Customization

Theme colors are defined in `css/theme.css` using CSS custom properties:

- **Event Sourcing**: Blue (#2563eb)
- **CQRS**: Green (#10b981)
- **Kafka**: Orange (#f59e0b)
- **Testing**: Purple (#8b5cf6)

### Adding New Pages

1. Create HTML file in `pages/` directory
2. Update `js/progress.js` PAGES array with page info
3. Add navigation link in all page headers
4. Update previous/next footer navigation buttons

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## License

Part of the SpringBootKafkaWorkshop project.

## Author

Omar Bautista - Software Engineer
