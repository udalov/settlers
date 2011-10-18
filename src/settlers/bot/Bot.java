package settlers.bot;

import settlers.*;
import settlers.util.*;

public abstract class Bot {

    protected final Game game;

    public Bot(Game game) {
        this.game = game;
    }

    public abstract String getName();
    public abstract void makeTurn();
    public abstract TradeResult trade(TradeOffer offer);
    public abstract void discardHalfOfTheCards();
    public abstract Pair<Board.Intersection, Board.Path> placeFirstSettlements(boolean first);

    public void tradeWithOtherPlayerOnHisTurn() { }
}

