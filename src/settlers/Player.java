package settlers;

import settlers.bot.Bot;
import settlers.util.Util;

public class Player {
    
    public static enum Color {
        RED,
        BLUE,
        ORANGE,
        WHITE,
    }

    private final Bot bot;
    private final Color color;

    private final ResourceStack cards = new ResourceStack();
    private final DevelopmentStack developments = new DevelopmentStack();
    private int armyStrength;
    
    private int settlementsLeft;
    private int citiesLeft;
    private int roadsLeft;

    Player(Bot bot, Color color) {
        this.bot = bot;
        this.color = color;
        settlementsLeft = 5;
        citiesLeft = 4;
        roadsLeft = 15;
        Util.checkBotName(bot.toString());
    }

    public Color color() { return color; }
    Bot bot() { return bot; }
    ResourceStack cards() { return cards; }
    DevelopmentStack developments() { return developments; }

    public int armyStrength() { return armyStrength; }
    public int cardsNumber() { return cards.size(); }
    public int developmentsNumber() { return developments.size(); }

    public int settlementsLeft() { return settlementsLeft; }
    public int citiesLeft() { return citiesLeft; }
    public int roadsLeft() { return roadsLeft; }
    void expendSettlement() { settlementsLeft--; }
    void expendCity() { citiesLeft--; settlementsLeft++; }
    void expendRoad() { roadsLeft--; }

    void increaseArmyStrength() { armyStrength++; }
    
    public boolean equals(Object o) {
        if (!(o instanceof Player))
            return false;
        return color == ((Player)o).color;
    }

    public int hashCode() {
        return color.ordinal();
    }

    public String toString() {
        return bot.toString();
    }
}

