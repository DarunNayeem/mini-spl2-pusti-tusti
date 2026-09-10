# Deployment Guide

## Overview
This guide reflects the project as it exists today: a working JavaFX desktop app with SQLite persistence, local data access, and a currently stable but still refactorable planning service.

## Current Deployment Status

The project is currently suitable for local development and personal use on a developer machine. It is not presented here as a multi-user production deployment platform.

## Prerequisites

- Java 17+
- Maven 3.6+
- A desktop environment capable of launching JavaFX
- Git for version control

## Verified Commands

```bash
java -version
mvn -version
mvn test
mvn javafx:run
```

The project was verified with:

```bash
cd /media/nym/endeavouros/home/nym/Pictures/Java-Pusti-Tusti && mvn -q test; echo EXIT:$?
```

Current result:

```text
EXIT:0
```

## Local Project Layout

```text
project-root/
├── pom.xml
├── README.md
├── docs/
├── src/
│   ├── main/java
│   ├── main/resources
│   └── test/java
├── target/
└── shopping_list.txt
```

## Build Workflow

### Compile
```bash
mvn compile
```

### Run tests
```bash
mvn test
```

### Run the app
```bash
mvn javafx:run
```

## Environment Notes

### JavaFX requirement
The app uses JavaFX and is expected to run in a standard desktop Java environment with the necessary JavaFX dependencies resolved through Maven.

### SQLite behavior
The app uses SQLite in the project workspace and initializes database structures on startup. Recent cleanup improved the database path and startup handling.

## Operational Guidance

### Local use
This app is best treated as a local desktop workflow tool:
- create profiles
- generate meal plans
- view weekly nutrition suggestions
- collect shopping list outputs

### Maintenance
Future maintenance should focus on:
- splitting the large planner service into narrower components
- formalizing logging across all layers
- keeping business logic separate from UI concerns

## Risks and Constraints

- App behavior is centered on desktop local execution, not cloud or multi-user deployment
- The planner service remains the main complex component to maintain
- The project is stable enough for development work, but not yet a full-scale distributed app architecture

## Recommended Next Step

A good next evolution would be breaking `MealPlannerService` into smaller strategy components while keeping the current working UI and SQLite behavior intact.

---

**Document Status**: Updated for the current project state
**Last Updated**: September 2026

# Run with increased memory
mvn javafx:run -Djavafx.options="-Xmx2g -Xms512m"
```

#### Using Java Command
```bash
# Run from compiled classes
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp target/classes \
     com.mealplanner.App

# Run with module system
java --module-path target/classes:/path/to/javafx/lib \
     --module com.mealplanner/com.mealplanner.App
```

#### VS Code Integration
Create `.vscode/tasks.json`:
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Run Pusti-Tusti Application",
            "type": "shell",
            "command": "mvn javafx:run",
            "group": "build",
            "isBackground": false,
            "presentation": {
                "echo": true,
                "reveal": "always",
                "focus": false,
                "panel": "shared"
            },
            "problemMatcher": []
        }
    ]
}
```

### Production Environment

#### Creating Executable JAR
```bash
# Build fat JAR with all dependencies
mvn clean package

# The JAR will be created at target/pusti-tusti-1.0-SNAPSHOT.jar
```

#### Running Executable JAR
```bash
# Run the JAR file
java --add-modules javafx.controls,javafx.fxml \
     -jar target/pusti-tusti-1.0-SNAPSHOT.jar

# Run with specific memory settings
java -Xmx1g -Xms256m \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/pusti-tusti-1.0-SNAPSHOT.jar
```

---

## Creating Native Distributions

### Using jpackage (Java 17+)

#### Windows Installer
```bash
# Create Windows .exe installer
jpackage --input target/classes \
         --name "Pusti-Tusti" \
         --main-jar pusti-tusti-1.0-SNAPSHOT.jar \
         --main-class com.mealplanner.App \
         --type exe \
         --dest dist \
         --icon src/main/resources/app-icon.ico \
         --app-version 1.0 \
         --vendor "Meal Planner Inc." \
         --description "Personalized Meal Planning Application"
```

#### macOS Application Bundle
```bash
# Create macOS .app bundle
jpackage --input target/classes \
         --name "Pusti-Tusti" \
         --main-jar pusti-tusti-1.0-SNAPSHOT.jar \
         --main-class com.mealplanner.App \
         --type app-image \
         --dest dist \
         --icon src/main/resources/app-icon.icns \
         --app-version 1.0 \
         --vendor "Meal Planner Inc."
```

