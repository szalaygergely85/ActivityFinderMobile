package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.util.DeviceLocationHelper;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive tests for participant application flow. Tests expressing interest, accepting,
 * declining, and removing participants.
 */
@RunWith(AndroidJUnit4.class)
public class ParticipantFlowTest {

    private TestApiHelper creatorApiHelper;
    private TestApiHelper participantApiHelper;

    private TestDataFactory.TestUser creatorUser;
    private TestDataFactory.TestUser participantUser;

    private Long creatorUserId;
    private Long participantUserId;

    private List<Long> createdActivityIds;

    @Before
    public void setUp() {
        creatorApiHelper = new TestApiHelper();
        participantApiHelper = new TestApiHelper();
        createdActivityIds = new ArrayList<>();

        // Get device location and set it in TestDataFactory
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // Create activity creator user
        creatorUser = TestDataFactory.createTestUser("Creator");
        LoginResponse creatorResponse =
                creatorApiHelper.createUser(
                        creatorUser.fullName,
                        creatorUser.email,
                        creatorUser.password,
                        creatorUser.birthDate);
        assertNotNull("Creator user creation should succeed", creatorResponse);
        creatorUserId = creatorResponse.getUserId();
        creatorApiHelper.waitShort();

        // Create participant user
        participantUser = TestDataFactory.createTestUser("Participant");
        LoginResponse participantResponse =
                participantApiHelper.createUser(
                        participantUser.fullName,
                        participantUser.email,
                        participantUser.password,
                        participantUser.birthDate);
        assertNotNull("Participant user creation should succeed", participantResponse);
        participantUserId = participantResponse.getUserId();
        participantApiHelper.waitShort();
    }

