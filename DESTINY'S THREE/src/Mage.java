public class Mage extends Player {
    public Mage(String name) {
        super(name, 100, 120, 15);
        skills.add(new Skill("Fireball", 40, 50));
        skills.add(new Skill("Ice Spike", 35, 40));
        skills.add(new Skill("Arcane Blast", 50, 60));
        skills.add(new Skill("Mana Shield", 10, 0)); // buff placeholder
    }

    @Override
    public String getDescription() {
        return "\n A prodigy once obsessed with mastering the arcane,\n Kaelen now embraces the storm as both weapon and curse.\n Every spell he casts teeters between control and chaos,\n his emotions dictating the very elements around him.";
    }
}
