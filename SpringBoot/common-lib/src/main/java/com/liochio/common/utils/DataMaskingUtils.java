package com.liochio.common.utils;

public final class DataMaskingUtils {

    private DataMaskingUtils() {}

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 6) return idCard;
        int len = idCard.length();
        int visible = Math.min(len - 4, 8);
        return idCard.substring(0, visible) + "****";
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        int len = phone.length();
        return phone.substring(0, 3) + "*****" + phone.substring(len - 2);
    }

    public static String maskBankCard(String card) {
        if (card == null || card.length() < 8) return card;
        int len = card.length();
        return card.substring(0, 4) + "********" + card.substring(len - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }
}
