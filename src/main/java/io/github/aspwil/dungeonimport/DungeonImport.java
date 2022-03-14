package io.github.aspwil.dungeonimport;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * hello and thank you for checking out my code,
 *
 * this code was built and tested on a minecraft 1.18.2 paper version git-paper-237
 *
 * the goal of this code is to take in a json file generated from https://donjon.bin.sh/5e/dungeon/ and build the dungeon in a minecraft world.
 *
 * it also has the added functionality to pull up general data, room specific data, feature data, and wandering monster data.
 *
 *
 */

public final class DungeonImport extends JavaPlugin {

    public Gson gson;
    public JsonObject jsonRoot = null;
    public JsonObject[] roomsByNumber = null;
    public String prefix = ChatColor.RED + "[DungeonImport] " + ChatColor.WHITE;
    public File pluginRoot = this.getDataFolder();
    public File dungeonsRoot = new File(pluginRoot, "Dungeons");

    @Override
    public void onEnable() {
        // Plugin startup logic
        gson = new GsonBuilder().setPrettyPrinting().create();

        //generate plugin folders if they don't exist
        if (!pluginRoot.exists()) {
            this.getLogger().log(Level.WARNING, "could not find plugin folder, generating");
            if (!pluginRoot.mkdirs()) {
                this.getLogger().log(Level.SEVERE, "COULD NOT GENERATE PLUGIN FOLDER. UNKNOWN ERROR");
            }
        }
        if (!dungeonsRoot.exists()) {
            this.getLogger().log(Level.WARNING, "could not find dungeons folder, generating");
            if (!dungeonsRoot.mkdirs()) {
                this.getLogger().log(Level.SEVERE, "COULD NOT GENERATE DUNGEONS FOLDER. UNKNOWN ERROR");
            }
        }

        //set the auto completer for the command
        getCommand("di").setTabCompleter(new DungeonImportTabCompleter(Arrays.stream(dungeonsRoot.list()).filter(s -> s.contains(".json")).map(s -> "\""+s.replace(".json", "")+"\"").collect(Collectors.toList())));

    }

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        //do this code if its the di command
        if (cmd.getName().equalsIgnoreCase("di")) {
            //quit the call if its from the terminal
            if (!(sender instanceof Player)) {
                return false;
            }
            //get player object
            Player player = (Player) sender;
            //build the help string
            String help =
                    ChatColor.RED + "~ ~ ~ [DungeonImport] ~ ~ ~\n" +
                            ChatColor.GOLD + "/di h/help " + ChatColor.RED + "show help (this message)\n" +
                            ChatColor.GOLD + "/di <l/load> \"file name\" " + ChatColor.RED + "load dungeon data (this has to be done before other commands)\n" +
                            ChatColor.GOLD + "/di <g/gen/generate> [wall block] [floor block]" + ChatColor.RED + "generate loaded in dungeon (will place blocks in world and cant be undone)\n" +
                            ChatColor.GOLD + "/di <i/info>" + ChatColor.RED + "list general dungeon info\n" +
                            ChatColor.GOLD + "/di <r/room> <num> " + ChatColor.RED + "shows the info for room #num\n" +
                            ChatColor.GOLD + "/di <f/feature> <letter> " + ChatColor.RED + "shows the info of feature letter\n" +
                            ChatColor.GOLD + "/di <w/wonder> " + ChatColor.RED + "displays a list of wondering monsters\n" +
                            ChatColor.GOLD + "/di reload " + ChatColor.RED + "reloads the plugins file listings\n" +
                            ChatColor.GREEN + "adding dungeons:"+ChatColor.WHITE+" dungeons are generated via https://donjon.bin.sh/5e/dungeon/ (make sure you turn egress off)\n once generated press download>json at the bottom of page\n then move the json file to the server directory /plugins/DungeonImport/Dungeons\n then run /di reload";

            //check if no args were provided
            if (args.length == 0) {
                player.sendMessage(help);
                return true;
            }

            //if we have a first arg switch based un it
            switch (args[0]) {
                //the load command
                case "l":
                case "load":
                    //check if they added a second argument
                    if (args.length < 2) {
                        player.sendMessage(prefix + "inavlid syntax, use /di <l/load> \"file name\"");
                        break;
                    }
                    //get all the args and concat into one string
                    StringBuilder input = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        input.append(args[i]).append(" ");
                    }

                    //chop off the start and end ", add .json, and start of path
                    String filePath = dungeonsRoot.getAbsolutePath() + "\\" + input.toString().substring(1, input.length() - 2) + ".json";

                    //read in the data
                    jsonRoot = null;
                    try {
                        jsonRoot = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
                    } catch (FileNotFoundException e) {
                        player.sendMessage(prefix + "could not find file @\"" + filePath + "\"");
                        break;
                    }
                    //tell player we read in data
                    player.sendMessage(prefix + "loaded json data from: " + input);

                    //tell player if the dungeon has "peripheral egress"
                    if (!jsonRoot.get("settings").getAsJsonObject().get("peripheral_egress").getAsString().equals("")) {
                        player.sendMessage(prefix + ChatColor.DARK_RED + "ERROR plugin does not support peripheral egress, make sure when generating the dungeon that peripheral egress is set to none");
                        jsonRoot = null;
                        break;
                    }
                    if (!jsonRoot.get("settings").getAsJsonObject().get("peripheral_egress").getAsString().equals("")) {
                        player.sendMessage(prefix + ChatColor.DARK_RED + "ERROR plugin does not support peripheral egress, make sure when generating the dungeon that peripheral egress is set to none");
                        jsonRoot = null;
                        break;
                    }

                    //tell player were generating the room array
                    player.sendMessage(prefix + "generating room array");

                    //generate the array of rooms
                    JsonArray roomsJsonArray = jsonRoot.get("rooms").getAsJsonArray();
                    JsonObject[] rooms = new JsonObject[roomsJsonArray.size()];
                    for (int i = 0; i < roomsJsonArray.size(); i++) {
                        if (roomsJsonArray.get(i).isJsonNull()) {
                            continue;
                        }
                        rooms[i] = roomsJsonArray.get(i).getAsJsonObject();
                    }
                    roomsByNumber = rooms;
                    player.sendMessage(prefix + "finished loading");
                    break;

                case "g":
                case "generate":
                case "gen":

                    //check if we have a file loaded
                    if (jsonRoot == null) {
                        player.sendMessage(prefix + "no file loaded, use /di load \"file_name\"");
                        break;
                    }

                    Material wallMaterial;
                    Material floorMaterial;

                    if (args.length == 1) {
                        wallMaterial = Material.BLACK_CONCRETE;
                        floorMaterial = Material.WHITE_CONCRETE;
                    } else if (args.length == 3) {
                        if (Material.matchMaterial(args[1]) == null) {
                            player.sendMessage(prefix + "unknown material: " + args[1]);
                            break;
                        }
                        wallMaterial = Material.matchMaterial(args[1]);
                        if (Material.matchMaterial(args[2]) == null) {
                            player.sendMessage(prefix + "unknown material: " + args[2]);
                            break;
                        }
                        floorMaterial = Material.matchMaterial(args[2]);
                    } else {
                        player.sendMessage(prefix + "error in syntax use /di gen [wall block] [floor block]");
                        break;
                    }

                    //convert cell data json array to int array
                    JsonArray allCells = jsonRoot.getAsJsonArray("cells");
                    int[][] cellData = new int[allCells.size()][];
                    for (int layerNum = 0; layerNum < allCells.size(); layerNum++) {
                        JsonArray layerAsJson = allCells.get(layerNum).getAsJsonArray();
                        int[] layerAsInt = new int[layerAsJson.size()];
                        for (int cellNum = 0; cellNum < layerAsJson.size(); cellNum++) {
                            layerAsInt[cellNum] = layerAsJson.get(cellNum).getAsInt();
                        }
                        cellData[layerNum] = layerAsInt;
                    }

                    //tell player were generating
                    player.sendMessage(prefix + "generating...");

                    //get block 1 below player location
                    Location playerLoc = player.getLocation().clone().add(0, -1, 0);

                    //build blocks
                    for (int layer = 0; layer < cellData.length; layer++) {
                        for (int cell = 0; cell < cellData[layer].length; cell++) {

                            switch (cellData[layer][cell]) {
                                case 0:
                                case 16:
                                case 1048580:
                                    playerLoc.clone().add(cell, 0, layer).getBlock().setType(wallMaterial);
                                    playerLoc.clone().add(cell, 1, layer).getBlock().setType(wallMaterial);
                                    break;
                                //archway
                                case 65540:
                                    playerLoc.clone().add(cell, 0, layer).getBlock().setType(floorMaterial);
                                    break;
                                //door
                                case 131076:
                                case 524292:
                                case 262148:
                                    playerLoc.clone().add(cell, 1, layer).getBlock().setType(Material.SPRUCE_FENCE_GATE);
                                    if (findOrientation(cellData, layer, cell) == 1) {
                                        setFaceData(playerLoc.clone().add(cell, 1, layer).getBlock(), BlockFace.EAST);
                                    }
                                    playerLoc.clone().add(cell, 0, layer).getBlock().setType(floorMaterial);
                                    break;
                                //portcullis
                                case 2097156:
                                    playerLoc.clone().add(cell, 1, layer).getBlock().setType(Material.IRON_BARS);
                                    if (findOrientation(cellData, layer, cell) == -1) {
                                        setMultiFaceData(playerLoc.clone().add(cell, 1, layer).getBlock(), BlockFace.EAST, BlockFace.WEST);
                                    } else {
                                        setMultiFaceData(playerLoc.clone().add(cell, 1, layer).getBlock(), BlockFace.NORTH, BlockFace.SOUTH);
                                    }

                                    playerLoc.clone().add(cell, 0, layer).getBlock().setType(floorMaterial);
                                default:
                                    playerLoc.clone().add(cell, 0, layer).getBlock().setType(floorMaterial);
                                    break;
                            }

                        }
                    }

                    //generate signs for every room
                    for (JsonObject room : roomsByNumber) {
                        if (room == null) {
                            continue;
                        }
                        Block sign = playerLoc.clone().add(room.get("col").getAsInt(), 1, room.get("row").getAsInt()).getBlock();
                        sign.setType(Material.OAK_SIGN);
                        setSignText(sign, "" + room.get("id").getAsInt());
                    }

                    //generate feature signs
                    if (jsonRoot.has("corridor_features")) {
                        for (String featureKey : jsonRoot.get("corridor_features").getAsJsonObject().keySet()) {
                            JsonObject location = jsonRoot.get("corridor_features").getAsJsonObject().get(featureKey).getAsJsonObject().get("marks").getAsJsonArray().get(0).getAsJsonObject();
                            Block sign = playerLoc.clone().add(location.get("col").getAsInt(), 1, location.get("row").getAsInt()).getBlock();
                            sign.setType(Material.OAK_SIGN);
                            setSignText(sign, featureKey);
                        }
                    }

                    if (jsonRoot.has("stairs")) {
                        for (JsonElement stairs : jsonRoot.get("stairs").getAsJsonArray()) {
                            Location loc = playerLoc.clone().add(stairs.getAsJsonObject().get("col").getAsInt(), 1, stairs.getAsJsonObject().get("row").getAsInt());
                            if (stairs.getAsJsonObject().get("key").getAsString().equals("down")) {
                                loc.add(0, -1, 0);
                            }
                            Block stair = loc.getBlock();
                            stair.setType(Material.OAK_STAIRS);
                            if (stairs.getAsJsonObject().get("key").getAsString().equals("up")) {
                                setFaceData(stair, parseInvertedBlockFace(stairs.getAsJsonObject().get("dir").getAsString()));
                            } else {
                                setFaceData(stair, parseBlockFace(stairs.getAsJsonObject().get("dir").getAsString()));
                            }

                        }
                    }


                    player.sendMessage(prefix + "generated");

                    break;
                case "room":
                case "r":
                    //check if we have a file loaded
                    if (jsonRoot == null) {
                        player.sendMessage(prefix + "no file loaded, use /di load \"name\"");
                        break;
                    }
                    //check if were provided enough args
                    if (args.length != 2) {
                        player.sendMessage(prefix + "invalid syntax provided /di r <num>");
                        break;
                    }
                    int roomNum;
                    //try to parse int
                    try {
                        roomNum = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix + "not a number: " + args[1]);
                        break;
                    }
                    //check if room num is within bounds
                    if (roomNum < 1 || roomNum > roomsByNumber.length - 1) {
                        player.sendMessage(prefix + "invalid room number: room must be from 1 to " + (roomsByNumber.length - 1));
                        break;
                    }
                    //get the room object
                    JsonObject room = roomsByNumber[roomNum];

