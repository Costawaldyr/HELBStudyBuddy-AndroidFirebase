package com.example.studybuddy.utils;

/**
 * Global constants for the StudyBuddy application.
 */
public final class MyConstants
{
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;
    public static final int EIGHT =8;
    public static final int SIXTEEN = EIGHT + 8;
    public static final int TEN = 10;
    public static final int NEGATIVE_ONE = -1;

    public static final int MAX_RESPONSE_TOKENS = 500;
    public static final int MAX_API_RETRIES = 3;
    public static final int RETRY_DELAY_MS = 2000;
    public static final int HTTP_STATUS_OVERLOADED = 529;
    public static final int FIRST_CONTENT_INDEX = 0;
    public static final int SPLASH_DURATION_MS = 3000;
    public static final int NOTIFICATION_WORK_INTERVAL_HOURS = 6;
    public static final int MATCHING_WORK_INTERVAL_HOURS = 3;
    public static final int MAX_DASHBOARD_CANDIDATES = 4;

    public static final long REFRESH_INTERVAL_MS = 60000L;
    public static final long UI_REFRESH_DELAY_MS = 2000L;
    public static final long MATCH_CREATION_DELAY_MS = 1500L;
    public static final long ONE_DAY_MS = 24 * 60 * 60 * 1000L;
    public static final long ONE_HOUR_MS = 3600000L;
    public static final long TIMER_INTERVAL_CHAT_MS = 60000L;
    public static final int TIMER_INTERVAL_MS = 30;
    public static final long MATCH_DURATION_MS = 24 * ONE_HOUR_MS;
    public static final long SIXTY = 60;
    public static final long THOUSAND = 1000;

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_BUDDIES = "buddies";
    public static final String COLLECTION_BUDDIES_AI = "buddies_ai";
    public static final String COLLECTION_CONVERSATIONS = "conversations";
    public static final String COLLECTION_MESSAGES = "messages";
    public static final String COLLECTION_SAVED = "saved_messages";
    public static final String COLLECTION_MATCHES = "matches";
    public static final String COLLECTION_CHATS = "chats";
    public static final String COLLECTION_REJECTED = "rejected_matches";

    public static final String FIELD_NAME = "name";
    public static final String FIELD_COURSE_NAME = "courseName";
    public static final String FIELD_SYSTEM_PROMPT = "systemPrompt";
    public static final String FIELD_COURSE_EMOJI = "courseEmoji";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_SENDER_ID = "senderId";
    public static final String FIELD_PROFILE_IMAGE = "profileImageUrl";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_ACTIVE = "isActive";
    public static final String FIELD_USER_ID_1 = "userId1";
    public static final String FIELD_USER_ID_2 = "userId2";
    public static final String FIELD_INITIATOR_ID = "initiatorId";
    public static final String FIELD_RECEIVER_NAME = "receiverName";
    public static final String FIELD_MATCHED_AT = "matchedAt";
    public static final String FIELD_EXPIRES_AT = "expiresAt";
    public static final String FIELD_SCHOOL = "school";
    public static final String FIELD_SUBJECTS = "subjects";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_REJECTED_AT = "rejectedAt";
    public static final String FIELD_PARTICIPANT_ID = "participantId";
    public static final String FIELD_PARTICIPANT_NAME = "participantName";
    public static final String FIELD_PARTICIPANT_IMAGE = "participantImage";
    public static final String FIELD_PARTICIPANT_EMOJI = "participantEmoji";
    public static final String FIELD_PARTICIPANT_COLOR = "participantColor";
    public static final String FIELD_HAS_MESSAGES = "hasMessages";
    public static final String FIELD_MESSAGE_COUNT = "messageCount";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_TIMESTAMP = "lastTimestamp";
    public static final String FIELD_IS_EXPIRED = "isExpired";
    public static final String FIELD_MATCH_ID = "matchId";
    public static final String FIELD_DARK_MODE = "darkModeEnabled";
    public static final String FIELD_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    public static final String FIELD_LOCATION_SHARING_ENABLED = "locationSharingEnabled";
    public static final String FIELD_LANGUAGE = "language";
    public static final String STORAGE_PATH_PROFILE_IMAGES = "profile_images";
    public static final String IMAGE_EXT_JPG = ".jpg";
    public static final String ERROR_PROFILE_NOT_FOUND = "Profile not found";
    public static final String FIELD_PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String FIELD_MATRICULE = "matricule";
    public static final String FIELD_BIO = "bio";
    public static final String FIELD_STUDY_LOCATION = "studyLocation";
    public static final String FIELD_STUDY_LOCATION_LAT = "studyLocationLat";
    public static final String FIELD_STUDY_LOCATION_LNG = "studyLocationLng";
    public static final String FIELD_LATITUDE_LEGACY = "latitude";
    public static final String FIELD_LONGITUDE_LEGACY = "longitude";
    public static final String FIELD_PROGRAM = "program";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_STREAK = "streak";
    public static final String FIELD_EMAIL = "email";