#### Linux Package
```bash
# Create Linux .deb package
jpackage --input target/classes \
         --name "pusti-tusti" \
         --main-jar pusti-tusti-1.0-SNAPSHOT.jar \
         --main-class com.mealplanner.App \
         --type deb \
         --dest dist \
         --icon src/main/resources/app-icon.png \
         --app-version 1.0 \
         --vendor "Meal Planner Inc." \
         --linux-menu-group "Office;Utility"
```

### Alternative: Launch4j (Windows)

Create `launch4j-config.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
    <dontWrapJar>false</dontWrapJar>
    <headerType>gui</headerType>
    <jar>target/pusti-tusti-1.0-SNAPSHOT.jar</jar>
    <outfile>dist/Pusti-Tusti.exe</outfile>
    <errTitle></errTitle>
    <cmdLine></cmdLine>
    <chdir>.</chdir>
    <priority>normal</priority>
    <downloadUrl>https://adoptopenjdk.net/</downloadUrl>
    <supportUrl></supportUrl>
    <stayAlive>false</stayAlive>
    <restartOnCrash>false</restartOnCrash>
    <manifest></manifest>
    <icon>src/main/resources/app-icon.ico</icon>
    <jre>
        <path></path>
        <bundledJre64Bit>false</bundledJre64Bit>
        <bundledJreAsFallback>false</bundledJreAsFallback>
        <minVersion>17</minVersion>
        <maxVersion></maxVersion>
        <jdkPreference>preferJre</jdkPreference>
        <runtimeBits>64/32</runtimeBits>
    </jre>
    <versionInfo>
        <fileVersion>1.0.0.0</fileVersion>
        <txtFileVersion>1.0.0</txtFileVersion>
        <fileDescription>Pusti-Tusti Meal Planner</fileDescription>
        <copyright>2025</copyright>
        <productVersion>1.0.0.0</productVersion>
        <txtProductVersion>1.0.0</txtProductVersion>
        <productName>Pusti-Tusti</productName>
        <companyName>Meal Planner Inc.</companyName>
        <internalName>pusti-tusti</internalName>
        <originalFilename>Pusti-Tusti.exe</originalFilename>
    </versionInfo>
</launch4jConfig>
```

---

## Database Deployment

### Database Setup

#### Development Database
```bash
# Database is included in resources
# No additional setup required for development
ls src/main/resources/recipes.db
```

#### Production Database Setup
```bash
# Create data directory
mkdir -p ./data

# Copy database to production location
cp src/main/resources/recipes.db ./data/

# Set appropriate permissions
chmod 644 ./data/recipes.db
```

#### Database Migration Scripts
Create `scripts/migrate-database.sql`:
```sql
-- Example migration script
-- Add new columns or tables as needed

-- Migration version tracking
CREATE TABLE IF NOT EXISTS schema_version (
    version TEXT PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Check current version
SELECT version FROM schema_version ORDER BY applied_at DESC LIMIT 1;

-- Apply migration if needed
-- ALTER TABLE recipes ADD COLUMN new_field TEXT;
-- INSERT INTO schema_version (version) VALUES ('2025.1.1');
```

### Backup Strategy

#### Automated Backup Script
Create `scripts/backup-database.sh`:
```bash
#!/bin/bash

# Backup configuration
BACKUP_DIR="./backups"
DB_PATH="./data/recipes.db"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="recipes_backup_${DATE}.db"

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Create backup
sqlite3 "$DB_PATH" ".backup $BACKUP_DIR/$BACKUP_FILE"

# Verify backup
if [ -f "$BACKUP_DIR/$BACKUP_FILE" ]; then
    echo "Backup created successfully: $BACKUP_DIR/$BACKUP_FILE"
    
    # Keep only last 30 backups
    find "$BACKUP_DIR" -name "recipes_backup_*.db" -type f -mtime +30 -delete
else
    echo "Backup failed!"
    exit 1
fi
```

#### Restore Script
Create `scripts/restore-database.sh`:
```bash
#!/bin/bash

BACKUP_FILE="$1"
DB_PATH="./data/recipes.db"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file>"
    echo "Available backups:"
    ls -la ./backups/recipes_backup_*.db
    exit 1
fi

# Create backup of current database
cp "$DB_PATH" "${DB_PATH}.before_restore"

# Restore from backup
sqlite3 "$DB_PATH" ".restore $BACKUP_FILE"

echo "Database restored from: $BACKUP_FILE"
echo "Previous database backed up as: ${DB_PATH}.before_restore"
```

---

## Configuration Management

### Application Configuration

