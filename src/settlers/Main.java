package settlers;

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
    
    void printEvent(Event event, Game game, PrintStream out) {
        switch (event.type()) {
            case INITIAL_ROAD:
                out.println(event.player().color() + " road " + event.path()); break;
            case INITIAL_SETTLEMENT:
                out.println(event.player().color() + " settlement " + event.xing()); break;
            case ROLL_DICE:
                out.println("roll " + event.number()); break;
            case ROBBER:
                Player pl = event.player();
                out.println("robber " + event.hex() + " " + (pl == null ? "nobody" : pl.color())); break;
            case RESOURCES:
                // TODO?
                break;
            case DISCARD:
                out.println("discard " + event.player().color() + " " + Util.toResourceString(event.resources())); break;
            case ROAD:
                out.println("road " + event.path()); break;
            case SETTLEMENT:
                out.println("settlement " + event.xing()); break;
            case CITY:
                out.println("city " + event.xing()); break;
            case DEVELOPMENT:
                out.println("development"); break;
            case KNIGHT:
                out.println("knight"); break;
            case INVENTION:
                out.println("invention " + event.resource() + " " + event.resource2()); break;
            case MONOPOLY:
                out.println("monopoly " + event.resource() + " " + event.number()); break;
            case ROAD_BUILDING:
                out.println("roadbuilding " + event.path() + " " + event.path2()); break;
            case LONGEST_ROAD:
                out.println("longestroad " + event.number()); break;
            case LARGEST_ARMY:
                out.println("largestarmy " + event.number()); break;
            case CHANGE:
                out.println("change " + event.sell() + " " + event.buy()); break;
            case TRADE:
                out.println("trade " + event.player().color() + " " + event.sell() + " " + event.buy()); break;
            case VICTORY:
                out.println("victory");
                for (Player p : game.players()) {
                    out.print(p.color() + " " + game.points(p));
                    int vp = p.developments().victoryPoint();
                    if (vp > 0)
                        out.print(" " + vp + "VP");
                    if (game.largestArmy() == p && p.armyStrength() >= 3)
                        out.print(" ARMY");
                    if (game.longestRoad() == p && game.roadLength(p) >= 5)
                        out.print(" ROAD");
                    out.println();
                }
                break;
        }
    }

    void printHistory(Game game, PrintStream out) {
        for (Player p : game.players()) {
            System.out.println(p.color() + " " + p.bot());
        }

        List<Pair<Player, List<Event>>> history = game.history().getAll();
        for (Pair<Player, List<Event>> pair : history) {
            Player player = pair.first();
            List<Event> events = pair.second();
            if (player != null) {
                out.println("- " + player.color());
            }
            for (Event event : events) {
                printEvent(event, game, out);
            }
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
                new Vis(game, game.new Runner());
            } else {
                try {
                    game.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

