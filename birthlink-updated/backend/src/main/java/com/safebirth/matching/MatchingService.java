package com.safebirth.matching;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerRepository;
import com.safebirth.sms.gateway.SmsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service for matching help requests to appropriate volunteers.
 * 
 * Matching Priority:
 * 1. Certified (MIDWIFE, NURSE) + Same Zone + Available
 * 2. Trained Attendant + Same Zone + Available
 * 3. Any Volunteer + Same Zone + Available
 * 4. Expand to adjacent zones (not implemented in POC)
 */
@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final VolunteerRepository volunteerRepository;
    private final SmsGateway smsGateway;
    private final HelpRequestService helpRequestService;

    public MatchingService(VolunteerRepository volunteerRepository, SmsGateway smsGateway,
                          HelpRequestService helpRequestService) {
        this.volunteerRepository = volunteerRepository;
        this.smsGateway = smsGateway;
        this.helpRequestService = helpRequestService;
    }

    /**
     * Match volunteers and notify them about a help request.
     * This is the main entry point for the matching flow.
     *
     * @param request the help request to match
     * @return list of volunteers who were notified
     */
    @Transactional
    public List<Volunteer> matchAndNotify(HelpRequest request) {
        log.info("🔍 Starting match and notify for request {} (type: {}, zone: {})",
                request.getCaseId(), request.getRequestType(), request.getZone());

        List<Volunteer> volunteersToNotify = findVolunteersToAlert(request);

        if (volunteersToNotify.isEmpty()) {
            log.warn("⚠️ No available volunteers found for request {} in zone {}",
                    request.getCaseId(), request.getZone());
            return List.of();
        }

        // Notify each volunteer
        int notified = 0;
        for (Volunteer volunteer : volunteersToNotify) {
            try {
                notifyVolunteer(volunteer, request);
                notified++;
            } catch (Exception e) {
                log.error("Failed to notify volunteer {}: {}", volunteer.getFormattedId(), e.getMessage());
            }
        }

        // Update alerts sent counter
        helpRequestService.incrementAlertsSent(request.getCaseId());
        
        log.info("✅ Notified {} volunteers for request {}", notified, request.getCaseId());
        return volunteersToNotify;
    }

    /**
     * Find the best matching volunteers for a help request.
     *
     * @param request the help request
     * @param limit   maximum number of volunteers to return
     * @return list of matching volunteers, ordered by priority
     */
    public List<Volunteer> findMatchingVolunteers(HelpRequest request, int limit) {
        log.info("Finding volunteers for request {} in zone {}", request.getCaseId(), request.getZone());

        List<Volunteer> available = volunteerRepository.findAvailableByZone(request.getZone());
        
        if (available.isEmpty()) {
            log.warn("No available volunteers in zone {}", request.getZone());
            return List.of();
        }

        // Sort by skill priority (certified first)
        List<Volunteer> sorted = available.stream()
                .sorted(Comparator.comparingInt(v -> v.getSkillType().getPriority()))
                .limit(limit)
                .toList();

        log.info("Found {} matching volunteers for request {}", sorted.size(), request.getCaseId());
        return sorted;
    }

    /**
     * Find the single best volunteer for a help request.
     *
     * @param request the help request
     * @return the best matching volunteer, or null if none available
     */
    public Volunteer findBestVolunteer(HelpRequest request) {
        List<Volunteer> matches = findMatchingVolunteers(request, 1);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * Find all available volunteers who should receive alerts for a request.
     * For emergencies, alerts go to all available volunteers in the zone.
     *
     * @param request the help request
     * @return list of volunteers to alert
     */
    public List<Volunteer> findVolunteersToAlert(HelpRequest request) {
        log.info("Finding volunteers to alert for request {} (type: {})", 
                request.getCaseId(), request.getRequestType());

        List<Volunteer> toAlert = new ArrayList<>();
        String zone = request.getZone();

        // For emergencies, alert all available certified volunteers first
        if (request.isEmergency()) {
            // Priority 1: Certified volunteers in zone
            List<Volunteer> certified = volunteerRepository.findAvailableByZone(zone).stream()
                    .filter(v -> v.getSkillType() == SkillType.MIDWIFE || v.getSkillType() == SkillType.NURSE)
                    .toList();
            toAlert.addAll(certified);

            // Priority 2: Trained attendants
            List<Volunteer> trained = volunteerRepository.findAvailableByZone(zone).stream()
                    .filter(v -> v.getSkillType() == SkillType.TRAINED_ATTENDANT)
                    .toList();
            toAlert.addAll(trained);

            // Priority 3: Community workers if no one else
            if (toAlert.isEmpty()) {
                toAlert.addAll(volunteerRepository.findAvailableByZone(zone));
            }
        } else {
            // For support requests, alert community workers first
            toAlert.addAll(volunteerRepository.findAvailableByZone(zone).stream()
                    .sorted(Comparator.comparingInt((Volunteer v) -> v.getSkillType().getPriority()).reversed())
                    .toList());
        }

        log.info("Will alert {} volunteers for request {}", toAlert.size(), request.getCaseId());
        return toAlert;
    }

    /**
     * Notify a single volunteer about a help request.
     * Sends an SMS alert in the volunteer's preferred language.
     *
     * @param volunteer the volunteer to notify
     * @param request   the help request
     */
    public void notifyVolunteer(Volunteer volunteer, HelpRequest request) {
        log.info("📱 Notifying volunteer {} about request {}", 
                volunteer.getFormattedId(), request.getCaseId());

        String alertMessage = buildAlertMessage(volunteer, request);
        smsGateway.sendSms(volunteer.getPhoneNumber(), alertMessage);
        
        log.debug("Alert sent to {}: {}", volunteer.getPhoneNumber(), alertMessage);
    }

    /**
     * Build a bilingual alert message for a volunteer.
     *
     * @param volunteer the volunteer (for language preference)
     * @param request   the help request
     * @return formatted alert message
     */
    public String buildAlertMessage(Volunteer volunteer, HelpRequest request) {
        Language lang = volunteer.getPreferredLanguage();

        String typeLabel = request.isEmergency()
                ? (lang == Language.ARABIC ? "طوارئ" : "EMERGENCY")
                : (lang == Language.ARABIC ? "مساعدة" : "SUPPORT");

        String riskLabel = formatRiskLevel(request.getRiskLevel(), lang);
        String dueDateStr = formatDueDate(request.getDueDate(), lang);
        String zone = request.getZone();
        String caseId = request.getCaseId();
        String motherPhone = request.getMother().getPhoneNumber();

        if (lang == Language.ARABIC) {
            return String.format("""
                    🚨 %s منطقة %s
                    الخطورة: %s | الموعد: %s
                    📞 رقم الأم: %s
                    للقبول أرسل: قبول %s""",
                    typeLabel, zone, riskLabel, dueDateStr, motherPhone, caseId);
        } else {
            return String.format("""
                    🚨 %s Zone %s
                    Risk: %s | Due: %s
                    📞 Mother: %s
                    Reply: ACCEPT %s""",
                    typeLabel, zone, riskLabel, dueDateStr, motherPhone, caseId);
        }
    }

    /**
     * Check if there are any available volunteers for a zone.
     *
     * @param zone the zone identifier
     * @return true if volunteers are available
     */
    public boolean hasAvailableVolunteers(String zone) {
        return !volunteerRepository.findAvailableByZone(zone).isEmpty();
    }

    /**
     * Count available volunteers in a zone.
     *
     * @param zone the zone identifier
     * @return count of available volunteers
     */
    public long countAvailableInZone(String zone) {
        return volunteerRepository.findAvailableByZone(zone).size();
    }

    /**
     * Format risk level for display in the specified language.
     */
    private String formatRiskLevel(RiskLevel risk, Language lang) {
        if (risk == null) {
            return lang == Language.ARABIC ? "غير محدد" : "N/A";
        }
        return switch (risk) {
            case HIGH -> lang == Language.ARABIC ? "عالية" : "HIGH";
            case MEDIUM -> lang == Language.ARABIC ? "متوسطة" : "MEDIUM";
            case LOW -> lang == Language.ARABIC ? "منخفضة" : "LOW";
        };
    }

    /**
     * Format due date for display in the specified language.
     */
    private String formatDueDate(LocalDate dueDate, Language lang) {
        if (dueDate == null) {
            return lang == Language.ARABIC ? "غير محدد" : "N/A";
        }

        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntil <= 0) {
            return lang == Language.ARABIC ? "اليوم/متأخر" : "Today/Overdue";
        } else if (daysUntil == 1) {
            return lang == Language.ARABIC ? "غداً" : "Tomorrow";
        } else if (daysUntil <= 7) {
            return lang == Language.ARABIC 
                    ? daysUntil + " أيام"
                    : daysUntil + " days";
        } else {
            return dueDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        }
    }
}
