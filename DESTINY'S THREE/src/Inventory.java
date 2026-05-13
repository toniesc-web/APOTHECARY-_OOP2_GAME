
import java.util.*;

public class Inventory {
    private List<Item> items = new ArrayList<>();
    private Scanner sc = new Scanner(System.in);

    public void addItem(Item i) {
        items.add(i);
        System.out.println("\n");
        System.out.println("═══════════════════════════════════════");
        System.out.println("║   " + i.getName() + " added to inventory. ║");
        System.out.println("═══════════════════════════════════════");
    }

    public void open(Player player) {
        boolean running = true;
        while (running) {
            System.out.println("              ╔═══════════╗              ");
            System.out.println("╔═════════════║ INVENTORY ║═══════════╗");
            System.out.println("║             ╚═══════════╝           ║");
            System.out.println("║                                     ║");

            if (items.isEmpty()) {
                System.out.println("║ Inventory is empty.                 ║");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    System.out.println((i + 1) + ". " + items.get(i).getName());
                }
            }

            System.out.println("║ U. Use Item                         ║");
            System.out.println("║ B. Back                             ║");
            System.out.println("║                                     ║");
            System.out.println("╚═════════════════════════════════════╝");
            System.out.print("> ");

            String choice = sc.nextLine().trim().toUpperCase();

            switch (choice) {
                case "U" -> useItem(player);
                case "B" -> running = false;
                default -> {
                    System.out.println("\n");
                    System.out.println("╔═══════════════════╗");
                    System.out.println("║  Invalid choice.  ║");
                    System.out.println("╚═══════════════════╝\n\n");
                }
            }
        }
    }

    private void useItem(Player player) {
        if (items.isEmpty()) {
            System.out.println("\n");
            System.out.println("╔══════════════════════════╗");
            System.out.println("║     No items to use!     ║");
            System.out.println("╚══════════════════════════╝\n\n");
            return;
        }

        System.out.println("\n");
        System.out.println("╔══════════════════════════╗");
        System.out.println("║    Select item to use:   ║");
        System.out.println("╚══════════════════════════╝");
        System.out.println("\n");

        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).getName());
        }

        System.out.print("> ");

        try {
            int choice = Integer.parseInt(sc.nextLine());
            Item selected = items.get(choice - 1);

            boolean used = true;

            if (selected instanceof Potion potion) {
                used = potion.apply(player);
            } else {
                selected.use(player);
            }

            if (used) {
                items.remove(selected);
            }

        } catch (Exception e) {
            System.out.println("\n\nInvalid input.");
        }
    }
        
        public void clear() {
    items.clear();
    System.out.println("\nAll items removed from inventory.");
}

}
