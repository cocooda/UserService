# Use an official Java runtime as a base image
FROM openjdk:22-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the .env file into the container
COPY .env /app/.env
RUN chmod 600 /app/.env  
# Set read/write permissions for the owner only

# Copy the compiled jar file from your local machine to the container
COPY target/vi_finance_news-1.0-SNAPSHOT.jar /app/vi_finance_news.jar

# Expose the port your app runs on
EXPOSE 7000

# Run the application
CMD ["java", "-jar", "vi_finance_news.jar"]