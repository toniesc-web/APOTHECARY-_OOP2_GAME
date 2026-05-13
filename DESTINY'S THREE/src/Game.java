import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

import javax.sound.sampled.*; 

public class Game {
    private Scanner sc = new Scanner(System.in);
    private Player player;
    private Adventure adventure;
    private Store store = new Store();
    private SwordUpgrader upgrader = new SwordUpgrader();
    private Clip introClip; 
    private Clip clickClip;
    
    // Helper method to find audio files in multiple locations
    private File findAudioFile(String filename) {
        // List of possible paths to check
        String[] paths = {
            "audio/" + filename,                    // root/audio/
            "src/audio/" + filename,                // src/audio/
            "../audio/" + filename,                 // parent folder
            "./audio/" + filename,                  // current folder
            "D:/APOTHECARY_OOP1_PROJECT_GAME/DESTINY'S THREE/src/audio/" + filename,
            "D:\\APOTHECARY_OOP1_PROJECT_GAME\\DESTINY'S THREE\\src\\audio\\" + filename
        };
        
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                System.out.println("");
                return f;
            }
        }
        return null;
    }
    
    public void start() {
        showIntroStory();
        selectCharacter();
        lobby();
    }

    // ========== SOUND HELPER METHODS ==========
    private void playSound(String filename, boolean loop) {
        try {
            AudioInputStream audioStream = null;
            
            // First try to find the file
            File audioFile = findAudioFile(filename);
            
            if (audioFile != null) {
                audioStream = AudioSystem.getAudioInputStream(audioFile);
            } else {
                // Try as resource
                java.net.URL soundURL = getClass().getResource("/audio/" + filename);
                if (soundURL != null) {
                    audioStream = AudioSystem.getAudioInputStream(soundURL);
                }
            }
            
            if (audioStream == null) {
                System.out.println("✗ Sound file not found: " + filename);
                return;
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.start();

            if (filename.contains("intro")) introClip = clip;
            if (filename.contains("Click")) clickClip = clip;
        } catch (Exception e) {
            System.out.println("Error playing sound: " + filename + " (" + e.getMessage() + ")");
        }
    }

    private void stopSound(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    
    private String getInput() {
        String input = sc.nextLine().trim();
        playSound("Click.wav", false);
        return input;
    }

    // ================= CINEMATIC STORY INTRO =================
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";


// --------------------- FULL CINEMATIC PROLOGUE ---------------------
private void playPrologue(Player player) {
    String ascii = "";
    switch (player.getClass().getSimpleName()) {
        case "Warrior" -> ascii = """
               ,   A           {}
              / \\, | ,        .--.
             |    =|= >      /.--.\\
              \\ /` | `       |====|
               `   |         |`::`|
                   | .-._    \\::::/
              Auron Steelheart - Warrior
        """;
        case "Mage" -> ascii = """
             .      .      .
           .´ \\\\  .´\\\\  //` .
          :   ( )  ( )  ( )   :
           `..'  `..'  `..'  '
              Kaelen Stormweaver - Mage
        """;
        default -> ascii = """
            /\\
           /  \\  __
          / /\\ \\/ _\\
         / /__\\ \\  /
        Sire Instanzia - Rogue
        """;
    }

    System.out.println("\n" + ascii + "\n");

    // ---------------- Universal cinematic opening ----------------
    System.out.println("╔═════════════════════════════════════════════════════════════════════════════════════════════════╗");
    String[] introLines = {
        " Narrator: Three worlds once stood in harmony - Light, Shadow, and the Citadel of Fate...",
        " Narrator: Until the day the sky cracked.",
        " Narrator: A deafening thunder split the silence. Shards of reality fell like glass.",
        " Glitched: YOU were chosen..... whether you wished it or not."
    };
    String[] introVoices = {"Narratorep1.wav", "Narratorep2.wav", "Narratorep3.wav", "Narratorep5.wav"};

    for (int i = 0; i < introLines.length; i++) {
        speakLine(introVoices[i], introLines[i], 70);
        System.out.println("\n........");
        sc.nextLine();
    }

    // ---------------- Character-specific cinematic POV ----------------
    switch (player.getClass().getSimpleName()) {
        case "Warrior" -> warriorCinematic();
        case "Mage" -> mageCinematic();
        default -> rogueCinematic();
    }

    printLetterByLetter("\nYour journey begins now...", 12);
    System.out.println("\nPress Enter to continue to the lobby...");
    sc.nextLine();
}

// ------------------- CINEMATIC POVS -------------------
private void warriorCinematic() {
    // Scene 1: Fortress Ruins
    String[] lines1 = {
        " Auron Steelheart: The Sunbreak Fortre.......... destroyed. Comrades....... fallen.",
        " Other Warriors: Auron!!!!!!!!! Stay strong!",
        " The Voice: Warrior... what do you fight for now?"
    };
    String[] voices1 = {"Steelheartep1.wav", "Warriorep1.wav", "Thevoiceep1.wav"};
    for (int i = 0; i < lines1.length; i++) {
        speakLine(voices1[i], lines1[i], 60);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    // Choice 1
    String[] choices1 = { 
        " Justice... I will avenge them.",
        " Duty. I'll finish what they started.",
        " Survival. Emotion is a weakness."
    };
    String[] choiceVoices1 = {"Steelheartep2.wav", "Steelheartep3.wav", "Steelheartep4.wav"};

    System.out.println(" What drives Auron?");
    presentChoice(choices1, choiceVoices1);

    // Scene 2: Rift opens, other classes appear
    String[] lines2 = {
        " Auron: The rift... strange forces converge here!",
        " Kaelen Stormweaver: The energy... I've never seen anything like it!",
        " Sire Instanzia: Heh... chaos smells delicious.",
        " Master: Auron! Protect the worlds... I must intervene!"
    };
    String[] voices2 = {"Steelheartep5.wav", "Stormweaverep1.wav", "Instanziaep1.wav", "Masterep1.wav"};
    for (int i = 0; i < lines2.length; i++) {
        speakLine(voices2[i], lines2[i], 60);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    // Choice 2
    String[] choices2 = {
        " I will charge the rift directly!",
        " I must coordinate with Kaelen and Sire.",
        " Focus on survival, regroup later."
    };
    String[] choiceVoices2 = {"Steelheartep6.wav", "Steelheartep7.wav", "Steelheartep8.wav"};
    System.out.println(" How will Auron respond?");
    presentChoice(choices2, choiceVoices2);

    // Scene 3: Master abducted, villain revealed
    String[] lines3 = {
        " Master: Auron! Kaelen! Sire! Stay back - the rift reacts to me!",
        " Narrator: A violent surge of energy tears open behind him.",
        " Villain: Ahh... there you are. The one who still resonates with the fracture.",
        " Narrator: The master staggers as chains of shimmering void-energy wrap around him.",
        " Auron: \"Master!!!! I'm coming!\"",
        " Master: No! Listen to me. You must not follow - not yet!",
        " Kaelen: The energy is overwhelming - I'm losing control of the readings!",
        " Sire: Damn it! Whoever you are - let him go!",
        " Villain: Come find him... if you survive the shattering of your worlds.",
        " Narrator: The master reaches a hand toward his students as he is pulled backward into the rift.",
        " Master: Do not give up. I trust you. All of you...",
        "The rift SLAMS shut.",
        "And so begins the quest across three broken worlds…\n" + //
        "not for glory, nor conquest…\n" + //
        "but to save the one who once guided you."
    };
    String[] voices3 = {"Masterep2.wav", "Narratorep6.wav", "Villainep1.wav", "Narratorep7.wav", "Steelheartep9.wav",
     "Masterep3.wav", "Stormweaverep2.wav", "Instanziaep2.wav", "Villainep2.wav", "Narratorep8.wav", "Masterep4.wav", "Narratorep9.wav", "Narratorep10.wav"};
    for (int i = 0; i < lines3.length; i++) {
        speakLine(voices3[i], lines3[i], 60);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }
}

//----------------------------------------------------------------------------------------------------------------------------------------//


private void mageCinematic() {
    // Scene 1: Observatory collapsing
    String[] lines1 = {
        "Narrator: The Floating Observatory trembles as reality unravels...",
        "Kaelen Stormweaver: The observatory... its collapsing mid-air!",
        "Other Mages: Kaelen! Protect the runes!",
        "The Voice: Mage... knowledge brought this. Will you act?"
    };
    String[] voices1 = {"Narratorep11.wav", "Stormweaverep3.wav", "OtherMages.wav", "Thevoiceep2.wav"};
    for (int i = 0; i < lines1.length; i++) {
        speakLine(voices1[i], lines1[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    // Choice 1
    String[] choices1 = {
        "I must save what I can.",
        "I will study the rift first.",
        "I will prioritize my own power."
    };
    String[] choiceVoices1 = {"Stormweaverep4.wav", "Stormweaverep5.wav", "Stormweaverep6.wav"};
    System.out.println("How does Kaelen react?");
    presentChoice(choices1, choiceVoices1);

    // Scene 2: Rift opens & other characters react
    String[] lines2 = {
        "Narrator: A rift tears through the skies, pulling at the fabric of reality...",
        "Kaelen: The rift grows... so much energy!",
        "Auron Steelheart: Magic this strong... we must be cautious!",
        "Sire Instanzia: Heh… chaos smells delicious.",
        "Master: Kaelen! Protect the worlds... I must intervene!"
    };
    String[] voices2 = {"Narratorep12.wav", "Stormweaverep7.wav", "Steelheartep10.wav", "Instanziaep1.wav", "Masterep5.wav"};
    for (int i = 0; i < lines2.length; i++) {
        speakLine(voices2[i], lines2[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    // Choice 2
    String[] choices2 = {
        "I pursue the rift immediately.",
        "I stabilize my magic first.",
        "I gather allies before action."
    };
    String[] choiceVoices2 = {"Stormweaverep8.wav", "Stormweaverep9.wav", "Stormweaverep10.wav"};
    System.out.println("What does Kaelen do?");
    presentChoice(choices2, choiceVoices2);

    // Scene 3: Villain introduction & Master abducted
    String[] lines3 = {
        "Narrator: A violent surge of energy tears open behind him.",
        "Villain: Ahh... there you are. The one who still resonates with the fracture.",
        "Kaelen: Master! They're taking you!",
        "Auron: Stay calm, we must act together!",
        "Sire: This could be fun… if we survive."
    };
    String[] voices3 = {"Narratorep6.wav", "Villainep1.wav", "Stormweaverep11.wav", "Steelheartep11.wav", "Instanziaep3.wav"};
    for (int i = 0; i < lines3.length; i++) {
        speakLine(voices3[i], lines3[i], 45);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }

    // Final Choice: Oath / tone
    String[] choices3 = {
        "I will restore balance, no matter what.",
        "I will master power to confront this threat.",
        "I will survive first, decide later."
    };
    String[] choiceVoices3 = {"Stormweaverep12.wav", "Stormweaverep13.wav", "Stormweaverep14.wav"};
    System.out.println("What oath does Kaelen take?");
    presentChoice(choices3, choiceVoices3);

    String[] lines4 = {
        "Master: Do not give up. I trust you. All of you...",
        "The rift SLAMS shut.",
        "And so begins the quest across three broken worlds…\n" + //
        "not for glory, nor conquest…\n" + //
        "but to save the one who once guided you."
    };
    String[] voices4 = {"Masterep4.wav", "Narratorep9.wav", "Narratorep10.wav"};
    for (int i = 0; i < lines4.length; i++) {
        speakLine(voices4[i], lines4[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }
}


//----------------------------------------------------------------------------------------------------------------------------------------//


private void rogueCinematic() {

    // ========== Scene 1: Sire's Instincts ========== 
    String[] scene1 = {
        "Sire Instanzia (thinking): The night was quiet... too quiet. When the Master calls us this late, it's never good.",
        "Master: Sire, Auron, Kaelen... thank you for coming.",
        "Sire: You look pale, Master. What happened?",
        "Auron: Yeah... you're not usually this shaken.",
        "Kaelen: Is this about the rifts again?"
    };
    String[] voices1 = {
        "Instanziaep4.wav", "Masterep6.wav", "Instanziaep5.wav",
        "Steelheartep12.wav", "Stormweaverep15.wav"
    };
    for (int i = 0; i < scene1.length; i++) {
        speakLine(voices1[i], scene1[i], 35);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }


    // ========== Scene 2: Something Feels Wrong ========== 
    String[] scene2 = {
        "Master: The rifts are moving... faster than before. I've been trying to keep them stable.",
        "Sire: That doesn't explain why your hands are shaking.",
        "Auron: Sire's right. What aren't you telling us?",
        "Kaelen: The air... changed just now. Did you feel that?",
        "Sire Instanzia (thinking): My instincts screamed-someone else was here."
    };
    String[] voices2 = {
        "Masterep7.wav", "Instanziaep6.wav", "Steelheartep13.wav",
        "Stormweaverep16.wav", "Instanziaep7.wav"
    };
    for (int i = 0; i < scene2.length; i++) {
        speakLine(voices2[i], scene2[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }


    // ========== Scene 3: Villain Appears ========== 
    String[] scene3 = {
        "Villain: Tskk... Took you long enough to notice me, little rogue.",
        "Sire: That voice... show yourself!",
        "Auron: Its coming from the rift-brace yourselves!",
        "Kaelen: Who are you?",
        "Villain: Someone your Master has been running from."
    };
    String[] voices3 = {
        "Villainep3.wav", "Instanziaep8.wav", "Steelheartep14.wav",
        "Stormweaverep17.wav", "Villainep4.wav"
    };
    for (int i = 0; i < scene3.length; i++) {
        speakLine(voices3[i], scene3[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }


    // ========== Scene 4: The Abduction ========== 
    String[] scene4 = {
        "Villain: Enough talking. I only came for him.",
        "Master: Stay back! All of you!",
        "Sire: MASTER-!",
        "Auron: Grab him before-!",
        "Kaelen: The rift it's dragging him in!",
        "Villain: Try and follow. I dare you.",
        "Narrator: The rift violently pulls the Master inside.",
        "Sire Instanzia (thinking): And just like that... he was gone."
    };
    String[] voices4 = {
        "Villainep5.wav", "Masterep8.wav", "Instanziaep9.wav",
        "Steelheartep15.wav", "Stormweaverep18.wav", "Villainep6.wav",
        "Narratorep13.wav", "Instanziaep10.wav"
    };
    for (int i = 0; i < scene4.length; i++) {
        speakLine(voices4[i], scene4[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }


    // ========== Scene 5: The Aftermath & Sire’s Resolve ========== 
    String[] scene5 = {
        "Auron: Sire... I'm sorry. We couldn't reach him.",
        "Kaelen: The rift's energy spreads into three directions.",
        "Sire: So we follow it. All three.",
        "Auron: You're sure you're ready for this?",
        "Sire: I don't get 'ready'. I move.",
        "Kaelen: Then we begin the chase.",
        "Sire Instanzia (thinking): Master... I don't know what you were hiding. But I'll drag you back myself if I have to."
    };
    String[] voices5 = {
        "Steelheartep16.wav", "Stormweaverep19.wav", "Instanziaep11.wav",
        "Steelheartep17.wav", "Instanziaep12.wav", "Stormweaverep20.wav",
        "Instanziaep13.wav"
    };
    for (int i = 0; i < scene5.length; i++) {
        speakLine(voices5[i], scene5[i], 40);
        System.out.println("\nPress Enter...");
        sc.nextLine();
    }


    // ========== Opening Choice Focused on Sire ========== 
    String[] choices = {
        "I'm bringing the Master back. No matter what's waiting in those rifts.",
        "That villain wants to play games? Fine. I'll hunt him in every world.",
        "Three rifts, three worlds... and I'll clear all of them."
    };
    String[] choiceVoices = {"Instanziaep14.wav", "Instanziaep15.wav", "Instanziaep16.wav"};

    System.out.println("What drives Sire forward?");
    presentChoice(choices, choiceVoices);
}



// -------------------- HELPERS --------------------
private void speakLine(String voiceFile, String text, int delay) {
    try {
        Clip clip;
        AudioInputStream audioStream = null;
        
        // First try to find the file
        File audioFile = findAudioFile(voiceFile);
        
        if (audioFile != null) {
            audioStream = AudioSystem.getAudioInputStream(audioFile);
        } else {
            // Try as resource
            java.net.URL soundURL = getClass().getResource("/audio/" + voiceFile);
            if (soundURL != null) {
                audioStream = AudioSystem.getAudioInputStream(soundURL);
            }
        }
        
        if (audioStream == null) {
            System.out.println("✗ Voice file not found: " + voiceFile);
            printLetterByLetter(text, delay);
            return;
        }
        
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();

        // Print text while audio plays
        printLetterByLetter(text, delay);

        // Wait for audio to finish
        while (clip.isRunning()) Thread.sleep(10);
        clip.close();
    } catch (Exception e) {
        System.out.println("Error playing line: " + e.getMessage());
        printLetterByLetter(text, delay);
    }
}


private void presentChoice(String[] options, String[] voices) {
    String choice = "";
    int index = -1;
    while (true) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%c) %s%n", 'A' + i, options[i]);
        }
        System.out.print("> ");
        choice = sc.nextLine().trim().toUpperCase();
        if (choice.length() == 1) {
            index = choice.charAt(0) - 'A';
            if (index >= 0 && index < options.length) break;
        }
        System.out.println("Invalid choice! Please enter A, B, or C.");
    }

    // Speak the chosen line
    speakLine(voices[index], options[index], 12);

    // Pause for player to read
    System.out.println("\nPress Enter to continue...");
    sc.nextLine();
}





    private void showIntroStory() {
        System.out.println();
         System.out.println(RED);
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║      █████  ██████   ██████  ████████ ██   ██ ███████   ██████   █████   ███████   ██    ██      ║");
        System.out.println("║     ██   ██ ██   ██ ██    ██    ██    ██   ██ ██       ██       ██   ██  ██    ██   ██  ██       ║");
        System.out.println("║     ███████ ██████  ██    ██    ██    ███████ █████    ██       ███████  ███████      ██         ║");
        System.out.println("║     ██   ██ ██      ██    ██    ██    ██   ██ ██       ██       ██   ██  ██    ██     ██         ║");
        System.out.println("║     ██   ██ ██       ██████     ██    ██   ██ ███████   ██████  ██   ██  ██     ██    ██         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("                             A   P   O   T   H   E   C   A   R   Y        ");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                  D E S T I N Y ' S   T H R E E                ");
        System.out.println(RESET);

        boolean validChoice = false;
        while (!validChoice) {
            System.out.println("\n");
            System.out.println("╔═══════════════════╗");
            System.out.println("║ 1. Skip Intro     ║");
            System.out.println("║ 2. Continue Story ║");
            System.out.println("╚═══════════════════╝");
            System.out.print("> ");

            String choice = getInput();
            try {
                int option = Integer.parseInt(choice);
                if (option == 1) {
                    System.out.println("\nSkipping intro...\n");
                    validChoice = true;
                    return;
                } else if (option == 2) {
                    validChoice = true;
                    break;
                } else {
                    System.out.println("Invalid choice! Please enter 1 or 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number (1 or 2).");
            }
        }

        playSound("intro_music.wav", true);

        String[] storyLines = {
            "In a land far beyond the stars, three worlds were bound by fate.",
            "The world of Light, the realm of Shadows, and the forgotten Citadel of Fate.",
            "Centuries ago, balance was lost and darkness spread across the lands.",
            "Now, destiny calls upon a new hero to restore harmony once again.",
            "The question remains - will you answer the call of destiny?"
        };

        System.out.println(RED);
        System.out.println("\n══════════════════════The Tale of Destiny══════════════════════\n");
        System.out.println(RESET);

        for (String line : storyLines) {
            printLetterByLetter(line, 10);
            System.out.println();
        }

        System.out.println("\n(Press Enter to begin your journey...)");
        getInput(); 
        stopSound(introClip);
    }

    private void printLetterByLetter(String text, int delay) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
        }
    }

    // ================= CHARACTER SELECTION =================
private void selectCharacter() {
    boolean validChoice = false;
    while (!validChoice) {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║ Choose your hero:                                          ║");
        System.out.println("║                                                            ║");
        System.out.println("║ 1. Warrior - Balanced strength and endurance.              ║");
        System.out.println("║ 2. Mage - High damage, fragile but wise.                   ║");
        System.out.println("║ 3. Rogue - Agile and fast, critical hits often.            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.print("> ");

        String input = getInput();
        switch (input) {
            case "1" -> { player = new Warrior("Auron Steelheart"); validChoice = true; }
            case "2" -> { player = new Mage("Kaelen Stormweaver"); validChoice = true; }
            case "3" -> { player = new Rogue("Sire Instanzia"); validChoice = true; }
            default -> System.out.println("Invalid choice! Please select 1, 2, or 3.\n");
        }
    }

    // Play the prologue cinematic for the selected character
    playPrologue(player);

    // Now create Adventure with player & game reference
    adventure = new Adventure(player, this);

    System.out.println(YELLOW);
    System.out.println("\nWelcome, " + player.getName() + "!");
    System.out.println(RESET);
    System.out.println(player.getDescription());
}


// ================= CHARACTER BACKSTORY + VOICE =================
private Clip playerVoiceClip; // global variable

private void showCharacterBackstory(Player player) {
    String ascii = "";
    String backstory = "";
    String voiceFile = "";

    switch(player.getName()) {
        case "Auron Steelheart" -> {
            ascii =
                    "   /\\\n" +
                    "  /  \\   _ __  _ __ ___  ___\n" +
                    " / /\\ \\ | '_ \\| '__/ _ \\/ __|\n" +
                    "/ ____ \\| | | | | |  __/\\__ \\\n" +
                    "/_/    \\_\\_| |_|_|  \\___||___/\n";
            backstory = "Auron Steelheart, the Warrior, balances strength and endurance.\n" +
                        "Raised in the Forest of Beginnings, he seeks justice and honor.\n" +
                        "Armed with his trusty sword, he stands against all darkness.";
            voiceFile = "Auron.wav";
        }
        case "Kaelen Stormweaver" -> {
            ascii =
                    "      ____\n" +
                    "     /\\' .\\    _____\n" +
                    "    /: \\___\\  / .  /\\\n" +
                    "    \\' / . / /____/..\\\n" +
                    "     \\/___/  \\'  '\\  /\n" +
                    "              \\'__'\\/\n";
            backstory = "Kaelen Stormweaver, the Mage, is wise but fragile.\n" +
                        "From the Caverns of Shadow, she wields elemental magic\n" +
                        "to turn the tide of battle and unravel ancient secrets.";
            voiceFile = "Kaelen.wav";
        }
        case "Sire Instanzia" -> {
            ascii =
                    "      .---.\n" +
                    "     /     \\  __\n" +
                    "    / /\\ /\\ \\(  )\n" +
                    "   /  \\/   \\/ /\n" +
                    "   \\  /\\   /\\ \\\n" +
                    "    '----'  '--'\n";
            backstory = "Sire Instanzia, the Rogue, moves with agility and stealth.\n" +
                        "From the shadows, he strikes swiftly, leaving enemies unaware.\n" +
                        "He thrives where cunning and speed rule the battlefield.";
            voiceFile = "Sire.wav";
        }
    }

    System.out.println("\n" + ascii);

    // Start voice BEFORE printing text
    try {
        File audioFile = findAudioFile(voiceFile);
        AudioInputStream audioStream;
        
        if (audioFile != null) {
            audioStream = AudioSystem.getAudioInputStream(audioFile);
        } else {
            java.net.URL soundURL = getClass().getResource("/audio/" + voiceFile);
            if (soundURL != null) {
                audioStream = AudioSystem.getAudioInputStream(soundURL);
            } else {
                audioStream = null;
            }
        }

        if (audioStream != null) {
            playerVoiceClip = AudioSystem.getClip();
            playerVoiceClip.open(audioStream);
            playerVoiceClip.start();
        }
    } catch (Exception e) {
        System.out.println("Error playing voice: " + e.getMessage());
    }

    printLetterByLetter(backstory, 40); // text prints while voice plays

    System.out.println("\nPress Enter to continue to the lobby...");
    sc.nextLine(); // wait for Enter

    // Stop voice if still playing
    if (playerVoiceClip != null && playerVoiceClip.isRunning()) {
        playerVoiceClip.stop();
        playerVoiceClip.close();
    }
}


    // ================= MAIN LOBBY =================
    private void lobby() {
        boolean running = true;
        while (running) {
            showStats();

            System.out.println("║                                                                                                               ║");
            System.out.println("║                                                                                                               ║");
            System.out.println("║                                                                                                               ║");
            System.out.println("║═════════════════════╦═════════════════════╦════════════════════════════╦════════════════════════╦═════════════║");
            System.out.println("║     ADVENTURE       ║  CANTILLAS'S STORE  ║   CANEDO'S SWORD UPGRADER  ║       INVENTORY        ║    EXIT     ║");
            System.out.println("╚═════════════════════╩═════════════════════╩════════════════════════════╩════════════════════════╩═════════════╝ ");
            System.out.println("[1] Adventure Mode");
            System.out.println("[2] Store");
            System.out.println("[3] Sword Upgrader");
            System.out.println("[4] Inventory");
            System.out.println("[5] Exit Game");
            System.out.print("> ");

            String choice = getInput();
            switch (choice) {
                case "1" -> adventure.start();
                case "2" -> store.open(player);
                case "3" -> upgrader.open(player);
                case "4" -> player.getInventory().open(player);
                case "5" -> {
                    System.out.println("\n\n");
                    System.out.println("╔════════════════════════════════════════════════════╗");
                    System.out.println("║        Thank you for playing Destiny's Three!      ║");
                    System.out.println("╚════════════════════════════════════════════════════╝");
                    running = false;

                    showCredits(); 
                    }

                default -> {
                    System.out.println("╔════════════════════════════════════╗");
                    System.out.println("║ Invalid choice! Please select 1-5. ║");
                    System.out.println("╚════════════════════════════════════╝");
                }
            }
            System.out.println();
        }
    }

    // ================= SHOW PLAYER STATS =================
    private void showStats() {
        System.out.println(GREEN);
        System.out.printf("\n\n %s", getCurrentDateTime());
        System.out.println(RESET);
        System.out.println(" ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("║  Player: %s  ║  HP: %d/%d  ║  MP: %d/%d  ║  ATK: %d  ║  Gold: %d  ║ \n",
                player.getName(), player.getHp(), player.getMaxHp(),
                player.getMana(), player.getMaxMana(),
                player.getAttack(), player.getGold());
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                                                               ║");
    }

    // ================= MAIN ENTRY =================
    public static void main(String[] args) {
        new Game().start();
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
    
    public void showCredits() {
        playSound("Ending.wav", true); 

        String[] creditsLines = {
            "╔════════════════════════════════════════════════════════════════════════╗",
            "║                         CREDITS - DESTINY'S THREE                      ║",
            "╚════════════════════════════════════════════════════════════════════════╝",
            "",
            "Game Design          : Salado, Samuel O.(Lead Designer & Developer)",
            "Programming          : Salado, Samuel O., Cantillas Bryce Josef R., Canedo James D.(OOP & Gameplay Systems)",
            "Art & ASCII Design   : Salado, Samuel O.(Story & Visuals)",
            "Music & Sound FX     : Salado, Samuel O.(WAV Tracks for Immersion)",
            "Educational Concept  : Cantillas Bryce Josef R. , Canedo James D.(Integrating coding and storytelling)",
            "Adventure & Combat   : Salado, Samuel 0., Canedo James D.(Game Mechanics & Enemy Design)",
            "Character Design     : Salado, Samuel O., Canedo James D. (Player Classes and Skills)",
            "User Interface       : Salado, Samuel O. (Console-based Menus & ASCII Graphics)",
            "Testing & QA         : Cantillas Bryce Josef R. (Ensuring Functionality & Fun)",
            "Special Thanks       : All learners exploring Java & Game Development",
            "",
            "Thank you for playing Destiny's Three!",
            "This game is designed for educational purposes, showcasing object-oriented programming,",
            "sound integration, and interactive storytelling. Throughout your journey, you experienced",
            "foundational programming techniques:\n  - Class and object relationships",
            "  - Combat and progression systems\n  - Inventory and item management\n  - User input and branching logic",
            "  - File and sound integration\n\n. Each world was crafted to highlight different design approaches-from atmospheric",
            "storytelling to gameplay pacing and challenge balancing.",
            "This game also showcases the power of Java in creating engaging",
            "text-based adventures,\nencouraging new learners to explore programming",
            "through creativity. We hope Destiny's Three inspires you to continue learning,",
            "building, and imagining\nnew projects of your own.",
            "Thank you for being part of this adventure!"
        }; 

        for (String line : creditsLines) {
            printLetterByLetter(line, 7);
            System.out.println();
        }

        System.out.println("\nPress Enter to exit...");
        sc.nextLine(); 
        stopSound(introClip); 
    }
}
