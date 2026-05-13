import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
    
public class Adventure  {
    private List<World> worlds = new ArrayList<>();
    private Player player;
    private Game game;              
    private Scanner sc = new Scanner(System.in);
    private boolean storyCompleted = false;
    private int currentWorld = 0;
    private int currentMobIndex = 0; 
    private boolean[] dialoguePlayed = new boolean[3]; // Tracks if post-boss storyline has played per world
    private boolean[] storylinePending = new boolean[3]; // Tracks if after-boss dialogue is pending
    private Clip introClip;

    public Adventure(Player p, Game game) {
        this.player = p;
        this.game = game;
        worlds.add(new World("Forest of Beginnings", 3));
        worlds.add(new World("Caverns of Shadow", 4));
        worlds.add(new World("Citadel of Fate", 5));
    }


    public void start() {
    if (!player.isAlive()) {
        System.out.println("\nYou cannot enter adventure! Your HP is 0. Heal first.\n");
        return;
    }


    // If story already completed → show ending menu
    if (storyCompleted) {
        storyFinishedMenu();
        return;
    }

    World w = worlds.get(currentWorld);

    // ======================================================
    //        CHECK IF STORYLINE POST-BOSS MUST PLAY
    // ======================================================
    if (storylinePending[currentWorld]) {

        switch (currentWorld) {
            case 0 -> showStoryline("afterForest");
            case 1 -> showStoryline("afterCaverns");
            case 2 -> showStoryline("afterCitadel");
        }

        storylinePending[currentWorld] = false;

        // ===== If this was the final world (Citadel) =====
        if (currentWorld == worlds.size() - 1) {
            handleNexusSequence();     // <--- NEW FINAL SCENE
            return;
        }

        // Move to next world
        currentWorld++;
        currentMobIndex = 0;
        w = worlds.get(currentWorld);
    }

    // ======================================================
    //                     ENTER WORLD
    // ======================================================
    System.out.println("\n╔════════════════════════════════════╗");
    System.out.println("║                                    ║");
    System.out.println("   Entering " + w.getName() + "...    ");
    System.out.println("║                                    ║");
    System.out.println("╚════════════════════════════════════╝");

    List<Mob> mobs = w.getMobs();

    // Show intro only when entering the first mob of that world
    if (currentMobIndex == 0) {
        showWorldIntro(w);
    }

    // ======================================================
    //                      MOBS
    // ======================================================
    while (currentMobIndex < mobs.size()) {
        Mob m = mobs.get(currentMobIndex);

        if (m.getHp() <= 0) {
            currentMobIndex++;
            continue;
        }

        m.setHp(m.getMaxHp());

        System.out.println("   ════════════════════════════════════");
        System.out.println("\t  --- Mob " + (currentMobIndex + 1) + " out of " + mobs.size() + " ---");
        System.out.println("   ════════════════════════════════════");

        boolean cont = battle(m);
        if (!cont) return;

        playWinSound();
        player.setMana(player.getMaxMana());
        currentMobIndex++;
    }

    // ======================================================
    //                      BOSS
    // ======================================================
    Boss boss = w.getBoss();

    if (boss.getHp() > 0) {
        boss.setHp(boss.getMaxHp());

        System.out.println("\nThe boss appears: " + boss.getName() + "!");

        // Boss intro voicelines
        switch (boss.getName()) {
            case "Forest Guardian" ->
                speakLine("ForestGuardian.wav", "Forest Guardian: \"I will protect this forest from intruders!\"", 30);
            case "Shadow Wraith" ->
                speakLine("ShadowWraith.wav", "Shadow Wraith: \"Your light cannot pierce my darkness!\"", 30);
            case "Citadel Warden" ->
                speakLine("CitadelWarden.wav", "Citadel Warden: \"You dare defy fate? Prepare to be undone!\"", 30);
        }

        System.out.println("\n........");
        sc.nextLine();

        boolean bossDefeated = battle(boss);

        if (bossDefeated) {
            playWinSound();
            System.out.println("\nYou defeated " + w.getName() + "!");
            player.addGold(100);
            player.getInventory().addItem(new Weapon("Boss Relic", "A powerful weapon fragment", 10));
            player.setMana(player.getMaxMana());

            // Activate storyline for AFTER boss
            storylinePending[currentWorld] = true;

            boss.setHp(0);
            currentMobIndex = 0;

            // If FINAL world beaten → play Nexus sequence next time start() is called
            return;

        } else {
            System.out.println("\nYou retreated to the lobby...");
            return;
        }

    } else {
        // Boss was already beaten but storyline not yet played
        if (!storylinePending[currentWorld]) {
            storylinePending[currentWorld] = true;
        }
    }
}