#### Properties File: `application.properties`
```properties
# Application Configuration
app.name=Pusti-Tusti
app.version=1.0.0
app.title=Pusti-Tusti - Personalized Meal Planner

# Database Configuration
database.url=jdbc:sqlite:./data/recipes.db
database.connection.timeout=30000
database.connection.pool.size=1

# UI Configuration
ui.window.width=1200
ui.window.height=800
ui.window.resizable=true
ui.theme=default

# Profile Storage
profile.storage.table=user_profiles
profile.storage.format=sqlite
profile.auto.backup=false

# Logging Configuration
logging.level=INFO
logging.file=./logs/application.log
logging.max.file.size=10MB
logging.max.files=5

# Performance Configuration
cache.recipe.size=1000
cache.recipe.ttl=3600000
memory.max.heap=1g
memory.initial.heap=256m
```

#### Environment-Specific Configuration
```bash
# Development environment
export PUSTI_TUSTI_ENV=development
export PUSTI_TUSTI_DB_PATH=src/main/resources/recipes.db
export PUSTI_TUSTI_LOG_LEVEL=DEBUG

# Production environment
export PUSTI_TUSTI_ENV=production
export PUSTI_TUSTI_DB_PATH=./data/recipes.db
export PUSTI_TUSTI_LOG_LEVEL=WARN
export PUSTI_TUSTI_PROFILE_DIR=./profiles
```

### Configuration Loading
```java
public class ConfigurationManager {
    private static final Properties config = new Properties();
    
    static {
        try {
            // Load default configuration
            config.load(ConfigurationManager.class
                .getResourceAsStream("/application.properties"));
            
            // Override with environment-specific config
            String env = System.getenv("PUSTI_TUSTI_ENV");
            if (env != null) {
                String envConfigFile = "/application-" + env + ".properties";
                InputStream envConfig = ConfigurationManager.class
                    .getResourceAsStream(envConfigFile);
                if (envConfig != null) {
                    Properties envProps = new Properties();
                    envProps.load(envConfig);
                    config.putAll(envProps);
                }
            }
            
            // Override with system properties
            config.putAll(System.getProperties());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    public static String getString(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    public static int getInt(String key, int defaultValue) {
        String value = config.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}
```

---

## Testing and Quality Assurance

### Automated Testing

#### Unit Test Execution
```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run integration tests
mvn integration-test

# Run tests for specific package
mvn test -Dtest="com.mealplanner.service.*Test"
```

#### Test Configuration
Create `src/test/resources/test.properties`:
```properties
# Test Database Configuration
test.database.url=jdbc:sqlite::memory:
test.database.init.script=test-data.sql

# Test User Profiles
test.profile.directory=src/test/resources/test-profiles

# Test Recipe Data
test.recipe.count.min=100
test.recipe.variety.threshold=0.8
```

### Quality Checks

#### Code Quality with SpotBugs
Add to `pom.xml`:
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Code Coverage with JaCoCo
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Run Quality Checks
```bash
# Run all quality checks
mvn clean test spotbugs:check jacoco:report

# Generate reports in target/site/
mvn site
```

---

## Deployment Strategies

### Local Deployment

#### Desktop Installation
1. **Download Release Package**:
   - Download platform-specific installer from releases
   - Run installer with administrator privileges
   - Follow installation wizard

2. **Manual Installation**:
   ```bash
   # Extract application archive
   unzip pusti-tusti-1.0.0.zip -d /opt/pusti-tusti
   
   # Set executable permissions
   chmod +x /opt/pusti-tusti/bin/pusti-tusti
   
   # Create symbolic link
   ln -s /opt/pusti-tusti/bin/pusti-tusti /usr/local/bin/pusti-tusti
   ```

3. **First Run Setup**:
   ```bash
   # Create user data directories
   mkdir -p ~/.pusti-tusti/profiles
   mkdir -p ~/.pusti-tusti/data
   mkdir -p ~/.pusti-tusti/logs
   
   # Copy default database
   cp /opt/pusti-tusti/data/recipes.db ~/.pusti-tusti/data/
   ```

### Network Deployment

#### Shared Network Drive
```bash
# Deploy to network share
cp -r dist/pusti-tusti/ /network/share/applications/

# Create launcher script
cat > /network/share/applications/pusti-tusti/launch.sh << 'EOF'
#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk
APP_DIR="$(dirname "$0")"
cd "$APP_DIR"
exec "$JAVA_HOME/bin/java" \
    --add-modules javafx.controls,javafx.fxml \
    -jar pusti-tusti-1.0.0.jar
EOF

chmod +x /network/share/applications/pusti-tusti/launch.sh
```

#### Docker Deployment (Future Enhancement)
Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim

# Install JavaFX dependencies
RUN apt-get update && apt-get install -y \
    openjfx \
    libgtk-3-0 \
    libxss1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy application
COPY target/pusti-tusti-1.0.0.jar app.jar
COPY src/main/resources/recipes.db data/

# Expose port (if web interface is added)
# EXPOSE 8080

# Run application
CMD ["java", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]
```

---

## Monitoring and Maintenance

### Application Monitoring

#### Health Check Script
Create `scripts/health-check.sh`:
```bash
#!/bin/bash

