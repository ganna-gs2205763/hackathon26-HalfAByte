package com.safebirth.sms.parser;

import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.SkillType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for SmsParser.
 * Tests both English and Arabic SMS commands.
 */
class SmsParserTest {

    private SmsParser parser;
    private static final String TEST_PHONE = "+1234567890";

    @BeforeEach
    void setUp() {
        parser = new SmsParser();
    }

    @Nested
    @DisplayName("Mother Registration Tests")
    class MotherRegistrationTests {

        @Test
        @DisplayName("Parse English mother registration with all fields")
        void testParseMotherRegistration_English_AllFields() {
            String message = "REG MOTHER CAMP A ZONE 3 DUE 15-02 RISK HIGH";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
            assertEquals(Language.ENGLISH, command.detectedLanguage());
            assertEquals("A", command.getCamp());
            assertEquals("3", command.getZone());
            assertEquals("HIGH", command.getRiskLevel());
            assertNotNull(command.getDueDate());
            assertTrue(command.isRegistration());
        }

        @Test
        @DisplayName("Parse English mother registration with minimal fields")
        void testParseMotherRegistration_English_Minimal() {
            String message = "REG MOTHER CAMP B ZONE 5";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
            assertEquals("B", command.getCamp());
            assertEquals("5", command.getZone());
            assertNull(command.getRiskLevel());
            assertNull(command.getDueDate());
        }

        @Test
        @DisplayName("Parse Arabic mother registration")
        void testParseMotherRegistration_Arabic() {
            String message = "تسجيل ام مخيم أ منطقة 3 موعد 15-02 خطورة عالية";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
            assertTrue(command.isRegistration());
        }

        @Test
        @DisplayName("Parse Arabic mother registration with alef variation (أم)")
        void testParseMotherRegistration_Arabic_AlefVariation() {
            String message = "تسجيل أم مخيم ب منطقة 2";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }

        @Test
        @DisplayName("Parse REGISTER MOTHER (full word)")
        void testParseMotherRegistration_FullWord() {
            String message = "REGISTER MOTHER CAMP C ZONE 1";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
            assertEquals("C", command.getCamp());
            assertEquals("1", command.getZone());
        }

