# Description

This example project shows how to integrate an Angular application inside a Spring Boot application.

It is a multi-project build with one project for the Spring Boot application and one project for the Angular one.

It bundles the Angular application as a jar in the `static` subdirectory that Spring serves as static resources.

The default Angular output directory (in `angular.json`) was changed to `build/webapp/static` and the `build/webapp`
directory was added to the Java resources.

A selenium based test ensures that the Angular application is served by Spring as expected.

It uses the globally installed Node.js and npm tools.

# Build

```bash
gradle build
```

# Serve

```bash
gradle bootRun
```

Open http://localhost:8080.