# Check if application is running
APP_PID=$(pgrep -f "pusti-tusti")
if [ -z "$APP_PID" ]; then
    echo "ERROR: Application is not running"
    exit 1
fi

# Check database accessibility
DB_PATH="./data/recipes.db"
if ! sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM breakfast;" > /dev/null 2>&1; then
    echo "ERROR: Database is not accessible"
    exit 1
fi

# Check disk space
AVAILABLE_SPACE=$(df -h . | awk 'NR==2 {print $4}' | sed 's/G//')
if [ "$AVAILABLE_SPACE" -lt 1 ]; then
    echo "WARNING: Low disk space: ${AVAILABLE_SPACE}GB available"
fi

# Check log file size
LOG_FILE="./logs/application.log"
if [ -f "$LOG_FILE" ]; then
    LOG_SIZE=$(du -m "$LOG_FILE" | cut -f1)
    if [ "$LOG_SIZE" -gt 100 ]; then
        echo "WARNING: Log file is large: ${LOG_SIZE}MB"
    fi
fi

echo "OK: Application health check passed"
```

#### Log Rotation
Create `scripts/rotate-logs.sh`:
```bash
#!/bin/bash

LOG_DIR="./logs"
LOG_FILE="application.log"
MAX_SIZE=10485760  # 10MB in bytes

if [ -f "$LOG_DIR/$LOG_FILE" ]; then
    FILE_SIZE=$(stat -f%z "$LOG_DIR/$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_DIR/$LOG_FILE")
    
    if [ "$FILE_SIZE" -gt "$MAX_SIZE" ]; then
        # Rotate logs
        mv "$LOG_DIR/$LOG_FILE" "$LOG_DIR/$LOG_FILE.$(date +%Y%m%d_%H%M%S)"
        touch "$LOG_DIR/$LOG_FILE"
        
        # Keep only last 5 rotated logs
        find "$LOG_DIR" -name "$LOG_FILE.*" -type f | sort -r | tail -n +6 | xargs rm -f
        
        echo "Log rotated: $LOG_FILE"
    fi
fi
```

### Performance Monitoring

#### System Resource Usage
```bash
# Monitor Java process resources
ps -p $(pgrep -f pusti-tusti) -o pid,ppid,pcpu,pmem,vsz,rss,etime,comm

# Monitor database locks
sqlite3 ./data/recipes.db "PRAGMA compile_options;"

# Check database integrity
sqlite3 ./data/recipes.db "PRAGMA integrity_check;"
```

---

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check Java version
java -version

# Check JavaFX modules
java --list-modules | grep javafx

# Run with verbose output
java -Xlog:class+load --add-modules javafx.controls,javafx.fxml -jar pusti-tusti-1.0.0.jar

# Check for port conflicts (if applicable)
netstat -tlnp | grep :8080
```

#### Database Issues
```bash
# Check database file permissions
ls -la ./data/recipes.db

# Verify database integrity
sqlite3 ./data/recipes.db "PRAGMA integrity_check;"

# Check database schema
sqlite3 ./data/recipes.db ".schema"

# Repair database (if needed)
sqlite3 ./data/recipes.db ".recover" | sqlite3 ./data/recipes_recovered.db
```

#### Performance Issues
```bash
# Monitor memory usage
jstat -gc $(pgrep -f pusti-tusti)

# Generate heap dump
jcmd $(pgrep -f pusti-tusti) GC.run_finalization
jcmd $(pgrep -f pusti-tusti) VM.heap_dump ./heap_dump.hprof

# Profile application
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr -jar pusti-tusti-1.0.0.jar
```

### Log Analysis

#### Common Log Patterns
```bash
# Find errors in logs
grep "ERROR" ./logs/application.log | tail -20

# Monitor real-time logs
tail -f ./logs/application.log

# Find memory-related issues
grep -i "memory\|heap\|gc" ./logs/application.log

# Database-related errors
grep -i "database\|sqlite\|sql" ./logs/application.log
```

---

## Security Considerations

### File System Security
```bash
# Set restrictive permissions on sensitive files
chmod 600 ./data/recipes.db
chmod 644 ./logs/application.log

# Create dedicated user (Linux/macOS)
sudo useradd -r -s /bin/false pusti-tusti
sudo chown -R pusti-tusti:pusti-tusti /opt/pusti-tusti
```

### Data Protection
- User profiles stored locally with restricted access
- Database files protected with file system permissions
- No network communication for sensitive data
- Automatic backup encryption (future enhancement)

---

This comprehensive deployment guide provides all necessary information for successfully building, testing, and deploying the Pusti-Tusti meal planning application across different environments and platforms.