    public static final long QUOTE_REFRESH_INTERVAL_MS = 300000L;
    public static final int HOUR_MORNING_START = 5;
    public static final int HOUR_AFTERNOON_START = 12;
    public static final int HOUR_EVENING_START = 18;
    public static final long POMODORO_TOTAL_TIME_MS = 1500000L;
    public static final long TIMER_TICK_INTERVAL_MS = 1000L;
    public static final long INITIAL_REMINDER_DELAY_MS = 120000L;

    public static final String TIMER_CATEGORY_NAME = "Pomodoro";
    public static final String QUOTE_API_URL = "https://zenquotes.io/";
    public static final String FALLBACK_AUTHOR_NAME = "StudyBuddy";
    public static final String FRAGMENT_TAG_CREATE_BUDDY = "CreateBuddy";
    public static final String FRAGMENT_TAG_MATCH_PROPOSAL = "MatchProposal";
    public static final String EXTRA_QUOTE = "quote";

    public static final int MAX_TOP_USERS = 6;

    public static final String EXTRA_BUDDY_ID = "buddy_id";
    public static final String EXTRA_BUDDY_ID_LEGACY = "buddyId";
    public static final String EXTRA_BUDDY_NAME = "buddy_name";
    public static final String EXTRA_BUDDY_NAME_LEGACY = "buddyName";
    public static final String EXTRA_COURSE_EMOJI = "course_emoji";
    public static final String EXTRA_COURSE_NAME = "course_name";
    public static final String EXTRA_COURSE_COLOR = "course_color";
    public static final String EXTRA_BUDDY_IMAGE_URL = "buddy_image_url";
    public static final String EXTRA_SYSTEM_PROMPT = "system_prompt";
    public static final String EXTRA_MATCH_ID = "match_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    public static final String CONVERSATION_TYPE_AI = "ai";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String MATCH_TAG = "[MATCH_SUGGESTION]";
    public static final String DIALOG_TAG_MATCH = "MatchProposal";
    public static final String DEFAULT_STUDENT_NAME = "Student";
    public static final String DEFAULT_SUBJECT = "a subject";
    public static final String DEFAULT_SCHOOL_NAME = "HELB ILYA PRIGOGINE";
    public static final String DEFAULT_PROGRAM = "BACHELIER EN INFORMATIQUE";
    public static final String DEFAULT_YEAR = "BAC2";
    public static final String DEFAULT_BIO = "Non renseigne";
    public static final String EMPTY_DASH = "-";

    public static final String[] ALL_SUBJECTS = {
            "Java III", "Algorithms & Data Structures", "Advanced Databases",
            "Web Development Q3", "Mobile Development I", "Networking",
            "Mathematics", "Technical English", "Project Management", "IT Security"
    };

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DECLINED = "declined";
    public static final String STATUS_EXPIRED = "expired";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_DUTCH = "nl";

    public static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    public static final String CLAUDE_MODEL = "claude-haiku-4-5-20251001";
    public static final String ANTHROPIC_VERSION_HEADER = "2023-06-01";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String HEADER_API_KEY = "x-api-key";
    public static final String HEADER_VERSION = "anthropic-version";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String CLAUDE_KEY_ROLE = "role";
    public static final String CLAUDE_KEY_CONTENT = "content";
    public static final String CLAUDE_KEY_MODEL = "model";
    public static final String CLAUDE_KEY_MAX_TOKENS = "max_tokens";
    public static final String CLAUDE_KEY_SYSTEM = "system";
    public static final String CLAUDE_KEY_MESSAGES = "messages";

