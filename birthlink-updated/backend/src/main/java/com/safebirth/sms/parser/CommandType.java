package com.safebirth.sms.parser;

/**
 * Types of SMS commands supported by the system.
 * Each command maps to both English and Arabic keywords.
 */
public enum CommandType {
    
    // Registration Commands
    REGISTER_MOTHER,      // REG MOTHER / تسجيل ام
    REGISTER_VOLUNTEER,   // REG VOLUNTEER / تسجيل متطوع
    
    // Emergency Commands
    EMERGENCY,            // EMERGENCY, SOS / طوارئ
    SUPPORT,              // SUPPORT / مساعدة
    
    // Case Management
    ACCEPT_CASE,          // ACCEPT HR-xxxx / قبول xxxx
    COMPLETE_CASE,        // COMPLETE HR-xxxx / انهاء xxxx
    CANCEL_CASE,          // CANCEL HR-xxxx / الغاء xxxx
    
    // Availability Status
    AVAILABLE,            // AVAILABLE / متاح
    BUSY,                 // BUSY / مشغول
    OFFLINE,              // OFFLINE / غير متاح
    
    // Information Commands
    STATUS,               // STATUS / حالة
    HELP,                 // HELP / مساعدة
    
    // Unknown/Invalid
    UNKNOWN
}
