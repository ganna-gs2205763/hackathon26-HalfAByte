package com.halfabyte.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * BroadcastReceiver that listens for incoming SMS messages.
 * Forwards received messages to SmsGatewayService for processing.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String PDU_TYPE = "pdus";
    private static final String FORMAT = "format";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        Object[] pdus = (Object[]) bundle.get(PDU_TYPE);
        if (pdus == null || pdus.length == 0) {
            return;
        }

        String format = bundle.getString(FORMAT);

        // Process each PDU (Protocol Data Unit)
        StringBuilder fullMessage = new StringBuilder();
        String senderNumber = null;

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (smsMessage != null) {
                if (senderNumber == null) {
                    senderNumber = smsMessage.getDisplayOriginatingAddress();
                }
                fullMessage.append(smsMessage.getMessageBody());
            }
        }

        if (senderNumber != null && fullMessage.length() > 0) {
            Log.d(TAG, "SMS received from: " + senderNumber);

            // Forward to the service
            Intent serviceIntent = new Intent(context, SmsGatewayService.class);
            serviceIntent.setAction(SmsGatewayService.ACTION_SMS_RECEIVED);
            serviceIntent.putExtra(SmsGatewayService.EXTRA_SENDER, senderNumber);
            serviceIntent.putExtra(SmsGatewayService.EXTRA_MESSAGE, fullMessage.toString());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
