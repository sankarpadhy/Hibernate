FROM maven:3.8.4-openjdk-11-slim AS builder

WORKDIR /app
COPY . .

# Build the application with dependencies
RUN mvn clean package -DskipTests

# Create the runtime image
FROM openjdk:11-jre-slim

# Install PostgreSQL client
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR files from each module
COPY --from=builder /app/common/target/common-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/basics/target/basics-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/caching/target/caching-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/querying/target/querying-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/relationships/target/relationships-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/inheritance/target/inheritance-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/bestpractices/target/bestpractices-1.0-SNAPSHOT.jar ./lib/
COPY --from=builder /app/locking/target/locking-1.0-SNAPSHOT.jar ./lib/

# Copy configuration files
COPY --from=builder /app/common/src/main/resources/hibernate.cfg.xml ./config/
COPY --from=builder /app/common/src/main/resources/logback.xml ./config/

# Make the script executable
COPY wait-for-postgres.sh .
RUN chmod +x wait-for-postgres.sh

ENV JAVA_OPTS="-Dlogback.configurationFile=/app/config/logback.xml"
ENV CLASSPATH="/app/lib/*:/app/config"

# Default to basics demo, can be overridden
ENV DEMO_MODULE="basics"
ENV DEMO_CLASS="com.hibernate.learning.basics.BasicsDemo"

CMD ["sh", "-c", "./wait-for-postgres.sh postgres java ${JAVA_OPTS} -cp ${CLASSPATH} ${DEMO_CLASS}"]
