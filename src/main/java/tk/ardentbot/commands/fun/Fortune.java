package tk.ardentbot.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tk.ardentbot.core.executor.Command;

import java.io.IOException;

public class Fortune extends Command {
    public Fortune(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        channel.sendTyping().queue();
        try {
            String title = "**Unix Fortune**";
            Document doc = Jsoup.connect("http://motd.ambians.com/quotes" +
                    ".php/name/linux_fortunes_random/toc_id/1-1-1").userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                    "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
            Elements fortuneElement = doc.getElementsByTag("pre");
            String fortune = fortuneElement.get(0).text();
            sendTranslatedMessage("**" + title + "**: " + fortune, channel, user);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
