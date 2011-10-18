package settlers;

import java.util.HashMap;
import java.util.Map;
import settlers.bot.Bot;

public class TradeOffer {
    
    private final Player trader;
    private final Map<Resource, Integer> iWant;
    private final Map<Resource, Integer> iGive;

    TradeOffer(
        Player trader,
        Map<Resource, Integer> iWant,
        Map<Resource, Integer> iGive
    ) {
        // TODO: check validity
        this.trader = trader;
        this.iWant = new HashMap<Resource, Integer>(iWant);
        this.iGive = new HashMap<Resource, Integer>(iGive);
    }

    public TradeResult accept() {
        return new TradeResult(TradeResult.ACCEPT, null);
    }

    public TradeResult decline() {
        return new TradeResult(TradeResult.DECLINE, null);
    }

    public TradeResult counteroffer(TradeOffer offer) {
        return new TradeResult(TradeResult.COUNTEROFFER, offer);
    }
}


