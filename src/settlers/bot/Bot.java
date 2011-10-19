package settlers.bot;

import settlers.*;
import settlers.util.*;

public abstract class Bot {

    protected final Game.API api;

    public Bot(Game.API api) {
        this.api = api;
    }

    public abstract String getName();
    public abstract void makeTurn();
    public abstract TradeResult trade(TradeOffer offer);
    public abstract void discardHalfOfTheCards();
    public abstract Pair<Board.Intersection, Board.Path> placeFirstSettlements(boolean first);

    public void tradeWithOtherPlayerOnHisTurn() { }
}

