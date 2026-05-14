import java.util.*;

public class SwordUpgrader {
    private Scanner sc = new Scanner(System.in);

    public void open(Player player) {
        boolean running = true;
        while (running) {
            System.out.println("\n\n");
            System.out.println("           ╔════════════════╗         ");
            System.out.println("╔══════════║ SWORD UPGRADER ║═════════╗");
            System.out.println("║          ╚════════════════╝         ║");
            System.out.println("║ Gold: " + player.getGold() + "                           ║");
            System.out.println("║ 1. Upgrade Attack (+5) - 50 Gold    ║");
            System.out.println("║ B. Back                             ║");
            System.out.println("╚═════════════════════════════════════╝");
            System.out.print("> ");
            String choice = sc.nextLine().trim().toUpperCase();
            switch (choice) {
                case "1" -> {
                    if (player.getGold() >= 50) {
                        player.addGold(-50);
                        player.increaseAttack(5);
                        System.out.println("Attack increased by 5!");
                    } else System.out.println("Not enough gold!");
                }
                case "B" -> { running = false; System.out.println("Leaving Sword Upgrader..."); }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}
