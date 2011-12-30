package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import settlers.util.Pair;

public class Board {

    private static final int[] DX = {1, -1, -2, -1, 1, 2};
    private static final int[] DY = {1, 1, 0, -1, -1, 0};
    
    private final Map<Hex, Resource> resources;
    private final Map<Hex, Integer> numbers;
    private final Map<Edge, Resource> ports2to1;
    private final List<Edge> ports3to1;


    private Board(
        Map<Hex, Resource> resources,
        Map<Hex, Integer> numbers,
        Map<Edge, Resource> ports2to1,
        List<Edge> ports3to1
    ) {
        this.resources = resources;
        this.numbers = numbers;
        this.ports2to1 = ports2to1;
        this.ports3to1 = ports3to1;
    }

    static Board create(Random rnd) {
        Map<Hex, Resource> resources = new HashMap<Hex, Resource>();
        Map<Hex, Integer> numbers = new HashMap<Hex, Integer>();
        Map<Edge, Resource> ports2to1 = new HashMap<Edge, Resource>();
        List<Edge> ports3to1 = new ArrayList<Edge>();

        List<Resource> allResources = new ArrayList<Resource>();
        for (int i = 0; i < 4; i++) {
            allResources.add(Resource.WOOL);
            allResources.add(Resource.GRAIN);
            allResources.add(Resource.LUMBER);
            if (i < 3) {
                allResources.add(Resource.BRICK);
                allResources.add(Resource.ORE);
            }
        }
        allResources.add(null); // desert
        Collections.shuffle(allResources, rnd);

        for (int i = 0, n = allHexes.size(); i < n; i++)
            resources.put(allHexes.get(i), allResources.get(i));

        final int[] allNumbers = new int[] {
          5,2,6,3,8,10,9,12,11,4,8,10,9,4,5,6,3,11
        };

        final int[] startX = new int[] {0,2,6,8,6,2};
        final int[] startY = new int[] {2,0,0,2,4,4};
        int d = rnd.nextInt(6);
        int x = startX[d];
        int y = startY[d];
        int p = 0;

        for (int i = 0; i < 12; i++) {
            Hex h = hex(x, y);
            if (resources.get(h) != null)
                numbers.put(h, allNumbers[p++]);
            if (hex(x + DX[d], y + DY[d]) == null)
                d = (d + 5) % 6;
            x += DX[d];
            y += DY[d];
        }
        x += (4 - x) / 2;
        y += (2 - y) / 2;

        for (int i = 0; i < 6; i++) {
            Hex h = hex(x, y);
            if (resources.get(h) != null)
                numbers.put(h, allNumbers[p++]);
            d = (d + 5) % 6;
            x += DX[d];
            y += DY[d];
        }

        Hex h = hex(4,2);
        if (resources.get(h) != null)
            numbers.put(h, allNumbers[p]);

        generatePorts(rnd, ports2to1, ports3to1);

        return new Board(resources, numbers, ports2to1, ports3to1);
    }

    private static void generatePorts(Random rnd, Map<Edge, Resource> ports2to1, List<Edge> ports3to1) {
        List<Edge> coast = new ArrayList<Edge>();
        int d = 0, x = 8, y = 2;
        for (int i = 0; i < 6; i++) {
            int nextd = (d + 1) % 6;
            int prevd = (d + 5) % 6;
            for (int j = 0; j < 2; j++) {
                coast.add(edge(hex(x, y), d));
                x += DX[nextd]; y += DY[nextd];
                coast.add(edge(hex(x, y), prevd));
            }
            coast.add(edge(hex(x, y), d));
            d = nextd;
        }

        List<Integer> permutation = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++)
            permutation.add(i);
        Collections.shuffle(permutation, rnd);

        // ports:
        // 0 --L--
        // 1 -B--3
        // 2 --3--
        // 3 -W--3
        // 4 --O--
        // 5 -G--3

