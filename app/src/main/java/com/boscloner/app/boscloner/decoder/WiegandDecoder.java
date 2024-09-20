package com.boscloner.app.boscloner.decoder;

import android.util.Log;

public class WiegandDecoder {
    private static final String LOG_TAG = "WiegandDecoder";

    public static class DecodedCard {
        public String format;
        public long cardNumber;
        public int facilityCode;

        public DecodedCard(String format, long cardNumber, int facilityCode) {
            this.format = format;
            this.cardNumber = cardNumber;
            this.facilityCode = facilityCode;
        }

        @Override
        public String toString() {
            return "Format: " + format + ", Card Number: " + cardNumber +
                    (facilityCode != -1 ? ", Facility Code: " + facilityCode : "");
        }
    }

    public static DecodedCard decode(byte[] data) {
        if (data.length != 5) {
            throw new IllegalArgumentException("Input must be 5 bytes long");
        }

        long fullCode = bytesToLong(data);
        String binaryString = String.format("%40s", Long.toBinaryString(fullCode)).replace(' ', '0');
        Log.d(LOG_TAG, "Full binary (40 bits): " + binaryString);

        // Try decoding as 26-bit
        DecodedCard result = decode26Bit(fullCode);
        if (result != null) {
            return result;
        }

        // Try decoding as 35-bit
        result = decode35Bit(fullCode);
        if (result != null) {
            return result;
        }

        Log.d(LOG_TAG, "Unable to decode as 26-bit or 35-bit format");
        return null;
    }

    private static DecodedCard decode26Bit(long code) {
        // Extract the last 26 bits
        long bits26 = code & 0x3FFFFFF;
        Log.d(LOG_TAG, "Attempting 26-bit decode. Last 26 bits: " + Long.toBinaryString(bits26));

        // 26-bit format: 1 parity bit + 8 bit facility code + 16 bit card number + 1 parity bit
        int facilityCode = (int)((bits26 >> 17) & 0xFF);
        int cardNumber = (int)((bits26 >> 1) & 0xFFFF);

        Log.d(LOG_TAG, "26-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("26-bit H10301", cardNumber, facilityCode);
    }

    private static DecodedCard decode35Bit(long code) {
        // Extract the last 35 bits
        long bits35 = code & 0x7FFFFFFF;
        Log.d(LOG_TAG, "Attempting 35-bit decode. Last 35 bits: " + Long.toBinaryString(bits35));

        // 35-bit format: 1 parity bit + 12 bit facility code + 21 bit card number + 1 parity bit
        int facilityCode = (int)((bits35 >> 22) & 0xFFF);
        long cardNumber = (bits35 >> 1) & 0x1FFFFF;

        Log.d(LOG_TAG, "35-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("35-bit Corporate 1000", cardNumber, facilityCode);
    }

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}