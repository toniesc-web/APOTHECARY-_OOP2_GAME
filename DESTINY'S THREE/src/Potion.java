public class Potion extends Item {
    private int restoreAmount;
    private int type; 

    public Potion(String name, String description, int restoreAmount, int type) {
        super(name, description);
        this.restoreAmount = restoreAmount;
        this.type = type;
    }


    public boolean apply(Player player) {
        if (type == 1) { 
            if (player.getHp() >= player.getMaxHp()) {
                System.out.println("\n\nYour HP is already full! Potion not used.\n\n");
                return false;
            }
            int before = player.getHp();
            player.setHp(Math.min(player.getHp() + restoreAmount, player.getMaxHp()));
            System.out.println("\n\nYou used a Health Potion! Restored " + (player.getHp() - before) + " HP.\n\n");
            return true;
        } else { 
            if (player.getMana() >= player.getMaxMana()) {
                System.out.println("\n\nYour Mana is already full! Potion not used.\n\n");
                return false;
            }
            int before = player.getMana();
            player.setMana(Math.min(player.getMana() + restoreAmount, player.getMaxMana()));
            System.out.println("\n\nYou used a Mana Potion! Restored " + (player.getMana() - before) + " MP.\n\n");
            return true;
        }
    }
    @Override
    public void use(Player player) {
    
    }
}
