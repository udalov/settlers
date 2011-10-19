package settlers;

import settlers.bot.Bot;

public class Player {
    
    public static enum Color {
        RED,
        BLUE,
        YELLOW,
        WHITE,
    }

    private final Bot bot;
    private final Color color;

    Player(Bot bot, Color color) {
        this.bot = bot;
        this.color = color;
    }

    Bot bot() {
        return bot;
    }

    public Color color() {
        return color;
    }
    
}