        int ptr = 0;
        for (int ind = 0; ind < 6; ind++) {
            int i = permutation.get(ind);
            if (i == 0) {
                ports2to1.put(coast.get(ptr + 2), Resource.LUMBER);
            } else if (i == 1) {
                ports2to1.put(coast.get(ptr + 1), Resource.BRICK);
                ports3to1.add(coast.get(ptr + 4));
            } else if (i == 2) {
                ports3to1.add(coast.get(ptr + 2));
            } else if (i == 3) {
                ports2to1.put(coast.get(ptr + 1), Resource.WOOL);
                ports3to1.add(coast.get(ptr + 4));
            } else if (i == 4) {
                ports2to1.put(coast.get(ptr + 2), Resource.ORE);
            } else if (i == 5) {
                ports2to1.put(coast.get(ptr + 1), Resource.GRAIN);
                ports3to1.add(coast.get(ptr + 4));
            }
            ptr += 5;
        }

    }



    public Resource resourceAt(Hex hex) {
        return resources.get(hex);
    }

    public int numberAt(Hex hex) {
        Integer i = numbers.get(hex);
        return i == null ? 0 : i;
    }

    public Pair<Boolean, Resource> portAt(Xing i) {
        for (Edge p : ports2to1.keySet()) {
            Xing[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, ports2to1.get(p));
        }
        for (Edge p : ports3to1) {
            Xing[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, null);
        }
        return Pair.make(false, null);
    }








    private static final Hex[] hexes = new Hex[128];
    private static final Edge[] edges = new Edge[1024];
    private static final Xing[] xings = new Xing[1024];
    private static final List<Hex> allHexes = new ArrayList<Hex>();
    private static final List<Edge> allEdges = new ArrayList<Edge>();
    private static final List<Xing> allXings = new ArrayList<Xing>();

    private static int enc(int x, int y) { return (x << 3) + y; }
    private static int enc(int x, int y, int d) { return (d << 7) + enc(x, y); }

    static {
        for (int y = 0; y <= 4; y++)
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2)
                hexes[enc(x, y)] = new Hex(x, y);

        for (int y = 0; y <= 4; y++)
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2)
                for (int d = 0; d < 3; d++)
                    edges[enc(x, y, d)] = new Edge(hexes[enc(x, y)], d);

        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                if (x + y == 2 || y == 0)
                    edges[enc(x, y, 3)] = new Edge(hexes[enc(x, y)], 3);
                else
                    edges[enc(x, y, 3)] = edges[enc(x - 1, y - 1, 0)];
                if (y == 0 || x - y == 6)
                    edges[enc(x, y, 4)] = new Edge(hexes[enc(x, y)], 4);
                else
                    edges[enc(x, y, 4)] = edges[enc(x + 1, y - 1, 1)];
                if (x - y == 6 || x + y == 10)
                    edges[enc(x, y, 5)] = new Edge(hexes[enc(x, y)], 5);
                else
                    edges[enc(x, y, 5)] = edges[enc(x + 2, y, 2)];
            }
        }
        
        for (int y = -1; y <= 5; y++) {
            for (int x = -2; x <= 9; x++) {
                if ((x + y) % 2 == 1)
                    continue;
                for (int it = 0; it < 2; it++) {
                    Hex[] c = new Hex[3];
                    if (it == 0) {
                        c[0] = hex(x + 1, y + 1);
                        c[1] = hex(x + 2, y);
                        c[2] = hex(x, y);
                    } else {
                        c[0] = hex(x, y);
                        c[1] = hex(x + 2, y);
                        c[2] = hex(x + 1, y - 1);
                    }
                    Xing z = null;
                    for (int i = 0; i < 3; i++)
                        if (c[i] != null)
                            z = new Xing(c[i], 4 - 2*i + it);
                    if (z == null) continue;
                    for (int i = 0; i < 3; i++)
                        if (c[i] != null)
                            xings[enc(c[i].x(), c[i].y(), 4 - 2*i + it)] = z;
                }
            }
        }

        Set<Hex> allHexes = new HashSet<Hex>();
        for (Hex h : hexes)
            if (h != null)
                allHexes.add(h);
        Board.allHexes.addAll(allHexes);

        Set<Edge> allEdges = new HashSet<Edge>();
        for (Edge p : edges)
            if (p != null)
                allEdges.add(p);
        Board.allEdges.addAll(allEdges);

        Set<Xing> allXings = new HashSet<Xing>();
        for (Xing x : xings)
            if (x != null)
                allXings.add(x);
        Board.allXings.addAll(allXings);
    }

    public static Hex hex(int x, int y) {
        int ind = enc(x, y);
        if (ind < 0 || ind >= hexes.length || hexes[ind] == null)
            return null;
        return hexes[ind];
    }

    public static Edge edge(Hex hex, int direction) {
        if (hex == null)
            return null;
        int ind = enc(hex.x(), hex.y(), direction);
        if (ind < 0 || ind >= edges.length || edges[ind] == null)
            return null;
        return edges[ind];
    }

    public static Xing xing(Hex hex, int direction) {
        if (hex == null)
            return null;
        int ind = enc(hex.x(), hex.y(), direction);
        if (ind < 0 || ind >= xings.length || xings[ind] == null)
            return null;
        return xings[ind];
    }


    public static List<Hex> allHexes() {
        return new ArrayList<Hex>(allHexes);
    }

    public static List<Edge> allEdges() {
        return new ArrayList<Edge>(allEdges);
    }

    public static List<Xing> allXings() {
        return new ArrayList<Xing>(allXings);
    }




    public static List<Hex> adjacentHexes(Xing a) {
        List<Hex> ans = new ArrayList<Hex>(3);
        if (a == null)
            return ans;
        for (Hex hex : allHexes())
            for (int d = 0; d < 6; d++)
                if (xing(hex, d) == a)
                    ans.add(hex);
        return ans;
    }

    public static List<Hex> adjacentHexes(Hex c) {
        List<Hex> ans = new ArrayList<Hex>(6);
        if (c == null)
            return ans;
        for (int d = 0; d < 6; d++) {
            Hex h = hex(c.x() + DX[d], c.y() + DY[d]);
            if (h != null)
                ans.add(h);
        }
        return ans;
    }

    public static List<Edge> adjacentEdges(Xing a) {
        List<Edge> ans = new ArrayList<Edge>(3);
        if (a == null)
            return ans;
        for (Edge edge : allEdges())
            if (areAdjacent(a, edge))
                ans.add(edge);
        return ans;
    }

    public static List<Edge> adjacentEdges(Edge p) {
        List<Edge> ans = new ArrayList<Edge>(4);
        if (p == null)
            return ans;
        Xing[] x = endpoints(p);
        for (Edge edge : allEdges()) {
            if (edge == p)
                continue;
            Xing[] y = endpoints(edge);
            if (areAdjacent(x[0], y[0])
             || areAdjacent(x[0], y[1])
             || areAdjacent(x[1], y[0])
             || areAdjacent(x[1], y[1]))
                ans.add(edge);
        }
        return ans;
    }

    public static List<Xing> adjacentXings(Xing a) {
        List<Xing> ans = new ArrayList<Xing>(3);
        if (a == null)
            return ans;
        for (Xing b : allXings())
            if (areAdjacent(a, b))
                ans.add(b);
        return ans;
    }

    public static List<Xing> adjacentXings(Hex c) {
        List<Xing> ans = new ArrayList<Xing>(6);
        if (c == null)
            return ans;
        for (int d = 0; d < 6; d++)
            ans.add(xing(c, d));
        return ans;
    }

    public static Xing[] endpoints(Edge p) {
        return p == null ? new Xing[] {} : new Xing[] {
            xing(p.hex(), p.direction()),
            xing(p.hex(), (p.direction() + 1) % 6),
        };
    }

    public static boolean areAdjacent(Xing a, Xing b) {
        for (Hex c : allHexes()) {
            for (int d = 0; d < 6; d++) {
                if (xing(c, d) == a && xing(c, (d + 1) % 6) == b)
                    return true;
                if (xing(c, d) == b && xing(c, (d + 1) % 6) == a)
                    return true;
            }
        }
        return false;
    }

    public static boolean areAdjacent(Xing a, Edge p) {
        if (a == null || p == null)
            return false;
        Xing[] ends = endpoints(p);
        return ends[0] == a || ends[1] == a;
    }
}

