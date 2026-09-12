package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrInitResponse {

    private String sessionId;
    private String qrCodeUri;
    private String wsTopic;
    private int expiresInSeconds;
    private Instant expiresAt;

    public static QrInitResponseBuilder builder() { return new QrInitResponseBuilder(); }
    public static class QrInitResponseBuilder {
        private String sessionId;
        private String qrCodeUri;
        private String wsTopic;
        private int expiresInSeconds;
        private Instant expiresAt;

        public QrInitResponseBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public QrInitResponseBuilder qrCodeUri(String qrCodeUri) { this.qrCodeUri = qrCodeUri; return this; }
        public QrInitResponseBuilder wsTopic(String wsTopic) { this.wsTopic = wsTopic; return this; }
        public QrInitResponseBuilder expiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; return this; }
        public QrInitResponseBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public QrInitResponse build() {
            QrInitResponse r = new QrInitResponse();
            r.setSessionId(sessionId);
            r.setQrCodeUri(qrCodeUri);
            r.setWsTopic(wsTopic);
            r.setExpiresInSeconds(expiresInSeconds);
            r.setExpiresAt(expiresAt);
            return r;
        }
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getQrCodeUri() { return qrCodeUri; }
    public void setQrCodeUri(String qrCodeUri) { this.qrCodeUri = qrCodeUri; }
    public String getWsTopic() { return wsTopic; }
    public void setWsTopic(String wsTopic) { this.wsTopic = wsTopic; }
    public int getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
