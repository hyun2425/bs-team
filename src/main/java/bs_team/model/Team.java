package bs_team.model;

import java.util.List;

public class Team {
    private String name;
    private List<Player> players;

    public Team(String name, List<Player> players) {
        this.name = name;
        this.players = players;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public int getTotalAttackPower() {
        return players.stream()
                .mapToInt(Player::getAttackPower)
                .sum();
    }

    public int getTotalDefensePower() {
        return players.stream()
                .mapToInt(Player::getDefensePower)
                .sum();
    }

    public int getTotalPower() {
        return players.stream()
                .mapToInt(Player::getTotalPower)
                .sum();
    }

    public int getHandlerCount() {
        return (int) players.stream()
                .filter(Player::isHandler)
                .count();
    }

    public double getAverageHeight() {
        if (players.isEmpty()) {
            return 0.0;
        }
        return players.stream()
                .mapToInt(Player::getHeight)
                .average()
                .orElse(0.0);
    }

    public int getPlayerCount() {
        return players.size();
    }
} 