    public static final String LANGUAGE_MIRROR_INSTRUCTION =
            "\n\nIMPORTANT — Language policy:\n"
                    + "• Detect the language of the user's most recent message.\n"
                    + "• Always reply in that exact same language, even if it changes mid-conversation.\n"
                    + "• If the user mixes languages, reply in the dominant one.\n"
                    + "• Never translate the user's message back to them; respond naturally in their language.";

    public static final String TYPING_PLACEHOLDER = "...";
    public static final String SAVED_TOAST = "Message saved to your favorites!";
    public static final String SAVE_ERROR_TOAST = "Could not save message.";
    public static final String SEND_ERROR_PREFIX = "Error: ";

    public static final String EMPTY_URL_SENTINEL = "__none__";
    public static final int DEFAULT_AVATAR_DP = 56;
    public static final int DEFAULT_BG_COLOR = 0xFFE8EAF6;
    public static final float EMOJI_TEXT_SIZE_RATIO = 0.55f;

    public static final double BRUSSELS_LAT = 50.8503;
    public static final double BRUSSELS_LNG = 4.3517;
    public static final double INITIAL_ZOOM = 13.0;
    public static final double USER_ZOOM = 15.5;

    public static final String HEX_COLOR_STUDY_SPOT = "#3949AB";
    public static final String HEX_COLOR_STUDENT = "#FF6D00";
    public static final String HEX_COLOR_WHITE = "#FFFFFF";
    public static final String HEX_COLOR_TEXT_PRIMARY = "#1a1a3e";

    public static final int MARKER_WIDTH_PX = 80;
    public static final int MARKER_HEIGHT_PX = 100;
    public static final double MARKER_OVERLAP_OFFSET = 0.001;

    public static final String KEY_CAMERA_LAT = "camera_latitude";
    public static final String KEY_CAMERA_LON = "camera_longitude";
    public static final String KEY_ZOOM = "map_zoom";

    public static final String KEY_MARKER_STUDY_SPOT = "study-marker";
    public static final String KEY_MARKER_STUDENT = "student-icon";

    public static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    public static final String NAV_URI_TEMPLATE = "google.navigation:q=%s,%s";
    public static final String WEB_URI_TEMPLATE = "https://www.google.com/maps/dir/?api=1&destination=%s,%s";
    public static final String MAP_DEFAULT_LABEL = "this location";
    public static final String MAP_DIALOG_TITLE = "Open in Google Maps?";
    public static final String MAP_DIALOG_MESSAGE_TEMPLATE = "Get directions to %s?";
    public static final String MAP_DIALOG_POSITIVE = "Open Maps";
    public static final String MAP_DIALOG_NEGATIVE = "Cancel";
    public static final String NO_MAPS_MESSAGE_TEMPLATE = "No maps app available to open %s";

    public static final String CHANNEL_ID_BUDDY = "channel_buddy";
    public static final String CHANNEL_ID_MESSAGE = "channel_message";
    public static final String CHANNEL_NAME_BUDDY = "AI Buddy Messages";
    public static final String CHANNEL_NAME_MESSAGE = "Student Messages";
    public static final String CHANNEL_DESC_BUDDY = "Notifications from your AI study buddies";
    public static final String CHANNEL_DESC_MESSAGE = "New messages and matches from your contacts";

    public static final int NOTIF_ID_BUDDY = 1001;
    public static final int NOTIF_ID_MESSAGE = 1002;
    public static final int NOTIF_ID_MATCH = 1003;

    public static final int REQUEST_CODE_BUDDY = 0;
    public static final int REQUEST_CODE_MESSAGE = 1;
    public static final int REQUEST_CODE_MATCH = 2;

    public static final String EXTRA_NAV_TYPE = "nav_type";
    public static final String EXTRA_TARGET_ID = "target_id";
    public static final String NAV_TYPE_BUDDY = "buddy";
    public static final String NAV_TYPE_MESSAGE = "message";
    public static final String NAV_TYPE_MATCH = "match";

