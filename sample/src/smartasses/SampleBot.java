package smartasses;

import java.util.List;
import settlers.*;
import settlers.bot.*;
import settlers.util.*;

public class SampleBot extends Bot {
    
    public SampleBot(Game.API api) {
        super(api);
    }

    public String toString() { return "Smart Ass"; }

    public void makeTurn() {
    }

    public TradeResult trade(TradeOffer offer) {
        return api.declineOffer(offer);
    }

    public List<Resource> discardHalfOfTheCards() {
        return null;
    }

    public Pair<Node, Edge> placeInitialSettlements(boolean first) {
        throw new RuntimeException("THE CAKE IS A LIE");
    }

}

