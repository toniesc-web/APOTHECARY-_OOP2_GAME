import java.util.*;

public class World {
    private String name;
    private List<Mob> mobs = new ArrayList<>();
    private Boss boss;
    

    public World(String name, int numMobs) {
        this.name = name;

        // Create mobs for this world
        switch (name) {
            case "Forest of Beginnings" -> {
    int baseHP = 30;
    int baseDMG = 7;
    int hpIncrease = 4;
    int dmgIncrease = 2;

    for (int i = 1; i <= numMobs; i++) {
        int scaledHP = baseHP + (i - 1) * hpIncrease;
        int scaledDMG = baseDMG + (i - 1) * dmgIncrease;

        mobs.add(new Mob("Forest Wolf " + i, scaledHP, scaledDMG));
    }

    boss = new Boss("Forest Guardian", 180, 15);
    boss.addSkill(new Skill("Nature’s Wrath", 40, 10));
}

            case "Caverns of Shadow" -> {
    int baseHP = 50;
    int baseDMG = 15;
    int hpIncrease = 10;
    int dmgIncrease = 3;

    for (int i = 1; i <= numMobs; i++) {
        int scaledHP = baseHP + (i - 1) * hpIncrease;
        int scaledDMG = baseDMG + (i - 1) * dmgIncrease;

        mobs.add(new Mob("Shadow Bat " + i, scaledHP, scaledDMG));
    }

    boss = new Boss("Shadow Wraith", 220, 20);
    boss.addSkill(new Skill("Dark Slash", 50, 15));
    boss.addSkill(new Skill("Fear Howl", 30, 5));
}

            case "Citadel of Fate" -> {
    int baseHP = 70;
    int baseDMG = 18;
    int hpIncrease = 15;
    int dmgIncrease = 4;

    for (int i = 1; i <= numMobs; i++) {
        int scaledHP = baseHP + (i - 1) * hpIncrease;
        int scaledDMG = baseDMG + (i - 1) * dmgIncrease;

        mobs.add(new Mob("Citadel Guard " + i, scaledHP, scaledDMG));
    }

    boss = new Boss("Citadel Warden", 280, 25);
    boss.addSkill(new Skill("Judgment Strike", 60, 15));
    boss.addSkill(new Skill("Fate Seal", 40, 10));
}

        }
    }

    public String getName() {
        return name;
    }

    public List<Mob> getMobs() {
        return mobs;
    }

    public Boss getBoss() {
        return boss;
    }
}
