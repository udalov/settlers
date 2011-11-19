package settlers.bot;

import java.util.*;
import settlers.*;
import settlers.util.*;

public class StupidBot extends Bot {

    private final Random rnd = api.rnd();
    private final Board board;
    
    public StupidBot(Game.API api) {
        super(api);
        board = api.board();
    }

    public void makeTurn() {
        if (api.rollDice() == 7) {
            List<Hex> hexes = Util.shuffle(Board.allHexes(), rnd);
            h: for (Hex h : hexes) {
                if (board.robber() == h)
                    continue;
                List<Town> ts = board.adjacentTowns(h); 
                for (Town t : ts)
                    if (t.player() == api.me())
                        continue h;
                for (Town t : ts) {
                    if (t.player().cardsNumber() > 0) {
                        api.moveRobber(h, t.player());
                        break h;
                    }
                }
            }
        }
        if (api.me().citiesLeft() > 0 && api.getIfPossible("OOOGG")) {
            for (Xing x : Board.allXings()) {
                Town t = board.townAt(x);
                if (t == null || t.player() != api.me() || t.isCity())
                    continue;
                api.buildCity(x);
                break;
            }
        }
        if (api.developments().monopoly() > 0) {
            api.monopoly(Resource.GRAIN);
        }
        if (api.developments().invention() > 0) {
            api.invention(Resource.BRICK, Resource.ORE);
        }
        if (api.me().settlementsLeft() > 0 && api.getIfPossible("BWGL")) {
            List<Xing> xings = Util.shuffle(Board.allXings(), rnd);
            for (Xing x : xings) {
                if (api.canBuildTownAt(x, true)) {
                    api.buildSettlement(x);
                    break;
                }
            }
        }
        while (api.developmentsLeft() > 0 && api.getIfPossible("WOG")) {
            api.drawDevelopment();
        }
        if (api.me().roadsLeft() > 0 && api.getIfPossible("BL")) {
            List<Path> paths = Util.shuffle(Board.allPaths(), rnd);
            for (Path p : paths) {
                if (api.canBuildRoadAt(p)) {
                    api.buildRoad(p);
                    break;
                }
            }
        }
    }

    public TradeResult trade(TradeOffer offer) {
        return api.declineOffer(offer);
    }

    public List<Resource> discardHalfOfTheCards() {
        List<Resource> ans = new ArrayList<Resource>();
        List<Resource> cards = api.cards().list();
        for (Resource r : cards) {
            if (ans.size() == cards.size() / 2)
                return ans;
            ans.add(r);
        }
        return ans;
    }

    public Pair<Xing, Path> placeFirstSettlements(boolean first) {
        List<Xing> xings = Util.shuffle(Board.allXings(), rnd);
        for (Xing x : xings)
            if (api.canBuildTownAt(x, false))
                for (Path p : Board.adjacentPaths(x))
                    if (board.roadAt(p) == null)
                        return Pair.make(x, p);
        return null;
    }

    public String toString() {
        return "STUPID Bot";
    }
    
}

