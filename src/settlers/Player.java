package settlers;

import settlers.bot.Bot;
import settlers.util.Util;

public class Player {
    
    private final Bot bot;
    private final int color;
    private final ResourceStack cards = new ResourceStack();
    private final DevelopmentDeck developments = new DevelopmentDeck();
    
    Player(Bot bot, int color) {
        String name = bot.toString();
        if (name == null)
            throw new GameException("Name of your bot cannot be null");
        if ("".equals(name))
            throw new GameException("Name of your bot cannot be empty");
        if (name.length() > 64)
            throw new GameException("Name of your bot cannot be that long");
        for (char c : name.toCharArray())
            if ((int)c < 32 || 126 < (int)c)
                throw new GameException("Name of your bot can contain only characters with ASCII codes from 32 to 126");
        this.bot = bot;
        this.color = color;
    }

    public int color() { return color; }
    Bot bot() { return bot; }
    ResourceStack cards() { return cards; }
    DevelopmentDeck developments() { return developments; }

    public int cardsNumber() { return cards.size(); }
    public int developmentsNumber() { return developments.size(); }

    public boolean equals(Object o) {
        return o instanceof Player && color == ((Player)o).color;
    }

    public int hashCode() {
        return color;
    }

    public String toString() {
        return bot.toString();
    }
}