    public static final String DB_TYPE_BUDDY = "buddy";
    public static final String DB_TYPE_MESSAGE = "message";
    public static final String DB_TYPE_MATCH = "match";

    public static final String SENDER_SUFFIX = " is writing to you";
    public static final String NEW_MESSAGE_TITLE = "New Message";
    public static final String NEW_MESSAGE_BODY_TEMPLATE = "%s replied to you";
    public static final String NEW_MATCH_TITLE = "New Partner Found!";
    public static final String NEW_MATCH_BODY_TEMPLATE = "%s is also studying %s";

    public static final String WORKER_TAG_BUDDY_MOTIVATION = "BuddyMotivationWorker";
    public static final String DEFAULT_BUDDY_NAME_MOTIVATION = "Your Buddy";
    public static final String DEFAULT_COURSE_NAME_MOTIVATION = "your studies";
    public static final int MAX_BUDDIES_FOR_MOTIVATION = 5;

    public static final String[] CONTEXTUAL_TEMPLATES = {
            "Hey! Got 5 minutes to talk about %s? I have a question 🤔",
            "Quick check-in — how are you doing on %s today? Need a hand? 📚",
            "I just saw a tricky concept in %s, want me to walk you through it? 💡",
            "Time for a short %s session? I'll keep it under 25 minutes ⏰",
            "Don't forget about %s — even 10 minutes a day keeps the panic away 🚀"
    };

    public static final String[] GENERIC_TEMPLATES = {
            "💡 Don't forget to study today!",
            "🎯 You can absolutely reach your goals — one Pomodoro at a time.",
            "📚 25 minutes of focus can make a big difference!",
            "🚀 Keep going, you're making progress.",
            "⏰ Time for a short study session?"
    };

    public static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    public static final String MAPBOX_COUNTRY_FILTER = "BE";
    public static final String EMPTY_ADDRESS_ERROR = "Address is empty";
    public static final String NO_MATCH_ERROR = "No matching location found";
    public static final String HTTP_ERROR_TEMPLATE = "Geocoding failed (HTTP %d)";
    public static final String NETWORK_ERROR = "Network error";
    public static final String MAPBOX_TAG = "MapboxGeocoding";

    public static final String EXPIRED_LABEL = "Expired";
    public static final String TIME_TEMPLATE_HM = "%dh %dm";
    public static final String TIME_TEMPLATE_M = "%d min";

    public static final String MATCHING_TAG = "MatchingService";
    public static final String ERROR_NOT_AUTHENTICATED = "User not authenticated";
    public static final String LOG_NO_SUBJECTS = "No subjects configured for current user";
    public static final String LOG_MATCH_FOUND_TEMPLATE = "Match found: %s on subject: %s";
    public static final String LOG_NO_MATCH_FINAL = "No match found after querying all subjects";
    public static final String LOG_QUERY_FAILED_TEMPLATE = "Query failed for subject: %s";
    public static final String DEFAULT_STUDENT_LABEL = "A student";

    public static final String WORKER_TAG_MATCHING = "MatchingWorker";
    public static final String LOG_MATCHING_STARTED = "MatchingWorker started";
    public static final String LOG_MATCH_FOUND_WORKER = "Match found for subject: %s";
    public static final String LOG_NO_MATCH_WORKER = "No match found this time";
    public static final String LOG_MATCHING_ERROR_TEMPLATE = "Matching error: %s";

    public static final String REMINDER_TAG = "ReminderReceiver";
    public static final String DEFAULT_REMINDER_QUOTE = "Success is not final, failure is not fatal: it is the courage to continue that counts.";
    public static final String REMINDER_MESSAGE_TEMPLATE = "Time for a quick study session! %s Open the app to study or find a partner.";
    public static final String LOG_REMINDER_RECEIVED = "Reminder alarm received";

    public static final int TYPE_USER = 0;
    public static final int TYPE_BUDDY = 1;
    public static final int TYPE_BUDDY_TYPING = 2;
    public static final String TIME_FORMAT_HM = "HH:mm";

