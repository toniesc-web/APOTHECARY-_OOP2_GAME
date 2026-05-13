import java.util.*;

public class Boss extends Enemy {
    private List<Skill> skills = new ArrayList<>();

    public Boss(String name, int hp, int attack) {
        super(name, hp, attack);
    }

    public void addSkill(Skill s) {
        skills.add(s);
    }

    public List<Skill> getSkills() {
        return skills;
    }
}