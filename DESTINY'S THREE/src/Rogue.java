public class Rogue extends Player {
    public Rogue(String name) {
        super(name, 120, 80, 18);
        skills.add(new Skill("Shadow Strike", 25, 35));
        skills.add(new Skill("Backstab", 40, 50));
        skills.add(new Skill("Poison Blade", 15, 25));
        skills.add(new Skill("Evasion", 5, 0)); // buff placeholder
    }

    @Override
    public String getDescription() {
        return "An agile rogue who strikes swiftly with precision.";
    }
}