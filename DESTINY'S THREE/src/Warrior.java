public class Warrior extends Player {
    public Warrior(String name) {
        super(name, 150, 60, 20);
        skills.add(new Skill("Power Slash", 20, 30));
        skills.add(new Skill("Whirlwind", 25, 40));
        skills.add(new Skill("Shield Bash", 10, 20));
        skills.add(new Skill("Battle Cry", 5, 10));
    }

    @Override
    public String getDescription() {
        return "\n\tA lone fighter reborn beneath Solmyr\'s blazing skies.\n Haunted by a past he can\'t forget, Auron wields his blade not for glory but redemption.\n His resolve burns brighter than the sun itself - steady, unyielding, and bound by honor.";
    }
} 