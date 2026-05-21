import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

// ═══════════════════════════════════════════════════════════════════
//   DESTINY'S THREE — PREMIUM RPG GUI
//   Drop this file in the same folder as your other .java files.
//   Compile & run:  javac *.java   →   java PremiumRPGGUI
// ═══════════════════════════════════════════════════════════════════
public class PremiumRPGGUI {

    // ── Core game objects ─────────────────────────────────────────
    private Game    game;
    private Player  player;
    private Store   store   = new Store();
    private SwordUpgrader upgrader = new SwordUpgrader();

    // ── Adventure state ───────────────────────────────────────────
    private List<World> worlds;
    private int    currentWorldIdx   = 0;
    private int    currentMobIdx     = 0;
    private boolean[] storylineDone  = new boolean[3];
    private boolean   storyCompleted = false;
    private Enemy   currentEnemy;
    private boolean inBattle         = false;

    // ── Audio ─────────────────────────────────────────────────────
    private Clip    bgMusic;
    private ExecutorService sfxPool = Executors.newCachedThreadPool();

    // ── Image cache ───────────────────────────────────────────────
    private Map<String, Image> imgs = new HashMap<>();

    // ── Swing top-level ───────────────────────────────────────────
    private JFrame      frame;
    private CardLayout  cards;
    private JPanel      root;
// Add this with your other variables
private long lastClickTime = 0;
private static final long CLICK_COOLDOWN = 200; // milliseconds
    // Shared HUD refs updated every tick
    private JLabel  hudName, hudGold;
    private JProgressBar hudHp, hudMana;

    // Battle panel refs
    private JLabel       bEnemyImg, bPlayerImg, bEnemyName, bBattleLog;
    private JProgressBar bEnemyHp, bPlayerHp;
    private JPanel       bActionPanel;

    // Particle layer (drawn over lobby background)
    private float[] px, py, palpha, pspeed;
    private static final int P_COUNT = 80;
    private Timer particleTick;

    // ── Palette ───────────────────────────────────────────────────
    private static final Color C_VOID   = new Color(8,  8, 18);
    private static final Color C_DEEP   = new Color(15, 12, 32);
    private static final Color C_PANEL  = new Color(18, 16, 38, 220);
    private static final Color C_BORDER = new Color(140, 90, 255, 90);
    private static final Color C_GOLD   = new Color(255, 200, 60);
    private static final Color C_SILVER = new Color(190, 200, 220);
    private static final Color C_HP     = new Color(210, 60, 60);
    private static final Color C_MP     = new Color(50, 120, 240);
    private static final Color C_ATK    = new Color(255, 160, 30);
    private static final Color C_WIN    = new Color(60, 200, 100);
    private static final Color C_PURPLE = new Color(140, 80, 255);

    // ── Fonts ─────────────────────────────────────────────────────
    private static final Font F_TITLE  = new Font("Monospaced", Font.BOLD,  26);
    private static final Font F_HEAD   = new Font("Monospaced", Font.BOLD,  17);
    private static final Font F_BODY   = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font F_BTN    = new Font("Monospaced", Font.BOLD,  14);
    private static final Font F_SMALL  = new Font("Monospaced", Font.PLAIN, 11);
    private static final Font F_HUGE   = new Font("Monospaced", Font.BOLD,  42);

    // ─────────────────────────────────────────────────────────────
    //   ENTRY POINT
    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new PremiumRPGGUI().launch());
    }

  private void launch() {
    game   = new Game();
    worlds = buildWorlds();
    loadImages();
    initParticles();
    buildFrame();
    showScreen("INTRO");
    
    // Test audio files
    System.out.println("=== TESTING AUDIO FILES ===");
    
    File auronFile = new File("C:/Users/Harry/APOTHECARY-_OOP2_GAME/DESTINY'S THREE/src/audio/Auron.wav");
    File introFile = new File("C:/Users/Harry/APOTHECARY-_OOP2_GAME/DESTINY'S THREE/src/audio/intro_music.wav");
    
    System.out.println("Auron.wav exists: " + auronFile.exists() + " | Size: " + auronFile.length() + " bytes");
    System.out.println("intro_music.wav exists: " + introFile.exists() + " | Size: " + introFile.length() + " bytes");
    
    // Play background music only (no click here)
    System.out.println("\n=== PLAYING INTRO_MUSIC.WAV ===");
    playBgMusic("intro_music.wav");
}

    private List<World> buildWorlds() {
    List<World> worlds = new ArrayList<>();
    worlds.add(new World("Forest of Beginnings", 3));
    worlds.add(new World("Caverns of Shadow", 4));
    worlds.add(new World("Citadel of Fate", 5));
    return worlds;
}

    // ─────────────────────────────────────────────────────────────
    //   IMAGE LOADING
    // ─────────────────────────────────────────────────────────────
    private void loadImages() {
    String[] keys = {
        "warrior","mage","rogue",
        "enemy_wolf","enemy_bat",
        "enemy_guard",
        "boss_guardian","boss_wraith",
        "boss_warden","logo",
        "battle_bg",
        "button_bg",
        "forest_bg",      // ← ADD THIS (World 1)
        "background",     // ← ADD THIS (World 2)
        "citadel_bg"      // ← ADD THIS (World 3)
    };

    for (String k : keys) {
        try {
            Image img = ImageIO.read(new File("images/" + k + ".png"));
            imgs.put(k, img);
        } catch (Exception e) {
            System.out.println("Missing image: " + k);
            imgs.put(k, makePlaceholder(k));
        }
    }
}

    private Image makePlaceholder(String name) {
        BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Radial gradient fill
        g.setColor(new Color(25, 18, 50));
        g.fillRect(0, 0, 256, 256);
        g.setColor(C_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(4, 4, 248, 248, 20, 20);
        g.setColor(C_GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 72));
        String lbl = name.substring(0,1).toUpperCase();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, (256 - fm.stringWidth(lbl))/2, 150);
        g.setColor(C_SILVER);
        g.setFont(F_SMALL);
        fm = g.getFontMetrics();
        String n = name.replace("_"," ").toUpperCase();
        g.drawString(n, (256 - fm.stringWidth(n))/2, 210);
        g.dispose();
        return bi;
    }

    // ─────────────────────────────────────────────────────────────
    //   PARTICLE SYSTEM
    // ─────────────────────────────────────────────────────────────
    private void initParticles() {
        px     = new float[P_COUNT];
        py     = new float[P_COUNT];
        palpha = new float[P_COUNT];
        pspeed = new float[P_COUNT];
        Random r = new Random();
        for (int i = 0; i < P_COUNT; i++) resetParticle(i, r, true);
    }

    private void resetParticle(int i, Random r, boolean randomY) {
        px[i]     = r.nextFloat() * 1400;
        py[i]     = randomY ? r.nextFloat() * 900 : 920;
        palpha[i] = r.nextFloat() * 0.5f + 0.1f;
        pspeed[i] = r.nextFloat() * 0.4f + 0.1f;
    }

    // ─────────────────────────────────────────────────────────────
    //   FRAME SETUP
    // ─────────────────────────────────────────────────────────────
    private void buildFrame() {
        frame = new JFrame("DESTINY'S THREE — APOTHECARY");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 800);
        frame.setLocationRelativeTo(null);
        frame.setBackground(C_VOID);

        cards = new CardLayout();
        root  = new JPanel(cards);
        root.setBackground(C_VOID);

        root.add(buildIntroScreen(),     "INTRO");
        root.add(buildCharSelectScreen(), "CHARSELECT");
        root.add(buildLobbyScreen(),      "LOBBY");
        root.add(buildBattleScreen(),     "BATTLE");
        root.add(buildGameOverScreen(),   "GAMEOVER");
        root.add(buildVictoryScreen(),    "VICTORY");

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private void showScreen(String name) {
        cards.show(root, name);
        root.revalidate();
        root.repaint();
    }

    // ─────────────────────────────────────────────────────────────
    //   BACKGROUNDS
    // ─────────────────────────────────────────────────────────────
    /** Dark starfield with floating rune-particles */
   private JPanel makeParticleBackground() {
    return makeParticleBackground(null);
}
private Image getWorldBackgroundImage() {
    switch (currentWorldIdx) {
        case 0: return imgs.get("forest_bg");     // Forest of Beginnings
        case 1: return imgs.get("background");    // Caverns of Shadow
        case 2: return imgs.get("citadel_bg");    // Citadel of Fate
        default: return imgs.get("background");
    }
}


