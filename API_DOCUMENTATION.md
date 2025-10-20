# Activity Partner API Documentation

## Table of Contents
1. [Authentication](#authentication)
2. [Activity Messages API](#activity-messages-api)
3. [Notifications API](#notifications-api)
4. [Reports API](#reports-api)
5. [Data Models](#data-models)

---

## Authentication

### JWT Token Authentication
All API endpoints require JWT token authentication using Bearer token in the Authorization header.

**Header Format:**
```
Authorization: Bearer <your-jwt-token>
```

**Important Changes:**
- The API has migrated to token-based authentication (JWT)
- Firebase integration is available for push notifications
- All authenticated endpoints require the `Authorization` header

---

## Activity Messages API

Base Path: `/api/activities/{activityId}/messages`

### 1. Send Message
Send a message in an activity chat.

**Endpoint:** `POST /api/activities/{activityId}/messages`

**Authentication:** Required

**Request Body:**
```json
{
  "messageText": "Hello everyone!"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "activityId": 123,
  "userId": 456,
  "userName": "John Doe",
  "messageText": "Hello everyone!",
  "createdAt": "2024-01-20T10:30:00",
  "isDeleted": false
}
```

---

### 2. Get All Messages
Get all messages for an activity.

**Endpoint:** `GET /api/activities/{activityId}/messages`

**Authentication:** Required

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "activityId": 123,
    "userId": 456,
    "userName": "John Doe",
    "messageText": "Hello everyone!",
    "createdAt": "2024-01-20T10:30:00",
    "isDeleted": false
  }
]
```

---

### 3. Get Messages Since Timestamp
Get new messages since a specific timestamp (useful for polling).

**Endpoint:** `GET /api/activities/{activityId}/messages/since`

**Authentication:** Required

**Query Parameters:**
- `timestamp` (required): ISO 8601 datetime format (e.g., `2024-01-20T10:30:00`)

**Example:**
```
GET /api/activities/123/messages/since?timestamp=2024-01-20T10:30:00
```

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "activityId": 123,
    "userId": 789,
    "userName": "Jane Smith",
    "messageText": "See you there!",
    "createdAt": "2024-01-20T10:35:00",
    "isDeleted": false
  }
]
```

---

### 4. Get Message Count
Get the total message count for an activity.

**Endpoint:** `GET /api/activities/{activityId}/messages/count`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "messageCount": 42
}
```

---

### 5. Delete Message
Delete a message (soft delete).

**Endpoint:** `DELETE /api/activities/{activityId}/messages/{messageId}`

**Authentication:** Required (must be message owner)

**Response:** `200 OK`
```json
{
  "message": "Message deleted successfully"
}
```

---

## Notifications API

Base Path: `/api/notifications`

### 1. Register Device Token
Register or update FCM device token for push notifications.

**Endpoint:** `POST /api/notifications/device-token`

**Authentication:** Required

**Request Body:**
```json
{
  "fcmToken": "dXp4k3m2n5o8p1q6r9s2t5u8v1w4x7y0"
}
```

**Response:** `200 OK`
```json
{
  "message": "Device token registered successfully"
}
```

---

### 2. Update Notification Preferences
Enable or disable push notifications.

**Endpoint:** `PUT /api/notifications/preferences`

**Authentication:** Required

**Request Body:**
```json
{
  "notificationsEnabled": true
}
```

**Response:** `200 OK`
```json
{
  "message": "Notification preferences updated",
  "notificationsEnabled": "true"
}
```

---

### 3. Get All Notifications
Get all notifications for the current user.

**Endpoint:** `GET /api/notifications`

**Authentication:** Required

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "New Message",
    "message": "John Doe sent a message in 'Morning Run'",
    "type": "NEW_MESSAGE",
    "isRead": false,
    "activityId": 123,
    "participantId": null,
    "reviewId": null,
    "createdAt": "2024-01-20T10:30:00",
    "readAt": null
  }
]
```

---

### 4. Get Unread Notifications
Get only unread notifications.

**Endpoint:** `GET /api/notifications/unread`

**Authentication:** Required

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "New Message",
    "message": "John Doe sent a message in 'Morning Run'",
    "type": "NEW_MESSAGE",
    "isRead": false,
    "activityId": 123,
    "participantId": null,
    "reviewId": null,
    "createdAt": "2024-01-20T10:30:00",
    "readAt": null
  }
]
```

---

### 5. Get Unread Count
Get the count of unread notifications.

**Endpoint:** `GET /api/notifications/unread/count`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "unreadCount": 5
}
```

---

### 6. Mark Notification as Read
Mark a specific notification as read.

**Endpoint:** `PATCH /api/notifications/{id}/read`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "message": "Notification marked as read"
}
```

---

### 7. Mark All Notifications as Read
Mark all notifications as read for the current user.

**Endpoint:** `PATCH /api/notifications/read-all`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "message": "All notifications marked as read"
}
```

---

### 8. Delete Notification
Delete a notification.

**Endpoint:** `DELETE /api/notifications/{id}`

**Authentication:** Required

**Response:** `204 No Content`

---

## Reports API

Base Path: `/api/reports`

### 1. Submit Report
Submit a new report for an activity, message, or user.

**Endpoint:** `POST /api/reports`

**Authentication:** Required

**Request Body:**
```json
{
  "reportType": "ACTIVITY",
  "reportedActivityId": 123,
  "reportedMessageId": null,
  "reportedUserId": null,
  "reason": "Inappropriate content or spam"
}
```

**Report Types:**
- `ACTIVITY` - Report an activity (requires `reportedActivityId`)
- `MESSAGE` - Report a message (requires `reportedMessageId`)
- `USER` - Report a user (requires `reportedUserId`)

