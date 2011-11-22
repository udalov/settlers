package settlers;

import java.io.File;
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
    
    public static void main(String[] args) {
        try {
            Game game = new Game();
            Bot[] bots = null;
            boolean vis = false;

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
                } else {
                    throw new IllegalArgumentException("Unknown parameter: " + args[i]);
                }
            }

            for (int i = 0; i < bots.length; i++) {
                game.addPlayer(new Player(bots[i], Player.Color.values()[i]));
            }

            try {
                game.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Pair<Player, List<Event>>> history = game.history().getAll();
            for (Pair<Player, List<Event>> pair : history) {
                Player player = pair.first();
                List<Event> events = pair.second();
                if (player != null) {
                    System.out.println("- " + player.color());
                }
                for (Event event : events) {
                    switch (event.type()) {
                        case INITIAL_ROAD:
                            System.out.println(event.player().color() + " road " + event.path());
                            break;
                        case INITIAL_SETTLEMENT:
                            System.out.println(event.player().color() + " settlement " + event.xing());
                            break;
                        case ROLL_DICE:
                            System.out.println("roll " + event.number());
                            break;
                        case ROBBER:
                            System.out.println("robber " + event.hex() + " " + (event.player() == null ? "nobody" : event.player().color()));
                            break;
                        case RESOURCES:
                            // TODO?
                            break;
                        case DISCARD:
                            System.out.println("discard " + event.player().color() + " " + Util.toResourceString(event.resources()));
                            break;
                        case ROAD:
                            System.out.println("road " + event.path());
                            break;
                        case SETTLEMENT:
                            System.out.println("settlement " + event.xing());
                            break;
                        case CITY:
                            System.out.println("city " + event.xing());
                            break;
                        case DEVELOPMENT:
                            System.out.println("development");
                            break;
                        case KNIGHT:
                            System.out.println("knight");
                            break;
                        case INVENTION:
                            System.out.println("invention " + event.resource() + " " + event.resource2());
                            break;
                        case MONOPOLY:
                            System.out.println("monopoly " + event.resource() + " " + event.number());
                            break;
                        case ROAD_BUILDING:
                            System.out.println("roadbuilding " + event.path() + " " + event.path2());
                            break;
                        case LONGEST_ROAD:
                            System.out.println("longestroad " + event.number());
                            break;
                        case LARGEST_ARMY:
                            System.out.println("largestarmy " + event.number());
                            break;
                        case CHANGE:
                            System.out.println("change " + event.sell() + " " + event.buy());
                            break;
                        case TRADE:
                            System.out.println("trade " + event.player().color() + " " + event.sell() + " " + event.buy());
                            break;
                        case VICTORY:
                            System.out.println("victory");
                            for (Player p : game.players()) {
                                System.out.print(p.color());
                                System.out.print(" " + game.points(p));
                                int vp = p.developments().victoryPoint();
                                if (vp > 0)
                                    System.out.print(" " + vp + "VP");
                                if (game.largestArmy() == p && p.armyStrength() >= 3)
                                    System.out.print(" ARMY");
                                if (game.longestRoad() == p && game.roadLength(p) >= 5)
                                    System.out.print(" ROAD");
                                System.out.println();
                            }
                            break;
                    }
                }
            }
            System.out.println(history.size() - 1 + " turns");

            if (vis) {
                JFrame jf = new JFrame();
                Vis v = new Vis(game);
                jf.getContentPane().add(v);
                jf.addWindowListener(v);
                jf.setSize(Vis.WIDTH + 2, Vis.HEIGHT + 24);
                jf.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

