package com.safebirth.sms.parser;

import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.SkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses incoming SMS messages into structured commands.
 * Supports both English and Arabic commands with full bilingual support.
 * 
 * Supported SMS formats:
 * - Mother Registration: REG MOTHER CAMP A ZONE 3 DUE 15-02 RISK HIGH
 *                        تسجيل ام مخيم أ منطقة 3 موعد 15-02 خطورة عالية
 * - Volunteer Registration: REG VOLUNTEER NAME FATIMA SKILL MIDWIFE ZONE 3,4,5
 *                           تسجيل متطوعة الاسم فاطمة مهارة قابلة منطقة 3,4,5
 * - Emergency: EMERGENCY / SOS / طوارئ
 * - Support: SUPPORT / مساعدة
 * - Accept Case: ACCEPT HR-0042 / ACCEPT 0042 / قبول 0042
 * - Complete Case: COMPLETE HR-0042 / انهاء 0042
 * - Cancel Case: CANCEL HR-0042 / الغاء 0042
 * - Set Available: AVAILABLE / متاح
 * - Set Busy: BUSY / مشغول
 * - Status: STATUS / حالة
 */
@Component
public class SmsParser {

    private static final Logger log = LoggerFactory.getLogger(SmsParser.class);

    // Arabic to English keyword mappings
    private static final Map<String, String> ARABIC_KEYWORDS = Map.ofEntries(
            // Commands
            Map.entry("تسجيل", "REG"),
            Map.entry("ام", "MOTHER"),
            Map.entry("أم", "MOTHER"),
            Map.entry("متطوع", "VOLUNTEER"),
            Map.entry("متطوعة", "VOLUNTEER"),
            Map.entry("طوارئ", "EMERGENCY"),
            Map.entry("مساعدة", "SUPPORT"),
            Map.entry("قبول", "ACCEPT"),
            Map.entry("انهاء", "COMPLETE"),
            Map.entry("إنهاء", "COMPLETE"),
            Map.entry("الغاء", "CANCEL"),
            Map.entry("إلغاء", "CANCEL"),
            Map.entry("متاح", "AVAILABLE"),
            Map.entry("متاحة", "AVAILABLE"),
            Map.entry("مشغول", "BUSY"),
            Map.entry("مشغولة", "BUSY"),
            Map.entry("غير متاح", "OFFLINE"),
            Map.entry("حالة", "STATUS"),
            // Field names
            Map.entry("مخيم", "CAMP"),
            Map.entry("منطقة", "ZONE"),
            Map.entry("موعد", "DUE"),
            Map.entry("خطورة", "RISK"),
            Map.entry("الاسم", "NAME"),
            Map.entry("اسم", "NAME"),
            Map.entry("مهارة", "SKILL"),
            // Risk levels
            Map.entry("عالية", "HIGH"),
            Map.entry("عالي", "HIGH"),
            Map.entry("متوسطة", "MEDIUM"),
            Map.entry("متوسط", "MEDIUM"),
            Map.entry("منخفضة", "LOW"),
            Map.entry("منخفض", "LOW"),
            // Skill types
            Map.entry("قابلة", "MIDWIFE"),
            Map.entry("ممرضة", "NURSE"),
            Map.entry("ممرض", "NURSE"),
            Map.entry("مدربة", "TRAINED"),
            Map.entry("مدرب", "TRAINED"),
            Map.entry("مجتمعي", "COMMUNITY"),
            Map.entry("مجتمعية", "COMMUNITY")
    );

