# Documentation Index

This documentation set reflects the current state of the project as of September 2026.

## Project Snapshot

Pusti-Tusti is a JavaFX-based desktop meal-planning application for generating personalized plans for individuals and families. The project is currently in a stable cleanup and maintenance phase with verified test health and improved startup/database safety.

## Documentation Overview

The documentation is organized to help developers, maintainers, and users understand the application from setup through architecture and operations.

---

## Core Documentation

### [README](README.md)
Main project summary, current status, setup steps, and verified health.

### [Architecture Guide](ARCHITECTURE.md)
System layers, responsibilities, flows, and current design trade-offs.

### [API Documentation](API.md)
Service and repository interfaces, usage patterns, and integration notes.

### [Database Guide](DATABASE.md)
SQLite schema, recipe data model, and persistence patterns.

### [UI Guide](UI.md)
JavaFX screens, controllers, and styling approach.

### [Models Documentation](MODELS.md)
Domain models, enums, profile model, and nutrition-related objects.

### [Deployment Guide](DEPLOYMENT.md)
Build, run, and environment notes for local development.

---

## Current Project Reality

### Verified State
- Project builds and tests successfully with Maven.
- The test command currently exits successfully.
- Some cleanup work has already been applied to reduce code smell in startup and DB management.

### Remaining Architectural Debt
- The planner service remains a large class and is a logical follow-up refactor target.
- The project is functioning and maintainable for local use, but it is not yet a fully decomposition-oriented enterprise application.

---

## Quick Navigation

### For New Contributors
1. Start with [README](README.md)
2. Read [ARCHITECTURE.md](ARCHITECTURE.md)
3. Review [API.md](API.md) and [MODELS.md](MODELS.md)
4. Use [DEPLOYMENT.md](DEPLOYMENT.md) for local setup

### For Database or Data Work
1. [DATABASE.md](DATABASE.md)
2. [MODELS.md](MODELS.md)
3. [API.md](API.md)

### For UI Work
1. [UI.md](UI.md)
2. [ARCHITECTURE.md](ARCHITECTURE.md)
3. [README.md](README.md)

---

## Documentation Status

| Document | Status |
|----------|--------|
| [README](README.md) | Updated |
| [Architecture Guide](ARCHITECTURE.md) | Updated |
| [API Documentation](API.md) | Present |
| [Database Guide](DATABASE.md) | Present |
| [UI Guide](UI.md) | Present |
| [Models Documentation](MODELS.md) | Present |
| [Deployment Guide](DEPLOYMENT.md) | Updated |

---

**Last Updated**: September 2026
**Project State**: Active maintenance and cleanup
