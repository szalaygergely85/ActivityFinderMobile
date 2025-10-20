# Activity Partner Backend API Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Configuration](#configuration)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)

---

## Project Overview

**Activity Partner** is a Spring Boot REST API for managing activities and connecting users with similar interests.

- **Base URL**: `http://localhost:8080`
- **API Prefix**: `/api`
- **Java Version**: 17
- **Spring Boot**: 3.5.6
- **Database**: MySQL

---

## Configuration

### Database
```properties
URL: jdbc:mysql://localhost:3306/activitypartner
Username: root
Password: password
```

### JWT Settings
```properties
Access Token Expiration: 24 hours (86400000 ms)
Refresh Token Expiration: 7 days (604800000 ms)
```

---

## Authentication

### JWT Token-Based Authentication

**Login Flow:**
1. POST `/api/users/login` with email/password
2. Receive `accessToken` and `refreshToken` in response
3. Include access token in subsequent requests (when JWT auth is enabled)
4. When access token expires, use `/api/users/refresh-token` to get new access token
5. Call `/api/users/logout` to invalidate refresh token

**Token Response Structure:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "profileImageUrl": "https://...",
  "rating": 4.5,
  "badge": "⭐"
}
```

---

## API Endpoints

### User Management (`/api/users`)

#### POST `/api/users/login`
Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `LoginResponse` (see above)

---

#### POST `/api/users/register`
Register a new user.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "email": "user@example.com",
  "bio": null,
  "profileImageUrl": null,
  "rating": 0.0,
  "completedActivities": 0,
  "interests": [],
  "badge": null,
  "createdAt": "2024-10-18T10:30:00"
}
```

---

#### POST `/api/users/refresh-token`
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response:** `LoginResponse`

---

#### POST `/api/users/logout?userId={userId}`
Logout from current device (invalidates refresh token).

**Response:** 204 No Content

---

#### POST `/api/users/logout-all/{userId}`
Logout from all devices.

**Response:** 204 No Content

---

#### GET `/api/users/`
Get all active users.

**Response:** Array of `UserResponse`

---

#### GET `/api/users/{id}`
Get user by ID.

**Response:** `UserResponse`

---

#### GET `/api/users/email/{email}`
Get user by email.

**Response:** `UserResponse`

---

#### GET `/api/users/search?name={name}`
Search users by name (partial match).

**Response:** Array of `UserResponse`

---

#### GET `/api/users/interest/{interest}`
Get users with specific interest.

**Response:** Array of `UserSimpleResponse`

---

#### GET `/api/users/top-rated`
Get users sorted by rating (highest first).

**Response:** Array of `UserSimpleResponse`

---

#### PUT `/api/users/{id}`
Update user profile.

**Request Body:**
```json
{
  "fullName": "John Updated",
  "bio": "I love outdoor activities!",
  "profileImageUrl": "https://example.com/image.jpg",
  "interests": ["hiking", "cycling", "photography"]
}
```

**Response:** `UserResponse`

---

#### DELETE `/api/users/{id}`
Deactivate user account.

**Response:** 204 No Content

---

### Activity Management (`/api/activities`)

#### POST `/api/activities?creatorId={userId}`
Create a new activity.

**Request Body:**
```json
{
  "title": "Morning Hike at Mt. Rainier",
  "description": "Join us for a scenic morning hike!",
  "activityDate": "2024-10-25T08:00:00",
  "location": "Mt. Rainier National Park",
  "category": "Hiking",
  "totalSpots": 10,
  "reservedForFriendsSpots": 2,
  "minParticipants": 5,
  "difficulty": "Moderate",
  "cost": 25.00,
  "minAge": 18
}
```

**Response:**
```json
{
  "id": 1,
  "title": "Morning Hike at Mt. Rainier",
  "description": "Join us for a scenic morning hike!",
  "activityDate": "2024-10-25T08:00:00",
  "location": "Mt. Rainier National Park",
  "category": "Hiking",
  "totalSpots": 10,
  "reservedForFriendsSpots": 2,
  "minParticipants": 5,
  "status": "OPEN",
  "trending": false,
  "difficulty": "Moderate",
  "cost": 25.00,
  "minAge": 18,
  "creatorId": 1,
  "creatorName": "John Doe",
  "creatorImageUrl": "https://...",
  "participantsCount": 0,
  "availableSpots": 10,
  "createdAt": "2024-10-18T10:30:00",
  "updatedAt": "2024-10-18T10:30:00"
}
```

