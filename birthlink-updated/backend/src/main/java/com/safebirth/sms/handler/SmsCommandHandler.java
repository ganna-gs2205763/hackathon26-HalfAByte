package com.safebirth.sms.handler;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherService;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerService;
import com.safebirth.matching.MatchingService;
import com.safebirth.sms.gateway.SmsGateway;
import com.safebirth.sms.parser.SmsCommand;
import com.safebirth.sms.parser.SmsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Routes parsed SMS commands to appropriate domain services.
 * Returns response messages in the sender's detected language.
 * 
 * Handles all SMS commands:
 * - Registration (mother, volunteer)
 * - Emergency/Support requests
 * - Case management (accept, complete, cancel)
 * - Availability status
 * - Status queries
 */
@Service
public class SmsCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SmsCommandHandler.class);

    private final MotherService motherService;
    private final VolunteerService volunteerService;
    private final HelpRequestService helpRequestService;
    private final MatchingService matchingService;
    private final SmsGateway smsGateway;
    private final SmsParser smsParser;

    public SmsCommandHandler(MotherService motherService, VolunteerService volunteerService,
                            HelpRequestService helpRequestService, MatchingService matchingService,
                            SmsGateway smsGateway, SmsParser smsParser) {
        this.motherService = motherService;
        this.volunteerService = volunteerService;
        this.helpRequestService = helpRequestService;
        this.matchingService = matchingService;
        this.smsGateway = smsGateway;
        this.smsParser = smsParser;
    }

    /**
     * Handle a parsed SMS command and return the response message.
     *
     * @param command the parsed SMS command
     * @return the response message to send back
     */
    @Transactional
    public String handle(SmsCommand command) {
        log.info("📥 Handling command: {} from {}", command.type(), maskPhone(command.senderPhone()));

        try {
            return switch (command.type()) {
                case REGISTER_MOTHER -> handleRegisterMother(command);
                case REGISTER_VOLUNTEER -> handleRegisterVolunteer(command);
                case EMERGENCY -> handleEmergency(command);
                case SUPPORT -> handleSupport(command);
                case ACCEPT_CASE -> handleAcceptCase(command);
                case COMPLETE_CASE -> handleCompleteCase(command);
                case CANCEL_CASE -> handleCancelCase(command);
                case AVAILABLE -> handleAvailabilityChange(command, AvailabilityStatus.AVAILABLE);
                case BUSY -> handleAvailabilityChange(command, AvailabilityStatus.BUSY);
                case OFFLINE -> handleAvailabilityChange(command, AvailabilityStatus.OFFLINE);
                case STATUS -> handleStatus(command);
                case HELP -> handleHelp(command);
                case UNKNOWN -> handleUnknown(command);
            };
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request from {}: {}", maskPhone(command.senderPhone()), e.getMessage());
            return formatError(command.detectedLanguage(), e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Invalid state for {}: {}", maskPhone(command.senderPhone()), e.getMessage());
            return formatError(command.detectedLanguage(), e.getMessage());
        } catch (Exception e) {
            log.error("Error handling command from {}: {}", maskPhone(command.senderPhone()), e.getMessage(), e);
            return getMessage(command.detectedLanguage(),
                    "❌ An error occurred. Please try again.",
                    "❌ حدث خطأ. يرجى المحاولة مرة أخرى.");
        }
    }

    /**
     * Handle mother registration.
     * Creates or updates a mother record with the provided details.
     */
    private String handleRegisterMother(SmsCommand command) {
        String phone = command.senderPhone();
        String camp = command.getCamp();
        String zone = command.getZone();
        Language lang = command.detectedLanguage();

        // Validate required fields
        if (camp == null || camp.isBlank()) {
            return getMessage(lang,
                    "❌ Camp is required. Example: REG MOTHER CAMP A ZONE 3",
                    "❌ المخيم مطلوب. مثال: تسجيل ام مخيم أ منطقة 3");
        }
        if (zone == null || zone.isBlank()) {
            return getMessage(lang,
                    "❌ Zone is required. Example: REG MOTHER CAMP A ZONE 3",
                    "❌ المنطقة مطلوبة. مثال: تسجيل ام مخيم أ منطقة 3");
        }

        // Parse optional fields
        LocalDate dueDate = smsParser.parseDueDate(command.getDueDate());
        RiskLevel riskLevel = smsParser.parseRiskLevel(command.getRiskLevel());

        log.info("Registering mother: phone={}, camp={}, zone={}, dueDate={}, risk={}", 
                maskPhone(phone), camp, zone, dueDate, riskLevel);

        Mother mother = motherService.register(phone, camp, zone, dueDate, riskLevel, lang);

        return getMessage(lang,
                "✅ Registered! Your ID: %s\nCamp: %s, Zone: %s\nSend EMERGENCY if you need urgent help.",
                "✅ تم التسجيل! رقمك: %s\nالمخيم: %s، المنطقة: %s\nأرسل 'طوارئ' إذا احتجت مساعدة عاجلة.",
                mother.getFormattedId(), camp, zone);
    }

    /**
     * Handle volunteer registration.
     * Creates or updates a volunteer record with the provided details.
     */
    private String handleRegisterVolunteer(SmsCommand command) {
        String phone = command.senderPhone();
        String name = command.getName();
        String camp = command.getCamp();
        String zonesStr = command.getZones();
        String skillStr = command.getSkillType();
        Language lang = command.detectedLanguage();

        // Validate required fields
        if (camp == null || camp.isBlank()) {
            return getMessage(lang,
                    "❌ Camp is required. Example: REG VOLUNTEER NAME Ali CAMP A ZONE 3 SKILL MIDWIFE",
                    "❌ المخيم مطلوب. مثال: تسجيل متطوع الاسم علي مخيم أ منطقة 3 مهارة قابلة");
        }

        // Parse zones and skill type
        Set<String> zones = smsParser.parseZones(zonesStr);
        if (zones.isEmpty()) {
            return getMessage(lang,
                    "❌ Zone is required. Example: REG VOLUNTEER NAME Ali CAMP A ZONE 3 SKILL MIDWIFE",
                    "❌ المنطقة مطلوبة. مثال: تسجيل متطوع الاسم علي مخيم أ منطقة 3 مهارة قابلة");
        }

        SkillType skillType = smsParser.parseSkillType(skillStr);

        log.info("Registering volunteer: phone={}, name={}, camp={}, zones={}, skill={}", 
                maskPhone(phone), name, camp, zones, skillType);

        Volunteer volunteer = volunteerService.register(phone, name, camp, skillType, zones, lang);

        String skillLabel = formatSkillType(skillType, lang);
        return getMessage(lang,
                "✅ Volunteer registered! Your ID: %s\nSkill: %s, Zones: %s\nYou are now AVAILABLE to receive alerts.",
                "✅ تم تسجيل المتطوع! رقمك: %s\nالمهارة: %s، المناطق: %s\nأنت الآن متاح لاستلام التنبيهات.",
                volunteer.getFormattedId(), skillLabel, String.join(", ", zones));
    }

    /**
     * Handle emergency request from a registered mother.
     * Creates a help request and notifies available volunteers.
     */
    private String handleEmergency(SmsCommand command) {
        String phone = command.senderPhone();
        Language lang = command.detectedLanguage();

        log.warn("🚨 EMERGENCY request from {}", maskPhone(phone));

        // Find registered mother
        Optional<Mother> motherOpt = motherService.findByPhone(phone);
        if (motherOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ You are not registered. Please register first: REG MOTHER CAMP [name] ZONE [number]",
                    "❌ لم يتم تسجيلك. يرجى التسجيل أولاً: تسجيل ام مخيم [اسم] منطقة [رقم]");
        }

        Mother mother = motherOpt.get();
        motherService.recordContact(phone);

        // Create emergency help request
        HelpRequest request = helpRequestService.createRequest(mother, RequestType.EMERGENCY);

        // Match and notify volunteers
        List<Volunteer> notified = matchingService.matchAndNotify(request);

        if (notified.isEmpty()) {
            return getMessage(lang,
                    "🚨 EMERGENCY received! Case: %s\n⚠️ No volunteers available in your zone. Stay calm, we are trying to find help.",
                    "🚨 تم استلام الطوارئ! الحالة: %s\n⚠️ لا يوجد متطوعين متاحين في منطقتك. ابق هادئاً، نحاول إيجاد المساعدة.",
                    request.getCaseId());
        }

        return getMessage(lang,
                "🚨 EMERGENCY received! Case: %s\n✅ %d volunteer(s) have been alerted. Help is on the way. Stay calm.",
                "🚨 تم استلام الطوارئ! الحالة: %s\n✅ تم إخطار %d متطوع(ين). المساعدة في الطريق. ابق هادئاً.",
                request.getCaseId(), notified.size());
    }

    /**
     * Handle support request from a registered mother.
     * Creates a support request and notifies available volunteers.
     */
    private String handleSupport(SmsCommand command) {
        String phone = command.senderPhone();
        Language lang = command.detectedLanguage();

        log.info("📞 Support request from {}", maskPhone(phone));

        // Find registered mother
        Optional<Mother> motherOpt = motherService.findByPhone(phone);
        if (motherOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ You are not registered. Please register first: REG MOTHER CAMP [name] ZONE [number]",
                    "❌ لم يتم تسجيلك. يرجى التسجيل أولاً: تسجيل ام مخيم [اسم] منطقة [رقم]");
        }

        Mother mother = motherOpt.get();
        motherService.recordContact(phone);

        // Create support help request
        HelpRequest request = helpRequestService.createRequest(mother, RequestType.SUPPORT);

        // Match and notify volunteers
        List<Volunteer> notified = matchingService.matchAndNotify(request);

        if (notified.isEmpty()) {
            return getMessage(lang,
                    "📞 Support request received! Case: %s\n⚠️ No volunteers available right now. We will notify you when someone is available.",
                    "📞 تم استلام طلب المساعدة! الحالة: %s\n⚠️ لا يوجد متطوعين متاحين حالياً. سنخبرك عندما يتوفر أحد.",
                    request.getCaseId());
        }

        return getMessage(lang,
                "📞 Support request received! Case: %s\n✅ %d volunteer(s) notified. Someone will contact you soon.",
                "📞 تم استلام طلب المساعدة! الحالة: %s\n✅ تم إخطار %d متطوع(ين). سيتواصل معك أحدهم قريباً.",
                request.getCaseId(), notified.size());
    }

    /**
     * Handle case acceptance by a volunteer.
     * Updates the help request and notifies the mother.
     */
    private String handleAcceptCase(SmsCommand command) {
        String phone = command.senderPhone();
        String caseId = command.getCaseId();
        Language lang = command.detectedLanguage();

        if (caseId == null || caseId.isBlank()) {
            return getMessage(lang,
                    "❌ Case ID is required. Example: ACCEPT HR-0042",
                    "❌ رقم الحالة مطلوب. مثال: قبول HR-0042");
        }

        log.info("Accept case request: caseId={}, volunteer={}", caseId, maskPhone(phone));

        // Verify sender is a registered volunteer
        Optional<Volunteer> volunteerOpt = volunteerService.findByPhone(phone);
        if (volunteerOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ You are not registered as a volunteer. Please register first.",
                    "❌ لم يتم تسجيلك كمتطوع. يرجى التسجيل أولاً.");
        }

        Volunteer volunteer = volunteerOpt.get();

        // Accept the case
        HelpRequest request = helpRequestService.acceptRequest(caseId, volunteer);

        // Notify the mother
        notifyMotherOfAcceptance(request, volunteer);

        return getMessage(lang,
                "✅ You have accepted case %s.\nMother in Zone %s has been notified.\nSend COMPLETE %s when finished.",
                "✅ لقد قبلت الحالة %s.\nتم إخطار الأم في المنطقة %s.\nأرسل انهاء %s عند الانتهاء.",
                caseId, request.getZone(), caseId);
    }

    /**
     * Handle case completion by a volunteer.
     * Marks the case as completed and updates statistics.
     */
    private String handleCompleteCase(SmsCommand command) {
        String phone = command.senderPhone();
        String caseId = command.getCaseId();
        Language lang = command.detectedLanguage();

        if (caseId == null || caseId.isBlank()) {
            return getMessage(lang,
                    "❌ Case ID is required. Example: COMPLETE HR-0042",
                    "❌ رقم الحالة مطلوب. مثال: انهاء HR-0042");
        }

        log.info("Complete case request: caseId={}, volunteer={}", caseId, maskPhone(phone));

        // Verify sender is a registered volunteer
        Optional<Volunteer> volunteerOpt = volunteerService.findByPhone(phone);
        if (volunteerOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ You are not registered as a volunteer.",
                    "❌ لم يتم تسجيلك كمتطوع.");
        }

        Volunteer volunteer = volunteerOpt.get();

        // Find the case and verify ownership
        Optional<HelpRequest> requestOpt = helpRequestService.findByCaseId(caseId);
        if (requestOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ Case %s not found.",
                    "❌ الحالة %s غير موجودة.",
                    caseId);
        }

        HelpRequest request = requestOpt.get();
        if (request.getAcceptedBy() == null || !request.getAcceptedBy().getId().equals(volunteer.getId())) {
            return getMessage(lang,
                    "❌ You are not assigned to case %s.",
                    "❌ لست مسؤولاً عن الحالة %s.",
                    caseId);
        }

        // Complete the case
        helpRequestService.completeRequest(caseId);
        volunteerService.incrementCompletedCases(volunteer.getId());

        return getMessage(lang,
                "✅ Case %s marked as COMPLETE.\nThank you for your help! Total cases completed: %d",
                "✅ تم وضع علامة اكتمال على الحالة %s.\nشكراً لمساعدتك! إجمالي الحالات المكتملة: %d",
                caseId, volunteer.getCompletedCases() + 1);
    }

    /**
     * Handle case cancellation.
     * Can be cancelled by the mother or the assigned volunteer.
     */
    private String handleCancelCase(SmsCommand command) {
        String phone = command.senderPhone();
        String caseId = command.getCaseId();
        Language lang = command.detectedLanguage();

        if (caseId == null || caseId.isBlank()) {
            return getMessage(lang,
                    "❌ Case ID is required. Example: CANCEL HR-0042",
                    "❌ رقم الحالة مطلوب. مثال: الغاء HR-0042");
        }

        log.info("Cancel case request: caseId={}, from={}", caseId, maskPhone(phone));

        // Find the case
        Optional<HelpRequest> requestOpt = helpRequestService.findByCaseId(caseId);
        if (requestOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ Case %s not found.",
                    "❌ الحالة %s غير موجودة.",
                    caseId);
        }

        HelpRequest request = requestOpt.get();

        // Verify authorization (mother or assigned volunteer)
        boolean isMother = request.getMother().getPhoneNumber().equals(phone);
        boolean isVolunteer = request.getAcceptedBy() != null && 
                request.getAcceptedBy().getPhoneNumber().equals(phone);

        if (!isMother && !isVolunteer) {
            return getMessage(lang,
                    "❌ You are not authorized to cancel case %s.",
                    "❌ ليس لديك صلاحية لإلغاء الحالة %s.",
                    caseId);
        }

        // Cancel the case
        helpRequestService.cancelRequest(caseId);

        // Notify the other party
        if (isMother && request.getAcceptedBy() != null) {
            notifyVolunteerOfCancellation(request);
        } else if (isVolunteer) {
            notifyMotherOfCancellation(request);
        }

        return getMessage(lang,
                "✅ Case %s has been cancelled.",
                "✅ تم إلغاء الحالة %s.",
                caseId);
    }

    /**
     * Handle availability status change for a volunteer.
     */
    private String handleAvailabilityChange(SmsCommand command, AvailabilityStatus newStatus) {
        String phone = command.senderPhone();
        Language lang = command.detectedLanguage();

        log.info("Availability change: {} -> {}", maskPhone(phone), newStatus);

        // Verify sender is a registered volunteer
        Optional<Volunteer> volunteerOpt = volunteerService.findByPhone(phone);
        if (volunteerOpt.isEmpty()) {
            return getMessage(lang,
                    "❌ You are not registered as a volunteer. Please register first.",
                    "❌ لم يتم تسجيلك كمتطوع. يرجى التسجيل أولاً.");
        }

        volunteerService.updateAvailability(phone, newStatus);

        return switch (newStatus) {
            case AVAILABLE -> getMessage(lang,
                    "✅ You are now AVAILABLE. You will receive alerts for emergencies in your zones.",
                    "✅ أنت الآن متاح. ستتلقى تنبيهات للطوارئ في مناطقك.");
            case BUSY -> getMessage(lang,
                    "✅ You are now BUSY. You will not receive new alerts until you set yourself as AVAILABLE.",
                    "✅ أنت الآن مشغول. لن تتلقى تنبيهات جديدة حتى تضع نفسك متاحاً.");
            case OFFLINE -> getMessage(lang,
                    "✅ You are now OFFLINE. You will not receive any alerts.",
                    "✅ أنت الآن غير متاح. لن تتلقى أي تنبيهات.");
        };
    }

    /**
     * Handle status query.
     * Returns different information based on whether the sender is a mother or volunteer.
     */
    private String handleStatus(SmsCommand command) {
        String phone = command.senderPhone();
        Language lang = command.detectedLanguage();

        // Check if mother
        Optional<Mother> motherOpt = motherService.findByPhone(phone);
        if (motherOpt.isPresent()) {
            Mother mother = motherOpt.get();
            return getMessage(lang,
                    "📊 Your Status:\nID: %s\nCamp: %s, Zone: %s\nRisk: %s\nSend EMERGENCY if you need urgent help.",
                    "📊 حالتك:\nالرقم: %s\nالمخيم: %s، المنطقة: %s\nالخطورة: %s\nأرسل 'طوارئ' إذا احتجت مساعدة عاجلة.",
                    mother.getFormattedId(), mother.getCamp(), mother.getZone(), 
                    formatRiskLevel(mother.getRiskLevel(), lang));
        }

        // Check if volunteer
        Optional<Volunteer> volunteerOpt = volunteerService.findByPhone(phone);
        if (volunteerOpt.isPresent()) {
            Volunteer volunteer = volunteerOpt.get();
            List<HelpRequest> activeCases = helpRequestService.findActiveByVolunteer(volunteer.getId());
            String statusLabel = formatAvailability(volunteer.getAvailability(), lang);
            
            return getMessage(lang,
                    "📊 Your Status:\nID: %s\nStatus: %s\nActive cases: %d\nCompleted: %d",
                    "📊 حالتك:\nالرقم: %s\nالحالة: %s\nالحالات النشطة: %d\nالمكتملة: %d",
                    volunteer.getFormattedId(), statusLabel, activeCases.size(), volunteer.getCompletedCases());
        }

        // Not registered
        return getMessage(lang,
                "❓ You are not registered. Register as:\n• Mother: REG MOTHER CAMP [name] ZONE [number]\n• Volunteer: REG VOLUNTEER NAME [name] CAMP [name] ZONE [number] SKILL [type]",
                "❓ لم يتم تسجيلك. للتسجيل:\n• أم: تسجيل ام مخيم [اسم] منطقة [رقم]\n• متطوع: تسجيل متطوع الاسم [اسم] مخيم [اسم] منطقة [رقم] مهارة [نوع]");
    }

    /**
     * Handle help request.
     * Returns a list of available commands in the appropriate language.
     */
    private String handleHelp(SmsCommand command) {
        Language lang = command.detectedLanguage();
        
        return getMessage(lang,
                """
                📱 SafeBirth Commands:
                
                REGISTRATION:
                • REG MOTHER CAMP [name] ZONE [number]
                • REG VOLUNTEER NAME [name] CAMP [name] ZONE [number] SKILL [type]
                
                REQUESTS:
                • EMERGENCY - Request urgent help
                • SUPPORT - Request non-urgent support
                
                VOLUNTEER:
                • ACCEPT HR-xxxx - Accept a case
                • COMPLETE HR-xxxx - Complete a case
                • AVAILABLE / BUSY - Change status
                
                • STATUS - Check your status
                • HELP - Show this message""",
                """
                📱 أوامر SafeBirth:
                
                التسجيل:
                • تسجيل ام مخيم [اسم] منطقة [رقم]
                • تسجيل متطوع الاسم [اسم] مخيم [اسم] منطقة [رقم] مهارة [نوع]
                
                الطلبات:
                • طوارئ - طلب مساعدة عاجلة
                • مساعدة - طلب دعم غير عاجل
                
                المتطوعين:
                • قبول HR-xxxx - قبول حالة
                • انهاء HR-xxxx - إنهاء حالة
                • متاح / مشغول - تغيير الحالة
                
                • حالة - التحقق من حالتك
                • مساعدة - عرض هذه الرسالة""");
    }

    /**
     * Handle unknown/unrecognized commands.
     */
    private String handleUnknown(SmsCommand command) {
        return getMessage(command.detectedLanguage(),
                "❓ Unknown command. Send HELP for available commands.",
                "❓ أمر غير معروف. أرسل 'مساعدة' للحصول على الأوامر المتاحة.");
    }

    // ==================== Notification Helpers ====================

    private void notifyMotherOfAcceptance(HelpRequest request, Volunteer volunteer) {
        Mother mother = request.getMother();
        Language lang = mother.getPreferredLanguage();
        
        String volunteerName = volunteer.getName() != null ? volunteer.getName() : volunteer.getFormattedId();
        String skillLabel = formatSkillType(volunteer.getSkillType(), lang);
        
        String message = getMessage(lang,
                "✅ Your request %s has been accepted!\nVolunteer: %s (%s)\nHelp is on the way.",
                "✅ تم قبول طلبك %s!\nالمتطوع: %s (%s)\nالمساعدة في الطريق.",
                request.getCaseId(), volunteerName, skillLabel);
        
        smsGateway.sendSms(mother.getPhoneNumber(), message);
    }

    private void notifyVolunteerOfCancellation(HelpRequest request) {
        Volunteer volunteer = request.getAcceptedBy();
        Language lang = volunteer.getPreferredLanguage();
        
        String message = getMessage(lang,
                "ℹ️ Case %s has been cancelled by the mother.",
                "ℹ️ تم إلغاء الحالة %s من قبل الأم.",
                request.getCaseId());
        
        smsGateway.sendSms(volunteer.getPhoneNumber(), message);
    }

    private void notifyMotherOfCancellation(HelpRequest request) {
        Mother mother = request.getMother();
        Language lang = mother.getPreferredLanguage();
        
        String message = getMessage(lang,
                "ℹ️ Your case %s has been cancelled by the volunteer. Send EMERGENCY to request help again.",
                "ℹ️ تم إلغاء حالتك %s من قبل المتطوع. أرسل 'طوارئ' لطلب المساعدة مرة أخرى.",
                request.getCaseId());
        
        smsGateway.sendSms(mother.getPhoneNumber(), message);
    }

    // ==================== Formatting Helpers ====================

    private String getMessage(Language language, String english, String arabic, Object... args) {
        String template = (language == Language.ARABIC) ? arabic : english;
        return String.format(template, args);
    }

    private String getMessage(Language language, String english, String arabic) {
        return (language == Language.ARABIC) ? arabic : english;
    }

    private String formatError(Language lang, String errorMessage) {
        return getMessage(lang,
                "❌ Error: " + errorMessage,
                "❌ خطأ: " + errorMessage);
    }

    private String formatSkillType(SkillType skill, Language lang) {
        return switch (skill) {
            case MIDWIFE -> lang == Language.ARABIC ? "قابلة" : "Midwife";
            case NURSE -> lang == Language.ARABIC ? "ممرضة" : "Nurse";
            case TRAINED_ATTENDANT -> lang == Language.ARABIC ? "مدربة" : "Trained Attendant";
            case COMMUNITY_HEALTH_WORKER -> lang == Language.ARABIC ? "عامل صحة مجتمعي" : "Community Health Worker";
            case COMMUNITY_VOLUNTEER -> lang == Language.ARABIC ? "متطوع مجتمعي" : "Community Volunteer";
        };
    }

    private String formatRiskLevel(RiskLevel risk, Language lang) {
        if (risk == null) return lang == Language.ARABIC ? "غير محدد" : "N/A";
        return switch (risk) {
            case HIGH -> lang == Language.ARABIC ? "عالية" : "HIGH";
            case MEDIUM -> lang == Language.ARABIC ? "متوسطة" : "MEDIUM";
            case LOW -> lang == Language.ARABIC ? "منخفضة" : "LOW";
        };
    }

    private String formatAvailability(AvailabilityStatus status, Language lang) {
        return switch (status) {
            case AVAILABLE -> lang == Language.ARABIC ? "متاح" : "AVAILABLE";
            case BUSY -> lang == Language.ARABIC ? "مشغول" : "BUSY";
            case OFFLINE -> lang == Language.ARABIC ? "غير متاح" : "OFFLINE";
        };
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
