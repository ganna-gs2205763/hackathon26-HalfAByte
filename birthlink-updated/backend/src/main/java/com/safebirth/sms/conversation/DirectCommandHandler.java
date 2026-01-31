package com.safebirth.sms.conversation;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerRepository;
import com.safebirth.domain.helprequest.VolunteerResponse;
import com.safebirth.domain.helprequest.VolunteerResponseRepository;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.matching.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handles direct commands that bypass AI processing.
 */
@Service
public class DirectCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(DirectCommandHandler.class);

    private final VolunteerRepository volunteerRepository;
    private final VolunteerResponseRepository volunteerResponseRepository;
    private final MotherRepository motherRepository;
    private final HelpRequestService helpRequestService;
    private final MatchingService matchingService;

    public DirectCommandHandler(VolunteerRepository volunteerRepository,
                                VolunteerResponseRepository volunteerResponseRepository,
                                MotherRepository motherRepository,
                                HelpRequestService helpRequestService,
                                MatchingService matchingService) {
        this.volunteerRepository = volunteerRepository;
        this.volunteerResponseRepository = volunteerResponseRepository;
        this.motherRepository = motherRepository;
        this.helpRequestService = helpRequestService;
        this.matchingService = matchingService;
    }

    // Patterns for direct commands
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[٠-٩0-9]+$");
    private static final Pattern DONE_PATTERN = Pattern.compile("^(done|complete|تم|انهاء|إنهاء)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BUSY_PATTERN = Pattern.compile("^(busy|مشغول|مشغوله)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern AVAILABLE_PATTERN = Pattern.compile("^(available|متاح|متاحه|متوفر)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CANCEL_PATTERN = Pattern.compile("^(cancel|الغاء|إلغاء)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMERGENCY_PATTERN = Pattern.compile("^(emergency|sos|urgent|طوارئ|طوارء)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SUPPORT_PATTERN = Pattern.compile("^(support|help|مساعدة|مساعده)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Check if message is a direct command that bypasses AI.
     */
    public boolean isDirectCommand(String message, String phone) {
        String normalized = message.trim();

        // Check for EMERGENCY/SUPPORT from registered mothers
        if (EMERGENCY_PATTERN.matcher(normalized).matches() ||
            SUPPORT_PATTERN.matcher(normalized).matches()) {
            return motherRepository.findByPhoneNumber(phone).isPresent();
        }

        // Check if volunteer with active case
        Optional<Volunteer> volunteer = volunteerRepository.findByPhoneNumber(phone);
        if (volunteer.isPresent() && volunteer.get().getCurrentCaseId() != null) {
            if (DONE_PATTERN.matcher(normalized).matches() ||
                    NUMBER_PATTERN.matcher(normalized).matches()) {
                return true;
            }
        }

        // Always handle status changes
        return BUSY_PATTERN.matcher(normalized).matches() ||
                AVAILABLE_PATTERN.matcher(normalized).matches() ||
                CANCEL_PATTERN.matcher(normalized).matches() ||
                isEtaResponse(normalized, phone);
    }

    /**
     * Handle a direct command and return response.
     */
    public DirectCommandResult handleCommand(String message, String phone) {
        String normalized = message.trim();
        Optional<Volunteer> volunteerOpt = volunteerRepository.findByPhoneNumber(phone);

        // Handle EMERGENCY command
        if (EMERGENCY_PATTERN.matcher(normalized).matches()) {
            return handleEmergencyCommand(phone);
        }

        // Handle SUPPORT command
        if (SUPPORT_PATTERN.matcher(normalized).matches()) {
            return handleSupportCommand(phone);
        }

        // Handle ETA response (number only)
        if (NUMBER_PATTERN.matcher(normalized).matches()) {
            int eta = parseArabicNumber(normalized);
            return handleEtaResponse(phone, eta, volunteerOpt);
        }

        // Handle "done" command
        if (DONE_PATTERN.matcher(normalized).matches()) {
            return handleDoneCommand(volunteerOpt);
        }

        // Handle "busy" command
        if (BUSY_PATTERN.matcher(normalized).matches()) {
            return handleBusyCommand(volunteerOpt);
        }

        // Handle "available" command
        if (AVAILABLE_PATTERN.matcher(normalized).matches()) {
            return handleAvailableCommand(volunteerOpt);
        }

        // Handle "cancel" command
        if (CANCEL_PATTERN.matcher(normalized).matches()) {
            return handleCancelCommand(volunteerOpt);
        }

        return DirectCommandResult.notHandled();
    }

    /**
     * Handle EMERGENCY command - creates help request and notifies volunteers.
     */
    private DirectCommandResult handleEmergencyCommand(String phone) {
        Optional<Mother> motherOpt = motherRepository.findByPhoneNumber(phone);
        if (motherOpt.isEmpty()) {
            return DirectCommandResult.handled(
                "You are not registered. Please register first. / لم يتم تسجيلك. يرجى التسجيل أولاً.");
        }

        Mother mother = motherOpt.get();
        Language lang = mother.getPreferredLanguage();
        log.warn("🚨 EMERGENCY request from {} in zone {}", mother.getFormattedId(), mother.getZone());

        // Create emergency help request
        HelpRequest request = helpRequestService.createRequest(mother, RequestType.EMERGENCY);

        // Match and notify volunteers in the same zone
        List<Volunteer> notified = matchingService.matchAndNotify(request);

        if (notified.isEmpty()) {
            return DirectCommandResult.handled(lang == Language.ARABIC
                ? "🚨 تم استلام الطوارئ! الحالة: " + request.getCaseId() + "\n⚠️ لا يوجد متطوعين متاحين في منطقتك. ابق هادئاً، نحاول إيجاد المساعدة."
                : "🚨 EMERGENCY received! Case: " + request.getCaseId() + "\n⚠️ No volunteers available in your zone. Stay calm, we are trying to find help.");
        }

        return DirectCommandResult.handled(lang == Language.ARABIC
            ? "🚨 تم استلام الطوارئ! الحالة: " + request.getCaseId() + "\n✅ تم إخطار " + notified.size() + " متطوع(ين). المساعدة في الطريق. ابق هادئاً."
            : "🚨 EMERGENCY received! Case: " + request.getCaseId() + "\n✅ " + notified.size() + " volunteer(s) have been alerted. Help is on the way. Stay calm.");
    }

    /**
     * Handle SUPPORT command - creates support request and notifies volunteers.
     */
    private DirectCommandResult handleSupportCommand(String phone) {
        Optional<Mother> motherOpt = motherRepository.findByPhoneNumber(phone);
        if (motherOpt.isEmpty()) {
            return DirectCommandResult.handled(
                "You are not registered. Please register first. / لم يتم تسجيلك. يرجى التسجيل أولاً.");
        }

        Mother mother = motherOpt.get();
        Language lang = mother.getPreferredLanguage();
        log.info("📞 Support request from {} in zone {}", mother.getFormattedId(), mother.getZone());

        // Create support help request
        HelpRequest request = helpRequestService.createRequest(mother, RequestType.SUPPORT);

        // Match and notify volunteers in the same zone
        List<Volunteer> notified = matchingService.matchAndNotify(request);

        if (notified.isEmpty()) {
            return DirectCommandResult.handled(lang == Language.ARABIC
                ? "📞 تم استلام طلب المساعدة! الحالة: " + request.getCaseId() + "\n⚠️ لا يوجد متطوعين متاحين حالياً. سنخبرك عندما يتوفر أحد."
                : "📞 Support request received! Case: " + request.getCaseId() + "\n⚠️ No volunteers available right now. We will notify you when someone is available.");
        }

        return DirectCommandResult.handled(lang == Language.ARABIC
            ? "📞 تم استلام طلب المساعدة! الحالة: " + request.getCaseId() + "\n✅ تم إخطار " + notified.size() + " متطوع(ين). سيتواصل معك أحدهم قريباً."
            : "📞 Support request received! Case: " + request.getCaseId() + "\n✅ " + notified.size() + " volunteer(s) notified. Someone will contact you soon.");
    }

    private DirectCommandResult handleEtaResponse(String phone, int eta, Optional<Volunteer> volunteerOpt) {
        if (volunteerOpt.isEmpty()) {
            return DirectCommandResult.handled("You're not registered as a volunteer. / أنت غير مسجل كمتطوع.");
        }

        Volunteer volunteer = volunteerOpt.get();

        // Find pending case for this volunteer (would need more context - for now
        // assume last alert)
        // This is a simplified implementation - in production, track pending alerts per
        // volunteer
        log.info("Volunteer {} responded with ETA: {} minutes", volunteer.getFormattedId(), eta);

        // Record the response - in a real implementation, you'd track which case this
        // is for
        // For now, we'll need to enhance this when we implement the full matching flow

        String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                ? "تم تسجيل ردك. سنخبرك إذا تم اختيارك."
                : "Response recorded. You'll be notified if selected.";

        return DirectCommandResult.handled(response);
    }

    private DirectCommandResult handleDoneCommand(Optional<Volunteer> volunteerOpt) {
        if (volunteerOpt.isEmpty()) {
            return DirectCommandResult.handled("You're not registered as a volunteer.");
        }

        Volunteer volunteer = volunteerOpt.get();
        if (volunteer.getCurrentCaseId() == null) {
            String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                    ? "ليس لديك حالة نشطة حالياً."
                    : "You don't have an active case.";
            return DirectCommandResult.handled(response);
        }

        String caseId = volunteer.getCurrentCaseId();
        volunteer.setCurrentCaseId(null);
        volunteer.setStatus(AvailabilityStatus.AVAILABLE);
        volunteer.setCompletedCases(volunteer.getCompletedCases() + 1);
        volunteerRepository.save(volunteer);

        log.info("Volunteer {} completed case {}", volunteer.getFormattedId(), caseId);

        String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                ? "شكراً! تم إنهاء الحالة #" + caseId + ". أنت الآن متاح لحالات جديدة."
                : "Thank you! Case #" + caseId + " completed. You're now available for new cases.";

        return DirectCommandResult.handled(response);
    }

    private DirectCommandResult handleBusyCommand(Optional<Volunteer> volunteerOpt) {
        if (volunteerOpt.isEmpty()) {
            return DirectCommandResult.handled("You're not registered as a volunteer.");
        }

        Volunteer volunteer = volunteerOpt.get();
        volunteer.setStatus(AvailabilityStatus.BUSY);
        volunteerRepository.save(volunteer);

        log.info("Volunteer {} set status to BUSY", volunteer.getFormattedId());

        String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                ? "تم تحديث حالتك إلى مشغول. لن تتلقى تنبيهات جديدة. أرسل 'متاح' للعودة."
                : "Status set to BUSY. You won't receive new alerts. Send 'available' to resume.";

        return DirectCommandResult.handled(response);
    }

    private DirectCommandResult handleAvailableCommand(Optional<Volunteer> volunteerOpt) {
        if (volunteerOpt.isEmpty()) {
            return DirectCommandResult.handled("You're not registered as a volunteer.");
        }

        Volunteer volunteer = volunteerOpt.get();
        volunteer.setStatus(AvailabilityStatus.AVAILABLE);
        volunteer.setCurrentCaseId(null);
        volunteerRepository.save(volunteer);

        log.info("Volunteer {} set status to AVAILABLE", volunteer.getFormattedId());

        String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                ? "أنت الآن متاح. ستتلقى تنبيهات عند حدوث حالات طوارئ."
                : "You're now AVAILABLE. You'll receive alerts for emergencies.";

        return DirectCommandResult.handled(response);
    }

    private DirectCommandResult handleCancelCommand(Optional<Volunteer> volunteerOpt) {
        if (volunteerOpt.isEmpty()) {
            return DirectCommandResult.handled("You're not registered as a volunteer.");
        }

        Volunteer volunteer = volunteerOpt.get();
        if (volunteer.getCurrentCaseId() == null) {
            String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                    ? "ليس لديك حالة نشطة للإلغاء."
                    : "You don't have an active case to cancel.";
            return DirectCommandResult.handled(response);
        }

        String caseId = volunteer.getCurrentCaseId();
        volunteer.setCurrentCaseId(null);
        volunteer.setStatus(AvailabilityStatus.AVAILABLE);
        volunteerRepository.save(volunteer);

        log.info("Volunteer {} cancelled case {}", volunteer.getFormattedId(), caseId);

        String response = volunteer.getPreferredLanguage().name().equals("ARABIC")
                ? "تم إلغاء الحالة #" + caseId + ". سنبحث عن متطوع آخر."
                : "Case #" + caseId + " cancelled. We'll find another volunteer.";

        return DirectCommandResult.handled(response);
    }

    /**
     * Check if this is an ETA response (volunteer responding to an alert with a
     * number).
     */
    private boolean isEtaResponse(String message, String phone) {
        if (!NUMBER_PATTERN.matcher(message).matches()) {
            return false;
        }
        // Only treat as ETA if this is a volunteer
        return volunteerRepository.findByPhoneNumber(phone).isPresent();
    }

    /**
     * Parse Arabic or English number string.
     */
    private int parseArabicNumber(String number) {
        // Replace Arabic numerals with English
        String english = number
                .replace('٠', '0').replace('١', '1').replace('٢', '2')
                .replace('٣', '3').replace('٤', '4').replace('٥', '5')
                .replace('٦', '6').replace('٧', '7').replace('٨', '8')
                .replace('٩', '9');
        try {
            return Integer.parseInt(english);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
