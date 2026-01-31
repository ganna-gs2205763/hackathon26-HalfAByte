package com.halfabyte.smsgateway;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for sending SMS messages.
 * Handles long messages by splitting them into parts.
 */
public class SmsSender {
    private static final String TAG = "SmsSender";
    private static final String ACTION_SMS_SENT = "com.halfabyte.smsgateway.SMS_SENT";
    private static final long SEND_TIMEOUT_MS = 30000; // 30 second timeout

    public interface SendCallback {
        void onResult(List<String> recipients, int successCount, int failureCount);
    }

    private final Context context;
    private final Handler mainHandler;

    public SmsSender(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Send an SMS message to multiple recipients.
     * 
     * @param recipients List of phone numbers to send to
     * @param message    The message content
     * @param callback   Callback for result
     */
    public void sendSms(List<String> recipients, String message, SendCallback callback) {
        if (recipients == null || recipients.isEmpty()) {
            Log.w(TAG, "No recipients provided");
            if (callback != null) {
                callback.onResult(recipients != null ? recipients : new ArrayList<>(), 0, 0);
            }
            return;
        }

        if (message == null || message.isEmpty()) {
            Log.w(TAG, "Empty message");
            if (callback != null) {
                callback.onResult(recipients, 0, recipients.size());
            }
            return;
        }

        Log.d(TAG, "Preparing to send SMS to " + recipients.size() + " recipients");
        Log.d(TAG, "Message: " + message.substring(0, Math.min(50, message.length())) + "...");

        SmsManager smsManager;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            if (smsManager == null) {
                Log.e(TAG, "SmsManager is null - SMS not available on this device");
                if (callback != null) {
                    callback.onResult(recipients, 0, recipients.size());
                }
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get SmsManager", e);
            if (callback != null) {
                callback.onResult(recipients, 0, recipients.size());
            }
            return;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger pendingCount = new AtomicInteger(recipients.size());
        final boolean[] callbackCalled = { false };

        // Create receiver for sent status
        BroadcastReceiver sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                int resultCode = getResultCode();

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        successCount.incrementAndGet();
                        Log.d(TAG, "SMS sent successfully");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        failureCount.incrementAndGet();
                        Log.e(TAG, "SMS failed: Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        failureCount.incrementAndGet();
                        Log.e(TAG, "SMS failed: No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        failureCount.incrementAndGet();
                        Log.e(TAG, "SMS failed: Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        failureCount.incrementAndGet();
                        Log.e(TAG, "SMS failed: Radio off");
                        break;
                    default:
                        failureCount.incrementAndGet();
                        Log.e(TAG, "SMS failed with code: " + resultCode);
                        break;
                }

                if (pendingCount.decrementAndGet() == 0) {
                    finishSending(ctx, this, callback, recipients, successCount.get(), failureCount.get(),
                            callbackCalled);
                }
            }
        };

        // Register receiver
        try {
            IntentFilter filter = new IntentFilter(ACTION_SMS_SENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(sentReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(sentReceiver, filter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to register receiver", e);
            if (callback != null) {
                callback.onResult(recipients, 0, recipients.size());
            }
            return;
        }

        // Set timeout to ensure callback is always called
        mainHandler.postDelayed(() -> {
            if (!callbackCalled[0]) {
                Log.w(TAG, "SMS send timeout - forcing callback");
                finishSending(context, sentReceiver, callback, recipients, successCount.get(),
                        recipients.size() - successCount.get(), callbackCalled);
            }
        }, SEND_TIMEOUT_MS);

        // Send to each recipient
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        for (int i = 0; i < recipients.size(); i++) {
            String recipient = recipients.get(i);

            if (recipient == null || recipient.trim().isEmpty()) {
                Log.w(TAG, "Skipping empty recipient");
                failureCount.incrementAndGet();
                if (pendingCount.decrementAndGet() == 0) {
                    finishSending(context, sentReceiver, callback, recipients, successCount.get(), failureCount.get(),
                            callbackCalled);
                }
                continue;
            }

            try {
                Log.d(TAG, "Sending SMS to: " + recipient);

                ArrayList<String> parts = smsManager.divideMessage(message);
                Log.d(TAG, "Message divided into " + parts.size() + " part(s)");

                if (parts.size() == 1) {
                    // Single part message
                    Intent sentIntent = new Intent(ACTION_SMS_SENT);
                    sentIntent.setPackage(context.getPackageName());
                    PendingIntent sentPI = PendingIntent.getBroadcast(context, i, sentIntent, flags);

                    smsManager.sendTextMessage(recipient, null, message, sentPI, null);
                } else {
                    // Multi-part message
                    ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                    for (int j = 0; j < parts.size(); j++) {
                        Intent sentIntent = new Intent(ACTION_SMS_SENT);
                        sentIntent.setPackage(context.getPackageName());
                        sentIntents.add(PendingIntent.getBroadcast(context, i * 100 + j, sentIntent, flags));
                    }
                    // Adjust pending count for multi-part (each part triggers a callback)
                    pendingCount.addAndGet(parts.size() - 1);
                    smsManager.sendMultipartTextMessage(recipient, null, parts, sentIntents, null);
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied for sending SMS to " + recipient, e);
                failureCount.incrementAndGet();
                if (pendingCount.decrementAndGet() == 0) {
                    finishSending(context, sentReceiver, callback, recipients, successCount.get(), failureCount.get(),
                            callbackCalled);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + recipient, e);
                failureCount.incrementAndGet();
                if (pendingCount.decrementAndGet() == 0) {
                    finishSending(context, sentReceiver, callback, recipients, successCount.get(), failureCount.get(),
                            callbackCalled);
                }
            }
        }
    }

    private void finishSending(Context ctx, BroadcastReceiver receiver, SendCallback callback,
            List<String> recipients, int success, int failure, boolean[] callbackCalled) {
        synchronized (callbackCalled) {
            if (callbackCalled[0]) {
                return;
            }
            callbackCalled[0] = true;
        }

        try {
            ctx.unregisterReceiver(receiver);
        } catch (Exception e) {
            // Ignore - receiver may already be unregistered
        }

        Log.d(TAG, "SMS sending complete: " + success + " succeeded, " + failure + " failed");

        if (callback != null) {
            mainHandler.post(() -> callback.onResult(recipients, success, failure));
        }
    }
}
