public abstract class Character {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;

    public Character(String name, int maxHp, int attack) {
        this.name = name;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.attack = attack;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
        System.out.println(name + " took " + damage + " damage! (HP: " + hp + ")");
    }

    public void attack(Character target) {
        System.out.println(name + " attacks!");
        target.takeDamage(attack);
    }
}
