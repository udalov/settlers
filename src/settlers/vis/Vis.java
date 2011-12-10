package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settlers.*;

public class Vis extends JFrame implements WindowListener, MouseListener, ComponentListener {
    
    public Vis(Game game) {
        setLayout(null);

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("testmenu");
        JMenuItem item = new JMenuItem("testitem");
        menu.add(item);
        bar.add(menu);
        setJMenuBar(bar);

        JButton b = new JButton("testbutton");
        b.setBounds(100, 100, 100, 40);
        getContentPane().add(b);

        BoardVis v = new BoardVis(game);
        v.setSize(BoardVis.WIDTH, BoardVis.HEIGHT);
        getContentPane().add(v);

        addWindowListener(this);
        addMouseListener(this);
        addComponentListener(this);

        pack();
        Insets insets = getInsets();

        int width = BoardVis.WIDTH + insets.left + insets.right;
        int height = BoardVis.HEIGHT + insets.top + insets.bottom + bar.getSize().height;
        setSize(width, height);
        setMinimumSize(new Dimension(width, height));
        setVisible(true);
    }

    public void windowClosing(WindowEvent e) { 
        System.exit(0); 
    }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }

    public void mouseExited(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) { }

    public void componentResized(ComponentEvent e) {
    }
    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
}

