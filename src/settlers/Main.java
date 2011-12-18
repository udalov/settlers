package settlers;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.util.List;
import javax.swing.JFrame;
import settlers.bot.Bot;
import settlers.bot.ExampleBot;
import settlers.bot.StupidBot;
import settlers.util.Pair;
import settlers.util.Util;
import settlers.vis.Vis;

public class Main {
    
    String eventString(Event event, Map<Player, Integer> index) {
        switch (event.type()) {
            case INITIAL_ROAD:
                return index.get(event.player()) + " road " + event.path();
            case INITIAL_SETTLEMENT:
                return index.get(event.player()) + " settlement " + event.xing();
            case ROLL_DICE:
                return "roll " + event.number();
            case ROBBER:
                Player pl = event.player();
                return "robber " + event.hex() + " " + (pl == null ? -1 : index.get(pl));
            case RESOURCES:
                // TODO?
                return "";
            case DISCARD:
                return "discard " + index.get(event.player()) + " " + Util.toResourceString(event.resources());
            case ROAD:
                return "road " + event.path();
            case SETTLEMENT:
                return "settlement " + event.xing();
            case CITY:
                return "city " + event.xing();
            case DEVELOPMENT:
                return "development";
            case KNIGHT:
                return "knight";
            case INVENTION:
                return "invention " + event.resource().chr() + " " + event.resource2().chr();
            case MONOPOLY:
                return "monopoly " + event.resource().chr() + " " + event.number();
            case ROAD_BUILDING:
                return "roadbuilding " + event.path() + " " + event.path2();
            case LONGEST_ROAD:
                return "longestroad " + event.number();
            case LARGEST_ARMY:
                return "largestarmy " + event.number();
            case CHANGE:
                return "change " + event.sell() + " " + event.buy();
            case TRADE:
                return "trade " + index.get(event.player()) + " " + event.sell() + " " + event.buy();
            case VICTORY:
                return "victory";
            case EXCEPTION:
                Exception e = event.exception();
                e.printStackTrace();
                return "exception " + e.getClass().getName() + " " + e.getMessage();
            default:
                return "";
        }
    }

    void printHistory(Game game, PrintStream out) {
        out.println(game.players().size());
        Map<Player, Integer> index = new HashMap<Player, Integer>();
        for (Player p : game.players()) {
            index.put(p, index.size());
            out.println(p.bot());
        }

        List<Pair<Player, List<Event>>> history = game.history().getAll();
        for (Pair<Player, List<Event>> pair : history) {
            Player player = pair.first();
            List<Event> events = pair.second();
            int ind = player == null ? -1 : index.get(player);
            for (Event event : events) {
                String s = eventString(event, index);
                if (ind >= 0) s = ind + " " + s;
                out.println(s);
                if (event.type() == EventType.EXCEPTION)
                    return;
            }
        }

        for (Player p : game.players()) {
            out.print(game.points(p));
            int vp = p.developments().victoryPoint();
            if (vp > 0)
                out.print(" " + vp + "VP");
            if (game.largestArmy() == p && p.armyStrength() >= 3)
                out.print(" ARMY");
            if (game.longestRoad() == p && game.roadLength(p) >= 5)
                out.print(" ROAD");
            out.println();
        }

        out.println(history.size() - 1 + " turns");
    }

    void run(String[] args) {
        try {
            Bot[] bots = null;
            boolean vis = false;
            long randSeed = 0;

            for (int i = 0; i < args.length; i++)
                if ("-seed".equals(args[i]))
                    randSeed = Long.parseLong(args[++i]);

            Game game = new Game(randSeed);

            for (int i = 0; i < args.length; i++) {
                if ("-3".equals(args[i]) || "-4".equals(args[i])) {
                    int nbots = -Integer.parseInt(args[i]);
                    bots = new Bot[nbots];
                    for (int j = 0; j < nbots; j++) {
                        Game.API api = game.new API();
                        String bot = args[++i];
                        if ("Example".equals(bot)) {
                            bots[j] = new ExampleBot(api);
                        } else if ("Stupid".equals(bot)) {
                            bots[j] = new StupidBot(api);
                        } else {
                            int colon = bot.lastIndexOf(':');
                            File jar = new File(bot.substring(0, colon));
                            String className = bot.substring(colon + 1);
                            ClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
                            Constructor<?> cns = cl.loadClass(className).getConstructor(Game.API.class);
                            bots[j] = (Bot)cns.newInstance(api);
                        }
                        api.setBot(bots[j]);
                    }
                } else if ("-vis".equals(args[i])) {
                    vis = true;
                } else if ("-seed".equals(args[i])) {
                    ++i;
                } else {
                    throw new IllegalArgumentException("Unknown parameter: " + args[i]);
                }
            }

            for (int i = 0; i < bots.length; i++) {
                game.addPlayer(new Player(bots[i], Player.Color.values()[i]));
            }

            if (vis) {
                new Vis(game, game.new VisAPI());
            } else {
                game.play();
                printHistory(game, System.out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

}

