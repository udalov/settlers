package settlers.bot;

import java.util.List;
import settlers.*;
import settlers.util.*;

public abstract class Bot {

    protected final Game.API api;

    public Bot(Game.API api) {
        this.api = api;
    }

    public abstract void makeTurn();
    public abstract TradeResult trade(TradeOffer offer);
    public abstract List<Resource> discardHalfOfTheCards();
    public abstract Pair<Xing, Path> placeFirstSettlements(boolean first);

    public void tradeWithOtherPlayerOnHisTurn() { }
}

