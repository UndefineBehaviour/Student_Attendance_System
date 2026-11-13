package com.group5.attendance.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * The rotating code shown in the live QR.
 * token = first 12 bytes of HMAC-SHA256(secret, sessionId + ":" + timeWindow), URL-safe Base64.
 * Nothing is stored in the database: the server just recomputes the token and compares.
 * A new window starts every STEP_SECONDS; a scan is accepted for the current window and the
 * GRACE_WINDOWS before it (so a slow phone camera still works, but an old screenshot does not).
 */
public final class QrToken {

    public static final int STEP_SECONDS = 5;
    public static final int GRACE_WINDOWS = 2;

    private final byte[] secret;

    public QrToken(byte[] secret) {
        this.secret = secret.clone();
    }

    public String create(int sessionId, long nowEpochSeconds) {
        return compute(sessionId, nowEpochSeconds / STEP_SECONDS);
    }

    public boolean verify(int sessionId, String token, long nowEpochSeconds) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        byte[] given = token.getBytes(StandardCharsets.UTF_8);
        long current = nowEpochSeconds / STEP_SECONDS;
        boolean ok = false;
        for (int back = 0; back <= GRACE_WINDOWS; back++) {      // no early exit: same work for every try
            byte[] expected = compute(sessionId, current - back).getBytes(StandardCharsets.UTF_8);
            ok |= MessageDigest.isEqual(expected, given);
        }
        return ok;
    }

    private String compute(int sessionId, long window) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] full = mac.doFinal((sessionId + ":" + window).getBytes(StandardCharsets.UTF_8));
            byte[] part = java.util.Arrays.copyOf(full, 12);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(part);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
