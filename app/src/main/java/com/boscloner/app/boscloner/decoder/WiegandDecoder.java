package com.boscloner.app.boscloner.decoder;

import android.util.Log;

public class WiegandDecoder {
    private static final String LOG_TAG = "WiegandDecoder";

    public enum CardFormat {
        BIT_26,
        BIT_35
    }

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

    public static DecodedCard decode(byte[] data, CardFormat format) {
        if (data.length != 5) {
            throw new IllegalArgumentException("Input must be 5 bytes long");
        }

        long fullCode = bytesToLong(data);
        String binaryString = String.format("%40s", Long.toBinaryString(fullCode)).replace(' ', '0');
        Log.d(LOG_TAG, "Full binary (40 bits): " + binaryString);

        switch (format) {
            case BIT_26:
                return decode26Bit(fullCode);
            case BIT_35:
                return decode35Bit(fullCode);
            default:
                Log.d(LOG_TAG, "Unsupported format");
                return null;
        }
    }

    private static DecodedCard decode26Bit(long code) {
        long bits26 = code & 0x3FFFFFF;
        int facilityCode = (int)((bits26 >> 17) & 0xFF);
        int cardNumber = (int)((bits26 >> 1) & 0xFFFF);
        Log.d(LOG_TAG, "26-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("26-bit H10301", cardNumber, facilityCode);
    }

    private static DecodedCard decode35Bit(long code) {
        // Extract the last 35 bits (ignore the leading 5 bits)
        long bits35 = code & 0x7FFFFFFF;

        // Facility code = bits 2 to 14 (13 bits)
        int facilityCode = (int)((bits35 >> 21) & 0x1FFF);

        // Card code = bits 15 to 34 (20 bits)
        long cardNumber = (bits35 >> 1) & 0xFFFFF;

        Log.d(LOG_TAG, "35-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("35-bit HID Corporate 1000", cardNumber, facilityCode);
    }

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}