private JPanel makeParticleBackground(String bgImageKey) {
    Random rng = new Random();
    JPanel bg = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g2d) {
            Graphics2D g = (Graphics2D) g2d;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background image if provided
            if (bgImageKey != null) {
                Image bgImg = imgs.get(bgImageKey);
                if (bgImg != null) {
                    g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), null);
                    // Dark overlay for readability
                    g.setColor(new Color(0, 0, 0, 150));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            } else {
                // Fallback gradient
                GradientPaint gp = new GradientPaint(0, 0, C_VOID, getWidth(), getHeight(), C_DEEP);
                g.setPaint(gp);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // Grid lines (subtle)
            g.setColor(new Color(80, 50, 160, 18));
            g.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x < getWidth(); x += 60)
                g.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 60)
                g.drawLine(0, y, getWidth(), y);
            
            // Particles
            for (int i = 0; i < P_COUNT; i++) {
                int alpha = Math.min(255, (int)(palpha[i] * 255));
                g.setColor(new Color(160, 100, 255, alpha));
                int sz = 2 + (int)(palpha[i] * 3);
                g.fillOval((int)px[i], (int)py[i], sz, sz);
            }
        }
    };
    
    // Animate particles
    particleTick = new Timer(33, e -> {
        for (int i = 0; i < P_COUNT; i++) {
            py[i] -= pspeed[i];
            if (py[i] < -10) resetParticle(i, rng, false);
        }
        bg.repaint();
    });
    particleTick.start();
    return bg;
}

    /** Glass panel for content overlays */
    private JPanel glassPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g = (Graphics2D) g2d;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(C_PANEL);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g.setColor(C_BORDER);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
            }
        };
        p.setOpaque(false);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //   INTRO SCREEN
    // ─────────────────────────────────────────────────────────────
   private JPanel buildIntroScreen() {
    JPanel bg = makeParticleBackground();
    bg.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0; c.gridy = GridBagConstraints.RELATIVE;
    c.insets = new Insets(8, 0, 8, 0);

    // Logo at the top
    Image logoImg = imgs.get("logo");
    if (logoImg != null) {
        Image scaledLogo = logoImg.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bg.add(logoLabel, c);
    }

    // Title art
    JLabel title = new JLabel("<html><center>" +
        "<span style='font-size:36px;color:#FFC83C;font-family:Monospaced;'>⚔&nbsp;APOTHECARY&nbsp;⚔</span><br>" +
        "<span style='font-size:22px;color:#B0A0FF;font-family:Monospaced;'>D E S T I N Y ' S &nbsp; T H R E E</span>" +
        "</center></html>", SwingConstants.CENTER);
    title.setForeground(C_GOLD);

    JLabel sub = label("― a tale of worlds unraveling ―", F_BODY, C_SILVER);
    sub.setHorizontalAlignment(SwingConstants.CENTER);

    String[] lines = {
        "In a land far beyond the stars,",
        "three worlds were bound by fate.",
        "Until the day the sky cracked.",
        "Shards of reality fell like glass.",
        "And three heroes were chosen...",
    };
    JTextArea story = new JTextArea(String.join("\n", lines));
    story.setFont(F_BODY);
    story.setForeground(C_SILVER);
    story.setOpaque(false);
    story.setEditable(false);
    story.setFocusable(false);
    story.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton startBtn = fancyButton("  BEGIN YOUR JOURNEY  ", C_PURPLE);
    startBtn.addActionListener(e -> { playSfx("Click.wav"); showScreen("CHARSELECT"); });

    JButton skipBtn = smallButton("Skip Story");
    skipBtn.addActionListener(e -> { playSfx("Click.wav"); showScreen("CHARSELECT"); });

    JPanel box = glassPanel();
    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
    box.setBorder(new EmptyBorder(40, 60, 40, 60));
    box.setPreferredSize(new Dimension(680, 480));
    for (Component comp : new Component[]{title, vgap(12), sub, vgap(20),
            story, vgap(28), center(startBtn), vgap(8), center(skipBtn)})
        box.add(comp);

    bg.add(box, c);
    return bg;
}

    // ─────────────────────────────────────────────────────────────
    //   CHARACTER SELECT
    // ─────────────────────────────────────────────────────────────
    private JPanel buildCharSelectScreen() {
        JPanel bg = makeParticleBackground();
        bg.setLayout(new BorderLayout());

        JLabel hdr = label("― Choose Your Destiny ―", F_TITLE, C_GOLD);
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        hdr.setBorder(new EmptyBorder(36, 0, 16, 0));
        bg.add(hdr, BorderLayout.NORTH);

        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 24, 0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(new EmptyBorder(10, 60, 20, 60));

        String[][] data = {
            {"warrior","WARRIOR","Auron Steelheart","HP 150 | MP 60 | ATK 20","Balanced strength and endurance.\nThe stalwart blade of the fortress."},
            {"mage",   "MAGE",   "Kaelen Stormweaver","HP 100 | MP 120 | ATK 15","High magic, fragile resolve.\nThe arcane storm given form."},
            {"rogue",  "ROGUE",  "Sire Instanzia","HP 120 | MP 80 | ATK 18","Agile, deadly, never seen coming.\nThe shadow that smiles."},
        };

        for (String[] d : data) {
            cardsRow.add(buildHeroCard(d[0], d[1], d[2], d[3], d[4]));
        }
        bg.add(cardsRow, BorderLayout.CENTER);

        JLabel tip = label("Click a hero card to begin", F_SMALL, new Color(100,90,150));
        tip.setHorizontalAlignment(SwingConstants.CENTER);
        tip.setBorder(new EmptyBorder(0, 0, 20, 0));
        bg.add(tip, BorderLayout.SOUTH);
        return bg;
    }

    private JPanel buildHeroCard(String imgKey, String cls, String name, String stats, String desc) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g = (Graphics2D) g2d;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(C_PANEL);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.setColor(C_BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hero portrait
        Image raw = imgs.get(imgKey);
        Image scaled = raw.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel portrait = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        portrait.setOpaque(false);

        // Labels
        JLabel clsLabel  = label(cls,   F_HEAD, C_GOLD);
        JLabel nameLabel = label(name,  F_BODY, C_SILVER);
        JLabel statsLabel= label(stats, F_SMALL,C_ATK);
        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(F_SMALL);
        descArea.setForeground(new Color(160,150,190));
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);

        clsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        for (Component c : new Component[]{clsLabel, nameLabel, vgap(4), statsLabel, vgap(8), descArea})
            info.add(c);

        card.add(portrait, BorderLayout.CENTER);
        card.add(info,     BorderLayout.SOUTH);

        // Hover & click
        card.addMouseListener(new MouseAdapter() {
            Color normalBorder = C_BORDER;
            public void mouseEntered(MouseEvent e) { card.putClientProperty("hover", true); card.repaint(); }
            public void mouseExited (MouseEvent e) { card.putClientProperty("hover", false); card.repaint(); }
            public void mouseClicked(MouseEvent e) {
                playSfx("Click.wav");
                selectHero(imgKey);
            }
        });

        return card;
    }

   private void selectHero(String imgKey) {
    switch (imgKey) {
        case "warrior" -> player = new Warrior("Auron Steelheart");
        case "mage"    -> player = new Mage("Kaelen Stormweaver");
        default        -> player = new Rogue("Sire Instanzia");
    }
    currentWorldIdx = 0;
    currentMobIdx   = 0;
    storyCompleted  = false;
    Arrays.fill(storylineDone, false);
    refreshLobbyUI();
    
    // Remove the duplicate click - keep only ONE
    // playSfx("Click.wav");  // ← REMOVE THIS LINE (already called from card click)
    playBgMusic("intro_music.wav");  // Play background music
    
    showScreen("LOBBY");
    showNarrativePopup("Prologue", "Three worlds once stood in harmony until the sky cracked open. The Forest of Beginnings burns, the Caverns of Shadow tremble, and the Citadel of Fate crumbles as rifts consume all. The Master has been taken by a dark Villain who watches from beyond. You are the last hope. Choose your hero, restore the balance, and save the worlds before everything is lost forever. — The Apothecary Prophecy");
}

    // ─────────────────────────────────────────────────────────────
    //   LOBBY SCREEN
    // ─────────────────────────────────────────────────────────────
    private JLabel   lobbyCharImg;
    private JLabel   lobbyCharName;
    private JLabel   lobbyWorldLabel;

 private JPanel buildLobbyScreen() {
    // Get the correct background for current world
    String bgKey = null;
    if (currentWorldIdx == 0) bgKey = "forest_bg";
    else if (currentWorldIdx == 1) bgKey = "background";
    else if (currentWorldIdx == 2) bgKey = "citadel_bg";
    
    JPanel bg = makeParticleBackground(bgKey);
    bg.setLayout(new BorderLayout());
        // ── TOP HUD ─────────────────────────────────────────────
        JPanel hud = buildHUD();
        bg.add(hud, BorderLayout.NORTH);

        // ── LEFT: character portrait ─────────────────────────────
        JPanel leftPane = glassPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setBorder(new EmptyBorder(24, 24, 24, 24));
        leftPane.setPreferredSize(new Dimension(280, 0));

        lobbyCharImg  = new JLabel("", SwingConstants.CENTER);
        lobbyCharName = new JLabel("", SwingConstants.CENTER);
        lobbyCharName.setFont(F_HEAD);
        lobbyCharName.setForeground(C_GOLD);
        lobbyCharName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lobbyWorldLabel = new JLabel("", SwingConstants.CENTER);
        lobbyWorldLabel.setFont(F_SMALL);
        lobbyWorldLabel.setForeground(C_SILVER);
        lobbyWorldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPane.add(lobbyCharImg);
        leftPane.add(vgap(10));
        leftPane.add(lobbyCharName);
        leftPane.add(vgap(4));
        leftPane.add(lobbyWorldLabel);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setOpaque(false);
        leftWrap.setBorder(new EmptyBorder(20, 20, 20, 10));
        leftWrap.add(leftPane, BorderLayout.NORTH);
        bg.add(leftWrap, BorderLayout.WEST);

        // ── CENTER: main menu ────────────────────────────────────
        JPanel centerPane = new JPanel(new GridBagLayout());
        centerPane.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 0, 10, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        JLabel menuTitle = label("L O B B Y", F_TITLE, C_PURPLE);
        menuTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 0; gc.insets = new Insets(10, 40, 24, 40);
        centerPane.add(menuTitle, gc);

        String[][] btns = {
            {"  ⚔  ADVENTURE MODE  ","adventure"},
            {"  🏪  BRYCE'S STORE  ","store"},
            {"  ⬆  CARL'S SWORD UPGRADER  ","upgrade"},
            {"  🎒  INVENTORY  ","inventory"},
            {"  ✕  EXIT GAME  ","exit"},
        };
        Color[] btnColors = {C_PURPLE, new Color(150,100,40), new Color(60,130,200),
                             new Color(60,130,80), new Color(160,40,40)};
        gc.insets = new Insets(6, 60, 6, 60);
        for (int i = 0; i < btns.length; i++) {
            JButton b = fancyButton(btns[i][0], btnColors[i]);
            final String action = btns[i][1];
            b.addActionListener(e -> handleLobbyAction(action));
            gc.gridy = i + 1;
            centerPane.add(b, gc);
        }
        bg.add(centerPane, BorderLayout.CENTER);
        return bg;
    }

   private void refreshLobbyUI() {
    if (player == null) return;
    // ... your existing refreshLobbyUI code ...
}