---

#### GET `/api/activities/`
Get all activities.

**Response:** Array of `ActivityResponseDTO`

---

#### GET `/api/activities/{id}`
Get activity by ID.

**Response:** `ActivityResponseDTO`

---

#### GET `/api/activities/creator/{creatorId}`
Get all activities created by a user.

**Response:** Array of `ActivityResponseDTO`

---

#### GET `/api/activities/category/{category}`
Get activities by category.

**Response:** Array of `ActivityResponseDTO`

---

#### GET `/api/activities/upcoming`
Get all available upcoming activities (status=OPEN, future dates).

**Response:** Array of `ActivityResponseDTO`

---

#### GET `/api/activities/trending`
Get trending activities.

**Response:** Array of `ActivityResponseDTO`

---

#### PATCH `/api/activities/{id}?userId={userId}`
Update activity (creator only).

**Request Body:** (all fields optional)
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "totalSpots": 15
}
```

**Response:** `ActivityResponseDTO`

---

#### PATCH `/api/activities/{id}/cancel?userId={userId}`
Cancel activity (creator only).

**Response:** `ActivityResponseDTO` with status=CANCELLED

---

#### PATCH `/api/activities/{id}/complete?userId={userId}`
Mark activity as completed (creator only).

**Response:** `ActivityResponseDTO` with status=COMPLETED

---

#### DELETE `/api/activities/{id}?userId={userId}`
Delete activity (creator only).

**Response:** 204 No Content

---

### Category Management (`/api/categories`)

#### POST `/api/categories`
Create a new category (admin only).

**Request Body:**
```json
{
  "name": "Hiking",
  "description": "Outdoor hiking activities",
  "icon": "🥾"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Hiking",
  "description": "Outdoor hiking activities",
  "icon": "🥾",
  "isActive": true,
  "activityCount": 0,
  "createdAt": "2024-10-18T10:30:00"
}
```

---

#### GET `/api/categories/`
Get all active categories.

**Response:** Array of `CategoryResponse`

---

#### GET `/api/categories/popular`
Get categories sorted by activity count.

**Response:** Array of `CategoryResponse`

---

#### GET `/api/categories/{id}`
Get category by ID.

**Response:** `CategoryResponse`

---

#### PUT `/api/categories/{id}`
Update category (admin only).

**Request Body:**
```json
{
  "name": "Mountain Hiking",
  "description": "Updated description",
  "icon": "⛰️",
  "isActive": true
}
```

**Response:** `CategoryResponse`

---

#### DELETE `/api/categories/{id}`
Deactivate category (admin only).

**Response:** 204 No Content

---

### Activity Participation (`/api/participants`)

#### POST `/api/participants/activities/{activityId}/interest?userId={userId}`
Express interest in an activity.

**Response:**
```json
{
  "id": 1,
  "activityId": 1,
  "activityTitle": "Morning Hike at Mt. Rainier",
  "user": {
    "id": 2,
    "fullName": "Jane Smith",
    "profileImageUrl": "https://...",
    "rating": 4.5,
    "badge": "⭐"
  },
  "status": "INTERESTED",
  "isFriend": false,
  "joinedAt": "2024-10-18T10:30:00",
  "updatedAt": "2024-10-18T10:30:00"
}
```

---

#### GET `/api/participants/activities/{activityId}`
Get all participants for an activity.

**Response:** Array of `ParticipantResponse`

---

#### GET `/api/participants/activities/{activityId}/interested?creatorId={userId}`
Get interested users for an activity (creator only).

**Response:** Array of `ParticipantResponse` with status=INTERESTED

---

#### GET `/api/participants/my-participations?userId={userId}`
Get all participations for a user.

**Response:** Array of `ParticipantActivityResponse`

---

#### GET `/api/participants/my-participations/status/{status}?userId={userId}`
Get user's participations by status (INTERESTED, ACCEPTED, JOINED, etc.).

**Response:** Array of `ParticipantActivityResponse`

---

#### PATCH `/api/participants/{participantId}/status?creatorId={userId}`
Update participant status (creator only).

**Request Body:**
```json
{
  "status": "ACCEPTED"
}
```

**Valid statuses:** `INTERESTED`, `ACCEPTED`, `DECLINED`, `JOINED`, `LEFT`

**Response:** `ParticipantResponse`

---

#### POST `/api/participants/{participantId}/confirm?userId={userId}`
Confirm joining after being accepted (changes status from ACCEPTED to JOINED).

**Response:** `ParticipantResponse`

---

#### DELETE `/api/participants/activities/{activityId}/leave?userId={userId}`
Leave an activity.

**Response:** 204 No Content

---

#### DELETE `/api/participants/activities/{activityId}/interest?userId={userId}`
Delete interest before acceptance.

**Response:** 204 No Content

---

### Review Management (`/api/reviews`)

#### POST `/api/reviews?reviewerId={userId}`
Create a review for a user after an activity.

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Great hiking partner! Very punctual and friendly.",
  "activityId": 1,
  "reviewedUserId": 2
}
```

