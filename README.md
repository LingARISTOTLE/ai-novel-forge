# AI Novel Forge

AI Novel Forge is a web-based tool designed to assist novelists in creating their stories using the power of AI.

## Project Structure

The project is divided into two main components:

- **frontend**: A Vue.js 3 + Vite application providing the user interface for the novel creation tool.
- **backend**: A Java Spring Boot application providing the API and business logic.

## Getting Started

### Prerequisites

- Node.js (v16+)
- Java Development Kit (JDK 8+)
- Maven (Optional, wrapper can be used)

### Backend Setup

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   (Note: You may need to make `mvnw` executable first: `chmod +x mvnw`)
   If `mvnw` is not present, use your local maven installation: `mvn spring-boot:run`

The backend API will be available at `http://localhost:8080`.

### Frontend Setup

1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173`.

## Features

- **Story Management**: Create and organize your novels.
- **AI Assistance**: Generate plot ideas, character descriptions, and more.
- **Rich Text Editor**: Write your chapters with a comfortable editor.