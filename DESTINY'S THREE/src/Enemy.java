public class Enemy extends Character {
    public Enemy(String name, int hp, int attack) {
        super(name, hp, attack);
    }
    
    @Override
    public void attack(Character target) {
        System.out.println(name + " attacks!");
        target.takeDamage(attack);
    }
}
