public class Skill {
    private String name;
    private int manaCost;
    private int damage;

    public Skill(String name, int manaCost, int damage) {
        this.name = name;
        this.manaCost = manaCost;
        this.damage = damage;
    }

    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public int getDamage() { return damage; }

    public boolean canUse(Player player) {
        return player.getMana() >= manaCost;
    }

    public void use(Player player, Enemy enemy) {
        if (!canUse(player)) {
            System.out.println("Not enough mana to use " + name + "!");
            return;
        }

        player.setMana(player.getMana() - manaCost);
        System.out.println(player.getName() + " uses " + name + "!");
        enemy.takeDamage(damage);
        System.out.println("Dealt " + damage + " damage!");
    }
}