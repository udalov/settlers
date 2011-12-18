package settlers;

import settlers.bot.Bot;
import settlers.util.Util;

public class Player {
    
    private final Bot bot;
    private final int color;

    private final ResourceStack cards = new ResourceStack();
    private final DevelopmentStack developments = new DevelopmentStack();
    private int armyStrength;
    
    Player(Bot bot, int color) {
        this.bot = bot;
        this.color = color;
        Util.checkBotName(bot.toString());
    }

    public int color() { return color; }
    Bot bot() { return bot; }
    ResourceStack cards() { return cards; }
    DevelopmentStack developments() { return developments; }
    int armyStrength() { return armyStrength; }

    public int cardsNumber() { return cards.size(); }
    public int developmentsNumber() { return developments.size(); }

    void increaseArmyStrength() { armyStrength++; }
    
    public boolean equals(Object o) {
        if (!(o instanceof Player))
            return false;
        return color == ((Player)o).color;
    }

    public int hashCode() {
        return color;
    }

    public String toString() {
        return bot.toString();
    }
}

