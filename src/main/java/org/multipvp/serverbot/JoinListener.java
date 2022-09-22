package org.multipvp.serverbot;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class JoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getUser().openPrivateChannel().complete().sendMessage(String.format(
                """
                        Welcome to the MultiPvP Discord, %s!
                        To interact with the community you must first link your Minecraft Profile.
                        Before linking your account, be sure to login to `play.multipvp.xyz`.
                        """, event.getUser().getName())
        ).addActionRow(
                Button.primary("verify_start_button", "Link account!")
        ).queue();

    }
}
