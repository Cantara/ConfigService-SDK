# ConfigService-SDK

## Purpose
Common core library shared by ConfigService and its clients. Provides data models, serialization, and utility classes for interacting with the ConfigService REST API.

## Tech Stack
- Language: Java 8
- Framework: None (pure library)
- Build: Maven
- Key dependencies: SLF4J

## Architecture
Pure Java library with no runtime framework dependencies. Contains shared data types, JSON/XML serialization helpers, and client utilities used by both the ConfigService server and client applications (such as Java-Auto-Update). Designed for minimal footprint and broad compatibility.

## Key Entry Points
- `src/main/java/` - SDK source code with shared models and utilities
- `pom.xml` - Maven coordinates: `no.cantara.jau:configservice-sdk`

## Development
```bash
# Build
mvn clean install

# Test
mvn test
```

## Domain Context
Shared library for the Cantara application lifecycle management ecosystem. Used by ConfigService, ConfigService-Dashboard, and Java-Auto-Update to ensure consistent data handling and API interaction.
