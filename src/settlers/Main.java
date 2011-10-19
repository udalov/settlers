package settlers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import javax.swing.JFrame;
import settlers.*;
import settlers.bot.Bot;
import settlers.bot.ExampleBot;
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
                        } else {
                            int colon = bot.indexOf(':');
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
                }
            }

            for (int i = 0; i < bots.length; i++) {
                game.addPlayer(new Player(bots[i], Player.Color.values()[i]));
            }

            game.play();

            JFrame jf = new JFrame();
            Vis v = new Vis(game.board());
            jf.getContentPane().add(v);
            jf.addWindowListener(v);
            jf.setSize(1002, 824);
            jf.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

