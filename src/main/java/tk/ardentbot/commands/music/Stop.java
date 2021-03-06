package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Stop extends Command {
    public Stop(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        Member member = guild.getMember(user);
        if (UserUtils.hasManageServerOrStaff(member) || UserUtils.isBotCommander(member) || (audioManager.isConnected() && audioManager
                .getConnectedChannel().getMembers().size() == 2)) {
            if (audioManager.isConnected()) {
                GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                if (manager.player.getPlayingTrack() != null) manager.player.stopTrack();
                manager.scheduler.manager.resetQueue();
                getShard().musicManagers.remove(Long.parseLong(guild.getId()));
                sendTranslatedMessage("Stopped playback and cleared songs in the queue", sendTo(channel, guild), user);
            }
            else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
        }
        else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