**Response:**
```json
{
  "id": 1,
  "rating": 5,
  "comment": "Great hiking partner! Very punctual and friendly.",
  "activityId": 1,
  "reviewer": {
    "id": 1,
    "fullName": "John Doe",
    "profileImageUrl": "https://...",
    "rating": 4.5,
    "badge": "⭐"
  },
  "reviewedUser": {
    "id": 2,
    "fullName": "Jane Smith",
    "profileImageUrl": "https://...",
    "rating": 4.8,
    "badge": "👑"
  },
  "createdAt": "2024-10-18T10:30:00"
}
```

---

#### GET `/api/reviews/user/{userId}`
Get all reviews received by a user.

**Response:** Array of `ReviewResponse`

---

#### GET `/api/reviews/reviewer/{reviewerId}`
Get all reviews written by a user.

**Response:** Array of `ReviewResponse`

---

#### GET `/api/reviews/activity/{activityId}`
Get all reviews for an activity.

**Response:** Array of `ReviewResponse`

---

#### PUT `/api/reviews/{id}?reviewerId={userId}`
Update a review (reviewer only).

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Updated review text"
}
```

**Response:** `ReviewResponse`

---

#### DELETE `/api/reviews/{id}?reviewerId={userId}`
Delete a review (reviewer only).

**Response:** 204 No Content

---

## Data Models

### User
```typescript
{
  id: number
  fullName: string           // 2-100 chars
  email: string              // valid email, unique
  bio?: string              // max 500 chars
  profileImageUrl?: string
  rating: number            // 0.0-5.0, average from reviews
  completedActivities: number
  interests: string[]       // user interests
  badge?: string           // special badges (⭐, 👑, 🏔️, 💎)
  createdAt: string        // ISO datetime
}
```

### Activity
```typescript
{
  id: number
  title: string                    // 3-100 chars
  description?: string            // max 1000 chars
  activityDate: string            // ISO datetime, must be future
  location: string
  category: string
  totalSpots: number              // 1-100
  reservedForFriendsSpots?: number
  minParticipants?: number
  status: "OPEN" | "FULL" | "CANCELLED" | "COMPLETED"
  trending: boolean
  difficulty?: "Easy" | "Moderate" | "Hard"
  cost?: number                   // default 0.0
  minAge?: number                 // 0-100
  creatorId: number
  creatorName: string
  creatorImageUrl?: string
  participantsCount: number       // current joined count
  availableSpots: number          // computed
  createdAt: string
  updatedAt: string
}
```

### Category
```typescript
{
  id: number
  name: string                 // unique
  description?: string        // max 200 chars
  icon?: string              // max 10 chars (emoji)
  isActive: boolean
  activityCount: number      // denormalized count
  createdAt: string
}
```

### Participant
```typescript
{
  id: number
  activityId: number
  activityTitle: string
  user: UserSimpleResponse
  status: "INTERESTED" | "ACCEPTED" | "DECLINED" | "JOINED" | "LEFT"
  isFriend: boolean
  joinedAt: string
  updatedAt: string
}
```

### Review
```typescript
{
  id: number
  rating: number              // 1-5
  comment?: string           // max 500 chars
  activityId: number
  reviewer: UserSimpleResponse
  reviewedUser: UserSimpleResponse
  createdAt: string
}
```

### UserSimpleResponse (Lightweight)
```typescript
{
  id: number
  fullName: string
  profileImageUrl?: string
  rating: number
  badge?: string
}
```

---

## Error Handling

### Error Response Format
```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2024-10-18T10:30:00.123456"
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful deletion/update) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (invalid credentials) |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 500 | Internal Server Error |

