# DungeonImport
this paper plugin was made in paper version git-paper-237 and tested on a 1.18.2 server

this plugin aims to allow the importation of dungeons generated via https://donjon.bin.sh/5e/dungeon/ into Minecraft for facilitating the use of Minecraft as a digital tabletop.

it uses a few commands

```
/di h/help : shows help

/di <l/load> "file name" : load dungeon data (this has to be done before other commands)

/di <g/gen/generate> [wall block] [floor block] : generate loaded in dungeon (will place blocks in world and cant be undone)

/di <i/info> : list general dungeon info

/di <r/room> <num> : shows the info for room #num
  
/di <f/feature> <letter> : shows the info of feature letter
  
/di <w/wonder> : displays a list of wondering monsters
  
/di reload : reloads the plugins file listings
```

notes:

THIS PLUGIN DOES NOT SUPPORT THE PERIPHERAL EGRESS OPTION on the generator, if you generate a dungeon with it turned on the dungeon will be unusable.

the block types for the generation command are optional, and it will use white concrete for floors and black concrete for walls by default.

a few special blocks are included, fence gates represent doors, locked doors, and trapped doors. iron bars represent portcullises (there basically doors that can be seen and shot through), and secret doors are replaced with regular wall blocks, as you don't want them being seen by players. however, they do still appear in the room info command.

the room info command lists doors based on what side of the room they are on, like "north", "east", "south" and "west" this lines up with the in game Minecraft directions when pressing the f3 button, listed near the bottom left side it says the direction your facing. also if your standing directly facing one of the signs you are facing north.

if you come across an error while using this plugin (if it says in chat "An internal error occurred while trying to complete this command") please provide a report on the issues page (https://github.com/aspwil/DungeonImport/issues) please include what command you ran that caused the issue and if you had a file loaded to make sure to include the file to via attachment.

this plugin and its code is provided under the standard MIT licence, this essentially means you are free to use, alter, and, distribute this code privately, publicly, and commercially as long as you don't remove the licence notice.

for more info on the licence see the LICENCE.txt file
