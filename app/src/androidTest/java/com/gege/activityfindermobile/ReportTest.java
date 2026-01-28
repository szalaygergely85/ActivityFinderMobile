package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.model.ActivityMessage;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.model.Report;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive tests for reporting functionality. Tests reporting activities, users, and
 * messages.
 */
@RunWith(AndroidJUnit4.class)
public class ReportTest {

    private TestApiHelper reporterApiHelper;
    private TestApiHelper targetApiHelper;

    private TestDataFactory.TestUser reporterUser;
    private TestDataFactory.TestUser targetUser;

    private Long reporterUserId;
    private Long targetUserId;

    private List<Long> createdActivityIds;

    @Before
    public void setUp() {
        reporterApiHelper = new TestApiHelper();
        targetApiHelper = new TestApiHelper();
        createdActivityIds = new ArrayList<>();

        // Create reporter user
        reporterUser = TestDataFactory.createTestUser("Reporter");
        LoginResponse reporterResponse =
                reporterApiHelper.createUser(
                        reporterUser.fullName,
                        reporterUser.email,
                        reporterUser.password,
                        reporterUser.birthDate);
        assertNotNull("Reporter user creation should succeed", reporterResponse);
        reporterUserId = reporterResponse.getUserId();
        reporterApiHelper.waitShort();

        // Create target user (creates activities, sends messages)
        targetUser = TestDataFactory.createTestUser("Target");
        LoginResponse targetResponse =
                targetApiHelper.createUser(
                        targetUser.fullName,
                        targetUser.email,
                        targetUser.password,
                        targetUser.birthDate);
        assertNotNull("Target user creation should succeed", targetResponse);
        targetUserId = targetResponse.getUserId();
        targetApiHelper.waitShort();
    }