**Response:** `201 Created`
```json
{
  "id": 1,
  "reporterId": 456,
  "reporterName": "John Doe",
  "reportType": "ACTIVITY",
  "reportedActivityId": 123,
  "reportedMessageId": null,
  "reportedUserId": null,
  "reason": "Inappropriate content or spam",
  "status": "PENDING",
  "createdAt": "2024-01-20T10:30:00",
  "resolvedAt": null
}
```

---

### 2. Get My Reports
Get all reports submitted by the current user.

**Endpoint:** `GET /api/reports/my-reports`

**Authentication:** Required

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "reporterId": 456,
    "reporterName": "John Doe",
    "reportType": "ACTIVITY",
    "reportedActivityId": 123,
    "reportedMessageId": null,
    "reportedUserId": null,
    "reason": "Inappropriate content or spam",
    "status": "PENDING",
    "createdAt": "2024-01-20T10:30:00",
    "resolvedAt": null
  }
]
```

---

### 3. Get Pending Reports (Admin Only)
Get all pending reports.

**Endpoint:** `GET /api/reports/pending`

**Authentication:** Required (Admin role)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "reporterId": 456,
    "reporterName": "John Doe",
    "reportType": "ACTIVITY",
    "reportedActivityId": 123,
    "reportedMessageId": null,
    "reportedUserId": null,
    "reason": "Inappropriate content or spam",
    "status": "PENDING",
    "createdAt": "2024-01-20T10:30:00",
    "resolvedAt": null
  }
]
```

---

### 4. Get Reports by Type and Status (Admin Only)
Filter reports by type and status.

**Endpoint:** `GET /api/reports`

**Authentication:** Required (Admin role)

**Query Parameters:**
- `type` (required): Report type (`ACTIVITY`, `MESSAGE`, `USER`)
- `status` (required): Report status (`PENDING`, `REVIEWING`, `RESOLVED`, `DISMISSED`)

**Example:**
```
GET /api/reports?type=ACTIVITY&status=PENDING
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "reporterId": 456,
    "reporterName": "John Doe",
    "reportType": "ACTIVITY",
    "reportedActivityId": 123,
    "reportedMessageId": null,
    "reportedUserId": null,
    "reason": "Inappropriate content or spam",
    "status": "PENDING",
    "createdAt": "2024-01-20T10:30:00",
    "resolvedAt": null
  }
]
```

---

### 5. Update Report Status (Admin Only)
Update the status of a report.

**Endpoint:** `PATCH /api/reports/{reportId}/status`

**Authentication:** Required (Admin role)

**Query Parameters:**
- `status` (required): New status (`PENDING`, `REVIEWING`, `RESOLVED`, `DISMISSED`)

**Example:**
```
PATCH /api/reports/1/status?status=RESOLVED
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "reporterId": 456,
  "reporterName": "John Doe",
  "reportType": "ACTIVITY",
  "reportedActivityId": 123,
  "reportedMessageId": null,
  "reportedUserId": null,
  "reason": "Inappropriate content or spam",
  "status": "RESOLVED",
  "createdAt": "2024-01-20T10:30:00",
  "resolvedAt": "2024-01-20T15:45:00"
}
```

---

### 6. Get Activity Report Count
Get the number of reports for a specific activity.

**Endpoint:** `GET /api/reports/activity/{activityId}/count`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "reportCount": 3
}
```

---

### 7. Get Message Report Count
Get the number of reports for a specific message.

**Endpoint:** `GET /api/reports/message/{messageId}/count`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "reportCount": 1
}
```

---

## Data Models

### NotificationType Enum
```
ACTIVITY_CREATED           // New activity by someone you follow
ACTIVITY_UPDATED           // Activity you joined was updated
ACTIVITY_CANCELLED         // Activity you joined was cancelled
ACTIVITY_COMPLETED         // Activity you joined was completed
ACTIVITY_REMINDER          // Reminder before activity starts
PARTICIPANT_INTERESTED     // Someone expressed interest in your activity
PARTICIPANT_ACCEPTED       // Your interest was accepted
PARTICIPANT_DECLINED       // Your interest was declined
PARTICIPANT_JOINED         // Someone confirmed joining your activity
PARTICIPANT_LEFT           // Someone left your activity
REVIEW_RECEIVED           // You received a new review
NEW_MESSAGE               // New message in activity chat
REPORT_SUBMITTED          // Your report was submitted
REPORT_RESOLVED           // Report you submitted was resolved
BADGE_EARNED              // You earned a new badge
MILESTONE_REACHED         // Completed activities milestone
GENERAL                   // General notification
```

### ReportType Enum
```
ACTIVITY    // Report an activity
MESSAGE     // Report a message
USER        // Report a user
```

### ReportStatus Enum
```
PENDING     // Report submitted, awaiting review
REVIEWING   // Report is being reviewed by admin
RESOLVED    // Report has been resolved
DISMISSED   // Report was dismissed (no action taken)
```

---

## Error Responses

All endpoints may return the following error responses:

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid request data"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## CORS Configuration

All endpoints support CORS with `origins = "*"` for development purposes.

**Important:** For production, configure specific allowed origins in the Security Configuration.

---

## Frontend Integration Checklist

- [ ] Update API client to use JWT Bearer token authentication
- [ ] Implement activity messaging feature
- [ ] Implement notifications system with FCM integration
- [ ] Add report functionality for activities, messages, and users
- [ ] Add admin panel for reviewing reports
- [ ] Implement notification badge showing unread count
- [ ] Add polling or WebSocket for real-time message updates
- [ ] Handle all notification types with appropriate UI
- [ ] Implement notification preferences toggle
- [ ] Add error handling for all API endpoints