                    //start the string for the room description
                    StringBuilder desc = new StringBuilder(ChatColor.GREEN + "[ROOM #" + roomNum + "]\n");

                    //generate the room details
                    if (room.get("contents").getAsJsonObject().has("detail")) {
                        JsonObject roomDetails = room.get("contents").getAsJsonObject().get("detail").getAsJsonObject();

                        if (roomDetails.has("room_features")) {
                            desc.append(ChatColor.GREEN).append("[FEATURES] ").append(ChatColor.WHITE).append(roomDetails.get("room_features").getAsString()).append("\n");
                        }
                        if (roomDetails.has("monster")) {
                            desc.append(ChatColor.RED).append("[MONSTER] ");
                            desc.append(ChatColor.WHITE).append(roomDetails.get("monster").getAsJsonArray().get(0).getAsString()).append(" ").append(ChatColor.GREEN).append(roomDetails.get("monster").getAsJsonArray().get(2).getAsString()).append("\n");
                        }
                        if (roomDetails.has("trap")) {
                            for (JsonElement trap : roomDetails.get("trap").getAsJsonArray()) {
                                desc.append(ChatColor.DARK_RED).append("[TRAP] ").append(ChatColor.RED).append(trap.getAsString()).append("\n");
                            }

                        }
                        if (roomDetails.has("hidden_treasure")) {
                            desc.append(ChatColor.GOLD).append("[TREASURE] ").append(ChatColor.WHITE).append(roomDetails.get("hidden_treasure").getAsJsonArray().get(0).getAsString()).append(" ").append(ChatColor.GREEN).append(roomDetails.get("hidden_treasure").getAsJsonArray().get(2).getAsString()).append("\n");
                        }
                    }

