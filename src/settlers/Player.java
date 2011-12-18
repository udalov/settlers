package settlers;

import settlers.bot.Bot;
import settlers.util.Util;

public class Player {
    
    private final Bot bot;
    private final int color;
    private final ResourceStack cards = new ResourceStack();
    private final DevelopmentStack developments = new DevelopmentStack();
    
    Player(Bot bot, int color) {
        Util.checkBotName(bot.toString());
        this.bot = bot;
        this.color = color;
    }

    public int color() { return color; }
    Bot bot() { return bot; }
    ResourceStack cards() { return cards; }
    DevelopmentStack developments() { return developments; }

    public int cardsNumber() { return cards.size(); }
    public int developmentsNumber() { return developments.size(); }

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

