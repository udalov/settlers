package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settlers.*;

public class Vis extends JFrame implements WindowListener, MouseListener, ComponentListener {
    
    public static final int BOARD_WIDTH = 1020;
    public static final int BOARD_HEIGHT = 740;

    private final BoardVis board;
    private final JMenuBar menuBar;

    public Vis(Game game) {
        setLayout(null);

        menuBar = new JMenuBar();
        JMenu menu = new JMenu("testmenu");
        JMenuItem item = new JMenuItem("testitem");
        menu.add(item);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        JButton b = new JButton("testbutton");
        b.setBounds(100, 100, 100, 40);
        getContentPane().add(b);

        board = new BoardVis(game);
        board.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        getContentPane().add(board);

        addWindowListener(this);
        addMouseListener(this);
        addComponentListener(this);

        pack();

        Insets insets = getInsets();
        int width = BOARD_WIDTH + insets.left + insets.right;
        int height = BOARD_HEIGHT + insets.top + insets.bottom + menuBar.getSize().height;
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
        Insets insets = getInsets();
        board.setSize(
            getSize().width - insets.left - insets.right,
            getSize().height - insets.top - insets.bottom - menuBar.getSize().height
        );
    }
    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
}

