package org.multipvp.serverbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.multipvp.serverbot.mongodb.Connection;
import org.multipvp.serverbot.verification.VerificationCode;
import org.multipvp.serverbot.verification.VerificationCodes;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class EventListener extends ListenerAdapter {
    public long lastTimeForDrop = new Date().getTime();
    public Map<String,Set<String>> previousSet;
    public Map<String,Set<String>> previousMinecraftId = new HashMap<>();
    public Map<String,Set<String>> currentMinecraftId;
    public static void arrangeMembers(Map<String, Set<String>> groups) {
        if (instance == null) return;
        long currentTime = new Date().getTime();
        if (Main.gson.toJson(groups).equals(Main.gson.toJson(instance.previousSet))) return;
        if (instance.lastTimeForDrop + 1000 > (currentTime)) return;
        instance.lastTimeForDrop = currentTime;
        int groupsNeeded = groups.size() + 3;
        AtomicInteger index = new AtomicInteger(0);
        Bot.category.getChannels().forEach(guildChannel -> {
            index.incrementAndGet();
            if (!guildChannel.getId().equals("1022410179888820224")) {
                if (index.get() > groupsNeeded) {
                    guildChannel.delete().queue();
                }
            }
        });
        while (groupsNeeded > Bot.category.getChannels().size()) {
            Bot.category.createVoiceChannel(UUID.nameUUIDFromBytes(String.format("group%d",Bot.category.getChannels().size()).getBytes()).toString()).complete();
        }
        instance.previousMinecraftId = instance.currentMinecraftId;
        instance.currentMinecraftId = new HashMap<>();
        for (String group : groups.keySet()) {
            VoiceChannel channel = Bot.mainGuild.getVoiceChannelsByName(group,true).get(0);
            Set<String> minecraftNames = new HashSet<>();
            for (String member : groups.get(group)) {
                String discordId = UserCache.getDiscord(member);
                if (discordId != null) {
                    try {
                        //System.out.println(discordId);
                        Member discordUser = Bot.mainGuild.getMemberById(discordId);//.getVoiceState().inAudioChannel()
                        //if (discordUser.getVoiceState().inAudioChannel()) {
                        if (Bot.category.getChannels().contains(discordUser.getVoiceState().getChannel())) {
                            if (!discordUser.getVoiceState().getChannel().equals(channel)) {

                                Bot.mainGuild.moveVoiceMember(discordUser, channel).complete();

                            }
                            minecraftNames.add(UserCache.discordIdMinecraftUUID.get(discordUser.getId()));
                        }
                        //}
                    } catch (Exception ignored) {
                       // ignored.printStackTrace();
                    }
                }
            }
            for (String member : groups.get(group)) {
                instance.currentMinecraftId.put(member,minecraftNames);
            }
        }
        for (String member : instance.currentMinecraftId.keySet()) {
            if (instance.previousMinecraftId.get(member) == null) continue;
            HashSet<String> oldConnections = new HashSet<>(instance.previousMinecraftId.get(member));
            HashSet<String> oldConnections2 = new HashSet<>(instance.previousMinecraftId.get(member));
            HashSet<String> newConnections = new HashSet<>(instance.currentMinecraftId.get(member));
            oldConnections.removeAll(newConnections);
            newConnections.removeAll(oldConnections2);
            for (String p : newConnections) {
                if (p.equals(member)) continue;
                Main.proxyServer.getPlayer(UUID.fromString(member)).sendMessage(new ComponentBuilder("[PC] ").bold(true).color(ChatColor.BLUE).append(String.format("+ %s", Main.proxyServer.getPlayer(UUID.fromString(p)).getName())).color(ChatColor.GREEN).bold(false).create());//(String.format("[PC]%s %s %s",ChatColor.RED,"-",Main.proxyServer.getPlayer(UUID.fromString(p)).getName()));
            }
            for (String p : oldConnections) {
                if (p.equals(member)) continue;
                Main.proxyServer.getPlayer(UUID.fromString(member)).sendMessage(new ComponentBuilder("[PC] ").bold(true).color(ChatColor.BLUE).append(String.format("- %s", Main.proxyServer.getPlayer(UUID.fromString(p)).getName())).color(ChatColor.RED).bold(false).create());
            }
        }
    }
    public static EventListener instance;
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        instance = this;
        Bot.mainGuild =   event.getJDA().getGuildById(1016628358848712704L);
        Bot.category = event.getJDA().getCategoryById(1022394493837791242L);
        Bot.verifiedRole = Bot.mainGuild.getRoleById(1022410790004867102L);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("verify_start_button")) {
            TextInput username = TextInput.create("username", "Minecraft Username", TextInputStyle.SHORT)
                    .setPlaceholder("JohnDoe")
                    .setMinLength(3)
                    .setMaxLength(16)
                    .build();
            Modal modal = Modal.create("verify_start_modal", "Discord + MultiPvP ")
                    .addActionRows(ActionRow.of(username))
                    .build();

            event.replyModal(modal).queue();
        } else if (event.getComponentId().equals("verify_code_button")) {
            TextInput code = TextInput.create("code", "Verification code", TextInputStyle.SHORT)
                    .setPlaceholder("0123")
                    .setMinLength(4)
                    .setMaxLength(16)
                    .build();
            Modal modal = Modal.create("verify_code_modal", "Verify code")
                    .addActionRows(ActionRow.of(code))
                    .build();

            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("verify_start_modal")) {

            String discordID = event.getUser().getId();
            // TODO: Make sure to remove any codes that already exist
            if (VerificationCodes.getCodesV2(discordID) != null) {
                return;
            }
            String minecraftUsername = event.getValue("username").getAsString();
            String code = VerificationCodes.generate();

            ProxiedPlayer igPlayer = Main.proxyServer.getPlayer(minecraftUsername);
            boolean online = igPlayer!=null;

            if (online) {
                String minecraftUUID =igPlayer.getUniqueId().toString();
                igPlayer.sendMessage(new ComponentBuilder("==================================").create());
                igPlayer.sendMessage(new ComponentBuilder("Your one-time verification code is ").color(ChatColor.BLUE).append(code).color(ChatColor.GOLD).color(ChatColor.BOLD).create());
                igPlayer.sendMessage(new ComponentBuilder("==================================").create());
                boolean hasCode = VerificationCodes.addCodeV2(code, discordID, minecraftUUID, minecraftUsername);
                if (hasCode) {
                    event.reply(String.format("Thanks, %s. I've just sent a verification code in-game, please enter it to verify your account.", event.getUser().getName())).addActionRow(
                            Button.primary("verify_code_button", "I've got the code!")
                    ).queue();
                } else {
                    event.reply(String.format("That user account is already getting verified!", event.getUser().getName())).queue();
                }
            } else {
                event.reply(String.format("Hmm, %s is not online. Make sure to login to `play.multipvp.xyz` and try again.", minecraftUsername));
            }

        }
        if (event.getModalId().equals("verify_code_modal")) {
            String codeInput = event.getValue("code").getAsString();
            int i = 0;
            VerificationCode code = VerificationCodes.getCodesV2(event.getUser().getId());
            if (code != null) {
//            for (VerificationCode code : VerificationCodes.getCodes()) {
//                if (code.dID.equals(event.getUser().getId())) {
                    if (codeInput.equals(code.c)) {
                        // TODO: persist in mongodb
                        try {
                            Connection.userCol().insertOne(new Document("discordID", code.dID)
                                    .append("minecraftUUID", code.mcUUID));
                            Bot.mainGuild.modifyNickname(event.getMember(),code.mcUN);
                            Bot.mainGuild.modifyMemberRoles(event.getMember(),List.of(new Role[]{Bot.verifiedRole}), Collections.emptyList());
                        } catch (Exception e) {
                            event.reply(String.format("❗️ERROR: %s", e));
                        }

                        event.reply(String.format("Successfully linked `%s` to `%s` (%s)! 🎉", event.getUser().getName(), code.mcUN, code.mcUUID)).queue();

                    } else {
                        event.reply("Hmm, that code is invalid. Please generate another.").addActionRow(
                                Button.primary("verify_start_button", "Generate another code")).queue();
                        // TODO: remove code once fail, or make 3 tries idk
                    }
                    return;

//                }
//                i++;
            }
            event.reply("You must first `Link Account` before entering a code.").queue();

        }
    }

