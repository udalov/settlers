package settlers.vis;

import settlers.*;
import settlers.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

public class Animator {
    
    private static final int SLEEP_INTERVAL = 10;
    
    private class Handle {
        private final Animation animation;
        private final int length;
        private int next = 0;
        private Handle(Animation animation, int length) {
            this.animation = animation;
            this.length = length;
        }
    }
    
    private final List<Handle> handles = new ArrayList<Handle>();

    Animator(final Vis vis) {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    // TODO: concurrent modification
                    for (Iterator<Handle> it = handles.iterator(); it.hasNext(); ) {
                        final Handle handle = it.next();
                        final double step = handle.next * 1. / handle.length;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                handle.animation.setStep(step);
                                vis.repaint();
                            }
                        });
                        if (++handle.next == handle.length)
                            it.remove();
                    }
                    try {
                        Thread.sleep(SLEEP_INTERVAL);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    void add(int duration, Animation animation) {
        handles.add(new Handle(animation, duration / SLEEP_INTERVAL));
    }
}