                    //generate the room doors
                    for (String s : new String[]{"north", "east", "south", "west"}) {
                        //if the room has doors on a wall
                        if (room.get("doors").getAsJsonObject().has(s)) {
                            //go through the doors in order and add lines to describe them
                            for (int i = 0; i < room.get("doors").getAsJsonObject().get(s).getAsJsonArray().size(); i++) {
                                //extract json object
                                JsonObject door = room.get("doors").getAsJsonObject().get(s).getAsJsonArray().get(i).getAsJsonObject();
                                //wring door side, number, and description
                                desc.append(ChatColor.GOLD).append(s).append(" door #").append(i + 1).append(" :").append(ChatColor.WHITE).append(" ").append(door.get("desc").getAsString()).append(";");
                                //if door leads to other room
                                if (door.has("out_id")) {
                                    desc.append(ChatColor.GREEN).append("->r#").append(door.get("out_id"));
                                }
                                //if door is trapped
                                if (door.has("trap")) {
                                    desc.append("\n").append(ChatColor.DARK_RED).append("[TRAP]: ").append(ChatColor.RED).append(door.get("trap").getAsString());
                                }
                                desc.append("\n");
                            }
                        }
                    }

                    //tell the player the room details
                    player.sendMessage(desc.toString());

                    break;
                case "f":
                case "feature":
                    //check if file loaded
                    if (jsonRoot == null) {
                        player.sendMessage(prefix + "no file loaded, use /di load \"name\"");
                        break;
                    }
                    //check if we have enough args
                    if (args.length != 2) {
                        player.sendMessage(prefix + "invalid syntax provided /di f <letter>");
                        break;
                    }
                    //check if we have corridor features
                    if (!jsonRoot.has("corridor_features")) {
                        player.sendMessage(prefix + "no features listed");
                        break;
                    }
                    //check if entry for prided key exists
                    if (!jsonRoot.get("corridor_features").getAsJsonObject().has(args[1])) {
                        player.sendMessage(prefix + "no feature named " + args[1] + " found");
                        break;
                    }
                    //tell play info about feature
                    player.sendMessage(ChatColor.GREEN + "[Feature #" + args[1] + "] " + ChatColor.WHITE + jsonRoot.get("corridor_features").getAsJsonObject().get(args[1]).getAsJsonObject().get("detail").getAsString());
                    break;

