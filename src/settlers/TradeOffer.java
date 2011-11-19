package settlers;

import java.util.HashMap;
import java.util.Map;
import settlers.bot.Bot;

public class TradeOffer {
    
    private final Player trader;
    private final String sell;
    private final String buy;

    TradeOffer(Player trader, String sell, String buy) {
        // TODO: check validity
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
    }
}


