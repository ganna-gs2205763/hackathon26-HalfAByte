package com.safebirth.sms.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safebirth.config.OpenAiConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with OpenAI API.
 */
@Service
public class AiConversationService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationService.class);

    private final OpenAiService openAiService;
    private final OpenAiConfig config;
    private final ObjectMapper objectMapper;

    // Cached prompts
    private String basePrompt;
    private String roleDetectionPrompt;
    private String motherRegistrationPrompt;
    private String motherHelpRequestPrompt;
    private String volunteerRegistrationPrompt;

    @Autowired
    public AiConversationService(
            @Autowired(required = false) OpenAiService openAiService,
            OpenAiConfig config,
            ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.config = config;
        this.objectMapper = objectMapper;

        if (openAiService == null) {
            log.warn("OpenAI service not available - AI features will return fallback responses");
        } else {
            log.info("OpenAI service initialized");
        }

        loadPrompts();
    }

    private void loadPrompts() {
        try {
            basePrompt = loadPrompt("prompts/base.txt");
            roleDetectionPrompt = loadPrompt("prompts/role_detection.txt");
            motherRegistrationPrompt = loadPrompt("prompts/mother_registration.txt");
            motherHelpRequestPrompt = loadPrompt("prompts/mother_help_request.txt");
            volunteerRegistrationPrompt = loadPrompt("prompts/volunteer_registration.txt");
            log.info("Loaded all AI prompts");
        } catch (IOException e) {
            log.error("Failed to load AI prompts", e);
            throw new RuntimeException("Failed to load AI prompts", e);
        }
    }

    private String loadPrompt(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Detect if the user is a mother or volunteer.
     */
    public AiResponse detectRole(String message, String language) {
        String prompt = roleDetectionPrompt
                .replace("{language}", language)
                .replace("{message}", message);
        return callAi(prompt, List.of());
    }

    /**
     * Continue mother registration conversation.
     */
    public AiResponse motherRegistration(String message, String collectedData, String messageHistory) {
        String prompt = motherRegistrationPrompt
                .replace("{collected_data}", collectedData != null ? collectedData : "{}")
                .replace("{message_history}", messageHistory != null ? messageHistory : "[]")
                .replace("{message}", message);
        return callAi(prompt, parseHistory(messageHistory));
    }

    /**
     * Handle mother help request.
     */
    public AiResponse motherHelpRequest(String message, Map<String, Object> motherProfile) {
        String prompt = motherHelpRequestPrompt
                .replace("{age}", String.valueOf(motherProfile.getOrDefault("age", "unknown")))
                .replace("{due_date}", String.valueOf(motherProfile.getOrDefault("due_date", "unknown")))
                .replace("{prev_complications}", String.valueOf(motherProfile.getOrDefault("prev_complications", "no")))
                .replace("{camp}", String.valueOf(motherProfile.getOrDefault("camp", "unknown")))
                .replace("{zone}", String.valueOf(motherProfile.getOrDefault("zone", "unknown")))
                .replace("{message}", message);
        return callAi(prompt, List.of());
    }

    /**
     * Continue volunteer registration conversation.
     */
    public AiResponse volunteerRegistration(String message, String collectedData, String messageHistory) {
        String prompt = volunteerRegistrationPrompt
                .replace("{collected_data}", collectedData != null ? collectedData : "{}")
                .replace("{message_history}", messageHistory != null ? messageHistory : "[]")
                .replace("{message}", message);
        return callAi(prompt, parseHistory(messageHistory));
    }

    private AiResponse callAi(String contextPrompt, List<ChatMessage> history) {
        if (openAiService == null) {
            log.warn("OpenAI service not available, returning fallback response");
            return AiResponse.builder()
                    .reply("SMS service temporarily unavailable. Please try again later.")
                    .complete(false)
                    .build();
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();

            // System message with base + context prompts
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), basePrompt + "\n\n" + contextPrompt));

            // Add conversation history
            messages.addAll(history);

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(config.getModel())
                    .messages(messages)
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature())
                    .build();

            log.debug("Sending to OpenAI: {}", contextPrompt.substring(0, Math.min(100, contextPrompt.length())));

            var response = openAiService.createChatCompletion(request);
            String content = response.getChoices().get(0).getMessage().getContent();

            log.debug("OpenAI response: {}", content);

            return parseAiResponse(content);

        } catch (Exception e) {
            log.error("OpenAI API error", e);
            return AiResponse.builder()
                    .reply("An error occurred. Please try again.")
                    .complete(false)
                    .build();
        }
    }

    private AiResponse parseAiResponse(String content) {
        try {
            // Try to extract JSON from response (might have extra text)
            String json = content;
            if (content.contains("{")) {
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}") + 1;
                json = content.substring(start, end);
            }
            return objectMapper.readValue(json, AiResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response as JSON, extracting reply: {}", content);
            // Fallback: treat entire response as reply
            return AiResponse.builder()
                    .reply(content.length() > 300 ? content.substring(0, 297) + "..." : content)
                    .complete(false)
                    .build();
        }
    }

    private List<ChatMessage> parseHistory(String messageHistory) {
        if (messageHistory == null || messageHistory.isBlank() || messageHistory.equals("[]")) {
            return List.of();
        }
        try {
            List<Map<String, String>> history = objectMapper.readValue(
                    messageHistory, new TypeReference<List<Map<String, String>>>() {
                    });
            return history.stream()
                    .map(m -> new ChatMessage(m.get("role"), m.get("content")))
                    .toList();
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse message history", e);
            return List.of();
        }
    }

    /**
     * Check if OpenAI is properly configured.
     */
    public boolean isAvailable() {
        return openAiService != null;
    }
}
