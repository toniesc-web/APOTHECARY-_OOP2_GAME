public class Weapon extends Item {
    private int attackBonus;

    public Weapon(String name, String description, int attackBonus) {
        super(name, description);
        this.attackBonus = attackBonus;
    }

    @Override
    public void use(Player player) {
        player.increaseAttack(attackBonus);
        System.out.println(player.getName() + " equips " + name + "! Attack +" + attackBonus);
    }
}