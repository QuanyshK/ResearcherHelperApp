# Researcher App

The **Researcher App** is designed to make scientific research accessible to everyone. It allows users to find, summarize, and access scientific papers conveniently. This app is a test project created to provide an open pathway to global research articles across various fields of study.

## Features

### 1. Chat Page (Gemini API Integration)
- **Purpose**: Summarizes texts, links, and files (e.g., `.docx`, `.pdf`).
- **Daily Limits**: Up to 20 requests per user per day.
- **Backend**: Powered by Django and integrated with Gemini API.
- **Functionality**: Acts as a summarizer for research materials.

### 2. Article Search Page (Arxiv API Integration)
- **Purpose**: Searches for open-access articles.
- **Details View**: Users can read the abstract and download articles in PDF format.
- **PDF Viewer**: Built-in feature to read PDF files directly within the app.
- **Integration**: Connected to Arxiv API through a Kotlin implementation.

### 3. Hack Page (Sci-Hub Integration)
- **Purpose**: Provides access to paid articles, primarily those published before 2021.
- **Functionality**:
  - Users input a DOI link to retrieve and read the article in PDF format.
  - Includes a parser for HTML pages to access PDF files.
  - Built-in PDF Viewer for a seamless reading experience.
- **Backend**: Powered by Django.

### 4. User Pages (Login/Register/Profile)
- **Purpose**: Basic authentication system for user management.
- **Backend**: Django-based setup without advanced authentication mechanisms.
- **Profile Page**: Displays user information. Future plans include CRUD functionality for updating user details.

### Additional Features
- View the last 5 requests for all types of queries (Gemini, Arxiv, Sci-Hub).
- Unified API for login, registration, and Gemini integration.

## Future Enhancements
1. **Redesign**: Modernize the UI/UX of all app fragments.
2. **Architectural Improvements**: Optimize the overall app structure.
3. **New API Integration**:
   - Add Google Scholar API through Django for autonomous search and retrieval.
   - Streamline the process of finding paid articles and transitioning to the hack page for PDF access.
4. **Profile Enhancements**:
   - Add CRUD functions for users to update name, password, and email.
   - Implement authentication methods, such as:
     - Email-based OTP (One-Time Password).
     - Google Sign-In/Sign-Up.
5. **Session Persistence**:
   - Improve Android session handling to retain login status for up to 7 days or until manual logout.

## Technology Stack
- **Backend**: Django
- **Android**: Kotlin
- **APIs**:
  - Gemini API (summarizer)
  - Arxiv API (open-access article search)
  - Sci-Hub Integration (HTML parser for accessing paid articles)
- **Frontend Features**: PDF Viewer, user-friendly interface for article access.

---

This project is a step towards democratizing access to scientific research, enabling researchers and enthusiasts to explore knowledge without barriers.
