package settlers;

import settlers.util.Util;

public class TradeOffer {
    
    private final Game game;
    private final Player trader;
    private final String sell;
    private final String buy;

    TradeOffer(Game game, Player trader, String sell, String buy) {
        if (sell == null || buy == null || "".equals(sell) || "".equals(buy))
            throw new GameException("You cannot trade with nothing");
        if (!Util.isResourceString(sell) || !Util.isResourceString(buy))
            throw new GameException("Invalid characters in trade offer");
        if (!trader.cards().areThere(sell))
            throw new GameException("You cannot sell resources you do not have");
        this.game = game;
        this.trader = trader;
        this.sell = sell;
        this.buy = buy;
    }

    public Player trader() { return trader; }
    public String sell() { return sell; }
    public String buy() { return buy; }

    TradeResult accept(Player player) {
        return new TradeResult(player, this, TradeResult.ACCEPT, null);
    }

    TradeResult decline(Player player) {
        return new TradeResult(player, this, TradeResult.DECLINE, null);
    }

    TradeResult counteroffer(TradeOffer counteroffer) {
        return new TradeResult(counteroffer.trader, this, TradeResult.COUNTEROFFER, counteroffer);
    }

    void complete(Player with) {
        trader.cards().sub(sell);
        trader.cards().add(buy);
        with.cards().sub(buy);
        with.cards().add(sell);
        game.history().trade(with, sell, buy);
    }
}


