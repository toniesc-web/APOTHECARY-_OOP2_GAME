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
    }

    // ─────────────────────────────────────────────────────────────
    //   WORLD FACTORY  (mirrors Adventure.java setup)
    // ─────────────────────────────────────────────────────────────
    private List<World> buildWorlds() {
        List<World> list = new ArrayList<>();
        list.add(new World("Forest of Beginnings", 3));
        list.add(new World("Caverns of Shadow",    4));
        list.add(new World("Citadel of Fate",      5));
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //   IMAGE LOADING
    // ─────────────────────────────────────────────────────────────
    private void loadImages() {
        String[] keys   = {"warrior","mage","rogue","enemy_wolf","enemy_bat",
                           "enemy_guard","boss_guardian","boss_wraith","boss_warden","logo"};
        String[] paths  = {"images/","src/images/","./","src/",
                           "D:/APOTHECARY_OOP2_PROJECT_GAME/DESTINY'S THREE/src/images/"};

        for (String k : keys) {
            Image img = null;
            for (String p : paths) {
                File f = new File(p + k + ".png");
                if (f.exists()) {
                    try { img = ImageIO.read(f); break; }
                    catch (Exception ignored) {}

                }

            }
            imgs.put(k, img != null ? img : makePlaceholder(k));
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
        Random rng = new Random();
        JPanel bg = new JPanel(null) {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g = (Graphics2D) g2d;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep gradient background
                GradientPaint gp = new GradientPaint(0, 0, C_VOID, getWidth(), getHeight(), C_DEEP);
                g.setPaint(gp);
                g.fillRect(0, 0, getWidth(), getHeight());
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

        // Layout
        JPanel box = glassPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(40, 60, 40, 60));
        box.setPreferredSize(new Dimension(680, 380));
        for (Component comp : new Component[]{title, vgap(12), sub, vgap(20),
                story, vgap(28), center(startBtn), vgap(8), center(skipBtn)})
            box.add(comp);

        bg.add(box, c);
        return bg;
    }