//    @Override
//    public void onMessageReceived(MessageReceivedEvent event)
//    {
//
//        if (event.getAuthor().isBot()) return;
//
//        // We don't want to respond to other bot accounts, including ourself
//        Message message = event.getMessage();
//        String content = message.getContentRaw();
//        // getContentRaw() is an atomic getter
//        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
//        if (content.equals("!ping"))
//        {
////            List<Member> members = event.getGuild().getVoiceChannelById(1017059320027955221L);
//            System.out.println(event.getGuild().getVoiceChannelById("1017059320027955221"));
////            System.out.println(members.size());
////            for (Member member : members){
////                System.out.println("Mem brrr");
////                AudioChannel channel = event.getGuild().createVoiceChannel("test123").complete();
////
////                channel.getGuild().moveVoiceMember(member, channel);
////
////            }
//            AudioChannel channel = event.getGuild().getVoiceChannelById("1022141443168550942");//.createVoiceChannel("test123").complete();
//            channel.getGuild().moveVoiceMember(message.getMember(), channel);
//
//
//            //MessageChannel channel = event.getChannel();
//            //channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
//        }
//    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        super.onMessageReceived(event);
        // LOUNGE => ROOM
        VoiceChannel roomVoiceChannel = event.getGuild().getVoiceChannelById("1017059320027955221");
        roomVoiceChannel.getMembers();
        Member memberToBeMoved = event.getMember();
        String[] moveMessage = event.getMessage().getContentRaw().split(" ", 1);





//        try {
//            assert memberToBeMoved != null;
//            event.getGuild().moveVoiceMember(memberToBeMoved, roomVoiceChannel).queue();
//            event.getChannel().sendMessage("IF YOU ARE NOT MOVED TO A NEW VOICE CHANNEL, REPORT AN ISSUE TO `https://github.com/talentedasian/Discord-Bot or https://github.com/godsofheaven/Discord-Bot`")
//                    .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
//        } catch (IllegalStateException e) {
//            event.getChannel().sendMessage("`CONNECT` to a voice channel first").queue();
//        }


    }
}

