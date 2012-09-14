package settlers;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.util.List;

import settlers.bot.Bot;
import settlers.bot.ExampleBot;
import settlers.bot.StupidBot;
import settlers.util.Pair;
import settlers.util.Util;
import settlers.vis.Vis;

public class Main {
    
    private String eventString(Event event) {
        switch (event.type()) {
            case INITIAL_ROAD:
                return event.player().color() + " road " + event.edge();
            case INITIAL_SETTLEMENT:
                return event.player().color() + " settlement " + event.node();
            case ROLL_DICE:
                return "roll " + event.number();
            case ROBBER:
                Player pl = event.player();
                return "robber " + event.hex() + " " + (pl == null ? -1 : pl.color());
            case DISCARD:
                return "discard " + event.player().color() + " " + Util.toResourceString(event.resources());
            case ROAD:
                return "road " + event.edge();
            case SETTLEMENT:
                return "settlement " + event.node();
            case CITY:
                return "city " + event.node();
            case DEVELOPMENT:
                return "development";
            case KNIGHT:
                return "knight";
            case INVENTION:
                return "invention " + event.resource().chr() + " " + event.resource2().chr();
            case MONOPOLY:
                return "monopoly " + event.resource().chr() + " " + event.number();
            case ROAD_BUILDING:
                return "roadbuilding " + event.edge() + " " + event.edge2();
            case LONGEST_ROAD:
                return "longestroad " + event.number();
            case LARGEST_ARMY:
                return "largestarmy " + event.number();
            case CHANGE:
                return "change " + event.sell() + " " + event.buy();
            case TRADE:
                return "trade " + event.player().color() + " " + event.sell() + " " + event.buy();
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

    private void printGameLog(Game.RunAPI api, PrintStream out, boolean silent) {
        if (!silent) {
            out.println(api.players().size());
            for (Player p : api.players())
                out.println(p.bot());

            for (Hex h : Board.allHexes()) {
                Resource r = api.board().resourceAt(h);
                if (r != null)
                    out.println(h + " " + r.chr() + " " + api.board().numberAt(h));
            }

            for (Node n : Board.allNodes()) {
                Harbor harbor = api.board().harborAt(n);
                if (harbor != null && harbor.resource != null)
                    out.println(n + " " + harbor.resource.chr());
            }
            for (Node n : Board.allNodes()) {
                Harbor harbor = api.board().harborAt(n);
                if (harbor != null && harbor.resource == null)
                    out.println(n);
            }

            for (Pair<Player, List<Event>> pair : api.history().getAll()) {
                Player player = pair.first;
                List<Event> events = pair.second;
                int ind = player == null ? -1 : player.color();
                for (Event event : events) {
                    String s = eventString(event);
                    if (ind >= 0)
                        s = ind + " " + s;
                    out.println(s);
                    if (event.type() == EventType.EXCEPTION)
                        return;
                }
            }
        }

        for (Player p : api.players()) {
            int vp = p.developments().victoryPoint();
            out.print(api.points(p) + vp);
            if (vp > 0)
                out.print(" " + vp + "VP");
            if (api.largestArmy() == p && api.armyStrength(p) >= Game.MINIMUM_ARMY_STRENGTH)
                out.print(" ARMY");
            if (api.longestRoad() == p && api.roadLength(p) >= Game.MINIMUM_ROAD_LENGTH)
                out.print(" ROAD");
            out.println();
        }

        out.println(api.history().size() - 1 + " turns");
    }

    private void run(String[] args) {
        try {
            if (args.length == 0) {
                printHelp(System.out);
                return;
            }
            Bot[] bots = null;
            boolean vis = false;
            long randSeed = 0;
            boolean silent = false;

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
                } else if ("-silent".equals(args[i])) {
                    silent = true;
                } else {
                    throw new IllegalArgumentException("Unknown parameter: " + args[i]);
                }
            }

            Game.RunAPI api = game.new RunAPI(vis);
            for (int i = 0; i < bots.length; i++) {
                try {
                    api.addPlayer(new Player(bots[i], i));
                } catch (GameException e) {
                    e.printStackTrace();
                    return;
                }
            }

            if (vis) {
                new Vis(api);
            } else {
                api.play();
                printGameLog(api, System.out, silent);
            }
        }
        catch (java.net.MalformedURLException e) { e.printStackTrace(); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        catch (NoSuchMethodException e) { e.printStackTrace(); }
        catch (InstantiationException e) { e.printStackTrace(); }
        catch (IllegalAccessException e) { e.printStackTrace(); }
        catch (java.lang.reflect.InvocationTargetException e) { e.printStackTrace(); }
    }

    private void printHelp(PrintStream out) {
        out.println("Settlers Online testing system (c) Alexander Udalov 2011-2012");
        out.println("http://settlersonline.org");
        out.println("");
        out.println("Usage:");
        out.println("  java -jar Settlers.jar [-seed <seed>] [-vis] [-silent] -3|-4");
        out.println("                         <botname1> <botname2> <botname3> [<botname4>]");
        out.println("");
        out.println("Arguments:");
        out.println("  -3/-4         - 3 or 4 players");
        out.println("  -vis          - enable visualization mode");
        out.println("  -seed <seed>  - specify random seed used by Game (0 means random)");
        out.println("  -silent       - print only summary of the game");
        out.println("  <botname>     - either Example or Stupid or path to your bot:");
        out.println("                  sample/bin/SampleBot.jar:smartasses.SampleBot");
        out.println("");
        out.println("Example of running a game between 3 example bots:");
        out.println("  java -jar Settlers.jar -3 Example Example Example");
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

}

