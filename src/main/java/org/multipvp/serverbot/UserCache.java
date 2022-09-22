package org.multipvp.serverbot;

import org.bson.Document;
import org.multipvp.serverbot.mongodb.Connection;

import java.util.HashMap;
import java.util.Map;

public class UserCache {

    public static Map<String,String> minecraftUUIDDiscordId = new HashMap<>();
    public static Map<String,String> discordIdMinecraftUUID = new HashMap<>();
    public static String getDiscord(String minecraftUUID) {
        if (minecraftUUIDDiscordId.containsKey(minecraftUUID)) {
            return minecraftUUIDDiscordId.get(minecraftUUID);
        }
        Document d = (Document) Connection.userCol().find(new Document("minecraftUUID", minecraftUUID)).first();
        if (d != null) {
            String discordId = d.getString("discordID");
            if (discordId != null) {
                minecraftUUIDDiscordId.put(minecraftUUID,discordId);
                discordIdMinecraftUUID.put(discordId,minecraftUUID);
            } else {
                minecraftUUIDDiscordId.put(minecraftUUID,"UNVERIFIED");
                discordIdMinecraftUUID.put(discordId,"UNVERIFIED");
            }
            return discordId;
        }
        return null;
    }
}