        @Test
        @DisplayName("Parse mother registration case insensitive")
        void testParseMotherRegistration_CaseInsensitive() {
            String message = "reg mother camp a zone 3";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_MOTHER, command.type());
        }
    }

    @Nested
    @DisplayName("Volunteer Registration Tests")
    class VolunteerRegistrationTests {

        @Test
        @DisplayName("Parse English volunteer registration with all fields")
        void testParseVolunteerRegistration_English_AllFields() {
            String message = "REG VOLUNTEER NAME FATIMA CAMP A SKILL MIDWIFE ZONE 3,4,5";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_VOLUNTEER, command.type());
            assertEquals(Language.ENGLISH, command.detectedLanguage());
            assertEquals("FATIMA", command.getName());
            assertEquals("A", command.getCamp());
            assertEquals("MIDWIFE", command.getSkillType());
            assertNotNull(command.getZones());
            assertTrue(command.isRegistration());
        }

        @Test
        @DisplayName("Parse English volunteer registration with single zone")
        void testParseVolunteerRegistration_English_SingleZone() {
            String message = "REG VOLUNTEER NAME AHMED SKILL NURSE ZONE 7";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_VOLUNTEER, command.type());
            assertEquals("AHMED", command.getName());
            assertEquals("NURSE", command.getSkillType());
            assertEquals("7", command.getZones());
        }

        @Test
        @DisplayName("Parse Arabic volunteer registration")
        void testParseVolunteerRegistration_Arabic() {
            String message = "تسجيل متطوعة الاسم فاطمة مهارة قابلة منطقة 3,4,5";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_VOLUNTEER, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }

        @Test
        @DisplayName("Parse Arabic volunteer registration (male form)")
        void testParseVolunteerRegistration_Arabic_Male() {
            String message = "تسجيل متطوع الاسم أحمد مهارة ممرض منطقة 1,2";
            
            SmsCommand command = parser.parse(TEST_PHONE, message);
            
            assertEquals(CommandType.REGISTER_VOLUNTEER, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }
    }

    @Nested
    @DisplayName("Emergency Command Tests")
    class EmergencyTests {

        @Test
        @DisplayName("Parse EMERGENCY command")
        void testParseEmergency_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "EMERGENCY");
            
            assertEquals(CommandType.EMERGENCY, command.type());
            assertEquals(Language.ENGLISH, command.detectedLanguage());
            assertTrue(command.isEmergency());
        }

        @Test
        @DisplayName("Parse SOS command")
        void testParseEmergency_SOS() {
            SmsCommand command = parser.parse(TEST_PHONE, "SOS");
            
            assertEquals(CommandType.EMERGENCY, command.type());
        }

        @Test
        @DisplayName("Parse URGENT command")
        void testParseEmergency_URGENT() {
            SmsCommand command = parser.parse(TEST_PHONE, "URGENT");
            
            assertEquals(CommandType.EMERGENCY, command.type());
        }

        @Test
        @DisplayName("Parse Arabic emergency (طوارئ)")
        void testParseEmergency_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "طوارئ");
            
            assertEquals(CommandType.EMERGENCY, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
            assertTrue(command.isEmergency());
        }

        @Test
        @DisplayName("Parse emergency case insensitive")
        void testParseEmergency_CaseInsensitive() {
            SmsCommand command = parser.parse(TEST_PHONE, "emergency");
            
            assertEquals(CommandType.EMERGENCY, command.type());
        }
    }

    @Nested
    @DisplayName("Support Command Tests")
    class SupportTests {

        @Test
        @DisplayName("Parse SUPPORT command")
        void testParseSupport_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "SUPPORT");
            
            assertEquals(CommandType.SUPPORT, command.type());
            assertEquals(Language.ENGLISH, command.detectedLanguage());
        }

        @Test
        @DisplayName("Parse Arabic support (مساعدة)")
        void testParseSupport_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "مساعدة");
            
            assertEquals(CommandType.SUPPORT, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }
    }

    @Nested
    @DisplayName("Accept Case Tests")
    class AcceptCaseTests {

        @Test
        @DisplayName("Parse ACCEPT with HR- prefix")
        void testParseAccept_English_WithPrefix() {
            SmsCommand command = parser.parse(TEST_PHONE, "ACCEPT HR-0042");
            
            assertEquals(CommandType.ACCEPT_CASE, command.type());
            assertEquals("HR-0042", command.getCaseId());
            assertTrue(command.isCaseManagement());
        }

        @Test
        @DisplayName("Parse ACCEPT without HR- prefix")
        void testParseAccept_English_WithoutPrefix() {
            SmsCommand command = parser.parse(TEST_PHONE, "ACCEPT 0042");
            
            assertEquals(CommandType.ACCEPT_CASE, command.type());
            assertEquals("HR-0042", command.getCaseId());
        }

        @Test
        @DisplayName("Parse Arabic accept (قبول)")
        void testParseAccept_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "قبول 0042");
            
            assertEquals(CommandType.ACCEPT_CASE, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
            assertEquals("HR-0042", command.getCaseId());
        }

        @Test
        @DisplayName("Parse accept case insensitive")
        void testParseAccept_CaseInsensitive() {
            SmsCommand command = parser.parse(TEST_PHONE, "accept hr-0001");
            
            assertEquals(CommandType.ACCEPT_CASE, command.type());
            assertEquals("HR-0001", command.getCaseId());
        }
    }

    @Nested
    @DisplayName("Complete Case Tests")
    class CompleteCaseTests {

        @Test
        @DisplayName("Parse COMPLETE command")
        void testParseComplete_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "COMPLETE HR-0042");
            
            assertEquals(CommandType.COMPLETE_CASE, command.type());
            assertEquals("HR-0042", command.getCaseId());
        }

        @Test
        @DisplayName("Parse Arabic complete (انهاء)")
        void testParseComplete_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "انهاء 0042");
            
            assertEquals(CommandType.COMPLETE_CASE, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }
    }

    @Nested
    @DisplayName("Cancel Case Tests")
    class CancelCaseTests {

        @Test
        @DisplayName("Parse CANCEL command")
        void testParseCancel_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "CANCEL HR-0042");
            
            assertEquals(CommandType.CANCEL_CASE, command.type());
            assertEquals("HR-0042", command.getCaseId());
        }

        @Test
        @DisplayName("Parse Arabic cancel (الغاء)")
        void testParseCancel_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "الغاء 0042");
            
            assertEquals(CommandType.CANCEL_CASE, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }
    }

    @Nested
    @DisplayName("Availability Status Tests")
    class AvailabilityTests {

        @Test
        @DisplayName("Parse AVAILABLE command")
        void testParseAvailable_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "AVAILABLE");
            
            assertEquals(CommandType.AVAILABLE, command.type());
            assertTrue(command.isAvailabilityCommand());
        }

        @Test
        @DisplayName("Parse Arabic available (متاح)")
        void testParseAvailable_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "متاح");
            
            assertEquals(CommandType.AVAILABLE, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }

        @Test
        @DisplayName("Parse Arabic available female form (متاحة)")
        void testParseAvailable_Arabic_Female() {
            SmsCommand command = parser.parse(TEST_PHONE, "متاحة");
            
            assertEquals(CommandType.AVAILABLE, command.type());
        }

        @Test
        @DisplayName("Parse BUSY command")
        void testParseBusy_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "BUSY");
            
            assertEquals(CommandType.BUSY, command.type());
            assertTrue(command.isAvailabilityCommand());
        }

        @Test
        @DisplayName("Parse Arabic busy (مشغول)")
        void testParseBusy_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "مشغول");
            
            assertEquals(CommandType.BUSY, command.type());
            assertEquals(Language.ARABIC, command.detectedLanguage());
        }

        @Test
        @DisplayName("Parse OFFLINE command")
        void testParseOffline_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "OFFLINE");
            
            assertEquals(CommandType.OFFLINE, command.type());
        }
    }

    @Nested
    @DisplayName("Status Command Tests")
    class StatusTests {

        @Test
        @DisplayName("Parse STATUS command")
        void testParseStatus_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "STATUS");
            
            assertEquals(CommandType.STATUS, command.type());
        }

        @Test
        @DisplayName("Parse Arabic status (حالة)")
        void testParseStatus_Arabic() {
            SmsCommand command = parser.parse(TEST_PHONE, "حالة");
            
            assertEquals(CommandType.STATUS, command.type());
        }

        @Test
        @DisplayName("Parse HELP command")
        void testParseHelp_English() {
            SmsCommand command = parser.parse(TEST_PHONE, "HELP");
            
            assertEquals(CommandType.HELP, command.type());
        }
    }

    @Nested
    @DisplayName("Language Detection Tests")
    class LanguageDetectionTests {

        @Test
        @DisplayName("Detect English language")
        void testDetectLanguage_English() {
            Language lang = parser.detectLanguage("REG MOTHER CAMP A ZONE 3");
            
            assertEquals(Language.ENGLISH, lang);
        }

        @Test
        @DisplayName("Detect Arabic language")
        void testDetectLanguage_Arabic() {
            Language lang = parser.detectLanguage("تسجيل ام مخيم أ منطقة 3");
            
            assertEquals(Language.ARABIC, lang);
        }

        @Test
        @DisplayName("Detect mixed language defaults to Arabic when >20% Arabic")
        void testDetectLanguage_Mixed() {
            Language lang = parser.detectLanguage("REG ام CAMP مخيم");
            
            // Should detect as Arabic if enough Arabic characters
            assertNotNull(lang);
        }

        @Test
        @DisplayName("Handle empty message")
        void testDetectLanguage_Empty() {
            Language lang = parser.detectLanguage("");
            
            assertEquals(Language.ENGLISH, lang);
        }

        @Test
        @DisplayName("Handle null message")
        void testDetectLanguage_Null() {
            Language lang = parser.detectLanguage(null);
            
            assertEquals(Language.ENGLISH, lang);
        }
    }

    @Nested
    @DisplayName("Due Date Parsing Tests")
    class DueDateParsingTests {

        @Test
        @DisplayName("Parse due date dd-mm format")
        void testParseDueDate_DashFormat() {
            LocalDate date = parser.parseDueDate("15-02-2026");
            
            assertNotNull(date);
            assertEquals(15, date.getDayOfMonth());
            assertEquals(2, date.getMonthValue());
            assertEquals(2026, date.getYear());
        }

        @Test
        @DisplayName("Parse due date dd/mm format")
        void testParseDueDate_SlashFormat() {
            LocalDate date = parser.parseDueDate("20/03/2026");
            
            assertNotNull(date);
            assertEquals(20, date.getDayOfMonth());
            assertEquals(3, date.getMonthValue());
        }

        @Test
        @DisplayName("Parse due date short year format")
        void testParseDueDate_ShortYear() {
            LocalDate date = parser.parseDueDate("10-04-26");
            
            assertNotNull(date);
            assertEquals(2026, date.getYear());
        }

        @Test
        @DisplayName("Handle invalid due date")
        void testParseDueDate_Invalid() {
            LocalDate date = parser.parseDueDate("invalid");
            
            assertNull(date);
        }

        @Test
        @DisplayName("Handle null due date")
        void testParseDueDate_Null() {
            LocalDate date = parser.parseDueDate(null);
            
            assertNull(date);
        }
    }

    @Nested
    @DisplayName("Risk Level Parsing Tests")
    class RiskLevelParsingTests {

        @ParameterizedTest
        @CsvSource({
            "HIGH, HIGH",
            "high, HIGH",
            "MEDIUM, MEDIUM",
            "medium, MEDIUM",
            "LOW, LOW",
            "low, LOW"
        })
        @DisplayName("Parse risk level variations")
        void testParseRiskLevel(String input, String expected) {
            RiskLevel level = parser.parseRiskLevel(input);
            
            assertEquals(RiskLevel.valueOf(expected), level);
        }

        @Test
        @DisplayName("Default to LOW for unknown risk level")
        void testParseRiskLevel_Unknown() {
            RiskLevel level = parser.parseRiskLevel("EXTREME");
            
            assertEquals(RiskLevel.LOW, level);
        }

        @Test
        @DisplayName("Default to LOW for null")
        void testParseRiskLevel_Null() {
            RiskLevel level = parser.parseRiskLevel(null);
            
            assertEquals(RiskLevel.LOW, level);
        }
    }

    @Nested
    @DisplayName("Skill Type Parsing Tests")
    class SkillTypeParsingTests {

        @ParameterizedTest
        @CsvSource({
            "MIDWIFE, MIDWIFE",
            "NURSE, NURSE",
            "TRAINED, TRAINED_ATTENDANT",
            "TRAINED_ATTENDANT, TRAINED_ATTENDANT",
            "TBA, TRAINED_ATTENDANT",
            "CHW, COMMUNITY_HEALTH_WORKER",
            "COMMUNITY, COMMUNITY_VOLUNTEER"
        })
        @DisplayName("Parse skill type variations")
        void testParseSkillType(String input, String expected) {
            SkillType skill = parser.parseSkillType(input);
            
            assertEquals(SkillType.valueOf(expected), skill);
        }

        @Test
        @DisplayName("Default to COMMUNITY_VOLUNTEER for unknown skill")
        void testParseSkillType_Unknown() {
            SkillType skill = parser.parseSkillType("UNKNOWN");
            
            assertEquals(SkillType.COMMUNITY_VOLUNTEER, skill);
        }
    }

    @Nested
    @DisplayName("Zones Parsing Tests")
    class ZonesParsingTests {

        @Test
        @DisplayName("Parse single zone")
        void testParseZones_Single() {
            Set<String> zones = parser.parseZones("3");
            
            assertEquals(1, zones.size());
            assertTrue(zones.contains("3"));
        }

        @Test
        @DisplayName("Parse comma-separated zones")
        void testParseZones_CommaSeparated() {
            Set<String> zones = parser.parseZones("3,4,5");
            
            assertEquals(3, zones.size());
            assertTrue(zones.contains("3"));
            assertTrue(zones.contains("4"));
            assertTrue(zones.contains("5"));
        }

        @Test
        @DisplayName("Parse zones with spaces")
        void testParseZones_WithSpaces() {
            Set<String> zones = parser.parseZones("3, 4, 5");
            
            assertEquals(3, zones.size());
        }

        @Test
        @DisplayName("Handle empty zones string")
        void testParseZones_Empty() {
            Set<String> zones = parser.parseZones("");
            
            assertTrue(zones.isEmpty());
        }

        @Test
        @DisplayName("Handle null zones string")
        void testParseZones_Null() {
            Set<String> zones = parser.parseZones(null);
            
            assertTrue(zones.isEmpty());
        }
    }

    @Nested
    @DisplayName("Unknown Command Tests")
    class UnknownCommandTests {

        @Test
        @DisplayName("Return UNKNOWN for unrecognized message")
        void testParseUnknown() {
            SmsCommand command = parser.parse(TEST_PHONE, "Hello world");
            
            assertEquals(CommandType.UNKNOWN, command.type());
            assertFalse(command.isRecognized());
        }

        @Test
        @DisplayName("Handle empty message")
        void testParseEmpty() {
            SmsCommand command = parser.parse(TEST_PHONE, "");
            
            assertEquals(CommandType.UNKNOWN, command.type());
        }

        @Test
        @DisplayName("Handle null message")
        void testParseNull() {
            SmsCommand command = parser.parse(TEST_PHONE, null);
            
            assertEquals(CommandType.UNKNOWN, command.type());
        }

        @Test
        @DisplayName("Handle whitespace only message")
        void testParseWhitespace() {
            SmsCommand command = parser.parse(TEST_PHONE, "   ");
            
            assertEquals(CommandType.UNKNOWN, command.type());
        }
    }

    @Nested
    @DisplayName("Message Normalization Tests")
    class NormalizationTests {

        @Test
        @DisplayName("Normalize Arabic keywords to English")
        void testNormalizeMessage_Arabic() {
            String normalized = parser.normalizeMessage("تسجيل ام مخيم أ");
            
            assertTrue(normalized.contains("REG"));
            assertTrue(normalized.contains("MOTHER"));
            assertTrue(normalized.contains("CAMP"));
        }

        @Test
        @DisplayName("Handle multiple spaces")
        void testNormalizeMessage_MultipleSpaces() {
            String normalized = parser.normalizeMessage("REG   MOTHER   CAMP   A");
            
            assertEquals("REG MOTHER CAMP A", normalized);
        }

        @Test
        @DisplayName("Handle null message")
        void testNormalizeMessage_Null() {
            String normalized = parser.normalizeMessage(null);
            
            assertNull(normalized);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Handle message with leading/trailing whitespace")
        void testParseWithWhitespace() {
            SmsCommand command = parser.parse(TEST_PHONE, "  EMERGENCY  ");
            
            assertEquals(CommandType.EMERGENCY, command.type());
        }

        @Test
        @DisplayName("Preserve raw message")
        void testPreserveRawMessage() {
            String originalMessage = "REG MOTHER CAMP A ZONE 3";
            SmsCommand command = parser.parse(TEST_PHONE, originalMessage);
            
            assertEquals(originalMessage, command.rawMessage());
        }

        @Test
        @DisplayName("Preserve sender phone")
        void testPreserveSenderPhone() {
            SmsCommand command = parser.parse(TEST_PHONE, "EMERGENCY");
            
            assertEquals(TEST_PHONE, command.senderPhone());
        }
    }
}
