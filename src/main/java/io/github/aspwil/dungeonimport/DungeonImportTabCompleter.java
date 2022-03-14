package io.github.aspwil.dungeonimport;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DungeonImportTabCompleter implements TabCompleter {


    private final List<String> materials = EnumSet.allOf(Material.class).stream().filter(Material::isBlock).filter(mat -> !mat.isLegacy()).map(e -> e.getKey().toString()).collect(Collectors.toList());
    private final List<String> subCommands = new ArrayList<>(Arrays.asList("help", "h", "g", "generate", "r", "room", "f", "feature", "w", "wonder", "reload", "i", "info"));
    private final List<String> files;

    public DungeonImportTabCompleter(List<String> files) {
        this.files = files;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                return match(subCommands, args[0]);
            case 2:
                switch (args[0]) {
                    case "l":
                    case "load":
                        return ExactCaseMatch(files, args[1]);
                    case "r":
                    case "room":
                        return new ArrayList<>(Arrays.asList("<num>"));
                    case "f":
                    case "feature":
                        return new ArrayList<>(Arrays.asList("<letter>"));
                    case "g":
                    case "gen":
                    case "generate":
                        return match(materials, args[1]);
                    default:
                        return new ArrayList<>();

                }
            case 3:
                if (args[0].equals("g") || args[0].equals("gen") || args[0].equals("g")) {
                    if (Material.matchMaterial(args[1]) != null) {
                        return match(materials, args[2]);
                    }
                }

            default:
                if (args[0].equals("l") || args[0].equals("load")) {
                    String fileName = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.joining(" "));
                    //System.out.println(fileName);
                    return files.stream().filter(s -> s.startsWith(fileName)).map(s -> s.replace(fileName.substring(0 , fileName.lastIndexOf(" ")+1),"")).collect(Collectors.toList());
                }
                return new ArrayList<>();
        }


    }

    private List<String> match(List<String> list, String start) {
        List<String> matchingObjects = list.stream().
                filter(s -> s.toLowerCase().startsWith(start.toLowerCase())).
                collect(Collectors.toList());
        Collections.sort(matchingObjects);
        return matchingObjects;
    }

    private List<String> ExactCaseMatch(List<String> list, String start) {
        List<String> matchingObjects = list.stream().
                filter(s -> s.startsWith(start)).
                collect(Collectors.toList());
        Collections.sort(matchingObjects);
        return matchingObjects;
    }
}