private void handleLobbyAction(String action) {
    // NO click sound for adventure mode - prevents the bug
    if (!action.equals("adventure")) {
        playSfx("Click.wav");
    }
    
    switch (action) {
        case "adventure" -> startAdventure();
        case "store"     -> openStoreUI();
        case "upgrade"   -> openUpgraderUI();
        case "inventory" -> openInventoryUI();
        case "exit"      -> confirmExit();
    }
}

    // ─────────────────────────────────────────────────────────────
    //   HUD (shared top bar)
    // ─────────────────────────────────────────────────────────────
    private JPanel buildHUD() {
        JPanel hud = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(10, 8, 25, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(100, 60, 200, 80));
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        hud.setOpaque(false);

        hudName = label("", F_BODY, C_GOLD);
        hudHp   = bar(C_HP,  120);
        hudMana = bar(C_MP,  100);
        hudGold = label("", F_BODY, C_ATK);

        hud.add(label("Player:", F_SMALL, C_SILVER));
        hud.add(hudName);
        hud.add(label("HP", F_SMALL, C_HP));
        hud.add(hudHp);
        hud.add(label("MP", F_SMALL, C_MP));
        hud.add(hudMana);
        hud.add(label("Gold:", F_SMALL, C_ATK));
        hud.add(hudGold);
        return hud;
    }

    private void refreshHUD() {
        if (player == null) return;
        hudName.setText(player.getName());
        hudHp.setMaximum(player.getMaxHp());
        hudHp.setValue(player.getHp());
        hudHp.setString(player.getHp() + "/" + player.getMaxHp());
        hudMana.setMaximum(player.getMaxMana());
        hudMana.setValue(player.getMana());
        hudMana.setString(player.getMana() + "/" + player.getMaxMana());
        hudGold.setText(player.getGold() + " g");
    }

    // ─────────────────────────────────────────────────────────────
    //   ADVENTURE LOGIC
    // ─────────────────────────────────────────────────────────────
    private void startAdventure() {
        if (!player.isAlive()) {
            showNarrativePopup("Cannot Enter", "Your HP is 0. Heal first at the Store.");
            return;
        }
        if (storyCompleted) {
            showStoryFinishedDialog();
            return;
        }
        if (currentWorldIdx >= worlds.size()) {
            storyCompleted = true;
            showNexusDialog();
            return;
        }
        World w = worlds.get(currentWorldIdx);
        List<Mob> mobs = w.getMobs();

        if (currentMobIdx < mobs.size()) {
            // Regular mob
            currentEnemy = mobs.get(currentMobIdx);
            currentEnemy.setHp(currentEnemy.getMaxHp());
            enterBattle(false);
        } else {
            // Boss
            Boss boss = w.getBoss();
            if (boss.getHp() > 0) {
                boss.setHp(boss.getMaxHp());
                currentEnemy = boss;
                showBossIntroDialog(boss.getName(), () -> enterBattle(true));
            } else {
                advanceWorld();
            }
        }
    }

    private void enterBattle(boolean isBoss) {
    World w = worlds.get(currentWorldIdx);
    int totalEnemies = w.getMobs().size() + 1;
    int enemyNum  = isBoss ? totalEnemies : currentMobIdx + 1;
    updateBattleUI(isBoss, enemyNum, totalEnemies, w.getName());
    
    // Play intro_music.wav for ALL battles (regular and boss)
    playBgMusic("intro_music.wav");
    
    inBattle = true;
    showScreen("BATTLE");
}

    private void updateBattleUI(boolean isBoss, int num, int total, String worldName) {
        // Enemy image
        String eKey = getEnemyKey(currentEnemy.getName());
        Image ei = imgs.get(eKey).getScaledInstance(230, 230, Image.SCALE_SMOOTH);
        bEnemyImg.setIcon(new ImageIcon(ei));
        bEnemyName.setText((isBoss ? "★ BOSS: " : "") + currentEnemy.getName());
        bEnemyName.setForeground(isBoss ? C_ATK : C_HP);
        bEnemyHp.setMaximum(currentEnemy.getMaxHp());
        bEnemyHp.setValue(currentEnemy.getHp());
        bEnemyHp.setString(currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());

        // Player image
        String pKey = (player instanceof Warrior) ? "warrior" : (player instanceof Mage) ? "mage" : "rogue";
        Image pi = imgs.get(pKey).getScaledInstance(230, 230, Image.SCALE_SMOOTH);
        bPlayerImg.setIcon(new ImageIcon(pi));
        bPlayerHp.setMaximum(player.getMaxHp());
        bPlayerHp.setValue(player.getHp());
        bPlayerHp.setString(player.getHp() + "/" + player.getMaxHp());

        setBattleLog("Enemy " + num + " / " + total + " in " + worldName +
            "\nYour move! Choose wisely.");
    }

    // ─────────────────────────────────────────────────────────────
    //   BATTLE SCREEN
    // ─────────────────────────────────────────────────────────────
    private JPanel buildBattleScreen() {
    // Use battle_bg.png as background
    Image battleBgImg = imgs.get("battle_bg");
    JPanel bg;
    
    if (battleBgImg != null) {
        // Custom panel with battle background
        bg = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g2d) {
                super.paintComponent(g2d);
                Graphics2D g = (Graphics2D) g2d;
                // Draw battle background
                g.drawImage(battleBgImg, 0, 0, getWidth(), getHeight(), null);
                // Dark overlay for readability
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    } else {
        bg = makeParticleBackground();
    }
    
    bg.setLayout(new BorderLayout(0, 0));
    bg.add(buildHUD(), BorderLayout.NORTH);

    // ── VS PANEL ─────────────────────────────────────────────
    JPanel vsRow = new JPanel(new GridLayout(1, 3, 0, 0));
    vsRow.setOpaque(false);
    vsRow.setBorder(new EmptyBorder(20, 40, 10, 40));

    // Enemy side
    JPanel enemyPane = glassPanel();
    enemyPane.setLayout(new BoxLayout(enemyPane, BoxLayout.Y_AXIS));
    enemyPane.setBorder(new EmptyBorder(16, 20, 16, 20));
    bEnemyName = label("Enemy", F_HEAD, C_HP);
    bEnemyName.setAlignmentX(Component.CENTER_ALIGNMENT);
    bEnemyImg  = new JLabel("", SwingConstants.CENTER);
    bEnemyImg.setAlignmentX(Component.CENTER_ALIGNMENT);
    bEnemyHp   = bar(C_HP, 300);
    bEnemyHp.setAlignmentX(Component.CENTER_ALIGNMENT);
    enemyPane.add(bEnemyName);
    enemyPane.add(vgap(8));
    enemyPane.add(bEnemyImg);
    enemyPane.add(vgap(10));
    enemyPane.add(label("HP", F_SMALL, C_HP));
    enemyPane.add(bEnemyHp);

    // VS label
    JLabel vsLbl = label("VS", F_HUGE, C_HP);
    vsLbl.setHorizontalAlignment(SwingConstants.CENTER);

    // Player side
    JPanel playerPane = glassPanel();
    playerPane.setLayout(new BoxLayout(playerPane, BoxLayout.Y_AXIS));
    playerPane.setBorder(new EmptyBorder(16, 20, 16, 20));
    JLabel pName = label(player != null ? player.getName() : "", F_HEAD, C_WIN);
    pName.setAlignmentX(Component.CENTER_ALIGNMENT);
    bPlayerImg  = new JLabel("", SwingConstants.CENTER);
    bPlayerImg.setAlignmentX(Component.CENTER_ALIGNMENT);
    bPlayerHp   = bar(C_WIN, 300);
    bPlayerHp.setAlignmentX(Component.CENTER_ALIGNMENT);
    playerPane.add(pName);
    playerPane.add(vgap(8));
    playerPane.add(bPlayerImg);
    playerPane.add(vgap(10));
    playerPane.add(label("HP", F_SMALL, C_WIN));
    playerPane.add(bPlayerHp);

    vsRow.add(enemyPane);
    vsRow.add(vsLbl);
    vsRow.add(playerPane);
    bg.add(vsRow, BorderLayout.CENTER);

    // ── BATTLE LOG + ACTIONS ──────────────────────────────────
    JPanel bottomPane = new JPanel(new BorderLayout(10, 0));
    bottomPane.setOpaque(false);
    bottomPane.setBorder(new EmptyBorder(0, 40, 24, 40));

    bBattleLog = label("", F_BODY, C_SILVER);
    bBattleLog.setPreferredSize(new Dimension(400, 80));
    bBattleLog.setVerticalAlignment(SwingConstants.TOP);
    JPanel logPanel = glassPanel();
    logPanel.setLayout(new BorderLayout());
    logPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
    logPanel.setPreferredSize(new Dimension(400, 90));
    logPanel.add(bBattleLog, BorderLayout.CENTER);

    bActionPanel = new JPanel(new GridLayout(2, 2, 12, 12));
    bActionPanel.setOpaque(false);
    bActionPanel.setPreferredSize(new Dimension(480, 100));

    String[] actionLabels = {"  ⚔  ATTACK  ","  ✨  SKILLS  ","  🎒  ITEMS  ","  🏃  RETREAT  "};
    Color[]  actionColors = {new Color(180,50,50), new Color(80,50,200), new Color(60,120,60), new Color(140,90,30)};

    for (int i = 0; i < 4; i++) {
        final int idx = i;
        JButton b = fancyButton(actionLabels[i], actionColors[i]);
        b.addActionListener(e -> handleBattleAction(idx));
        bActionPanel.add(b);
    }

    bottomPane.add(logPanel,     BorderLayout.WEST);
    bottomPane.add(bActionPanel, BorderLayout.EAST);
    bg.add(bottomPane, BorderLayout.SOUTH);
    return bg;
}

    private void handleBattleAction(int action) {
    if (!inBattle || currentEnemy == null || player == null) return;
    
    boolean playerActed = false;

    switch (action) {
        case 0 -> { // Attack
            int dmg = player.getAttack() + (int)(Math.random() * 8) + 1;
            currentEnemy.takeDamage(dmg);
            setBattleLog("You attack " + currentEnemy.getName() + " for " + dmg + " damage!");
            playSfx("Win.wav");
            playerActed = true;
        }
        case 1 -> { // Skills
            showSkillDialog();
            return;
        }
        case 2 -> { // Items
            openInventoryUI();
            refreshBattleHPBars();
            return;
        }
        case 3 -> { // Retreat
            inBattle = false;
            setBattleLog("You retreat to the lobby...");
            playBgMusic("Auron.wav");
            refreshLobbyUI();
            showScreen("LOBBY");
            return;
        }
    }

    if (playerActed) {
        refreshBattleHPBars();
        if (!currentEnemy.isAlive()) {
            handleEnemyDefeated();
            return;
        }
        int eDmg = currentEnemy.getAttack() + (int)(Math.random() * 6);
        player.takeDamage(eDmg);
        setBattleLog(getBattleLog() + "\n" + currentEnemy.getName() + " strikes back for " + eDmg + "!");
        refreshBattleHPBars();
        refreshHUD();

        if (!player.isAlive()) {
            inBattle = false;
            playSfx("Ending.wav");
            showScreen("GAMEOVER");
        }
    }
}
    private String lastLog = "";
    private void setBattleLog(String txt) { lastLog = txt; if (bBattleLog != null) bBattleLog.setText("<html>" + txt.replace("\n","<br>") + "</html>"); }
    private String getBattleLog() { return lastLog; }

    private void refreshBattleHPBars() {
        if (bEnemyHp != null && currentEnemy != null) {
            bEnemyHp.setValue(Math.max(0, currentEnemy.getHp()));
            bEnemyHp.setString(currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());
        }
        if (bPlayerHp != null && player != null) {
            bPlayerHp.setValue(Math.max(0, player.getHp()));
            bPlayerHp.setString(player.getHp() + "/" + player.getMaxHp());
        }
        refreshHUD();
    }

   