    // ==========================
    // WORLD INTRO SYSTEM
    // ==========================
    private void showWorldIntro(World world) {
    playWorldIntroMusic(world);

    System.out.println("\n\t    ╔═══════════════════════════════════╗");
    System.out.println("                    "+ world.getName().toUpperCase() + "       ");
    System.out.println("\t    ╚═══════════════════════════════════╝\n");

    String backstory = switch (world.getName()) {
        case "Forest of Beginnings" ->
            "The Forest of Beginnings flourished as a sanctuary of life and growth,\n" +
            "where nature's magic nurtured both creatures and heroes-in-training.\n" +
            "Birdsong fills the air, but shadows creep where the sunlight fades.";
        case "Caverns of Shadow" ->
            "Deep beneath the mountains, the Caverns of Shadow spread their creeping darkness.\n" +
            "The air is cold and damp, echoing with whispers of creatures born from fear.\n" +
            "Your heart pounds as unseen eyes follow every step.";
        case "Citadel of Fate" ->
            "At the center of all stood the Citadel of Fate, an ancient fortress holding\n" +
            "the secrets of destiny and the threads that bind the worlds.\n" +
            "Towering walls loom above, and the wind carries the whispers of long-dead kings.";
        default -> "";
    };

    printLetterByLetter(backstory, 10);

    System.out.println("\nPress Enter to continue...");
    sc.nextLine(); 

    if (introClip != null) {
        introClip.stop();
        introClip.close();
        introClip = null;
    }
}


    private void playWorldIntroMusic(World world) {
        String filename = switch (world.getName()) {
            case "Forest of Beginnings" -> "world1.wav";
            case "Caverns of Shadow" -> "world2.wav";
            case "Citadel of Fate" -> "Citadel.wav";
            default -> "";
        };

        if (filename.isEmpty()) return;

        try {
            java.net.URL soundURL = getClass().getResource(filename);
            AudioInputStream audioStream;

            if (soundURL != null) audioStream = AudioSystem.getAudioInputStream(soundURL);
            else {
                File file = new File("Win.wav");
                if (!file.exists()) {
                    return;
                }
                audioStream = AudioSystem.getAudioInputStream(file);
            }

            introClip = AudioSystem.getClip();
            introClip.open(audioStream);
            introClip.start();

        } catch (Exception e) {
            System.out.println("Error playing world intro music: " + e.getMessage());
        }
    }

    private void printLetterByLetter(String text, int delayMS) {
        try {
            for (char c : text.toCharArray()) {
                System.out.print(c);
                Thread.sleep(delayMS);
            }
            System.out.println();
        } catch (Exception ignored) { }
    }

