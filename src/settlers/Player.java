package settlers;

import settlers.bot.Bot;

public class Player {
    
    public static enum Color {
        RED,
        BLUE,
        ORANGE,
        WHITE,
    }

    private final Bot bot;
    private final Color color;

    private final CardStack cards = new CardStack();
    private final DevelopmentStack developments = new DevelopmentStack();
    private int armyStrength;

    Player(Bot bot, Color color) {
        this.bot = bot;
        this.color = color;
    }

    Bot bot() { return bot; }
    public Color color() { return color; }
    CardStack cards() { return cards; }
    DevelopmentStack developments() { return developments; }
    public int armyStrength() { return armyStrength; }
    
}

