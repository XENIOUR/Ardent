package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.List;

public class Setnickname extends Command {
    public Setnickname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("setnickname", language, "help").getTranslation().replace("{0}",
                    GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else if (args.length == 2) {
            sendTranslatedMessage(getTranslation("setnickname", language, "invalidarguments").getTranslation()
                    .replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 1) {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    String newNickname = message.getRawContent().replace("<@" + mentioned.getId() + ">", "").replace
                            (GuildUtils.getPrefix(guild) + args[0], "");
                    while (newNickname.startsWith(" ")) newNickname = newNickname.substring(1);
                    if (newNickname.length() > 32 && newNickname.length() < 2) {
                        sendRetrievedTranslation(channel, "setnickname", language, "between2and32");
                    }
                    else {
                        String finalNewNickname = newNickname;
                        try {
                            guild.getController().setNickname(guild.getMember(mentioned), newNickname).queue(aVoid -> {
                                try {
                                    sendTranslatedMessage(getTranslation("setnickname", language, "success")
                                            .getTranslation().replace("{0}", mentioned.getName()).replace("{1}",
                                                    finalNewNickname), channel);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        }
                        catch (PermissionException e) {
                            sendRetrievedTranslation(channel, "setnickname", language, "failure");
                        }
                    }
                }
                else {
                    sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                }
            }
            else sendRetrievedTranslation(channel, "setnickname", language, "wrongmentionedusers");
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