### Common Error Scenarios

**404 - Resource Not Found**
```json
{
  "status": 404,
  "message": "User not found with id: 123",
  "timestamp": "2024-10-18T10:30:00"
}
```

**409 - Duplicate Resource**
```json
{
  "status": 409,
  "message": "User already exists with email: user@example.com",
  "timestamp": "2024-10-18T10:30:00"
}
```

**400 - Validation Error**
```json
{
  "status": 400,
  "message": "fullName: must not be blank; email: must be a valid email",
  "timestamp": "2024-10-18T10:30:00"
}
```

**401 - Invalid Credentials**
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2024-10-18T10:30:00"
}
```

---

## Important Notes

### Current Authentication Status
- JWT infrastructure is implemented but **not yet enforced**
- Most endpoints currently accept `userId`, `creatorId`, or `reviewerId` as query parameters
- **TODO**: Implement JWT authentication filter to extract user from token

### Participant Status Flow
```
INTERESTED (user expresses interest)
    ↓
ACCEPTED (creator accepts)
    ↓
JOINED (user confirms participation)
    ↓
LEFT (user leaves) OR activity completes
```

### Activity Status Flow
```
OPEN (accepting participants)
    ↓
FULL (all spots taken) OR CANCELLED (creator cancels)
    ↓
COMPLETED (after activity date)
```

### Validation Rules
- Email must be unique across users
- Category name must be unique
- One review per (reviewer, reviewedUser, activity) combination
- One participation per (user, activity) combination
- Activity date must be in the future
- Total spots must be between 1-100
- Rating must be between 1-5

### CORS Configuration
- Currently allows all origins (`*`)
- Should be restricted to frontend domain in production

---

## Example Integration (Frontend)

### Login Example (JavaScript/TypeScript)
```typescript
async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const data = await response.json();

  // Store tokens
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('userId', data.userId);

  return data;
}
```

### Create Activity Example
```typescript
async function createActivity(activityData: any, userId: number) {
  const response = await fetch(
    `http://localhost:8080/api/activities?creatorId=${userId}`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(activityData)
    }
  );

  return await response.json();
}
```

### Get Upcoming Activities Example
```typescript
async function getUpcomingActivities() {
  const response = await fetch('http://localhost:8080/api/activities/upcoming');
  return await response.json();
}
```

---

## Database Schema Summary

### Tables
- `users` - User accounts and profiles
- `activities` - Activity listings
- `categories` - Activity categories
- `activity_participants` - User-activity participation tracking
- `reviews` - User reviews after activities
- `refresh_tokens` - JWT refresh token storage
- `user_interests` - User interests (element collection)
- `spring_session` - Session management (JDBC store)
- `spring_session_attributes` - Session attributes

### Key Relationships
- User ← (1:N) → Activity (creator)
- User ← (1:N) → ActivityParticipant
- Activity ← (1:N) → ActivityParticipant
- User ← (1:N) → Review (as reviewer)
- User ← (1:N) → Review (as reviewedUser)
- User ← (1:N) → RefreshToken

---

## Contact & Support

For issues or questions about this API, please refer to the backend repository or contact the development team.

**Last Updated**: October 18, 2024
