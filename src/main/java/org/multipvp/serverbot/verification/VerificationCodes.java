package org.multipvp.serverbot.verification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class VerificationCodes {
    static VerificationCode[] codes = {};
    static Map<String,String> minecraftNameDiscord = new HashMap<>();
    static Map<String,VerificationCode> codesV2 = new HashMap<>();

    public static VerificationCode[] getCodes() {
        return codes;
    }
    public static VerificationCode getCodesV2(String discordId) {
        return codesV2.get(discordId);
    }

    public static String generate() {
        Random rand = new Random();
        String code = String.format("%04d", rand.nextInt(10000));
        System.out.println(code);

        return code;
    }
    public static void addCode(String code, String discordId, String minecraftUUID, String minecraftUsername) {
        codes = Arrays.copyOf(codes, codes.length + 1);

        codes[codes.length - 1] = new VerificationCode(code, discordId, minecraftUUID, minecraftUsername);
        System.out.println(Arrays.toString(codes));
    }
    public static boolean addCodeV2(String code, String discordId, String minecraftUUID, String minecraftUsername) {
        if (minecraftNameDiscord.containsKey(minecraftUUID)) {
            return false;
        }
        codesV2.put(discordId,new VerificationCode(code, discordId, minecraftUUID, minecraftUsername));
        minecraftNameDiscord.put(minecraftUUID,discordId);
        //System.out.println(Arrays.toString());
        return true;
    }


}
