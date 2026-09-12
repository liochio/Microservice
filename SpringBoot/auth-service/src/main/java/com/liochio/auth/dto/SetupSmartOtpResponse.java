package com.liochio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupSmartOtpResponse {

    private String secret;
    private String qrBarcodeUri;
    private String issuer;
    private String accountName;

    public static SetupSmartOtpResponseBuilder builder() { return new SetupSmartOtpResponseBuilder(); }
    public static class SetupSmartOtpResponseBuilder {
        private String secret;
        private String qrBarcodeUri;
        private String issuer;
        private String accountName;

        public SetupSmartOtpResponseBuilder secret(String secret) { this.secret = secret; return this; }
        public SetupSmartOtpResponseBuilder qrBarcodeUri(String qrBarcodeUri) { this.qrBarcodeUri = qrBarcodeUri; return this; }
        public SetupSmartOtpResponseBuilder issuer(String issuer) { this.issuer = issuer; return this; }
        public SetupSmartOtpResponseBuilder accountName(String accountName) { this.accountName = accountName; return this; }
        public SetupSmartOtpResponse build() {
            SetupSmartOtpResponse r = new SetupSmartOtpResponse();
            r.setSecret(secret);
            r.setQrBarcodeUri(qrBarcodeUri);
            r.setIssuer(issuer);
            r.setAccountName(accountName);
            return r;
        }
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getQrBarcodeUri() { return qrBarcodeUri; }
    public void setQrBarcodeUri(String qrBarcodeUri) { this.qrBarcodeUri = qrBarcodeUri; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
}