    @After
    public void tearDown() {
        // Cleanup activities
        for (Long activityId : createdActivityIds) {
            try {
                creatorApiHelper.deleteActivity(activityId, creatorUserId);
            } catch (Exception e) {
                // Ignore
            }
        }
        createdActivityIds.clear();

        // Cleanup participant user
        if (participantUserId != null) {
            try {
                participantApiHelper.clearSession();
                participantApiHelper.waitShort();
                participantApiHelper.loginWithRetry(participantUser.email, participantUser.password);
                participantApiHelper.deleteUser(participantUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Cleanup creator user
        if (creatorUserId != null) {
            try {
                creatorApiHelper.clearSession();
                creatorApiHelper.waitShort();
                creatorApiHelper.loginWithRetry(creatorUser.email, creatorUser.password);
                creatorApiHelper.deleteUser(creatorUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

        creatorApiHelper.clearSession();
        participantApiHelper.clearSession();
    }

    private Activity createTestActivity() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = creatorApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());
        creatorApiHelper.waitShort();
        return activity;
    }

    // ==================== EXPRESS INTEREST TESTS ====================

    @Test
    public void expressInterest_shouldCreatePendingParticipant() {
        Activity activity = createTestActivity();

        Participant participant = participantApiHelper.expressInterest(activity.getId());

        assertNotNull("Participant should be created", participant);
        assertNotNull("Participant should have ID", participant.getId());
        assertEquals(
                "Status should be PENDING",
                TestApiHelper.PARTICIPANT_STATUS_PENDING,
                participant.getStatus());
    }

    @Test
    public void expressInterest_twice_shouldFail() {
        Activity activity = createTestActivity();

        Participant first = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("First interest should succeed", first);

        participantApiHelper.waitShort();

        Participant second = participantApiHelper.expressInterest(activity.getId());
        // Backend should reject duplicate interest
        assertNull("Second interest should fail", second);
    }

    @Test
    public void expressInterest_onOwnActivity_shouldFail() {
        Activity activity = createTestActivity();

        // Creator tries to express interest on their own activity
        Participant participant = creatorApiHelper.expressInterest(activity.getId());

        assertNull("Cannot express interest on own activity", participant);
    }

    @Test
    public void expressInterest_onNonExistentActivity_shouldFail() {
        Participant participant = participantApiHelper.expressInterest(999999999L);

        assertNull("Interest on non-existent activity should fail", participant);
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Test
    public void acceptParticipant_shouldUpdateStatus() {
        Activity activity = createTestActivity();

        // Participant expresses interest
        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        // Creator accepts participant
        Participant accepted =
                creatorApiHelper.updateParticipantStatus(
                        pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        assertNotNull("Status update should succeed", accepted);
        assertEquals(
                "Status should be ACCEPTED",
                TestApiHelper.PARTICIPANT_STATUS_ACCEPTED,
                accepted.getStatus());
    }

    @Test
    public void declineParticipant_shouldUpdateStatus() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        Participant declined =
                creatorApiHelper.updateParticipantStatus(
                        pending.getId(), TestApiHelper.PARTICIPANT_STATUS_DECLINED);

        assertNotNull("Status update should succeed", declined);
        assertEquals(
                "Status should be DECLINED",
                TestApiHelper.PARTICIPANT_STATUS_DECLINED,
                declined.getStatus());
    }

    @Test
    public void removeParticipant_afterAccepting_shouldUpdateStatus() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        // First accept
        Participant accepted =
                creatorApiHelper.updateParticipantStatus(
                        pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);
        assertNotNull("Accept should succeed", accepted);

        creatorApiHelper.waitShort();

        // Then remove
        Participant removed =
                creatorApiHelper.updateParticipantStatus(
                        pending.getId(), TestApiHelper.PARTICIPANT_STATUS_REMOVED);

        assertNotNull("Remove should succeed", removed);
        assertEquals(
                "Status should be REMOVED",
                TestApiHelper.PARTICIPANT_STATUS_REMOVED,
                removed.getStatus());
    }

    @Test
    public void updateStatus_byNonCreator_shouldFail() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        participantApiHelper.waitShort();

        // Participant tries to accept themselves (should fail)
        Participant result =
                participantApiHelper.updateParticipantStatus(
                        pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        assertNull("Non-creator should not be able to update status", result);
    }

    // ==================== GET PARTICIPANTS TESTS ====================

    @Test
    public void getActivityParticipants_shouldReturnList() {
        Activity activity = createTestActivity();

        // Express interest
        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        List<Participant> participants =
                creatorApiHelper.getActivityParticipants(activity.getId());

        assertNotNull("Participants list should not be null", participants);
        assertTrue("Should have at least one participant", participants.size() >= 1);
    }

    @Test
    public void getInterestedUsers_shouldReturnPendingParticipants() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        List<Participant> interested = creatorApiHelper.getInterestedUsers(activity.getId());

        assertNotNull("Interested users list should not be null", interested);
        assertTrue("Should have pending participants", interested.size() >= 1);

        // Verify all are PENDING
        for (Participant p : interested) {
            assertEquals(
                    "All should be PENDING", TestApiHelper.PARTICIPANT_STATUS_PENDING, p.getStatus());
        }
    }

    @Test
    public void getMyParticipations_shouldReturnUserActivities() {
        Activity activity = createTestActivity();

        // Express interest and get accepted
        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        creatorApiHelper.updateParticipantStatus(
                pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        participantApiHelper.waitShort();

        List<Participant> participations = participantApiHelper.getMyParticipations();

        assertNotNull("Participations list should not be null", participations);
        assertTrue("Should have at least one participation", participations.size() >= 1);
    }

    // ==================== LEAVE ACTIVITY TESTS ====================

    @Test
    public void leaveActivity_afterAccepted_shouldSucceed() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        // Accept participant
        creatorApiHelper.updateParticipantStatus(
                pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        participantApiHelper.waitShort();

        // Participant leaves
        boolean left = participantApiHelper.leaveActivity(activity.getId());

        assertTrue("Leave should succeed", left);
    }

    @Test
    public void leaveActivity_whenPending_shouldSucceed() {
        Activity activity = createTestActivity();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        participantApiHelper.waitShort();

        // Leave while still pending
        boolean left = participantApiHelper.leaveActivity(activity.getId());

        assertTrue("Leave while pending should succeed", left);
    }

    // ==================== SPOTS TESTS ====================

    @Test
    public void acceptParticipant_shouldDecreaseAvailableSpots() {
        ActivityCreateRequest request =
                TestDataFactory.createActivity(
                        TestDataFactory.uniqueActivityTitle("SpotsTest"),
                        TestDataFactory.CATEGORY_SPORTS,
                        5); // 5 total spots
        Activity activity = creatorApiHelper.createActivity(request);
        assertNotNull("Activity should be created", activity);
        createdActivityIds.add(activity.getId());

        int initialSpots = activity.getAvailableSpots();

        creatorApiHelper.waitShort();

        Participant pending = participantApiHelper.expressInterest(activity.getId());
        assertNotNull("Participant should be created", pending);

        creatorApiHelper.waitShort();

        creatorApiHelper.updateParticipantStatus(
                pending.getId(), TestApiHelper.PARTICIPANT_STATUS_ACCEPTED);

        creatorApiHelper.waitShort();

        // Fetch activity again
        Activity updatedActivity = creatorApiHelper.getActivity(activity.getId());
        assertNotNull("Activity should be fetched", updatedActivity);

        assertEquals(
                "Available spots should decrease by 1",
                Integer.valueOf(initialSpots - 1),
                updatedActivity.getAvailableSpots());
    }
}