    @After
    public void tearDown() {
        // Cleanup activities
        for (Long activityId : createdActivityIds) {
            try {
                targetApiHelper.deleteActivity(activityId, targetUserId);
            } catch (Exception e) {
                // Ignore
            }
        }
        createdActivityIds.clear();

        // Cleanup reporter user
        if (reporterUserId != null) {
            try {
                reporterApiHelper.clearSession();
                reporterApiHelper.waitShort();
                reporterApiHelper.loginWithRetry(reporterUser.email, reporterUser.password);
                reporterApiHelper.deleteUser(reporterUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Cleanup target user
        if (targetUserId != null) {
            try {
                targetApiHelper.clearSession();
                targetApiHelper.waitShort();
                targetApiHelper.loginWithRetry(targetUser.email, targetUser.password);
                targetApiHelper.deleteUser(targetUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

        reporterApiHelper.clearSession();
        targetApiHelper.clearSession();
    }

    // ==================== ACTIVITY REPORT TESTS ====================

    @Test
    public void reportActivity_withValidReason_shouldSucceed() {
        // Target creates an activity
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        // Reporter reports the activity
        Report report =
                reporterApiHelper.reportActivity(
                        activity.getId(), TestDataFactory.REPORT_REASON_INAPPROPRIATE);

        assertNotNull("Report should be created", report);
        assertNotNull("Report should have ID", report.getId());
        assertEquals("Report type should be ACTIVITY", "ACTIVITY", report.getReportType());
        assertEquals(
                "Reported activity ID should match", activity.getId(), report.getReportedActivityId());
        assertEquals(
                "Reason should match",
                TestDataFactory.REPORT_REASON_INAPPROPRIATE,
                report.getReason());
    }

    @Test
    public void reportActivity_withUniqueReason_shouldSucceed() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        String uniqueReason = TestDataFactory.uniqueReportReason();
        Report report = reporterApiHelper.reportActivity(activity.getId(), uniqueReason);

        assertNotNull("Report should be created", report);
        assertEquals("Reason should match", uniqueReason, report.getReason());
    }

    @Test
    public void reportActivity_nonExistent_shouldFail() {
        Report report =
                reporterApiHelper.reportActivity(999999999L, TestDataFactory.REPORT_REASON_SPAM);

        assertNull("Report on non-existent activity should fail", report);
    }

    @Test
    public void reportActivity_ownActivity_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        // Target tries to report their own activity
        Report report =
                targetApiHelper.reportActivity(activity.getId(), TestDataFactory.REPORT_REASON_SPAM);

        assertNull("Cannot report own activity", report);
    }

    // ==================== USER REPORT TESTS ====================

    @Test
    public void reportUser_withValidReason_shouldSucceed() {
        Report report =
                reporterApiHelper.reportUser(
                        targetUserId, TestDataFactory.REPORT_REASON_HARASSMENT);

        assertNotNull("Report should be created", report);
        assertNotNull("Report should have ID", report.getId());
        assertEquals("Report type should be USER", "USER", report.getReportType());
        assertEquals("Reported user ID should match", targetUserId, report.getReportedUserId());
    }

    @Test
    public void reportUser_nonExistent_shouldFail() {
        Report report =
                reporterApiHelper.reportUser(999999999L, TestDataFactory.REPORT_REASON_FAKE);

        assertNull("Report on non-existent user should fail", report);
    }

    @Test
    public void reportUser_self_shouldFail() {
        Report report =
                reporterApiHelper.reportUser(
                        reporterUserId, TestDataFactory.REPORT_REASON_HARASSMENT);

        assertNull("Cannot report self", report);
    }

    // ==================== MESSAGE REPORT TESTS ====================

    @Test
    public void reportMessage_withValidReason_shouldSucceed() {
        // Create activity and add reporter as participant
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        // Reporter expresses interest and gets accepted
        Participant participant = reporterApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", participant);

        targetApiHelper.waitShort();

        targetApiHelper.updateParticipantStatus(
                participant.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        targetApiHelper.waitShort();

        // Target sends a message
        ActivityMessage message =
                targetApiHelper.sendMessage(
                        activity.getId(), TestDataFactory.uniqueMessageContent());
        assertNotNull("Message should be sent", message);

        targetApiHelper.waitShort();

        // Reporter reports the message
        Report report =
                reporterApiHelper.reportMessage(
                        message.getId(), TestDataFactory.REPORT_REASON_INAPPROPRIATE);

        assertNotNull("Report should be created", report);
        assertEquals("Report type should be MESSAGE", "MESSAGE", report.getReportType());
        assertEquals(
                "Reported message ID should match", message.getId(), report.getReportedMessageId());
    }

    // ==================== GET MY REPORTS TESTS ====================

    @Test
    public void getMyReports_afterReporting_shouldContainReport() {
        // Create and report an activity
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        Report report =
                reporterApiHelper.reportActivity(
                        activity.getId(), TestDataFactory.uniqueReportReason());
        assertNotNull("Report should be created", report);

        reporterApiHelper.waitShort();

        // Get my reports
        List<Report> myReports = reporterApiHelper.getMyReports();

        assertNotNull("My reports should not be null", myReports);
        assertTrue("Should have at least one report", myReports.size() >= 1);

        // Verify our report is in the list
        boolean found = false;
        for (Report r : myReports) {
            if (r.getId().equals(report.getId())) {
                found = true;
                break;
            }
        }
        assertTrue("Our report should be in the list", found);
    }

    @Test
    public void getMyReports_forNewUser_shouldReturnEmptyList() {
        List<Report> myReports = reporterApiHelper.getMyReports();

        assertNotNull("My reports should not be null", myReports);
        assertEquals("New user should have no reports", 0, myReports.size());
    }

    // ==================== REPORT COUNT TESTS ====================

    @Test
    public void getActivityReportCount_afterReporting_shouldIncrease() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        int initialCount = reporterApiHelper.getActivityReportCount(activity.getId());
        assertTrue("Initial count should be >= 0", initialCount >= 0);

        // Report the activity
        Report report =
                reporterApiHelper.reportActivity(
                        activity.getId(), TestDataFactory.uniqueReportReason());
        assertNotNull("Report should be created", report);

        reporterApiHelper.waitShort();

        int newCount = reporterApiHelper.getActivityReportCount(activity.getId());
        assertEquals("Count should increase by 1", initialCount + 1, newCount);
    }

    // ==================== REPORT STATUS TESTS ====================

    @Test
    public void report_shouldHavePendingStatus() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        Report report =
                reporterApiHelper.reportActivity(
                        activity.getId(), TestDataFactory.uniqueReportReason());

        assertNotNull("Report should be created", report);
        assertEquals("New report should have PENDING status", "PENDING", report.getStatus());
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    public void submitReport_withoutAuthentication_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = targetApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        targetApiHelper.waitShort();

        // Clear session
        reporterApiHelper.clearSession();

        Report report =
                reporterApiHelper.reportActivity(
                        activity.getId(), TestDataFactory.REPORT_REASON_SPAM);

        assertNull("Report without auth should fail", report);

        // Re-login for cleanup
        reporterApiHelper.login(reporterUser.email, reporterUser.password);
    }

    @Test
    public void getMyReports_withoutAuthentication_shouldFail() {
        reporterApiHelper.clearSession();

        List<Report> reports = reporterApiHelper.getMyReports();

        assertNull("Get reports without auth should fail", reports);

        // Re-login for cleanup
        reporterApiHelper.login(reporterUser.email, reporterUser.password);
    }
}
