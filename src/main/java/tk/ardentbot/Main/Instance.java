package tk.ardentbot.Main;

import ch.qos.logback.classic.Level;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.BotCommands.BotAdministration.*;
import tk.ardentbot.BotCommands.BotInfo.*;
import tk.ardentbot.BotCommands.Fun.*;
import tk.ardentbot.BotCommands.GuildAdministration.*;
import tk.ardentbot.BotCommands.GuildInfo.Botname;
import tk.ardentbot.BotCommands.GuildInfo.GuildInfo;
import tk.ardentbot.BotCommands.GuildInfo.Points;
import tk.ardentbot.BotCommands.GuildInfo.Whois;
import tk.ardentbot.BotCommands.Music.GuildMusicManager;
import tk.ardentbot.BotCommands.Music.Music;
import tk.ardentbot.Core.BotData.BotLanguageData;
import tk.ardentbot.Core.BotData.BotMuteData;
import tk.ardentbot.Core.BotData.BotPrefixData;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.CommandExecution.Category;
import tk.ardentbot.Core.CommandExecution.CommandFactory;
import tk.ardentbot.Core.Events.Join;
import tk.ardentbot.Core.Events.Leave;
import tk.ardentbot.Core.Events.OnMessage;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.SQL.MuteDaemon;
import tk.ardentbot.Utils.Updaters.BotlistUpdater;
import tk.ardentbot.Utils.Updaters.GuildDaemon;
import tk.ardentbot.Utils.Updaters.PermissionsDaemon;
import tk.ardentbot.Utils.Updaters.PhraseUpdater;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static tk.ardentbot.Core.Translation.LangFactory.languages;
public class Instance {
    public boolean testingBot;
    public ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);
    public BotMuteData botMuteData;
    public BotPrefixData botPrefixData;
    public BotLanguageData botLanguageData;
    public ArrayList<Language> crowdinLanguages = new ArrayList<>();
    public TextChannel botLogs;
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ChatterBot cleverBot;
    public ConcurrentHashMap<String, ChatterBotSession> cleverbots = new ConcurrentHashMap<>();
    public Gson gson = new Gson();
    public Connection conn;
    public CommandFactory factory;
    public JDA jda;
    public User bot;
    public Twitter twitter;
    public BaseCommand help;
    public BaseCommand patreon;
    public BaseCommand translateForArdent;
    public BaseCommand getDevHelp;
    public BaseCommand manage;
    public String url = "https://ardentbot.tk";
    public ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();
    public String announcement;
    public BotlistUpdater botlistUpdater;
    public String botsDiscordPwToken;
    public String discordBotsOrgToken;
    private int gameCounter = 0;
    private int matureLanguages = 0;

    public Instance(boolean testingBot) throws Exception {
        this.testingBot = testingBot;
        String token;
        if (testingBot) {
            token = IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\v2testtoken.key")));
        }
        else {
            token = IOUtils.toString(new FileReader(new File("/root/Ardent/v2bottoken.key")));
            botsDiscordPwToken = IOUtils.toString(new FileReader(new File("/root/Ardent/botsdiscordpw.key")));
            discordBotsOrgToken = IOUtils.toString(new FileReader(new File("/root/Ardent/discordbotsorg.key")));
        }

        jda = new JDABuilder(AccountType.BOT)
                .setEventManager(new AnnotatedEventManager())
                .setToken(token)
                .setAutoReconnect(true)
                .setAudioEnabled(true)
                .setGame(Game.of("- New Update! -"))
                .setStatus(OnlineStatus.ONLINE)
                .setBulkDeleteSplittingEnabled(true)
                .setEnableShutdownHook(true)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .buildBlocking();

        bot = jda.getUserById(jda.getSelfUser().getId());

        Class.forName("com.mysql.jdbc.Driver");

        if (!testingBot) {
            PhraseUpdater.ACCOUNT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_account_key" +
                    ".key")));
            PhraseUpdater.PROJECT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_project_key" +
                    ".key")));

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("Fi9IjqqsGmOXqjR5uYK8YM2Pr")
                    .setOAuthConsumerSecret(IOUtils.toString(new FileReader(new File
                            ("/root/Ardent/twitterconsumersecret.key"))))
                    .setOAuthAccessToken("818984879018954752-aCzxyML6Xp0QcRpq5sjoe8wfp0sjVDt")
                    .setOAuthAccessTokenSecret(IOUtils.toString(new FileReader(new File
                            ("/root/Ardent/twitteroauthsecret.key"))));
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();

            conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("/root/Ardent/v2url.key"))),
                    IOUtils.toString(new FileReader(new File("/root/Ardent/v2user.key"))), IOUtils.toString(new
                            FileReader(new File("/root/Ardent/v2password.key"))));
        }
        else {
            conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop" +
                            "\\Ardent\\dburl.key"))),
                    IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbuser.key"))),
                    IOUtils.toString(new
                            FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key"))));
        }

        executorService.schedule(() -> {
            try {
                GIF.setupCategories();

                // Register event listeners
                jda.addEventListener(new OnMessage());
                jda.addEventListener(new Join());
                jda.addEventListener(new Leave());

                // Add languages
                languages = new ArrayList<>();
                languages.add(LangFactory.english);
                languages.add(LangFactory.french);
                languages.add(LangFactory.turkish);
                languages.add(LangFactory.croatian);
                languages.add(LangFactory.romanian);
                languages.add(LangFactory.portugese);
                languages.add(LangFactory.german);
                languages.add(LangFactory.cyrillicserbian);
                languages.add(LangFactory.dutch);
                languages.add(LangFactory.emoji);
                languages.add(LangFactory.arabic);
                languages.add(LangFactory.hindi);
                languages.add(LangFactory.spanish);

                crowdinLanguages.add(LangFactory.croatian);
                crowdinLanguages.add(LangFactory.french);
                crowdinLanguages.add(LangFactory.turkish);
                crowdinLanguages.add(LangFactory.german);
                crowdinLanguages.add(LangFactory.cyrillicserbian);
                crowdinLanguages.add(LangFactory.spanish);
                crowdinLanguages.add(LangFactory.dutch);
                crowdinLanguages.add(LangFactory.arabic);
                crowdinLanguages.add(LangFactory.hindi);

                // Adding the "handler" for mutes
                botMuteData = new BotMuteData();

                // Adding the handler for prefixes
                botPrefixData = new BotPrefixData();

                // Adding the handler for languages
                botLanguageData = new BotLanguageData();

                // Instantiate the bot list updater
                botlistUpdater = new BotlistUpdater();

                languages.stream().filter(lang -> lang.getLanguageStatus() == Language.Status.MATURE).forEach(lang ->
                        matureLanguages++);

                factory = new CommandFactory();

                help = new Help(new BaseCommand.CommandSettings("help", true, true, Category.BOTINFO));
                patreon = new Patreon(new BaseCommand.CommandSettings("patreon", true, true, Category.GUILDINFO));
                translateForArdent = new TranslateForArdent(new BaseCommand.CommandSettings("translateforardent", true,
                        true, Category.BOTINFO));
                getDevHelp = new Bug(new BaseCommand.CommandSettings("getdevhelp", false, true, Category.BOTINFO));
                manage = new Manage(new BaseCommand.CommandSettings("manage", false, true, Category
                        .BOTADMINISTRATION));
                // Register tk.ardentbot.CommandExecution
                factory.registerCommand(new AddEnglishBase(new BaseCommand.CommandSettings("addenglishbase", true, true,
                        Category

                        .BOTADMINISTRATION)));
                factory.registerCommand(new Todo(new BaseCommand.CommandSettings("todo", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Tweet(new BaseCommand.CommandSettings("tweet", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Admin(new BaseCommand.CommandSettings("admin", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Eval(new BaseCommand.CommandSettings("eval", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(manage);

                factory.registerCommand(getDevHelp);
                factory.registerCommand(new Support(new BaseCommand.CommandSettings("support", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Website(new BaseCommand.CommandSettings("website", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Invite(new BaseCommand.CommandSettings("invite", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(translateForArdent);
                factory.registerCommand(new Changelog(new BaseCommand.CommandSettings("changelog", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(patreon);
                factory.registerCommand(new Joinmessage(new BaseCommand.CommandSettings("joinmessage", true, true,
                        Category.BOTINFO)));
                factory.registerCommand(new Status(new BaseCommand.CommandSettings("status", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Request(new BaseCommand.CommandSettings("request", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Ping(new BaseCommand.CommandSettings("ping", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(help);
                factory.registerCommand(new Stats(new BaseCommand.CommandSettings("stats", true, true, Category
                        .BOTINFO)));

                factory.registerCommand(new UD(new BaseCommand.CommandSettings("ud", true, true, Category.FUN)));
                factory.registerCommand(new GIF(new BaseCommand.CommandSettings("gif", true, true, Category.FUN)));
                factory.registerCommand(new FML(new BaseCommand.CommandSettings("fml", true, true, Category.FUN)));
                factory.registerCommand(new Yoda(new BaseCommand.CommandSettings("yoda", true, true, Category.FUN)));
                factory.registerCommand(new EightBall(new BaseCommand.CommandSettings("8ball", true, true, Category
                        .FUN)));
                factory.registerCommand(new Tags(new BaseCommand.CommandSettings("tag", false, true, Category.FUN)));
                factory.registerCommand(new tk.ardentbot.BotCommands.Fun.Translate(new BaseCommand.CommandSettings("translate",
                        true, true, Category
                        .FUN)));
                factory.registerCommand(new Roll(new BaseCommand.CommandSettings("roll", true, true, Category.FUN)));
                // factory.registerCommand(new Coinflip(new BaseCommand.CommandSettings("coinflip", true, true, Category
                // .FUN)));
                factory.registerCommand(new Music(new BaseCommand.CommandSettings("music", false, true, Category.FUN)));

                factory.registerCommand(new Prefix(new BaseCommand.CommandSettings("prefix", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new GuildLanguage(new BaseCommand.CommandSettings("language", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Iam(new BaseCommand.CommandSettings("iam", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Setnickname(new BaseCommand.CommandSettings("setnickname", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Prune(new BaseCommand.CommandSettings("prune", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Kick(new BaseCommand.CommandSettings("kick", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Ban(new BaseCommand.CommandSettings("ban", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Clear(new BaseCommand.CommandSettings("clear", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Roles(new BaseCommand.CommandSettings("roles", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new DefaultRole(new BaseCommand.CommandSettings("defaultrole", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Unmute(new BaseCommand.CommandSettings("unmute", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Mute(new BaseCommand.CommandSettings("mute", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Automessage(new BaseCommand.CommandSettings("automessage", false, true,
                        Category.GUILDADMINISTRATION)));

                factory.registerCommand(new GuildInfo(new BaseCommand.CommandSettings("guildinfo", false, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new Whois(new BaseCommand.CommandSettings("whois", true, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new Points(new BaseCommand.CommandSettings("points", false, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new Botname(new BaseCommand.CommandSettings("botname", false, true, Category
                        .GUILDINFO)));
                // factory.registerCommand(new Emojis(new BaseCommand.CommandSettings("emojis", true, true, Category
                // .GUILDINFO)));

                cleverBot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "f5d922d97e345aa1");
                botLogs = jda.getTextChannelById("270572632343183361");

                musicManagers = new HashMap<>();

                playerManager = new DefaultAudioPlayerManager();
                playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.LOW);
                playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
                playerManager.registerSourceManager(new HttpAudioSourceManager());

                AudioSourceManagers.registerRemoteSources(playerManager);
                AudioSourceManagers.registerLocalSource(playerManager);

                jda.getGuilds().forEach((guild -> {
                    Status.commandsByGuild.put(guild.getId(), 0);
                    cleverbots.put(guild.getId(), cleverBot.createSession());
                }));

                executorService.scheduleAtFixedRate(() -> {
                    String game = null;
                    switch (gameCounter) {
                        case 0:
                            game = "Join our team! /translateforardent";
                            break;
                        case 1:
                            game = "serving " + Status.getUserAmount() + " users";
                            break;
                        case 2:
                            game = "serving " + jda.getGuilds().size() + " guilds";
                            break;
                        case 3:
                            game = "music for " + Status.getVoiceConnections() + " servers!";
                            break;
                        default:
                            game = "with many languages";
                    }
                    jda.getPresence().setGame(Game.of(game, url));

                    if (gameCounter == 3) gameCounter = 0;
                    else gameCounter++;

                    botlistUpdater.run();
                }, 10, 25, TimeUnit.SECONDS);

                // On hold for a bit
                // PhraseUpdater phraseUpdater = new PhraseUpdater();
                // TranslationUpdater translationUpdater = new TranslationUpdater();

                PermissionsDaemon patronDaemon = new PermissionsDaemon();
                executorService.scheduleAtFixedRate(patronDaemon, 1, 15, TimeUnit.SECONDS);

                GuildDaemon guildDaemon = new GuildDaemon();
                executorService.scheduleAtFixedRate(guildDaemon, 1, 5, TimeUnit.SECONDS);

                MuteDaemon muteDaemon = new MuteDaemon();
                executorService.scheduleAtFixedRate(muteDaemon, 1, 5, TimeUnit.SECONDS);

                Logger.getLogger("org.apache.http").

                        setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.wire").

                        setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);

                Music.checkMusicConnections();

                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                        .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.OFF);
            }
            catch (Exception ex) {
                new BotException(ex);
            }
        }, 5, TimeUnit.SECONDS);
    }
}
