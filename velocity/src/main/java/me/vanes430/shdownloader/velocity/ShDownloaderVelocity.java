package me.vanes430.shdownloader.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.vanes430.shdownloader.common.FileDownloader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "shdownloader", name = "shdownloader", version = "1.0.0-SNAPSHOT", 
        description = "Simple file downloader", authors = {"vanes430"})
public class ShDownloaderVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public ShDownloaderVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register("shdown", new ShDownCommand());
        logger.info("shdownloader (Velocity) enabled!");
    }

    private class ShDownCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource sender = invocation.source();
            String[] args = invocation.arguments();

            if (sender instanceof com.velocitypowered.api.proxy.Player) {
                sender.sendMessage(Component.text("This command can only be executed from the console!", NamedTextColor.RED));
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(Component.text("Usage: /shdown <url> <filename>", NamedTextColor.RED));
                return;
            }

            String url = args[0];
            String fileName = args[1];
            
            if (!dataDirectory.toFile().exists()) dataDirectory.toFile().mkdirs();
            Path dest = dataDirectory.resolve(fileName);

            sender.sendMessage(Component.text("Downloading " + fileName + "...", NamedTextColor.YELLOW));

            FileDownloader.download(url, dest,
                msg -> logger.info(msg),
                () -> sender.sendMessage(Component.text("Successfully downloaded " + fileName, NamedTextColor.GREEN)),
                ex -> sender.sendMessage(Component.text("Failed to download: " + ex.getMessage(), NamedTextColor.RED))
            );
        }
    }
}