private void handleEnemyDefeated() {
    playSfx("Win.wav");
    int gold = 20 + (int)(Math.random() * 20);
    player.addGold(gold);
    boolean isBoss = currentEnemy instanceof Boss;

    // Loot
    String loot = "";
    if (!isBoss && Math.random() < 0.5) {
        if (Math.random() < 0.5) {
            player.getInventory().addItem(new Potion("Health Potion","Restores 50 HP",50,1));
            loot = "\n📦 Found: Health Potion!";
        } else {
            player.getInventory().addItem(new Weapon("Rusty Blade","A dented but usable sword",5));
            loot = "\n📦 Found: Rusty Blade!";
        }
    }
    if (isBoss) {
        player.getInventory().addItem(new Weapon("Boss Relic","Imbued with the essence of the fallen boss",10));
        player.setMana(player.getMaxMana());
        loot = "\n★ Boss Relic obtained! +100 gold!";
        player.addGold(100);
    }
    refreshHUD();

    inBattle = false;
    if (isBoss) {
        // Advance world
        currentMobIdx = 0;
        currentWorldIdx++;
        playBgMusic("intro_music.wav");
        showNarrativePopup("World Cleared!",
            "You have defeated " + currentEnemy.getName() + "!\n" +
            "+100 Gold" + loot + "\n\n" +
            (currentWorldIdx < worlds.size() ?
                "Onwards to: " + worlds.get(currentWorldIdx).getName() :
                "The Riven Nexus awaits...") +
            "\n\nReturn to lobby to continue.");
        refreshLobbyUI();
        refreshLobbyBackground();  // ✅ ADDED - Updates background for next world
        showScreen("LOBBY");
    } else {
        currentMobIdx++;
        player.setMana(player.getMaxMana());
        showVictoryPause(gold + loot);
    }
}
    private void refreshLobbyBackground() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'refreshLobbyBackground'");
}

    private void showVictoryPause(String msg) {
        JDialog d = new JDialog(frame, "Victory!", false);
        d.setSize(400, 220);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setBackground(new Color(10,30,20));
        p.setBorder(BorderFactory.createLineBorder(C_WIN, 2));
        JLabel t = label("⚔ ENEMY DEFEATED! ⚔", F_HEAD, C_WIN);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        t.setBorder(new EmptyBorder(14,0,0,0));
        JLabel info = label("<html><center>+" + msg.replace("\n","<br>") + "</center></html>", F_BODY, C_SILVER);
        info.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel btns = new JPanel(new GridLayout(1,2,10,0));
        btns.setOpaque(false);
        btns.setBorder(new EmptyBorder(0,20,14,20));
        JButton cont = fancyButton("Continue", C_WIN);
        JButton lobby= fancyButton("Lobby",    new Color(80,60,100));
        cont.addActionListener(e  -> { d.dispose(); startAdventure(); });
        lobby.addActionListener(e -> { d.dispose(); refreshLobbyUI(); showScreen("LOBBY"); playBgMusic("lobby_music.wav"); });
        btns.add(cont); btns.add(lobby);
        p.add(t,    BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    //   GAME OVER
    // ─────────────────────────────────────────────────────────────
    private JPanel buildGameOverScreen() {
        JPanel bg = makeParticleBackground();
        bg.setLayout(new GridBagLayout());
        JPanel box = glassPanel();
        box.setPreferredSize(new Dimension(500, 320));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = label("★  GAME  OVER  ★", F_HUGE, C_HP);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub   = label("You have fallen in battle.", F_BODY, C_SILVER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton restart = fancyButton("  ↺  RESTART FROM BEGINNING  ", C_PURPLE);
        restart.setAlignmentX(Component.CENTER_ALIGNMENT);
        restart.addActionListener(e -> {
            playSfx("Click.wav");
            player.reset();
            currentWorldIdx = 0; currentMobIdx = 0; storyCompleted = false;
            Arrays.fill(storylineDone, false);
            worlds = buildWorlds();
            refreshLobbyUI();
            playBgMusic("lobby_music.wav");
            showScreen("LOBBY");
        });

        JButton credits = fancyButton("  Credits  ", new Color(80,60,100));
        credits.setAlignmentX(Component.CENTER_ALIGNMENT);
        credits.addActionListener(e -> showCreditsDialog());

        box.add(title); box.add(vgap(12)); box.add(sub); box.add(vgap(30));
        box.add(restart); box.add(vgap(10)); box.add(credits);
        bg.add(box);
        return bg;
    }

    // ─────────────────────────────────────────────────────────────
    //   VICTORY SCREEN (story end)
    // ─────────────────────────────────────────────────────────────
    private JPanel buildVictoryScreen() {
        JPanel bg = makeParticleBackground();
        bg.setLayout(new GridBagLayout());
        JPanel box = glassPanel();
        box.setPreferredSize(new Dimension(620, 380));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = label("★  T H E   S T O R Y   E N D S . . .  ★", F_HEAD, C_GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextArea story = new JTextArea(
            "The Nexus trembles as the truth is revealed.\n" +
            "The Master and the Villain vanish into the void.\n\n" +
            "\"The hunt for the Master begins.\"\n\n" +
            "— to be continued —");
        story.setFont(F_BODY); story.setForeground(C_SILVER);
        story.setOpaque(false); story.setEditable(false);
        story.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton again   = fancyButton("  ↺  Play Again  ", C_PURPLE);
        again.setAlignmentX(Component.CENTER_ALIGNMENT);
        again.addActionListener(e -> {
            playSfx("Click.wav");
            player.reset(); currentWorldIdx = 0; currentMobIdx = 0;
            storyCompleted = false; Arrays.fill(storylineDone, false);
            worlds = buildWorlds();
            refreshLobbyUI(); playBgMusic("lobby_music.wav"); showScreen("LOBBY");
        });
        JButton credits = fancyButton("  Credits  ", new Color(80,60,100));
        credits.setAlignmentX(Component.CENTER_ALIGNMENT);
        credits.addActionListener(e -> showCreditsDialog());

        box.add(title); box.add(vgap(16)); box.add(story); box.add(vgap(28));
        box.add(again); box.add(vgap(10)); box.add(credits);
        bg.add(box);
        return bg;
    }

    // ─────────────────────────────────────────────────────────────
    //   DIALOGS
    // ─────────────────────────────────────────────────────────────
    private void showSkillDialog() {
        if (player == null || currentEnemy == null) return;
        List<Skill> skills = player.getSkills();
        if (skills.isEmpty()) { setBattleLog("You have no skills!"); return; }

        JDialog d = new JDialog(frame, "Skills", true);
        d.setSize(420, 320);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(C_DEEP);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER, 2));
        JLabel hdr = label("  Choose a Skill  (MP: " + player.getMana() + "/" + player.getMaxMana() + ")", F_HEAD, C_PURPLE);
        hdr.setBorder(new EmptyBorder(14,14,0,14));
        p.add(hdr, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(skills.size(), 1, 0, 8));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(10,20,10,20));

        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            boolean canUse = s.canUse(player);
            JButton b = fancyButton(
                String.format("%-20s  MP:%-4d  DMG:%-4d", s.getName(), s.getManaCost(), s.getDamage()),
                canUse ? new Color(80,50,160) : new Color(60,50,60));
            if (!canUse) b.setEnabled(false);
            final int idx = i;
            b.addActionListener(e -> {
                player.useSkill(idx, currentEnemy);
                d.dispose();
                playSfx("Win.wav");
                refreshBattleHPBars();
                setBattleLog("You cast " + s.getName() + " for " + s.getDamage() + " damage!");
                if (!currentEnemy.isAlive()) { handleEnemyDefeated(); return; }
                int eDmg = currentEnemy.getAttack() + (int)(Math.random() * 6);
                player.takeDamage(eDmg);
                setBattleLog(getBattleLog() + "\n" + currentEnemy.getName() + " hits back for " + eDmg + "!");
                refreshBattleHPBars();
                if (!player.isAlive()) { inBattle = false; showScreen("GAMEOVER"); }
            });
            list.add(b);
        }

        JButton back = smallButton("Back");
        back.addActionListener(e -> d.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false); south.setBorder(new EmptyBorder(0,10,10,10));
        south.add(back);

        p.add(list, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.setVisible(true);
    }

    private void openStoreUI() {
        JDialog d = new JDialog(frame, "Store", true);
        d.setSize(480, 360);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(C_DEEP);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER,2));

        JLabel hdr = label("  🏪  CANTILLAS'S STORE   (Gold: " + player.getGold() + "g)", F_HEAD, C_GOLD);
        hdr.setBorder(new EmptyBorder(14,14,4,14));
        p.add(hdr, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(3,1,0,10));
        items.setOpaque(false);
        items.setBorder(new EmptyBorder(10,24,10,24));

        Object[][] stock = {
            {"❤  Health Potion", "Restores 50 HP",  15, 1},
            {"💙  Mana Potion",   "Restores 30 MP",  15, 2},
            {"⚔  Iron Sword",    "+5 Attack",        50, 3},
        };
        for (Object[] item : stock) {
            JPanel row = new JPanel(new BorderLayout(10,0));
            row.setBackground(new Color(20,18,40));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER,1),
                new EmptyBorder(8,14,8,14)));
            row.add(label((String)item[0], F_BTN, C_SILVER), BorderLayout.WEST);
            row.add(label((String)item[1], F_SMALL, new Color(120,110,160)), BorderLayout.CENTER);
            JLabel price = label(item[2] + "g", F_BTN, C_GOLD);
            JButton buy = smallButton("Buy");
            buy.addActionListener(e -> {
                int cost = (int)item[2];
                if (player.getGold() < cost) {
                    hdr.setText("  Not enough gold! Need " + cost + "g");
                } else {
                    player.addGold(-cost);
                    int type = (int)item[3];
                    if (type == 1) player.getInventory().addItem(new Potion("Health Potion","Restores 50 HP",50,1));
                    else if (type == 2) player.getInventory().addItem(new Potion("Mana Potion","Restores 30 MP",30,2));
                    else { player.getInventory().addItem(new Weapon("Iron Sword","A sturdy blade",5)); }
                    hdr.setText("  Purchased! Gold: " + player.getGold() + "g");
                    refreshHUD();
                    playSfx("Click.wav");
                }
            });
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
            right.setOpaque(false);
            right.add(price); right.add(buy);
            row.add(right, BorderLayout.EAST);
            items.add(row);
        }
        p.add(items, BorderLayout.CENTER);

        JButton close = fancyButton("Close", new Color(80,50,50));
        close.addActionListener(e -> { d.dispose(); refreshLobbyUI(); });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false); south.setBorder(new EmptyBorder(0,0,14,0));
        south.add(close);
        p.add(south, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private void openUpgraderUI() {
        int cost = 50;
        JDialog d = new JDialog(frame, "Upgrader", true);
        d.setSize(400, 240);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setBackground(C_DEEP);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER,2));
        JLabel hdr = label("  ⬆  CANEDO'S SWORD UPGRADER", F_HEAD, C_ATK);
        hdr.setBorder(new EmptyBorder(14,14,4,14));
        p.add(hdr, BorderLayout.NORTH);
        JLabel info = label("Current ATK: " + player.getAttack() + "   |   Gold: " + player.getGold() + "g\nUpgrade +5 ATK for " + cost + " gold.", F_BODY, C_SILVER);
        info.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(info, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout());
        btns.setOpaque(false);
        JButton upgrade = fancyButton("  Upgrade (+5 ATK, -50g)  ", new Color(60,120,200));
        upgrade.addActionListener(e -> {
            if (player.getGold() < cost) {
                hdr.setText("  Not enough gold!");
            } else {
                player.addGold(-cost);
                player.increaseAttack(5);
                hdr.setText("  ATK is now " + player.getAttack() + "! Gold: " + player.getGold() + "g");
                refreshHUD();
                playSfx("Click.wav");
            }
        });
        JButton close = fancyButton("Close", new Color(80,50,50));
        close.addActionListener(e -> { d.dispose(); refreshLobbyUI(); });
        btns.add(upgrade); btns.add(close);
        p.add(btns, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.setVisible(true);
    }

    private void openInventoryUI() {
        JDialog d = new JDialog(frame, "Inventory", true);
        d.setSize(460, 360);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBackground(C_DEEP);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER,2));

        JLabel hdr = label("  🎒  INVENTORY", F_HEAD, C_WIN);
        hdr.setBorder(new EmptyBorder(14,14,4,14));
        p.add(hdr, BorderLayout.NORTH);

        try {
            java.lang.reflect.Field f = Inventory.class.getDeclaredField("items");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Item> invItems = (List<Item>) f.get(player.getInventory());

            if (invItems.isEmpty()) {
                JLabel empty = label("Your inventory is empty.", F_BODY, C_SILVER);
                empty.setHorizontalAlignment(SwingConstants.CENTER);
                p.add(empty, BorderLayout.CENTER);
            } else {
                JPanel list = new JPanel(new GridLayout(Math.min(invItems.size(), 8), 1, 0, 6));
                list.setOpaque(false);
                list.setBorder(new EmptyBorder(8,20,8,20));
                for (int i = 0; i < invItems.size(); i++) {
                    Item item = invItems.get(i);
                    JPanel row = new JPanel(new BorderLayout(8,0));
                    row.setBackground(new Color(18,16,38));
                    row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(C_BORDER,1),
                        new EmptyBorder(6,12,6,12)));
                    row.add(label(item.getName(), F_BTN, C_SILVER), BorderLayout.WEST);
                    row.add(label(item.getDescription(), F_SMALL, new Color(110,100,150)), BorderLayout.CENTER);
                    JButton use = smallButton("Use");
                    final int idx = i;
                    use.addActionListener(e -> {
                        if (item instanceof Potion) {
                            Potion pot = (Potion) item;
                            boolean used = pot.apply(player);
                            if (used) invItems.remove(idx);
                        } else {
                            item.use(player);
                            invItems.remove(idx);
                        }
                        refreshHUD();
                        refreshBattleHPBars();
                        d.dispose();
                    });
                    row.add(use, BorderLayout.EAST);
                    list.add(row);
                }
                JScrollPane sp = new JScrollPane(list);
                sp.setOpaque(false); sp.getViewport().setOpaque(false);
                sp.setBorder(null);
                p.add(sp, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            p.add(label("Error loading inventory.", F_BODY, C_HP), BorderLayout.CENTER);
        }

        JButton close = fancyButton("Close", new Color(80,50,50));
        close.addActionListener(e -> d.dispose());
        JPanel south = new JPanel(new FlowLayout());
        south.setOpaque(false); south.setBorder(new EmptyBorder(0,0,12,0));
        south.add(close);
        p.add(south, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.setVisible(true);
    }

    private void showNarrativePopup(String title, String text) {
        JDialog d = new JDialog(frame, title, true);
        d.setSize(560, 320);
        d.setLocationRelativeTo(frame);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setBackground(C_DEEP);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER,2));

        JLabel hdr = label("  " + title, F_HEAD, C_GOLD);
        hdr.setBorder(new EmptyBorder(14,14,4,14));
        p.add(hdr, BorderLayout.NORTH);

        JTextArea ta = new JTextArea(text);
        ta.setFont(F_BODY); ta.setForeground(C_SILVER);
        ta.setOpaque(false); ta.setEditable(false);
        ta.setWrapStyleWord(true); ta.setLineWrap(true);
        ta.setBorder(new EmptyBorder(8,20,8,20));
        p.add(ta, BorderLayout.CENTER);

        JButton ok = fancyButton("Continue →", C_PURPLE);
        ok.addActionListener(e -> { playSfx("Click.wav"); d.dispose(); });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false); south.setBorder(new EmptyBorder(0,0,14,0));
        south.add(ok);
        p.add(south, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private void showBossIntroDialog(String bossName, Runnable onProceed) {
        showNarrativePopup("⚠ BOSS ENCOUNTER",
            "The ground trembles...\n\n" +
            "★ " + bossName.toUpperCase() + " ★\n\n" +
            "\"A powerful aura radiates from the darkness.\"\n\n" +
            "Prepare yourself, " + player.getName() + ".");
        onProceed.run();
    }

   private void advanceWorld() {
    currentMobIdx   = 0;
    currentWorldIdx++;
    if (currentWorldIdx >= worlds.size()) {
        storyCompleted = true;
        showNexusDialog();
    } else {
        showNarrativePopup("World Cleared!",
            "You advance to: " + worlds.get(currentWorldIdx).getName());
        refreshLobbyUI();
        refreshLobbyBackground();  // ✅ ADD THIS LINE
    }
}

    private void showNexusDialog() {
        showNarrativePopup("THE RIVEN NEXUS",
            "The gateway to the Riven Nexus opens before you...\n\n" +
            "The Master and the Villain reveal themselves.\n" +
            "Truth — shattered. Trust — undone.\n\n" +
            "\"The hunt for the Master begins.\"\n\n" +
            "— Narrator");
        showScreen("VICTORY");
    }

    private void showStoryFinishedDialog() {
        showNarrativePopup("The Story Is Over",
            "You have completed Destiny's Three!\n\n" +
            "Would you like to play again from the lobby?");
        refreshLobbyUI();
    }

    private void confirmExit() {
        int r = JOptionPane.showConfirmDialog(frame,
            "Exit Destiny's Three?", "Exit", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }

    private void showCreditsDialog() {
        showNarrativePopup("Credits — Destiny's Three",
            "Game Design & Dev   : Rafanan James Carl P.\n" +
            "Programming         : Rafanan James Carl P., Cantillas Bryce J., Canedo James D.\n" +
            "Art & ASCII         : Rafanan James Carl P.\n" +
            "Music & SFX         : Rafanan James Carl P.\n" +
            "Adventure & Combat  : Rafanan James Carl P., Canedo James D.\n" +
            "Store & Inventory   : Cantillas Bryce Josef R.\n\n" +
            "Thank you for playing Destiny's Three!\n" +
            "Built with Java OOP — for learning and adventure.");
        System.exit(0);
    }

    // ─────────────────────────────────────────────────────────────
    //   AUDIO
    // ─────────────────────────────────────────────────────────────
    private File findAudio(String name) {
    System.out.println("🔍 Searching for: " + name);
    
    // Absolute path - this WILL work
    String basePath = "C:/Users/Harry/APOTHECARY-_OOP2_GAME/DESTINY'S THREE/src/audio/";
    File f = new File(basePath + name);
    System.out.println("   Checking: " + f.getAbsolutePath());
    System.out.println("   Exists: " + f.exists());
    
    if (f.exists()) {
        System.out.println("   ✅ FOUND!");
        return f;
    }
    
    System.out.println("   ❌ NOT FOUND!");
    return null;
}
    

   private void playSfx(String name) {
    // Cooldown to prevent rapid clicking
    if (name.equals("Click.wav")) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 300) {
            return;  // Ignore clicks that are too fast
        }
        lastClickTime = now;
    }
    
    sfxPool.submit(() -> {
        try {
            File f = findAudio(name);
            if (f == null) return;
            AudioInputStream a = AudioSystem.getAudioInputStream(f);
            Clip c = AudioSystem.getClip();
            c.open(a);
            c.start();
        } catch (Exception e) {
            System.out.println("SFX ERROR: " + e.getMessage());
        }
    });
}
private void playBgMusic(String name) {
    System.out.println("🎵 PLAYING MUSIC: " + name);
    sfxPool.submit(() -> {
        try {
            if (bgMusic != null) {
                bgMusic.stop();
                bgMusic.close();
            }
            File f = findAudio(name);
            if (f == null) {
                System.out.println("❌ Music file not found: " + name);
                return;
            }
            System.out.println("✅ Music file found: " + f.getAbsolutePath());
            AudioInputStream a = AudioSystem.getAudioInputStream(f);
            bgMusic = AudioSystem.getClip();
            bgMusic.open(a);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            bgMusic.start();
            System.out.println("✅ Music started playing!");
        } catch (Exception e) {
            System.out.println("❌ Music error: " + e.getMessage());
            e.printStackTrace();
        }
    });
}
    // ─────────────────────────────────────────────────────────────
    //   UI HELPERS
    // ─────────────────────────────────────────────────────────────
    private JLabel label(String text, Font font, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(fg);
        return l;
    }

    private JProgressBar bar(Color fg, int w) {
        JProgressBar b = new JProgressBar(0, 100);
        b.setForeground(fg);
        b.setBackground(new Color(30, 25, 50));
        b.setBorderPainted(false);
        b.setStringPainted(true);
        b.setFont(F_SMALL);
        b.setPreferredSize(new Dimension(w, 18));
        return b;
    }

    private JButton fancyButton(String text, Color bg) {
    JButton b = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g2d) {
            Graphics2D g = (Graphics2D) g2d;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Use button_bg.png if available
            Image buttonBg = imgs.get("button_bg");
            if (buttonBg != null) {
                g.drawImage(buttonBg, 0, 0, getWidth(), getHeight(), null);
                // Add overlay for hover/press effects
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(255, 255, 255, 50));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
            } else {
                // Fallback to colored button
                Color base = getModel().isPressed() ? bg.darker() :
                             getModel().isRollover() ? bg.brighter() : bg;
                g.setColor(base);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            
            g.setColor(new Color(255,255,255,60));
            g.setStroke(new BasicStroke(1));
            g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            super.paintComponent(g);
        }
    };
    b.setFont(F_BTN);
    b.setForeground(Color.WHITE);
    b.setOpaque(false);
    b.setContentAreaFilled(false);
    b.setBorderPainted(false);
    b.setFocusPainted(false);
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 42));
    return b;
}

    private JButton smallButton(String text) {
    JButton b = fancyButton(text, new Color(50, 45, 80));
    b.setPreferredSize(new Dimension(80, 32));
    b.setFont(F_SMALL);
    return b;
}

    private JPanel center(Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false); p.add(c); return p;
    }

    private Box.Filler vgap(int h) {
        return (Box.Filler) Box.createVerticalStrut(h);
    }

    private String getEnemyKey(String name) {
        if (name.contains("Wolf"))     return "enemy_wolf";
        if (name.contains("Bat"))      return "enemy_bat";
        if (name.contains("Guard") && !(name.contains("Guardian"))) return "enemy_guard";
        if (name.contains("Guardian")) return "boss_guardian";
        if (name.contains("Wraith"))   return "boss_wraith";
        if (name.contains("Warden"))   return "boss_warden";
        return "enemy_wolf";
    }
}