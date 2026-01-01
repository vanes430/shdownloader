package me.vanes430.shdownloader.spigot;

import me.vanes430.shdownloader.common.FileDownloader;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;

public class ShDownloaderSpigot extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("shdown").setExecutor(this);
        getLogger().info("shdownloader (Spigot) enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof org.bukkit.entity.Player) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed from the console!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /shdown <url> <filename>");
            return true;
        }

        String url = args[0];
        String fileName = args[1];
        File folder = getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        
        Path dest = new File(folder, fileName).toPath();

        sender.sendMessage(ChatColor.YELLOW + "Downloading " + fileName + "...");
        
        FileDownloader.download(url, dest, 
            msg -> getLogger().info(msg),
            () -> sender.sendMessage(ChatColor.GREEN + "Successfully downloaded " + fileName),
            ex -> sender.sendMessage(ChatColor.RED + "Failed to download: " + ex.getMessage())
        );

        return true;
    }
}