    // ==========================
    // BATTLE SYSTEM
    // ==========================
    private boolean battle(Enemy enemy) {
        while (enemy.isAlive() && player.isAlive()) {

            showBattleStatus(enemy);

            System.out.print("> ");
            String input = sc.nextLine().trim();
            int c;
            try { c = Integer.parseInt(input); }
            catch (Exception e) {
                System.out.println("Invalid input!");
                continue;
            }

            boolean playerActed = false;

            switch (c) {
    case 1 -> { 
        player.attack(enemy); 
        playerActed = true; 
    }

    case 2 -> {
        while (true) {
            player.showSkills();
            System.out.println("B. Back");
            System.out.print("Select a skill: ");
            String choice = sc.nextLine().trim().toUpperCase();

            if (choice.equals("B")) break;

            int skillChoice;
            try { skillChoice = Integer.parseInt(choice) - 1; }
            catch (Exception e) {
                System.out.println("Invalid input! Enter a number or B to go back.");
                continue;
            }

            if (skillChoice < 0 || skillChoice >= player.getSkills().size()) {
                System.out.println("Invalid skill number!");
                continue;
            }

            player.useSkill(skillChoice, enemy);
            playerActed = true;
            break;
        }
    }

    case 3 -> {
        player.getInventory().open(player);
        continue;  
    }

    case 4 -> { 
        System.out.println("You run back to the lobby."); 
        return false; 
    }

    default -> System.out.println("Invalid choice!");
}

            if (playerActed && enemy.isAlive()) enemy.attack(player);
        }

        // ======= PLAYER DIED: show Game Over and choices =======
        if (!player.isAlive()) {
            // ASCII Game Over (kept simple to preserve console layout)
            System.out.println("\n╔════════════════════════════╗");
            System.out.println("║         GAME OVER          ║");
            System.out.println("╚════════════════════════════╝\n");

            while (true) {
                System.out.println("1. Restart Adventure (full reset)");
                System.out.println("2. Exit to Credits");
                System.out.print("> ");
                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    restartAdventure(); // full reset and back to lobby
                    return false;       // stop current battle
                } else if (choice.equals("2")) {
                    // Call the credits routine in Game
                    if (game != null) game.showCredits();
                    return false;
                } else {
                    System.out.println("Invalid choice! Please select 1 or 2.");
                }
            }
        }

        // Player won the fight
        System.out.println("\nYou defeated " + enemy.getName() + "!");
        int gold = 20 + (int)(Math.random() * 20);
        player.addGold(gold);
        System.out.println("You gained " + gold + " gold!");
        

        // Play win sound (kept as you had it)
        try {
            java.net.URL soundURL = getClass().getResource("Win.wav");
            AudioInputStream audioStream;

            if (soundURL != null) audioStream = AudioSystem.getAudioInputStream(soundURL);
            else {
                File file = new File("Win.wav");
                if (!file.exists()) {
                    System.out.println("Win.wav file not found!");
                    audioStream = null;
                } else audioStream = AudioSystem.getAudioInputStream(file);
            }

            if (audioStream != null) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            }

        } catch (Exception e) {
            System.out.println("Enemy defeat sound error: " + e.getMessage());
        }

        // Loot for regular enemies
if (Math.random() < 0.5) {
    if (Math.random() < 0.5)
        player.getInventory().addItem(new Potion("Health Potion", 
            "Restores 50 HP. Brewed from forest herbs.", 50, 1));
    else
        player.getInventory().addItem(new Weapon("Rusty Blade", 
            "A weak sword, dented but usable.", 5));
}

