package bs_team.model;

public class Player {
    private String name;
    private int height;
    private String position;
    private int attackPower;
    private int defensePower;
    private boolean handler;

    public Player(String name, int height, String position, int attackPower, int defensePower, boolean handler) {
        this.name = name;
        this.height = height;
        this.position = position;
        this.attackPower = attackPower;
        this.defensePower = defensePower;
        this.handler = handler;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public int getDefensePower() {
        return defensePower;
    }

    public void setDefensePower(int defensePower) {
        this.defensePower = defensePower;
    }

    public boolean isHandler() {
        return handler;
    }

    public void setHandler(boolean handler) {
        this.handler = handler;
    }

    // 총 능력치 (공격 + 수비)
    public int getTotalPower() {
        return attackPower + defensePower;
    }

    @Override
    public String toString() {
        String handlerText = handler ? " (핸들러)" : "";
        return name + " (" + position + ", " + height + "cm, 공격 " + attackPower + ", 수비 " + defensePower + ")" + handlerText;
    }
} 