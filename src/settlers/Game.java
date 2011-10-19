package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import settlers.bot.Bot;
import settlers.util.Pair;

public class Game {
    
    private final List<Player> players = new ArrayList<Player>();
    private final Board board;

    Game() {
        board = Board.create();
    }

    public List<Player> players() {
        return Collections.unmodifiableList(players);
    }

    public Board board() {
        return board;
    }

    public TradeOffer createTradeOffer(
        Bot bot,
        Map<Resource, Integer> iWant,
        Map<Resource, Integer> iGive
    ) {
        return new TradeOffer(player(bot), iWant, iGive);
    }


    void addPlayer(Player player) {
        players.add(player);
    }

    void start() {
        for (int it = 0; it < 2; it++) {
            for (Player player : players) {
                Pair<Board.Intersection, Board.Path> p = player.bot().placeFirstSettlements(it == 0);
                if (!board.canBuildTownAt(p.first()))
                    throw new RuntimeException("Cannot build a town here");
                if (!board.areAdjacent(p.first(), p.second()))
                    throw new RuntimeException("Cannot build a road not connected to a town");
                board.buildTown(p.first(), new Town(player, false));
                board.buildRoad(p.second(), player);
            }
            Collections.reverse(players);
        }
    }

    Player player(Bot bot) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).bot().equals(bot)) {
                return players.get(i);
            }
        }
        throw new IllegalStateException("Internal: no player corresponds to the given bot");
    }

}

