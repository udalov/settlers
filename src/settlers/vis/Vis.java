package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import settlers.*;
import settlers.util.*;

public class Vis extends JPanel implements WindowListener {

    public static final int X0_POS = 200;
    public static final int Y0_POS = 500;
    public static final int HEX_SIZE = 54;
    public static final int TOWN_RADIUS = 8;
    public static final int PORT_DISTANCE = 6;

    public static final int[] DX = new int[] {2, 0, -2, -2, 0, 2};
    public static final int[] DY = new int[] {-1, -2, -1, 1, 2, 1};

    // TODO: generate
    public static final String[] PORTS = new String[] {
        "820,821,730,731,640",
        "641,440,441,240,241",
        "242,131,132,021,022",
        "023,112,113,202,203",
        "204,403,404,603,604",
        "605,600,715,710,825"
    };

    private final Game game;
    private final Board board;
    private final Map<Board.Cell, Point> hex;
    private final Map<Board.Path, Polygon> path;

    public Vis(Game game) {
        this.game = game;
        this.board = game.board();
        this.hex = new HashMap<Board.Cell, Point>();
        for (Board.Cell c : Board.allCells()) {
            hex.put(c, new Point(X0_POS + c.x() * HEX_SIZE, Y0_POS - c.y() * HEX_SIZE * 3 / 2));
        }
        this.path = new HashMap<Board.Path, Polygon>();
        Board.allPaths();
    }

    int[][] calcHexagonVertices(int x, int y) {
        int[][] ans = new int[2][6];
        for (int d = 0; d < 6; d++) {
            ans[0][d] = x + HEX_SIZE * DX[d] / 2;
            ans[1][d] = y + HEX_SIZE * DY[d] / 2;
        }
        return ans;
    }

    Polygon createHexagon(Point z) {
        int[][] v = calcHexagonVertices(z.x, z.y);
        return new Polygon(v[0], v[1], 6);
    }

    Point intsCoords(Board.Intersection i) {
        Point z = hex.get(i.cell());
        int[][] v = calcHexagonVertices(z.x, z.y);
        int d = i.direction();
        return new Point(v[0][d], v[1][d]);
    }

    Point[] pathCoords(Board.Path p) {
        Point z = hex.get(p.cell());
        int[][] v = calcHexagonVertices(z.x, z.y);
        int d = p.direction(), nd = (d + 1) % 6;
        return new Point[]{
            new Point(v[0][d], v[1][d]),
            new Point(v[0][nd], v[1][nd])
        };
    }

    void drawCell(Graphics2D g, Board.Cell c) {
        Point z = hex.get(c);
        Polygon p = createHexagon(z);
        Color color = resourceToColor(board.resourceAt(c));
        g.setColor(color);
        g.fillPolygon(p);
        int number = board.numberAt(c);
        if (number != 0) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Tahoma",
                number == 6 || number == 8 ? Font.BOLD : Font.PLAIN, 36));
            String str = number + "";
            if (board.robber() == c)
                str = "[" + str + "]";
            g.drawString(str, z.x - g.getFontMetrics().stringWidth(str) / 2, z.y + 13);
        }
    }

    void drawPath(Graphics2D g, Board.Path p) {
        Player pl = board.roadAt(p);
        if (pl == null) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(playerColorToColor(pl.color()));
            g.setStroke(new BasicStroke(5));
        }
        Point[] z = pathCoords(p);
        g.drawLine(z[0].x, z[0].y, z[1].x, z[1].y);
    }

    void drawIntersection(Graphics2D g, Board.Intersection i) {
        Town t = board.townAt(i);
        if (t == null)
            return;
        g.setColor(playerColorToColor(t.player().color()));
        Point p = intsCoords(i);
        if (t.isCity()) {
            g.fillRect(p.x - TOWN_RADIUS, p.y - TOWN_RADIUS, 2 * TOWN_RADIUS, 2 * TOWN_RADIUS);
        } else {
            g.fillOval(p.x - TOWN_RADIUS, p.y - TOWN_RADIUS, 2 * TOWN_RADIUS, 2 * TOWN_RADIUS);
        }
    }

    void drawPort(Graphics2D g, Board.Intersection i) {
        Pair<Boolean, Resource> br = board.portAt(i);
        if (!br.first())
            return;
        Resource r = br.second();
        int d = -1;
        for (int j = 0; j < 6; j++) {
            if (PORTS[j].indexOf("" + i.x() + i.y() + i.direction()) >= 0) {
                d = j;
                break;
            }
        }
        if (d < 0)
            throw new IllegalStateException("Internal: invalid port location");
        Point p = intsCoords(i);
        String str = resourceToPort(r);
        g.setFont(new Font("Tahoma", Font.BOLD, 10));
        Color c = r == null ? new Color(0x8888FF) : resourceToColor(r);
        if (r == Resource.LUMBER || r == Resource.BRICK)
            c = c.brighter().brighter();
        g.setColor(c);
        g.drawString(str,
            p.x + PORT_DISTANCE * DX[d] - g.getFontMetrics().stringWidth(str) / 2,
            p.y + PORT_DISTANCE * DY[d] + 5
        );
    }

    String resourceToPort(Resource r) {
        return r == null ? "3:1" : r.toString().substring(0, 1);
    }

    Color resourceToColor(Resource r) {
        if (r == null)
            return new Color(0x778822);
        switch (r) {
            case BRICK: return new Color(0x550000);
            case WOOL: return new Color(0x00FF00);
            case ORE: return new Color(0x999999);
            case GRAIN: return new Color(0xAAAA00);
            case LUMBER: return new Color(0x004400);
            default: return Color.WHITE;
        }
    }

    Color playerColorToColor(Player.Color c) {
        switch (c) {
            case BLUE: return new Color(0x0000FF);
            case WHITE: return new Color(0xFFFFFF);
            case ORANGE: return new Color(0xFF9900);
            case RED: return new Color(0xFF0000);
            default: return Color.WHITE;
        }
    }

    public void paint(Graphics gg) {
        BufferedImage bi = new BufferedImage(1002, 824, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();

        for (Board.Cell c : Board.allCells()) {
            drawCell(g, c);
        }
        for (Board.Path p : Board.allPaths()) {
            if (board.roadAt(p) == null)
                drawPath(g, p);
        }
        for (Board.Path p : Board.allPaths()) {
            if (board.roadAt(p) != null)
                drawPath(g, p);
        }
        for (Board.Intersection i : Board.allIntersections()) {
            drawIntersection(g, i);
        }
        for (Board.Intersection i : Board.allIntersections()) {
            drawPort(g, i);
        }

        gg.drawImage(bi, 1, 1, 1000, 800, null);
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
}

