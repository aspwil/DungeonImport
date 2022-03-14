package io.github.aspwil.dungeonimport;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * this file is under the standard MIT License, which a copy of has been included at the bottom of the file.
 * this essentially means you are free to use, alter, and, distribute this code privately, publicly, and commercially as long as don't remove the licence notice.
 */

public class DungeonImportTabCompleter implements TabCompleter {

    //the set of all valid blocks
    private final List<String> materials = EnumSet.allOf(Material.class).stream().filter(Material::isBlock).filter(mat -> !mat.isLegacy()).map(e -> e.getKey().toString()).collect(Collectors.toList());
    //all the sub command of the /di command
    private final List<String> subCommands = new ArrayList<>(Arrays.asList("help", "h", "g", "gen", "generate", "r", "room", "f", "feature", "w", "wonder", "reload", "i", "info", "l", "load"));
    //this list hold the file names of all the dungeons in the dungeons folder
    private final List<String> files;

    public DungeonImportTabCompleter(List<String> files) {
        this.files = files;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        //return the auto complete list based on how many args deep we are
        switch (args.length) {
            //1st arg, only sub commands
            case 1:
                return match(subCommands, args[0]);
            //2nd arg
            case 2:
                //switch based on the first arg
                switch (args[0]) {
                    //if its load recommend all the files names, after we get more args we will need to switch to the default
                    case "l":
                    case "load":
                        return ExactCaseMatch(files, args[1]);
                    //if it's the room command tell them it's a number
                    case "r":
                    case "room":
                        return new ArrayList<>(Arrays.asList("<num>"));
                    //if it's the room command tell them it's a letter
                    case "f":
                    case "feature":
                        return new ArrayList<>(Arrays.asList("<letter>"));
                    //if its generate then there choosing block for the floor
                    case "g":
                    case "gen":
                    case "generate":
                        return match(materials, args[1]);
                    //if its an invalid first arg then don't return any tab completes
                    default:
                        return new ArrayList<>();

                }
                //if there are 3 args then we must be an error, or be choosing the last material for generation
            case 3:
                //check the first arg is correct for generation
                if (args[0].equals("g") || args[0].equals("gen") || args[0].equals("generate")) {
                    //check the 2nd arg is a valid material
                    if (Material.matchMaterial(args[1]) != null) {
                        //return recommendations for 2nd material
                        return match(materials, args[2]);
                    }
                }

            default:
                //here we deal with the complexities of having spaces in the file name and
                //if were tryiong to load then display the names of files
                if (args[0].equals("l") || args[0].equals("load")) {
                    //get all the args provided and concat them into a string
                    String fileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    //return a list of files that match with the provided start and with the start cut off (only at the last space though)
                    return files.stream().filter(s -> s.startsWith(fileName)).map(s -> s.replace(fileName.substring(0, fileName.lastIndexOf(" ") + 1), "")).collect(Collectors.toList());
                }
                //if were not trying to load then there must be a syntax error so don't recommend anything
                return new ArrayList<>();
        }


    }

    //return all entries of input list that start with the provided starting string, sorted in alphabetical order, ignore capitalization
    private List<String> match(List<String> list, String start) {
        return list.stream().filter(s -> s.toLowerCase().startsWith(start.toLowerCase())).sorted().collect(Collectors.toList());
    }

    //return all entries of input list that start with the provided starting string, sorted in alphabetical order
    private List<String> ExactCaseMatch(List<String> list, String start) {
        return list.stream().filter(s -> s.startsWith(start)).sorted().collect(Collectors.toList());
    }
}

/**
 * MIT License
 * <p>
 * Copyright (c) 2022 Aspen Wilson
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