    public static final String FIELD_USER_ID_1_LEGACY = "user1Id";
    public static final String FIELD_USER_ID_2_LEGACY = "user2Id";
    public static final String FIELD_USER_NAME_1_LEGACY = "user1Name";
    public static final String FIELD_USER_NAME_2_LEGACY = "user2Name";

    public static final int REQUEST_CODE_NOTIF_PERMISSION = 101;

    public static final String WORK_NAME_BUDDY_MOTIVATION = "buddy_motivation";
    public static final String WORK_NAME_MATCHING_SEARCH = "matching_search";

    public static final String DEFAULT_WEB_CLIENT_ID = "417597305039-xxxxxxxx.apps.googleusercontent.com";
    public static final String FIREBASE_ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD";
    public static final String FIREBASE_ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL";
    public static final String FIREBASE_ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND";
    public static final String FIREBASE_ERROR_USER_DISABLED = "ERROR_USER_DISABLED";
    public static final String KEY_NETWORK = "network";
    public static final String KEY_TOO_MANY_REQUESTS = "too-many-requests";
    public static final String KEY_TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS";
    public static final String KEY_APP_CHECK = "App Check";
    public static final String KEY_APP_CHECK_ALT = "AppCheck";

    public static final String KEY_FIELD_EMAIL = "login_email";
    public static final String KEY_FIELD_PASSWORD = "login_password";
    public static final int RC_SIGN_IN = 9001;
    public static final String STRING_RES_WEB_CLIENT_ID = "default_web_client_id";
    public static final String STRING_RES_TYPE = "string";

    public static final String KEY_STEP = "current_step";
    public static final String KEY_PENDING_UID = "pending_uid";
    public static final String KEY_PENDING_NAME = "pending_name";
    public static final String KEY_PENDING_EMAIL = "pending_email";
    public static final String KEY_FIELD_NAME_STATE = "field_name";
    public static final String KEY_FIELD_EMAIL_STATE = "field_email";
    public static final String KEY_FIELD_PASSWORD_STATE = "field_password";
    public static final String KEY_CHECKED_SUBJECTS = "checked_subjects";

    public static final int STEP_ACCOUNT = 1;
    public static final int STEP_SUBJECTS = 2;
    public static final float CHECKBOX_TEXT_SIZE = 14f;
    public static final int MARGIN_SUBJECT_CB = 8;

    public static final String DEFAULT_SCHOOL = "HELB";
    public static final String DEFAULT_PROGRAM_IT = "Bachelor in IT";
    public static final String DEFAULT_YEAR_IT = "B2 Q3-Q4";

    public static final double BRUSSELS_LAT_DEFAULT = 50.8503;
    public static final double BRUSSELS_LNG_DEFAULT = 4.3517;
    public static final double FALLBACK_ZOOM = 12.0;
    public static final double FOCUS_ZOOM = 14.0;

    public static final String MARKER_COLOR_HEX = "#3F2EAA";
    public static final String OTHER_MARKER_COLOR_HEX = "#9E9E9E";
    public static final String MARKER_TEXT_COLOR_HEX = "#1A1A3E";
    public static final String MARKER_TEXT_HALO_HEX = "#FFFFFF";
    public static final String MARKER_IMAGE_ID = "chat-participant-marker";
    public static final String OTHER_MARKER_IMAGE_ID = "other-student-marker";

    public static final double DUPLICATE_OFFSET_DEGREES = 0.0003;
    public static final double PARTICIPANT_ICON_SIZE = 1.0;
    public static final double OTHER_ICON_SIZE = 0.85;
    public static final double PARTICIPANT_TEXT_SIZE = 12.0;
    public static final double OTHER_TEXT_SIZE = 10.0;
    public static final double TEXT_HALO_WIDTH = 2.0;

    public static final float MARKER_CUBIC_Y_RATIO = 0.4f;
    public static final float MARKER_CENTER_X_RATIO = 2f;
    public static final float MARKER_CIRCLE_Y_RATIO = 0.35f;
    public static final float MARKER_CIRCLE_RADIUS_RATIO = 0.2f;

    public static final String MAP_ANNOTATIONS_PLUGIN = "annotations";
    public static final int MIN_TOKEN_OVERLAP = 2;
    public static final int MIN_TOKEN_LENGTH = 3;

    private MyConstants()
    {
    }
}
