# Eatclub Tech Challenge

A Java project using Maven for build management.

## Project Setup

1. Ensure you have the following installed:
   - Java 17 (LTS)
   - Maven 3.6 or later

2. Run the following commands to build the project:
   ```bash
   mvn clean install
   ```

## Assumptions

### Deal Timing
- If a deal's start/open or end/close time is missing, the deal is considered active during the restaurant's opening hours
- This ensures no deals are incorrectly excluded due to missing timing information

### Multiple Peak Time Slots
- The API may return multiple peak time slots if they have the same maximum number of active deals
- Response format: `["18:00–19:00", "21:00–22:00"]`
- This provides a complete view of all peak periods

### Caching
- No caching mechanism is currently implemented
- API calls are made for each request
- Future optimization could include caching to improve performance

### Time Inclusivity
- Time comparisons are inclusive of both start and end times
- A deal is considered active at both its start and end times
- This ensures consistent behavior for boundary cases
- Example: If a deal starts at 12:00 and ends at 12:30, it is considered active from 12:00 to 12:30 inclusive

### Deal Timing Must Honor Restaurant Operating Hours
- If deal duration exceeds restaurant operating hours, the deal is considered active during the restaurant's opening hours
- Example: If a deal starts at 12:00 and ends at 15:30 and restaurant operating hours are 14:00 to 22:30, then deal is considered active from 14:00 to 15:30 inclusive


## Project Structure
```
src/
├── main/
│   ├── java/          # Source code
│   └── resources/     # Resource files
└── test/
    ├── java/          # Test source code
    └── resources/     # Test resource files
```

## Building the Project

```bash
mvn clean package
```

## Running Tests

```bash
mvn test
```

## Local Testing with AWS SAM

### Prerequisites
- AWS SAM CLI installed
- Docker installed and running (required for local Lambda testing)
- AWS CLI configured with appropriate credentials
- Maven installed

### Setting Up Local Environment

1. **Install AWS SAM CLI**
   Follow the official AWS SAM CLI installation guide for your operating system:
   [AWS SAM CLI Installation Guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

2. **Install Docker**
   - Download and install Docker Desktop for your OS: [Docker Desktop](https://www.docker.com/products/docker-desktop)
   - Ensure Docker is running before starting local testing

### Running Tests Locally

1. **Build the application**
   ```bash
   mvn clean package
   ```

2. **Start the API locally**
   ```bash
   sam local start-api --warm-containers EAGER --template template.yaml --parameter-overrides RequireApiKeyForCloud='true'
   ```
   This will start a local API Gateway on `http://127.0.0.1:3000`

3. **Test the endpoints**
   - Get active deals:
     ```bash
     curl "http://127.0.0.1:3000/v1/restaurants/deals?timeOfDay=6:30pm"
     ```
   - Get peak times:
     ```bash
     curl http://127.0.0.1:3000/v1/restaurants/deals/peak-times
     ```

## AWS Deployment

### Prerequisites
- AWS CLI configured with appropriate credentials
- AWS SAM CLI installed
- Maven installed
- Java 17 (LTS) - As specified in the main prerequisites

### Deployment Steps

1. **Build the project**
   ```bash
   mvn clean package
   ```

2. **Package the application**
   ```bash
   sam package \
     --template-file template.yaml \
     --output-template-file packaged.yaml \
     --s3-bucket your-s3-bucket-name
   ```

3. **Deploy the application**
   ```bash
   sam deploy \
     --template-file packaged.yaml \
     --stack-name eatclub-restaurant-deals \
     --capabilities CAPABILITY_IAM \
     --region ap-southeast-2 \
     --parameter-overrides Environment=dev \
     --parameter-overrides RequireApiKeyForCloud='true'
   ```

4. **Verify the deployment**
   - Check the CloudFormation stack in AWS Console
   - Verify the API Gateway endpoints
   - Test the Lambda functions in the AWS Console

### API Endpoints

#### Task 1: Get Active Deals
```bash
curl -X GET -H "X-API-Key: xyrYAZTeJb297Wi95MuAM5OXIGYELRE87Ld0yhMB" "https://4bazj1l4vi.execute-api.ap-southeast-2.amazonaws.com/dev/v1/restaurants/deals?timeOfDay=8:32pm"
```

#### Task 2: Get Peak Times for Deals
```bash
curl -X GET -H "X-API-Key: xyrYAZTeJb297Wi95MuAM5OXIGYELRE87Ld0yhMB" "https://4bazj1l4vi.execute-api.ap-southeast-2.amazonaws.com/dev/v1/restaurants/deals/peak-times
```

### Cleanup
To remove all resources created by this deployment:
```bash
aws cloudformation delete-stack --stack-name eatclub-restaurant-deals --region ap-southeast-2
```

### Architecture
- **API Gateway**: Handles HTTP requests and routes to appropriate Lambda functions
- **Lambda**: Serverless functions for business logic
- **IAM**: Role-based permissions for secure access to AWS services
- **CloudFormation**: Infrastructure as Code for repeatable deployments

### Environment Variables
- `ENVIRONMENT`: Set to 'dev', 'staging', or 'prod' to control deployment stage

### Monitoring
- CloudWatch Logs for Lambda functions
- API Gateway access logs
- CloudWatch Alarms for error rates and throttling