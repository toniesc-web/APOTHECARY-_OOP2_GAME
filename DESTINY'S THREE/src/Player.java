import java.util.*;

public abstract class Player extends Character {
    protected int mana;
    protected int maxMana;
    protected int gold;
    protected Inventory inventory = new Inventory();
    protected List<Skill> skills = new ArrayList<>();
    protected int baseAttack;

    public Player(String name, int maxHp, int maxMana, int attack) {
        super(name, maxHp, attack);
        this.mana = maxMana;
        this.maxMana = maxMana;
        this.gold = 100;       // default starting gold
        this.baseAttack = attack; // store original attack
    }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }
    public int getMaxMana() { return maxMana; }

    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }

    public Inventory getInventory() { return inventory; }
    public List<Skill> getSkills() { return skills; }


    public abstract String getDescription(); //method overriding//

    
    public void increaseAttack(int value) {
        this.attack += value;
    }

    public void useSkill(int index, Enemy enemy) {
        if (index < 0 || index >= skills.size()) {
            System.out.println("Invalid skill choice!");
            return;
        }
        skills.get(index).use(this, enemy);
    }

    public void showSkills() {
        System.out.println("\n=== Skills ===");
        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            System.out.println((i + 1) + ". " + s.getName() + " (Mana: " + s.getManaCost() + ", Damage: " + s.getDamage() + ")");
        }
    }

    // Reset player to default state
    public void reset() {
    this.hp = this.maxHp;
    this.mana = this.maxMana;
    this.attack = this.baseAttack; // restore base attack
    this.gold = 100;                // reset gold to starting amount
    this.inventory.clear();
}

}
