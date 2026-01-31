package com.safebirth.sms.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safebirth.config.ConversationConfig;
import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerRepository;
import com.safebirth.domain.volunteer.Profession;
import com.safebirth.matching.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Main service for handling SMS conversations with AI.
 */
@Service("aiConversationManager")
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final AiConversationService aiService;
    private final DirectCommandHandler directCommandHandler;
    private final ConversationStateRepository conversationStateRepository;
    private final MotherRepository motherRepository;
    private final VolunteerRepository volunteerRepository;
    private final HelpRequestService helpRequestService;
    private final MatchingService matchingService;
    private final ConversationConfig config;
    private final ObjectMapper objectMapper;

    // Language detection patterns
    private static final Pattern ARABIC_PATTERN = Pattern.compile("[\\u0600-\\u06FF]");

    public ConversationService(AiConversationService aiService, DirectCommandHandler directCommandHandler,
                               ConversationStateRepository conversationStateRepository,
                               MotherRepository motherRepository, VolunteerRepository volunteerRepository,
                               HelpRequestService helpRequestService, MatchingService matchingService,
                               ConversationConfig config, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.directCommandHandler = directCommandHandler;
        this.conversationStateRepository = conversationStateRepository;
        this.motherRepository = motherRepository;
        this.volunteerRepository = volunteerRepository;
        this.helpRequestService = helpRequestService;
        this.matchingService = matchingService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Process an incoming SMS message and return the response.
     */
    @Transactional
    public String processMessage(String phone, String message) {
        log.info("Processing message from {}: {}", phone,
                message.length() > 50 ? message.substring(0, 50) + "..." : message);

        // 1. Check for direct commands first
        if (directCommandHandler.isDirectCommand(message, phone)) {
            DirectCommandResult result = directCommandHandler.handleCommand(message, phone);
            if (result.isHandled()) {
                log.info("Direct command handled: {}", result.getResponse());
                return result.getResponse();
            }
        }

        // 2. Expire old conversations
        expireOldConversations();

        // 3. Check for existing user
        Optional<Mother> existingMother = motherRepository.findByPhoneNumber(phone);
        Optional<Volunteer> existingVolunteer = volunteerRepository.findByPhoneNumber(phone);

        // 4. Get or create conversation state
        Optional<ConversationState> activeConversation = conversationStateRepository
                .findByPhoneNumberAndStatus(phone, ConversationStatus.ACTIVE);

        // 5. Detect language
        String language = detectLanguage(message);

        // 6. Route to appropriate handler
        if (activeConversation.isPresent()) {
            return continueConversation(activeConversation.get(), message);
        }

        // Existing registered mother asking for help
        if (existingMother.isPresent() && existingMother.get().isFullyRegistered()) {
            return handleMotherHelpRequest(existingMother.get(), message, language);
        }

        // Existing registered volunteer (but message wasn't a direct command)
        if (existingVolunteer.isPresent() && existingVolunteer.get().isFullyRegistered()) {
            // Could be general query or asking for help
            return handleVolunteerMessage(existingVolunteer.get(), message);
        }

        // New user or incomplete registration
        return handleNewUser(phone, message, language, existingMother.orElse(null), existingVolunteer.orElse(null));
    }

    private String handleNewUser(String phone, String message, String language, Mother existingMother,
            Volunteer existingVolunteer) {
        log.info("New user or incomplete registration from {}", phone);

        // Use AI to detect role
        AiResponse aiResponse = aiService.detectRole(message, language);

        // Create conversation state
        ConversationState state = ConversationState.builder()
                .phoneNumber(phone)
                .conversationType(ConversationType.ROLE_DETECTION)
                .language(language)
                .build();

        // Check if AI detected role from emergency keywords
        if (aiResponse.getExtractedData() != null) {
            String role = (String) aiResponse.getExtractedData().get("role");
            if ("mother".equalsIgnoreCase(role)) {
                state.setConversationType(ConversationType.MOTHER_REGISTRATION);
            } else if ("volunteer".equalsIgnoreCase(role)) {
                state.setConversationType(ConversationType.VOLUNTEER_REGISTRATION);
            }
            state.setCollectedData(toJson(aiResponse.getExtractedData()));
        }

        // Add message to history
        state.setMessageHistory(toJson(List.of(
                Map.of("role", "user", "content", message),
                Map.of("role", "assistant", "content", aiResponse.getReply()))));
        state.incrementTurn();

        conversationStateRepository.save(state);
        log.info("Created conversation state: {}", state.getConversationType());

        return aiResponse.getReply();
    }

    private String continueConversation(ConversationState state, String message) {
        log.info("Continuing {} conversation with {}", state.getConversationType(), state.getPhoneNumber());

        AiResponse aiResponse;
        switch (state.getConversationType()) {
            case ROLE_DETECTION:
                aiResponse = handleRoleDetection(state, message);
                break;
            case MOTHER_REGISTRATION:
                aiResponse = handleMotherRegistration(state, message);
                break;
            case VOLUNTEER_REGISTRATION:
                aiResponse = handleVolunteerRegistration(state, message);
                break;
            case HELP_REQUEST:
                aiResponse = handleHelpRequestConversation(state, message);
                break;
            default:
                aiResponse = aiService.detectRole(message, state.getLanguage());
        }

        // Update conversation state
        updateConversationHistory(state, message, aiResponse.getReply());
        state.incrementTurn();

        // Check if conversation is complete
        if (aiResponse.isComplete()) {
            executeAction(state, aiResponse);
            state.setStatus(ConversationStatus.COMPLETED);
        }

        conversationStateRepository.save(state);
        return aiResponse.getReply();
    }

    private AiResponse handleRoleDetection(ConversationState state, String message) {
        AiResponse response = aiService.detectRole(message, state.getLanguage());

        if (response.getExtractedData() != null) {
            String role = (String) response.getExtractedData().get("role");
            if ("mother".equalsIgnoreCase(role)) {
                state.setConversationType(ConversationType.MOTHER_REGISTRATION);
            } else if ("volunteer".equalsIgnoreCase(role)) {
                state.setConversationType(ConversationType.VOLUNTEER_REGISTRATION);
            }
        }

        return response;
    }

    private AiResponse handleMotherRegistration(ConversationState state, String message) {
        AiResponse response = aiService.motherRegistration(message, state.getCollectedData(),
                state.getMessageHistory());

        // Merge extracted data
        if (response.getExtractedData() != null) {
            Map<String, Object> current = parseJson(state.getCollectedData());
            current.putAll(response.getExtractedData());
            state.setCollectedData(toJson(current));
        }

        return response;
    }

    private AiResponse handleVolunteerRegistration(ConversationState state, String message) {
        AiResponse response = aiService.volunteerRegistration(message, state.getCollectedData(),
                state.getMessageHistory());

        // Merge extracted data
        if (response.getExtractedData() != null) {
            Map<String, Object> current = parseJson(state.getCollectedData());
            current.putAll(response.getExtractedData());
            state.setCollectedData(toJson(current));
        }

        return response;
    }

    private AiResponse handleHelpRequestConversation(ConversationState state, String message) {
        Optional<Mother> mother = motherRepository.findByPhoneNumber(state.getPhoneNumber());
        if (mother.isEmpty()) {
            return AiResponse.builder()
                    .reply("Please register first. / يرجى التسجيل أولاً.")
                    .complete(false)
                    .build();
        }

        Map<String, Object> profile = Map.of(
                "age", mother.get().getAge() != null ? mother.get().getAge() : "unknown",
                "due_date", mother.get().getDueDate() != null ? mother.get().getDueDate().toString() : "unknown",
                "prev_complications",
                mother.get().getPrevComplications() != null ? mother.get().getPrevComplications() : false,
                "camp", mother.get().getCamp() != null ? mother.get().getCamp() : "unknown",
                "zone", mother.get().getZone() != null ? mother.get().getZone() : "unknown");

        return aiService.motherHelpRequest(message, profile);
    }

    private String handleMotherHelpRequest(Mother mother, String message, String language) {
        log.info("Registered mother {} requesting help", mother.getFormattedId());

        Map<String, Object> profile = Map.of(
                "age", mother.getAge() != null ? mother.getAge() : "unknown",
                "due_date", mother.getDueDate() != null ? mother.getDueDate().toString() : "unknown",
                "prev_complications", mother.getPrevComplications() != null ? mother.getPrevComplications() : false,
                "camp", mother.getCamp() != null ? mother.getCamp() : "unknown",
                "zone", mother.getZone() != null ? mother.getZone() : "unknown");

        AiResponse response = aiService.motherHelpRequest(message, profile);

        if (response.isComplete() && "CREATE_HELP_REQUEST".equals(response.getAction())) {
            // Extract request type and emergency status from AI response
            String requestTypeStr = response.getExtractedData() != null
                    ? (String) response.getExtractedData().get("request_type")
                    : "OTHER";
            boolean isEmergency = response.getExtractedData() != null
                    && Boolean.TRUE.equals(response.getExtractedData().get("is_emergency"));

            log.info("AI detected help request: type={}, isEmergency={} for mother {}",
                    requestTypeStr, isEmergency, mother.getFormattedId());

            // Map AI request type to our RequestType enum
            RequestType requestType = mapToRequestType(requestTypeStr, isEmergency);

            // Create help request and trigger matching
            HelpRequest helpRequest = helpRequestService.createRequest(mother, requestType);
            List<Volunteer> notified = matchingService.matchAndNotify(helpRequest);

            log.info("Created help request {} and notified {} volunteers",
                    helpRequest.getCaseId(), notified.size());

            // Build response with case info
            Language lang = mother.getPreferredLanguage();
            String caseInfo = lang == Language.ARABIC
                    ? "\n\nرقم الحالة: " + helpRequest.getCaseId() + " | تم إخطار " + notified.size() + " متطوع"
                    : "\n\nCase: " + helpRequest.getCaseId() + " | " + notified.size() + " volunteer(s) notified";

            return response.getReply() + caseInfo;
        } else {
            // Need more info - create conversation state
            ConversationState state = ConversationState.builder()
                    .phoneNumber(mother.getPhoneNumber())
                    .conversationType(ConversationType.HELP_REQUEST)
                    .language(language)
                    .messageHistory(toJson(List.of(
                            Map.of("role", "user", "content", message),
                            Map.of("role", "assistant", "content", response.getReply()))))
                    .build();
            state.incrementTurn();
            conversationStateRepository.save(state);
        }

        return response.getReply();
    }

    /**
     * Map AI-detected request type string to RequestType enum.
     */
    private RequestType mapToRequestType(String typeStr, boolean isEmergency) {
        if (typeStr == null) {
            return isEmergency ? RequestType.EMERGENCY : RequestType.SUPPORT;
        }

        return switch (typeStr.toUpperCase()) {
            case "LABOR" -> RequestType.LABOR;
            case "BLEEDING" -> RequestType.BLEEDING;
            case "PAIN_FEVER" -> RequestType.PAIN_FEVER;
            case "BABY_MOVEMENT" -> RequestType.BABY_MOVEMENT;
            case "ADVICE" -> RequestType.ADVICE;
            case "EMERGENCY" -> RequestType.EMERGENCY;
            case "SUPPORT" -> RequestType.SUPPORT;
            default -> isEmergency ? RequestType.EMERGENCY : RequestType.OTHER;
        };
    }

    private String handleVolunteerMessage(Volunteer volunteer, String message) {
        // General message from volunteer - could be asking questions
        log.info("Message from registered volunteer {}", volunteer.getFormattedId());

        String response = volunteer.getPreferredLanguage() == Language.ARABIC
                ? "أنت مسجل كمتطوع. أرسل 'مشغول' لإيقاف التنبيهات أو 'متاح' لاستقبالها."
                : "You're registered as a volunteer. Send 'busy' to pause alerts or 'available' to receive them.";

        return response;
    }

    private void executeAction(ConversationState state, AiResponse response) {
        log.info("Executing action: {}", response.getAction());
        Map<String, Object> data = parseJson(state.getCollectedData());

        switch (response.getAction()) {
            case "REGISTER_MOTHER":
                registerMother(state.getPhoneNumber(), data, state.getLanguage());
                break;
            case "REGISTER_VOLUNTEER":
                registerVolunteer(state.getPhoneNumber(), data, state.getLanguage());
                break;
            case "CREATE_HELP_REQUEST":
                // TODO: Trigger matching
                log.info("Help request created, triggering matching...");
                break;
            default:
                log.warn("Unknown action: {}", response.getAction());
        }
    }

    private void registerMother(String phone, Map<String, Object> data, String language) {
        Mother mother = motherRepository.findByPhoneNumber(phone)
                .orElse(Mother.builder().phoneNumber(phone).build());

        // Map name
        if (data.get("name") != null) {
            mother.setName(data.get("name").toString());
        }
        // Map age
        if (data.get("age") != null) {
            mother.setAge(parseInteger(data.get("age")));
        }
        // Map due date
        if (data.get("due_date") != null) {
            mother.setDueDate(parseDate(data.get("due_date").toString()));
        }
        // Map previous complications
        if (data.get("prev_complications") != null) {
            mother.setPrevComplications(parseBoolean(data.get("prev_complications")));
        }
        // Map camp
        if (data.get("camp") != null) {
            mother.setCamp(data.get("camp").toString());
        }
        // Map zone
        if (data.get("zone") != null) {
            mother.setZone(data.get("zone").toString());
        }
        mother.setPreferredLanguage("ARABIC".equalsIgnoreCase(language) ? Language.ARABIC : Language.ENGLISH);
        mother.setLastContactAt(LocalDateTime.now());

        motherRepository.save(mother);
        log.info("Registered mother: {} (name: {}, camp: {}, zone: {})",
                mother.getFormattedId(), mother.getName(), mother.getCamp(), mother.getZone());
    }

    private void registerVolunteer(String phone, Map<String, Object> data, String language) {
        Volunteer volunteer = volunteerRepository.findByPhoneNumber(phone)
                .orElse(Volunteer.builder().phoneNumber(phone).build());

        // Map name
        if (data.get("name") != null) {
            volunteer.setName(data.get("name").toString());
        }
        // Map profession
        if (data.get("profession") != null) {
            try {
                volunteer.setProfession(Profession.valueOf(data.get("profession").toString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown profession: {}", data.get("profession"));
            }
        }
        // Map camp
        if (data.get("camp") != null) {
            volunteer.setCamp(data.get("camp").toString());
        }
        // Map zones
        if (data.get("zones") != null) {
            Set<String> zones = new HashSet<>();
            Object zonesData = data.get("zones");
            if (zonesData instanceof List) {
                for (Object z : (List<?>) zonesData) {
                    zones.add(z.toString());
                }
            } else if (zonesData instanceof String) {
                // Handle comma-separated string
                for (String z : zonesData.toString().split(",")) {
                    zones.add(z.trim());
                }
            }
            volunteer.setZones(zones);
        }
        // Map skills
        volunteer.setCanAssistLabor(parseBoolean(data.get("can_assist_labor")));
        volunteer.setCanAssistBleeding(parseBoolean(data.get("can_assist_bleeding")));
        volunteer.setCanAssistPainFever(parseBoolean(data.get("can_assist_pain_fever")));
        volunteer.setCanAssistBabyMovement(parseBoolean(data.get("can_assist_baby_movement")));
        volunteer.setCanGiveAdvice(parseBoolean(data.get("can_give_advice")));
        volunteer.setPreferredLanguage("ARABIC".equalsIgnoreCase(language) ? Language.ARABIC : Language.ENGLISH);
        volunteer.setLastActiveAt(LocalDateTime.now());

        volunteerRepository.save(volunteer);
        log.info("Registered volunteer: {} (name: {}, camp: {}, zones: {})",
                volunteer.getFormattedId(), volunteer.getName(), volunteer.getCamp(), volunteer.getZones());
    }

    private void updateConversationHistory(ConversationState state, String userMessage, String assistantReply) {
        List<Map<String, String>> history = new ArrayList<>();
        if (state.getMessageHistory() != null) {
            try {
                history = objectMapper.readValue(state.getMessageHistory(),
                        new TypeReference<List<Map<String, String>>>() {
                        });
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse history", e);
            }
        }
        history.add(Map.of("role", "user", "content", userMessage));
        history.add(Map.of("role", "assistant", "content", assistantReply));
        state.setMessageHistory(toJson(history));
    }

    private void expireOldConversations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(config.getTimeoutMinutes());
        List<ConversationState> expired = conversationStateRepository.findExpiredConversations(cutoff);
        for (ConversationState state : expired) {
            state.setStatus(ConversationStatus.EXPIRED);
            conversationStateRepository.save(state);
            log.info("Expired conversation for {}", state.getPhoneNumber());
        }
    }

    private String detectLanguage(String message) {
        return ARABIC_PATTERN.matcher(message).find() ? "ARABIC" : "ENGLISH";
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null)
            return null;
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            // Try other formats
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e2) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "yes".equals(str) || "1".equals(str) || "نعم".equals(str);
    }
}
