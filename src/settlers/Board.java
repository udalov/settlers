package settlers;

import java.util.*;

public class Board {

    private static final int[] DX = {1, -1, -2, -1, 1, 2};
    private static final int[] DY = {1, 1, 0, -1, -1, 0};

    private static final int[] ALL_NUMBERS = {
        5,2,6,3,8,10,9,12,11,4,8,10,9,4,5,6,3,11
    };

    private final Map<Hex, Resource> resources;
    private final Map<Hex, Integer> numbers;
    private final Map<Edge, Harbor> harbors;


    private Board(
        Map<Hex, Resource> resources,
        Map<Hex, Integer> numbers,
        Map<Edge, Harbor> harbors
    ) {
        this.resources = resources;
        this.numbers = numbers;
        this.harbors = harbors;
    }

    static Board create(Random rnd) {
        Map<Hex, Resource> resources = new HashMap<Hex, Resource>();
        Map<Hex, Integer> numbers = new HashMap<Hex, Integer>();
        Map<Edge, Harbor> harbors = new HashMap<Edge, Harbor>();

        List<Resource> allResources = new ArrayList<Resource>(ALL_NUMBERS.length + 1);
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
            if (allResources.get(i) != null)
                resources.put(allHexes.get(i), allResources.get(i));

        final int[] startX = new int[] {0,2,6,8,6,2};
        final int[] startY = new int[] {2,0,0,2,4,4};
        int d = rnd.nextInt(6);
        int x = startX[d];
        int y = startY[d];
        int p = 0;

        for (int i = 0; i < 12; i++) {
            Hex h = hex(x, y);
            if (resources.get(h) != null)
                numbers.put(h, ALL_NUMBERS[p++]);
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
                numbers.put(h, ALL_NUMBERS[p++]);
            d = (d + 5) % 6;
            x += DX[d];
            y += DY[d];
        }

        Hex h = hex(4, 2);
        if (resources.get(h) != null)
            numbers.put(h, ALL_NUMBERS[p]);

        generatePorts(rnd, harbors);

        return new Board(resources, numbers, harbors);
    }

    private static void generatePorts(Random rnd, Map<Edge, Harbor> harbors) {
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
                harbors.put(coast.get(ptr + 2), new Harbor(Resource.LUMBER));
            } else if (i == 1) {
                harbors.put(coast.get(ptr + 1), new Harbor(Resource.BRICK));
                harbors.put(coast.get(ptr + 4), new Harbor(null));
            } else if (i == 2) {
                harbors.put(coast.get(ptr + 2), new Harbor(null));
            } else if (i == 3) {
                harbors.put(coast.get(ptr + 1), new Harbor(Resource.WOOL));
                harbors.put(coast.get(ptr + 4), new Harbor(null));
            } else if (i == 4) {
                harbors.put(coast.get(ptr + 2), new Harbor(Resource.ORE));
            } else if (i == 5) {
                harbors.put(coast.get(ptr + 1), new Harbor(Resource.GRAIN));
                harbors.put(coast.get(ptr + 4), new Harbor(null));
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

    public Harbor harborAt(Node n) {
        for (Edge e : harbors.keySet()) {
            Node[] u = endpoints(e);
            if (u[0] == n || u[1] == n)
                return harbors.get(e);
        }
        return null;
    }








    private static final Hex[] hexes = new Hex[128];
    private static final Edge[] edges = new Edge[1024];
    private static final Node[] nodes = new Node[1024];
    private static final List<Hex> allHexes = new ArrayList<Hex>(19);
    private static final List<Edge> allEdges = new ArrayList<Edge>(72);
    private static final List<Node> allNodes = new ArrayList<Node>(54);

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
                    Node z = null;
                    for (int i = 0; i < 3; i++)
                        if (c[i] != null)
                            z = new Node(c[i], 4 - 2*i + it);
                    if (z == null) continue;
                    for (int i = 0; i < 3; i++)
                        if (c[i] != null)
                            nodes[enc(c[i].x, c[i].y, 4 - 2*i + it)] = z;
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

        Set<Node> allNodes = new HashSet<Node>();
        for (Node x : nodes)
            if (x != null)
                allNodes.add(x);
        Board.allNodes.addAll(allNodes);
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
        int ind = enc(hex.x, hex.y, direction);
        if (ind < 0 || ind >= edges.length || edges[ind] == null)
            return null;
        return edges[ind];
    }

    public static Node node(Hex hex, int direction) {
        if (hex == null)
            return null;
        int ind = enc(hex.x, hex.y, direction);
        if (ind < 0 || ind >= nodes.length || nodes[ind] == null)
            return null;
        return nodes[ind];
    }


    public static List<Hex> allHexes() {
        return new ArrayList<Hex>(allHexes);
    }

    public static List<Edge> allEdges() {
        return new ArrayList<Edge>(allEdges);
    }

    public static List<Node> allNodes() {
        return new ArrayList<Node>(allNodes);
    }




    public static List<Hex> adjacentHexes(Node a) {
        List<Hex> ans = new ArrayList<Hex>(3);
        if (a == null)
            return ans;
        for (Hex hex : allHexes())
            for (int d = 0; d < 6; d++)
                if (node(hex, d) == a)
                    ans.add(hex);
        return ans;
    }

    public static List<Hex> adjacentHexes(Hex c) {
        List<Hex> ans = new ArrayList<Hex>(6);
        if (c == null)
            return ans;
        for (int d = 0; d < 6; d++) {
            Hex h = hex(c.x + DX[d], c.y + DY[d]);
            if (h != null)
                ans.add(h);
        }
        return ans;
    }

    public static List<Edge> adjacentEdges(Node a) {
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
        Node[] x = endpoints(p);
        for (Edge edge : allEdges()) {
            if (edge == p)
                continue;
            Node[] y = endpoints(edge);
            if (areAdjacent(x[0], y[0])
             || areAdjacent(x[0], y[1])
             || areAdjacent(x[1], y[0])
             || areAdjacent(x[1], y[1]))
                ans.add(edge);
        }
        return ans;
    }

    public static List<Node> adjacentNodes(Node a) {
        List<Node> ans = new ArrayList<Node>(3);
        if (a == null)
            return ans;
        for (Node b : allNodes())
            if (areAdjacent(a, b))
                ans.add(b);
        return ans;
    }

    public static List<Node> adjacentNodes(Hex c) {
        List<Node> ans = new ArrayList<Node>(6);
        if (c == null)
            return ans;
        for (int d = 0; d < 6; d++)
            ans.add(node(c, d));
        return ans;
    }

    public static Node[] endpoints(Edge p) {
        return p == null ? new Node[] {} : new Node[] {
            node(p.hex, p.direction),
            node(p.hex, (p.direction + 1) % 6),
        };
    }

    public static boolean areAdjacent(Node a, Node b) {
        for (Hex c : allHexes()) {
            for (int d = 0; d < 6; d++) {
                if (node(c, d) == a && node(c, (d + 1) % 6) == b)
                    return true;
                if (node(c, d) == b && node(c, (d + 1) % 6) == a)
                    return true;
            }
        }
        return false;
    }

    public static boolean areAdjacent(Node a, Edge p) {
        if (a == null || p == null)
            return false;
        Node[] ends = endpoints(p);
        return ends[0] == a || ends[1] == a;
    }
}