    // English patterns for command detection
    private static final Pattern REG_MOTHER_PATTERN = Pattern.compile(
            "(?i)^REG(?:ISTER)?\\s+MOTHER",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern REG_VOLUNTEER_PATTERN = Pattern.compile(
            "(?i)^REG(?:ISTER)?\\s+VOLUNTEER",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern EMERGENCY_PATTERN = Pattern.compile(
            "(?i)^(EMERGENCY|SOS|URGENT)$",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern SUPPORT_PATTERN = Pattern.compile(
            "(?i)^SUPPORT$",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern ACCEPT_PATTERN = Pattern.compile(
            "(?i)^ACCEPT\\s+(HR-?)?(\\d+)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern COMPLETE_PATTERN = Pattern.compile(
            "(?i)^COMPLETE\\s+(HR-?)?(\\d+)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern CANCEL_PATTERN = Pattern.compile(
            "(?i)^CANCEL\\s+(HR-?)?(\\d+)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    // Field extraction patterns
    private static final Pattern CAMP_PATTERN = Pattern.compile(
            "(?i)CAMP\\s+([A-Za-z0-9\\u0600-\\u06FF]+)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern ZONE_PATTERN = Pattern.compile(
            "(?i)ZONE\\s+([A-Za-z0-9,\\s\\u0600-\\u06FF]+?)(?:\\s+(?:DUE|RISK|NAME|SKILL|$))",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern ZONE_SIMPLE_PATTERN = Pattern.compile(
            "(?i)ZONE\\s+([A-Za-z0-9,\\u0600-\\u06FF]+)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern DUE_PATTERN = Pattern.compile(
            "(?i)DUE\\s+(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern RISK_PATTERN = Pattern.compile(
            "(?i)RISK\\s+(HIGH|MEDIUM|LOW)",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?i)NAME\\s+([A-Za-z\\u0600-\\u06FF]+)(?:\\s+(?:CAMP|SKILL|ZONE|$))",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private static final Pattern SKILL_PATTERN = Pattern.compile(
            "(?i)SKILL\\s+(MIDWIFE|NURSE|TRAINED|COMMUNITY)",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    // Date formatters for parsing
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yy"),
            DateTimeFormatter.ofPattern("d/M/yy")
    );

    /**
     * Parse an incoming SMS message into a structured command.
     *
     * @param senderPhone the phone number of the sender
     * @param message     the raw SMS message body
     * @return the parsed command
     */
    public SmsCommand parse(String senderPhone, String message) {
        if (message == null || message.isBlank()) {
            log.warn("Received empty message from {}", maskPhone(senderPhone));
            return createUnknownCommand(senderPhone, message);
        }

        log.debug("Parsing SMS from {}: {}", maskPhone(senderPhone), message);
        
        String trimmedMessage = message.trim();
        Language language = detectLanguage(trimmedMessage);
        
        // Normalize Arabic to English for unified parsing
        String normalizedMessage = normalizeMessage(trimmedMessage);
        
        // Parse the command
        SmsCommand command = parseCommand(senderPhone, trimmedMessage, normalizedMessage, language);
        
        log.debug("Parsed command: type={}, language={}", command.type(), command.detectedLanguage());
        return command;
    }

    /**
     * Detect the language of the message based on character analysis.
     *
     * @param message the message to analyze
     * @return the detected language
     */
    public Language detectLanguage(String message) {
        if (message == null || message.isEmpty()) {
            return Language.ENGLISH;
        }
        
        // Count Arabic characters
        long arabicCount = message.chars()
                .filter(c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC)
                .count();
        
        // If more than 20% Arabic characters, consider it Arabic
        if (arabicCount > message.length() * 0.2) {
            return Language.ARABIC;
        }
        return Language.ENGLISH;
    }

    /**
     * Normalize Arabic keywords to English equivalents for unified parsing.
     *
     * @param message the original message
     * @return the normalized message
     */
    public String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        
        String normalized = message;
        
        // Sort keywords by length (descending) to ensure longer matches are processed first
        // This prevents issues like "متاح" matching before "متاحة"
        List<Map.Entry<String, String>> sortedEntries = ARABIC_KEYWORDS.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                .toList();
        
        // Replace Arabic keywords with English equivalents
        for (Map.Entry<String, String> entry : sortedEntries) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        
        // Normalize whitespace
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }

    /**
     * Parse the command from the normalized message.
     */
    private SmsCommand parseCommand(String phone, String originalMessage, 
                                    String normalizedMessage, Language language) {
        Map<String, String> params = new HashMap<>();
        CommandType type = CommandType.UNKNOWN;

        // Check for registration commands
        if (REG_MOTHER_PATTERN.matcher(normalizedMessage).find()) {
            type = CommandType.REGISTER_MOTHER;
            extractMotherRegistrationParams(normalizedMessage, params);
        } else if (REG_VOLUNTEER_PATTERN.matcher(normalizedMessage).find()) {
            type = CommandType.REGISTER_VOLUNTEER;
            extractVolunteerRegistrationParams(normalizedMessage, params);
        }
        // Check for emergency/support commands
        else if (EMERGENCY_PATTERN.matcher(normalizedMessage).matches() || 
                 normalizedMessage.equalsIgnoreCase("EMERGENCY")) {
            type = CommandType.EMERGENCY;
        } else if (SUPPORT_PATTERN.matcher(normalizedMessage).matches()) {
            type = CommandType.SUPPORT;
        }
        // Check for case management commands
        else if (ACCEPT_PATTERN.matcher(normalizedMessage).find()) {
            type = CommandType.ACCEPT_CASE;
            extractCaseId(normalizedMessage, ACCEPT_PATTERN, params);
        } else if (COMPLETE_PATTERN.matcher(normalizedMessage).find()) {
            type = CommandType.COMPLETE_CASE;
            extractCaseId(normalizedMessage, COMPLETE_PATTERN, params);
        } else if (CANCEL_PATTERN.matcher(normalizedMessage).find()) {
            type = CommandType.CANCEL_CASE;
            extractCaseId(normalizedMessage, CANCEL_PATTERN, params);
        }
        // Check for availability commands
        else if (normalizedMessage.matches("(?i)^AVAILABLE$")) {
            type = CommandType.AVAILABLE;
        } else if (normalizedMessage.matches("(?i)^BUSY$")) {
            type = CommandType.BUSY;
        } else if (normalizedMessage.matches("(?i)^(OFFLINE|UNAVAILABLE)$")) {
            type = CommandType.OFFLINE;
        }
        // Check for information commands
        else if (normalizedMessage.matches("(?i)^STATUS$")) {
            type = CommandType.STATUS;
        } else if (normalizedMessage.matches("(?i)^HELP$")) {
            type = CommandType.HELP;
        }

        return new SmsCommand(type, phone, language, originalMessage, params);
    }

    /**
     * Extract mother registration parameters from the normalized message.
     */
    private void extractMotherRegistrationParams(String message, Map<String, String> params) {
        // Extract camp
        Matcher campMatcher = CAMP_PATTERN.matcher(message);
        if (campMatcher.find()) {
            params.put("camp", campMatcher.group(1).trim());
        }

        // Extract zone(s) - try complex pattern first, then simple
        Matcher zoneMatcher = ZONE_PATTERN.matcher(message);
        if (zoneMatcher.find()) {
            params.put("zone", zoneMatcher.group(1).trim());
        } else {
            zoneMatcher = ZONE_SIMPLE_PATTERN.matcher(message);
            if (zoneMatcher.find()) {
                params.put("zone", zoneMatcher.group(1).trim());
            }
        }

        // Extract due date
        Matcher dueMatcher = DUE_PATTERN.matcher(message);
        if (dueMatcher.find()) {
            String day = dueMatcher.group(1);
            String month = dueMatcher.group(2);
            String year = dueMatcher.group(3);
            
            // Default year to current year if not provided
            if (year == null) {
                year = String.valueOf(LocalDate.now().getYear());
            } else if (year.length() == 2) {
                year = "20" + year;
            }
            
            params.put("dueDate", day + "-" + month + "-" + year);
        }

        // Extract risk level
        Matcher riskMatcher = RISK_PATTERN.matcher(message);
        if (riskMatcher.find()) {
            params.put("riskLevel", riskMatcher.group(1).toUpperCase());
        }
    }

    /**
     * Extract volunteer registration parameters from the normalized message.
     */
    private void extractVolunteerRegistrationParams(String message, Map<String, String> params) {
        // Extract name
        Matcher nameMatcher = NAME_PATTERN.matcher(message);
        if (nameMatcher.find()) {
            params.put("name", nameMatcher.group(1).trim());
        }

        // Extract camp
        Matcher campMatcher = CAMP_PATTERN.matcher(message);
        if (campMatcher.find()) {
            params.put("camp", campMatcher.group(1).trim());
        }

        // Extract zones (can be comma-separated)
        Matcher zoneMatcher = ZONE_SIMPLE_PATTERN.matcher(message);
        if (zoneMatcher.find()) {
            String zones = zoneMatcher.group(1).trim();
            // Normalize comma separation
            zones = zones.replaceAll("\\s*,\\s*", ",");
            params.put("zones", zones);
        }

        // Extract skill type
        Matcher skillMatcher = SKILL_PATTERN.matcher(message);
        if (skillMatcher.find()) {
            params.put("skillType", skillMatcher.group(1).toUpperCase());
        }
    }

    /**
     * Extract case ID from the message.
     */
    private void extractCaseId(String message, Pattern pattern, Map<String, String> params) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String digits = matcher.group(2);
            params.put("caseId", "HR-" + digits);
        }
    }

    /**
     * Parse a due date string into a LocalDate.
     *
     * @param dateStr the date string (e.g., "15-02", "15-02-2026", "15/02")
     * @return the parsed LocalDate or null if parsing fails
     */
    public LocalDate parseDueDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        // Handle short format (day-month) by adding current year
        String fullDateStr = dateStr;
        if (!dateStr.matches(".*\\d{4}.*") && !dateStr.matches(".*\\d{2}$")) {
            // Only day and month provided
            fullDateStr = dateStr + "-" + LocalDate.now().getYear();
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(fullDateStr, formatter);
                // If the date is in the past, assume next year
                if (date.isBefore(LocalDate.now())) {
                    date = date.plusYears(1);
                }
                return date;
            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }

        log.warn("Could not parse due date: {}", dateStr);
        return null;
    }

    /**
     * Parse a risk level string into a RiskLevel enum.
     *
     * @param riskStr the risk level string
     * @return the parsed RiskLevel or LOW as default
     */
    public RiskLevel parseRiskLevel(String riskStr) {
        if (riskStr == null || riskStr.isBlank()) {
            return RiskLevel.LOW;
        }

        try {
            return RiskLevel.valueOf(riskStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown risk level '{}', defaulting to LOW", riskStr);
            return RiskLevel.LOW;
        }
    }

    /**
     * Parse a skill type string into a SkillType enum.
     *
     * @param skillStr the skill type string
     * @return the parsed SkillType or COMMUNITY_VOLUNTEER as default
     */
    public SkillType parseSkillType(String skillStr) {
        if (skillStr == null || skillStr.isBlank()) {
            return SkillType.COMMUNITY_VOLUNTEER;
        }

        String normalized = skillStr.toUpperCase().trim();
        
        // Handle common abbreviations and variations
        return switch (normalized) {
            case "MIDWIFE" -> SkillType.MIDWIFE;
            case "NURSE" -> SkillType.NURSE;
            case "TRAINED", "TRAINED_ATTENDANT", "TBA" -> SkillType.TRAINED_ATTENDANT;
            case "CHW", "COMMUNITY_HEALTH", "HEALTH_WORKER" -> SkillType.COMMUNITY_HEALTH_WORKER;
            case "COMMUNITY", "COMMUNITY_VOLUNTEER", "VOLUNTEER" -> SkillType.COMMUNITY_VOLUNTEER;
            default -> {
                log.warn("Unknown skill type '{}', defaulting to COMMUNITY_VOLUNTEER", skillStr);
                yield SkillType.COMMUNITY_VOLUNTEER;
            }
        };
    }

    /**
     * Parse zones string into a Set of zone identifiers.
     *
     * @param zonesStr comma-separated zones string
     * @return set of zone identifiers
     */
    public Set<String> parseZones(String zonesStr) {
        if (zonesStr == null || zonesStr.isBlank()) {
            return Collections.emptySet();
        }

        Set<String> zones = new HashSet<>();
        String[] parts = zonesStr.split("[,،\\s]+"); // Support both English and Arabic comma
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                zones.add(trimmed);
            }
        }
        
        return zones;
    }

    /**
     * Create an UNKNOWN command for unrecognized messages.
     */
    private SmsCommand createUnknownCommand(String phone, String message) {
        return new SmsCommand(CommandType.UNKNOWN, phone, Language.ENGLISH, message, Collections.emptyMap());
    }

    /**
     * Mask phone number for logging (privacy).
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
