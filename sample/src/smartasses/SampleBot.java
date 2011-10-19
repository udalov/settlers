package smartasses;

import settlers.*;
import settlers.bot.*;
import settlers.util.*;

public class SampleBot extends Bot {
    
    public SampleBot(Game.API api) {
        super(api);
    }

    public String getName() {
        return "Smart Ass";
    }

    public void makeTurn() {
    }

    public TradeResult trade(TradeOffer offer) {
        return offer.decline();
    }

    public void discardHalfOfTheCards() {
    }

    public Pair<Board.Intersection, Board.Path> placeFirstSettlements(boolean first) {
        throw new RuntimeException("THE CAKE IS A LIE");
    }

}

