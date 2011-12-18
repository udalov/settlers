package settlers.bot;

import java.util.*;
import settlers.*;
import settlers.util.*;

public class StupidBot extends Bot {

    private final Random rnd;

    public StupidBot(Game.API api) {
        super(api);
        rnd = api.rnd();
    }

    public void makeTurn() {
        if (api.rollDice() == 7) {
            List<Hex> hexes = Util.shuffle(Board.allHexes(), rnd);
            for (Hex h : hexes) {
                if (api.robber() == h)
                    continue;
                List<Player> robbable = api.robbable(h);
                api.moveRobber(h, robbable.isEmpty() ? null : robbable.get(0));
                break;
            }
        }
        if (api.citiesLeft() > 0 && api.getIfPossible("OOOGG")) {
            for (Xing x : Board.allXings()) {
                Town t = api.townAt(x);
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
        if (api.settlementsLeft() > 0 && api.getIfPossible("BWGL")) {
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
        if (api.roadsLeft() > 0 && api.getIfPossible("BL")) {
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

    public Pair<Xing, Path> placeInitialSettlements(boolean first) {
        List<Xing> xings = Util.shuffle(Board.allXings(), rnd);
        for (Xing x : xings)
            if (api.canBuildTownAt(x, false))
                for (Path p : Board.adjacentPaths(x))
                    if (api.roadAt(p) == null)
                        return Pair.make(x, p);
        return null;
    }

    public String toString() {
        return "STUPID Bot";
    }
    
}