// Boss loot (if the enemy is a boss)
if (enemy instanceof Boss) {
    player.getInventory().addItem(new Weapon("Boss Relic", 
        "A fragment of immense power, imbued with the essence of the fallen boss.", 10));
}


        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Continue to next enemy");
            System.out.println("2. Return to Lobby");
            System.out.print("> ");
            String choice = sc.nextLine().trim();
            if (choice.equals("1")) return true;
            else if (choice.equals("2")) return false;
            else System.out.println("Invalid choice! Please select 1 or 2.");
        }
    }

    private void showBattleStatus(Enemy enemy) {
        System.out.println("\n");
        System.out.println("╔═══════════════════════╦═════╦═══════════════╦═════╦══════════════════════╗");
        System.out.println("║                       ║     ║ BATTLE STATUS ║     ║                      ║");
        System.out.println("║                       ║     ╚═══════════════╝     ║                      ║");
        System.out.println("   " + player.getName() + "\t\t\t\t       " + enemy.getName());
        System.out.println("   HP: " +  player.getHp() + " / " + player.getMaxHp() + "                                       HP: " + enemy.getHp() + " / " + enemy.getMaxHp() + "         ");
        System.out.println("   MANA: " + player.getMana() + "                                                           ");
        System.out.println("║                       ║                           ║                      ║");
        System.out.println("║                       ║                           ║                      ║");
        System.out.println("╠═══════════════════════╝                           ╚══════════════════════╣");
        System.out.println("║                                                                          ║");
        System.out.println("║                                                                          ║");
        System.out.println("║                                                                          ║");
        System.out.println("╠══════════════╦════════════╦══════════════════════════════════════════════╣");
        System.out.println("║   1. ATTACK  ║  2. SKILL  ║                                              ║");
        System.out.println("╠══════════════╬════════════╣                                              ║");
        System.out.println("║   3. BAG     ║  4. RUN    ║                                              ║");
        System.out.println("╚══════════════╩════════════╩══════════════════════════════════════════════╝");
    }

    private void playWinSound() {
        try {
            File file = new File("Win.wav");
            if (!file.exists()) {
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            Thread.sleep(8500);
            clip.stop();
            clip.close();
        } catch (Exception e) {
            System.out.println("Win sound error: " + e.getMessage());
        }
    }

    // ==========================
    // RESTART (FULL RESET)
    // ==========================
    private void restartAdventure() {
    // Reset player core stats
    player.reset(); // will reset HP, Mana, Attack, clear inventory, and set gold to default in Player.reset()

    // Reset world progress counters
    currentWorld = 0;
    currentMobIndex = 0;

    // Reset all mobs and bosses HP to their max
    for (World w : worlds) {
        for (Mob m : w.getMobs()) {
            m.setHp(m.getMaxHp());
        }
        w.getBoss().setHp(w.getBoss().getMaxHp());
    }

    // Reset story completion flag
    storyCompleted = false;
}


// ==========================
// STORYLINE DISPLAY
// ==========================
private void showStoryline(String key) {

    switch (key) {

        case "afterForest" -> {

    // Boss defeat voiceline
      speakLine("ForestGuardianDefeated.wav",
      "Forest Guardian: \"You have proven your worth... protect the balance...\"", 45);

      System.out.println("\n........");
      sc.nextLine();

      // Narrator lines (each voiced)
      speakLine("NarratorForest1.wav",
      "Narrator: The Forest of Beginnings breathes again, its wounds slowly healing...", 45);
      System.out.println("\n........");
      sc.nextLine();

      speakLine("NarratorForest2.wav",
      "Narrator: Yet the air trembles... something deeper has awakened.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Auron’s line
      speakLine("AuronForest1.wav",
      "Auron: The road ahead grows darker. I can feel it in my steel.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Kaelen’s line
      speakLine("KaelenForest1.wav",
      "Kaelen: The shadows gather in the Caverns... I sensed the Rift pulsing again.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Sire’s line
      speakLine("SireForest1.wav",
      "Sire: Heh... Then we follow the pulse. Something deliciously chaotic awaits.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Final narrator wrap
      speakLine("NarratorForest3.wav",
      "Narrator: And so, the three descend toward the Caverns of Shadow... unaware that the true calamity has only begun.", 50);
      System.out.println("\n........");
      sc.nextLine();
}
    
    //----------------------------------------------------------------------------------------------------------------------------------------//

        case "afterCaverns" -> {

    // Boss defeat voiceline
      speakLine("ShadowWraithDefeated.wav",
      "Shadow Wraith: \"The darkness... folds... you cannot outrun what follows...\"", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Narrator line 1
      speakLine("NarratorCavern1.wav",
      "Narrator: The Caverns of Shadow fall silent... yet an unease lingers beneath the earth.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Narrator line 2
      speakLine("NarratorCavern2.wav",
      "Narrator: Something twisted in the dark... watching... waiting.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Kaelen’s line (he senses something)
      speakLine("KaelenCavern1.wav",
      "Kaelen: That last surge of energy… it wasn't the Wraith. Something deeper was controlling it.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Auron’s line (determined)
      speakLine("AuronCavern1.wav",
      "Auron: Then we cut through whatever waits next. The Citadel lies ahead.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Sire’s line (suspicious, observing the others)
      speakLine("SireCavern1.wav",
      "Sire: Mmh... funny how both of you felt that same pulse. Almost like someone wanted us to.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Narrator line 3 — rising tension among the trio
      speakLine("NarratorCavern3.wav",
      "Narrator: Tension stirs between the three... trust bends, shadows deepen.", 45);
      System.out.println("\n........");
      sc.nextLine();

    // Narrator final line
      speakLine("NarratorCavern4.wav",
      "Narrator: The Citadel of Fate awaits... and with it, the truth behind the Rift.", 45);
      System.out.println("\n........");
      sc.nextLine();
}
    
    //----------------------------------------------------------------------------------------------------------------------------------------//

        case "afterCitadel" -> {

    // Boss defeat voiceline
    speakLine("CitadelWardenDefeated.wav",
    "Citadel Warden: \"I... fall... but fate still coils around you... all of you...\"", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Narrator 1 — the Citadel trembles
    speakLine("NarratorCitadel1.wav",
    "Narrator: The Citadel shudders, ancient gears grinding as the Warden collapses.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Narrator 2 — something is awakening
    speakLine("NarratorCitadel2.wav",
    "Narrator: In the heart of the chamber... a dormant presence flickers to life.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Kaelen — alarmed
    speakLine("KaelenCitadel1.wav",
    "Kaelen: That energy spike-it's the same one from the forest and caverns... but stronger!", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Auron — senses his master's aura
    speakLine("AuronCitadel1.wav",
    "Auron: Wait... this aura... I know it. Impossible... it's him.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Sire — enjoying the chaos
    speakLine("SireCitadel1.wav",
    "Sire: Oooh... I love revelations. Go on, Auron. Whose presence is crawling up your spine?", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Narrator 3 — the truth begins to surface
    speakLine("NarratorCitadel3.wav",
    "Narrator: From beyond the fractured gate, a silhouette stirs... bound by runes of pure fate.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Auron — realization
    speakLine("AuronCitadel2.wav",
    "Auron: Master... Why are you sealed inside the Citadel?", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Mysterious Voice (distorted master, not revealing yet)
    speakLine("MasterDistortedCitadel.wav",
    "Mysterious Voice: \"Auron! Kaelen! Sire! do not come closer... the truth is not yet yours...\"", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Kaelen — confused
    speakLine("KaelenCitadel2.wav",
    "Kaelen: That was him. But his voice-twisted, restrained. Something's controlling him.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Sire — quietly disturbed
    speakLine("SireCitadel2.wav",
    "Sire: For once... I don't find this amusing.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Narrator 4 — ending line before the final act
    speakLine("NarratorCitadel4.wav",
    "Narrator: The Citadel parts, revealing the path into the Riven Nexus... where the mastermind waits.", 45);
    System.out.println("\n........");
    sc.nextLine();

    // Narrator 5 — final tension line
    speakLine("NarratorCitadel5.wav",
    "Narrator: And at its center... the one they came to save.", 45);
    System.out.println("\n........");
    sc.nextLine();
     }

    }

    System.out.println("\nPress Enter to continue...");
    sc.nextLine();
}
 
    //----------------------------------------------------------------------------------------------------------------------------------------//


private void storyFinishedMenu() {
    System.out.println("\n╔════════════════════════════╗");
    System.out.println("║     THE STORY IS OVER!     ║");
    System.out.println("╚════════════════════════════╝\n");

    while (true) {
        System.out.println("What would you like to do?");
        System.out.println("1. Play Again (Start from Beginning)");
        System.out.println("2. Exit to Credits");
        System.out.print("> ");
        String choice = sc.nextLine().trim();

        if (choice.equals("1")) {
            // Restart full game: reset player stats, inventory, and world progress
            if (game != null) {
                System.out.println("\nRestarting the full game...\n");
                game.start(); // Call Game.start() to show intro, character selection, and lobby
            }
            break; // exit the menu
        } else if (choice.equals("2")) {
            if (game != null) game.showCredits(); // go to Game.java credits
            break;
        } else {
            System.out.println("Invalid choice! Please select 1 or 2.");
        }
    }
}
// ============================================
// VOICE + TEXT SYSTEM
// ============================================

// Plays audio + prints text letter by letter
private void speakLine(String audioFile, String text, int speed) {
    try {
        // Try loading from resources (inside jar/classpath)
        java.net.URL soundURL = getClass().getResource(audioFile);
        AudioInputStream audioStream;

        if (soundURL != null) {
            audioStream = AudioSystem.getAudioInputStream(soundURL);
        } else {
            // Fallback file load
            File file = new File(audioFile);
            if (!file.exists()) {
                System.out.println("[Missing audio file: " + audioFile + "]");
                printLetterByLetter(text, speed);
                return;
            }
            audioStream = AudioSystem.getAudioInputStream(file);
        }

        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();

        printLetterByLetter(text, speed);

    } catch (Exception e) {
        System.out.println("Audio error (" + audioFile + "): " + e.getMessage());
        printLetterByLetter(text, speed);
    }
}


// ============================================
// PRESENT CHOICE (A / B / C) WITH VOICELINES
// ============================================
private void presentChoice(String[] choices, String[] voiceFiles) {
    Scanner sc = new Scanner(System.in);

    char option = 'A';
    for (String c : choices) {
        System.out.println(option + ". " + c);
        option++;
    }

    char selected;
    while (true) {
        System.out.print("> ");
        String input = sc.nextLine().trim().toUpperCase();

        if (input.length() == 1 && input.charAt(0) >= 'A' && input.charAt(0) < ('A' + choices.length)) {
            selected = input.charAt(0);
            break;
        }

        System.out.println("Invalid choice! Please enter A, B, or C.");
    }

    int index = selected - 'A';

    // Speak the line of the selected choice
    speakLine(voiceFiles[index], choices[index], 25);

    System.out.println("\n........");
    sc.nextLine(); // pause
}

private void handleNexusSequence() {
    System.out.println("\nA strange humming fills the air...");
    System.out.println("The gateway to the Riven Nexus opens before you.\n");

    while (true) {
        System.out.println("1. Step forward into the Nexus");
        System.out.println("2. End Game");
        System.out.print("> ");

        String choice = sc.nextLine().trim();

        if (choice.equals("1")) {
            playNexusScene();
            storyCompleted = true;
            storyFinishedMenu();
            break;
        } else if (choice.equals("2")) {
            System.out.println("\nYou turn away, but destiny waits...");
            storyCompleted = true;
            storyFinishedMenu();
            break;
        } else {
            System.out.println("Invalid input! Please select 1 or 2.");
        }
    }
}



private void playNexusScene() {

    System.out.println("\n╔══════════════════════════════════╗");
    System.out.println("║        R I V E N   N E X U S     ║");
    System.out.println("╚══════════════════════════════════╝\n");

    speakLine("Narratorep14.wav",
        "Narrator: The Nexus... a place where worlds overlap, where time folds and truth reveals itself.", 45);
    sc.nextLine();

    speakLine("Steelheartep18.wav",
        "Auron: The air here... it cuts deeper than any blade.", 35);
    sc.nextLine();

    speakLine("Stormweaverep21.wav",
        "Kaelen: Stay close. Something ancient is stirring.", 35);
    sc.nextLine();

    speakLine("Instanziaep17.wav",
        "Sire Instanzia: Hah... look ahead. We are no longer alone.", 35);
    sc.nextLine();

    
    // MASTER APPEARS
    speakLine("Masterep9.wav",
        "Master: You've finally arrived...", 40);
    sc.nextLine();

    speakLine("Stormweaverep22.wav",
        "Kaelen: M-Master... You're alive!", 40);
    sc.nextLine();

    speakLine("Steelheartep19.wav",
        "Auron: Then who took you? What happened in the Observatory?", 40);
    sc.nextLine();


    // VILLAIN EMERGES
    speakLine("Villainep7.wav",
        "Villain: Ohhh, haven't they told you?", 40);
    sc.nextLine();

    speakLine("Villainep8.wav",
        "Villain: Go on, old friend... tell them your little secret.", 45);
    sc.nextLine();


    // MASTER'S CONFESSION
    speakLine("Masterep10.wav",
        "Master: The rift... the chaos... each world collapsing... it was all because of me.", 50);
    sc.nextLine();

    speakLine("Stormweaverep23.wav",
        "Kaelen: N-no... Master... you guided us—", 45);
    sc.nextLine();

    speakLine("Masterep11.wav",
        "Master: I guided you, yes... straight into the path I required.", 45);
    sc.nextLine();

    speakLine("Narratorep15.wav",
        "Narrator: A chill runs through the Nexus. Trust — shattered.", 40);
    sc.nextLine();


    // VILLAIN AND MASTER TEAM-UP / TWIST
    speakLine("Villainep9.wav",
        "Villain: You still don't see it, do you? We aren't enemies...", 45);
    sc.nextLine();

    speakLine("Villainep10.wav",
        "Villain: The Master and I... we opened the Nexus together.", 40);
    sc.nextLine();

    speakLine("Masterep12.wav",
        "Master: And now that the path is stable... we have no more use for you.", 40);
    sc.nextLine();


    // HEROES REACT
    speakLine("Instanziaep18.wav",
        "Sire Instanzia: You traitorous fossil! I knew something reeked about your disappearance!", 45);
    sc.nextLine();

    speakLine("Steelheartep20.wav",
        "Auron: You're not getting away! Not after everything you've done!", 45);
    sc.nextLine();


    // ESCAPE — SEQUEL SETUP
    speakLine("Villainep11.wav",
        "Villain: Oh, but we are. Another world awaits us... and a throne left empty.", 45);
    sc.nextLine();

    speakLine("Masterep13.wav",
        "Master: Follow if you dare, my students. But know this — I will not hold back next time.", 50);
    sc.nextLine();


    // TELEPORT EFFECT
    speakLine("Narratorep16.wav",
        " A violent tearing sound echoes as the Master and Villain vanish into a rippling void.", 45);
    sc.nextLine();


    // CHASE ATTEMPT
    speakLine("Steelheartep21.wav",
        "Auron: We have to go after them! NOW!", 40);
    sc.nextLine();

    speakLine("Stormweaverep24.wav",
        "Kaelen: Auron, wait-! The rift is unstable! One step and you'll be torn apart!", 45);
    sc.nextLine();

    speakLine("Narratorep17.wav",
        "Narrator: In the trembling silence, the heroes stand before the fading rift...", 45);
    sc.nextLine();


    // FINAL RESOLUTION
    speakLine("Instanziaep19.wav",
        "Sire Instanzia: Let them run. They can't hide forever.", 40);
    sc.nextLine();

    speakLine("Stormweaverep25.wav",
        "Kaelen: We'll find the truth. No matter where they flee.", 45);
    sc.nextLine();

    speakLine("Narratorep18.wav",
        "Narrator: And thus, the story does not end... it merely shifts to another page.", 50);
    sc.nextLine();

    speakLine("Narratorep19.wav",
        "Narrator: The hunt for the Master begins.", 50);
    sc.nextLine();
}
}
