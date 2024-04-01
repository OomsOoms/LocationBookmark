package io.github.oomsooms.locationbookmark;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.ResultSet;

public class BookmarkCommand implements CommandExecutor, TabCompleter {

    private DatabaseManager dbManager;

    public BookmarkCommand(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        String dimension = location.getWorld().getEnvironment().toString();
        String seed = String.valueOf(location.getWorld().getSeed());
        String userUuid = player.getUniqueId().toString();

        String name = null;
        try {
            name = args[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage("Usage: /bookmark <add|find> <name> [private]");
            return false;
        }

        if (args[0].equalsIgnoreCase("add")) {
            try {
                String query = String.format(
                        "SELECT name FROM bookmarks WHERE seed = '%s' and userUuid = '%s' and name = '%s' and private = 0;",
                        seed, userUuid, name);
                ResultSet rs = dbManager.executeQuery(query);
                if (rs.next()) {
                    sender.sendMessage("A bookmark with this name already exists.");
                    return false;
                }

                Boolean privateBookmark = (args.length > 2) ? args[2].equalsIgnoreCase("private") : false;
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                query = String.format(
                        "INSERT INTO bookmarks (userUuid, name, x, y, z, dimension, seed, private) VALUES ('%s', '%s', %f, %f, %f, '%s', '%s', %b)",
                        userUuid, name, x, y, z, dimension, seed, privateBookmark);
                dbManager.executeQuery(query);
                switch (dimension) {
                    case "NORMAL":
                        dimension = "Overworld";
                        break;
                    case "NETHER":
                        dimension = "Nether";
                        break;
                    case "THE_END":
                        dimension = "End";
                        break;
                    default:
                        dimension = "Unknown";
                        break;
                }
                sender.sendMessage(String.format("Bookmark added '%s' (%.2f %.2f %.2f, %s) (%s)", name, x, y, z,
                        dimension, privateBookmark ? "Private" : "Public"));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("Failed to add bookmark.");
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("find")) {
            String query = String.format(
                    "SELECT x, y, z, dimension, private FROM bookmarks WHERE seed = '%s' and name = '%s' and (private = 0 or userUuid = '%s');",
                    seed, name, userUuid);
            ResultSet rs = dbManager.executeQuery(query);
            try {
                if (rs.next()) {
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    String dimensionName = rs.getString("dimension");
                    Boolean privateBookmark = rs.getBoolean("private");
                    switch (dimensionName) {
                        case "NORMAL":
                            dimensionName = "Overworld";
                            break;
                        case "NETHER":
                            dimensionName = "Nether";
                            break;
                        case "THE_END":
                            dimensionName = "End";
                            break;
                        default:
                            dimensionName = "Unknown";
                            break;
                    }
                    sender.sendMessage(String.format("Bookmark found '%s' (%.2f %.2f %.2f, %s) (%s)", name, x, y, z,
                            dimensionName, privateBookmark ? "Private" : "Public"));
                    return true;
                } else {
                    sender.sendMessage("Bookmark not found.");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("Failed to find bookmark.");
                return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("add");
            completions.add("find");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                completions.add("<name>");
            } else if (args[0].equalsIgnoreCase("find")) {
                Player player = (Player) sender;
                Location location = player.getLocation();
                String seed = String.valueOf(location.getWorld().getSeed());
                String userUuid = player.getUniqueId().toString();

                String query = String.format(
                        "SELECT name FROM bookmarks WHERE seed = '%s' and private = 0 or userUuid = '%s';",
                        seed, userUuid);

                try {
                    ResultSet rs = dbManager.executeQuery(query);
                    while (rs.next()) {
                        System.out.println(rs.getString("name"));
                        completions.add(rs.getString("name"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (args.length == 3) {
            completions.add("private");
        }

        return completions;
    }
}
