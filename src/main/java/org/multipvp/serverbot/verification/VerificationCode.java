package org.multipvp.serverbot.verification;

//public class VerificationCode {
//    String mcuuid;
//    String c;
//    String dscid;
//    VerificationCode( String code, String minecraftUUID, String discordID) {
//        mcuuid = minecraftUUID;
//        c = code;
//        dscid = discordID;
//    }
//}

public class VerificationCode {

    public String mcUUID;
    public String c;
    public String dID;
    public String mcUN;


    public VerificationCode( String code, String discordID, String minecraftUUID, String minecraftUsername ) {

        mcUUID = minecraftUUID;
        c  = code;
        dID = discordID;
        mcUN = minecraftUsername;
    }

}