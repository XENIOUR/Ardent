package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.commands.music.Music.*;

public class Play extends Command {
    public Play(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length > 1) {
            AudioManager audioManager = guild.getAudioManager();
            String url = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            boolean shouldDeleteMessage = shouldDeleteMessages(guild);
            boolean implement = false;
            if (!audioManager.isConnected()) {
                VoiceChannel success = joinChannel(guild, guild.getMember(user), language, this,
                        audioManager, channel);
                if (success != null) {
                    loadAndPlay(message, user, this, language, (TextChannel) channel, url, success, false, true);
                    implement = true;
                }
            }
            else {
                loadAndPlay(message, user, this, language, (TextChannel) sendTo(channel, guild), url, audioManager
                        .getConnectedChannel(), false, true);
                implement = true;
            }
            if (implement) {
                if (shouldDeleteMessage) {
                    try {
                        message.delete().queue();
                    }
                    catch (PermissionException ex) {
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Auto-deleting music play messages is enabled, " +
                                    "but you need to give me the `MANAGE MESSAGES` permission so I can " +
                                    "actually delete the messages.").queue();
                        });
                    }
                }
            }
        }
        else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
