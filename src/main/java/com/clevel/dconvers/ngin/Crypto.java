package com.clevel.dconvers.ngin;

import org.apache.commons.codec.binary.Base64;

public final class Crypto {

    public static Base64 base64 = new Base64();

    public static final String encrypt(String plain) {
        byte[] round1 = base64.encode(plain.getBytes());
        byte[] round2 = base64.encode(round1);
        return new String(round2);
    }

    public static final String decrypt(String encrypted) {
        byte[] round1 = base64.decode(encrypted.getBytes());
        byte[] round2 = base64.decode(round1);
        return new String(round2);
    }

}