                case "w":
                case "wonder":
                    //check if we have a file loaded
                    if (jsonRoot == null) {
                        player.sendMessage(prefix + "no file loaded, use /di load \"name\"");
                        break;
                    }

                    //generate the text for all wondering monsters
                    StringBuilder wondering = new StringBuilder(ChatColor.GREEN + "[WONDERING MONSTERS]\n");
                    for (String key : jsonRoot.get("wandering_monsters").getAsJsonObject().keySet()) {
                        wondering.append(ChatColor.RED).append("[").append(key).append("] ").append(ChatColor.WHITE).append(jsonRoot.get("wandering_monsters").getAsJsonObject().get(key).getAsString()).append("\n");
                    }

                    //tell the player the wondering monsters info
                    player.sendMessage(wondering.toString());
                    break;
                case "reload":
                    player.sendMessage(prefix+"reloading file lists");
                    getCommand("di").setTabCompleter(new DungeonImportTabCompleter(Arrays.stream(dungeonsRoot.list()).filter(s -> s.contains(".json")).map(s -> "\""+s.replace(".json", "")+"\"").collect(Collectors.toList())));
                    player.sendMessage(prefix+"finished reloading file lists");
                    break;
                case "i":
                case "info":
                    if (jsonRoot == null) {
                        player.sendMessage(prefix + "no file loaded, use /di load \"name\"");
                        break;
                    }
                    JsonObject dungeonDetails = jsonRoot.get("details").getAsJsonObject();
                    String detailsString = ChatColor.GREEN+"[DUNGEON DETAILS]\n";
                    for(String k: dungeonDetails.keySet()){
                        if(dungeonDetails.get(k).isJsonNull()){
                            detailsString+=ChatColor.GOLD+"["+k.toUpperCase()+"] "+ChatColor.WHITE+"none\n";
                        } else {
                            detailsString+=ChatColor.GOLD+"["+k.toUpperCase()+"] "+ChatColor.WHITE+dungeonDetails.get(k).getAsString()+"\n";
                        }
                    }
                    player.sendMessage(detailsString);
                    break;
                case "h":
                case "help":
                default:
                    player.sendMessage(help);
                    break;
            }
            //If this has happened the function will return true.
            return true;
        }
        // If this hasn't happened the value of false will be returned.
        return false;
    }

    private int findOrientation(int[][] data, int x, int y) {

        //vertical is 1
        if (data[x + 1][y] == 16 || data[x + 1][y] == 0) {
            return 1;
        }
        //horizontal is -1
        if (data[x][y + 1] == 16 || data[x][y + 1] == 0) {
            return -1;
        }
        return 0;
    }

    private void setFaceData(Block b, BlockFace face) {
        BlockData bData = b.getBlockData();
        BlockState bState = b.getState();
        Directional dir = (Directional) bData;
        dir.setFacing(face);
        bState.setBlockData(dir);
        bState.update(true, true);
    }

    private void setMultiFaceData(Block b, BlockFace... faces) {
        BlockData bData = b.getBlockData();
        BlockState bState = b.getState();
        MultipleFacing dirs = (MultipleFacing) bData;
        for (BlockFace face : faces) {
            dirs.setFace(face, true);
        }
        bState.setBlockData(dirs);
        bState.update(true, true);
    }

    private void setSignText(Block b, String text) {
        Sign sign = (Sign) b.getState();
        sign.line(1, Component.text(text));
        sign.update(true);
    }

    private BlockFace parseBlockFace(String face) {
        switch (face) {
            case "north":
                return BlockFace.NORTH;
            case "east":
                return BlockFace.EAST;
            case "south":
                return BlockFace.SOUTH;
            case "west":
                return BlockFace.WEST;
        }
        return null;
    }

    private BlockFace parseInvertedBlockFace(String face) {
        switch (face) {
            case "north":
                return BlockFace.SOUTH;
            case "east":
                return BlockFace.WEST;
            case "south":
                return BlockFace.NORTH;
            case "west":
                return BlockFace.EAST;
        }
        return null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
