import java.util.*;

public class Store {
    private Scanner sc = new Scanner(System.in);

    public void open(Player player) {
        boolean inStore = true;
        while (inStore) {
            System.out.println("\n");
            System.out.println("                ╔═══════╗               ");
            System.out.println("╔═══════════════║ STORE ║═════════════╗");
            System.out.println("║               ╚═══════╝             ║");
            System.out.println("║ Gold: " + player.getGold() + "                           ");
            System.out.println("║                                     ║");
            System.out.println("║ 1. Health Potion (15 gold)          ║ ");
            System.out.println("║ 2. Mana Potion (15 gold)            ║ ");
            System.out.println("║ 3. Iron Sword (+5 Attack, 50 gold)  ║ ");
            System.out.println("║ B. Back                             ║ ");
            System.out.println("╚═════════════════════════════════════╝");
            System.out.print("> ");
            String choice = sc.nextLine().trim().toUpperCase();

            switch (choice) {
                case "1" -> buyHealthPotion(player);
                case "2" -> buyManaPotion(player);
                case "3" -> buySword(player);
                case "B" -> { inStore = false; System.out.println("Leaving store..."); }
                default -> {
                    System.out.println("\n\n");
                    System.out.println("╔══════════════════════════╗");
                    System.out.println("║       Invalid input      ║");
                    System.out.println("╚══════════════════════════╝");
            }
            }
        }
    }

    private void buyHealthPotion(Player player) {
        if (player.getGold() >= 15) {
            player.getInventory().addItem(new Potion("Health Potion", "Restores HP", 30, 1));
            player.addGold(-15);
        } else System.out.println("\n\nNot enough gold!");
    }

    private void buyManaPotion(Player player) {
        if (player.getGold() >= 15) {
            player.getInventory().addItem(new Potion("Mana Potion", "Restores MP", 30, 2));
            player.addGold(-15);
        } else System.out.println("\n\nNot enough gold!");
    }

    private void buySword(Player player) {
        if (player.getGold() >= 50) {
            player.getInventory().addItem(new Weapon("Iron Sword", "A sturdy blade", 5));
            player.addGold(-50);
        } else System.out.println("\n\nNot enough gold!");
    }
}