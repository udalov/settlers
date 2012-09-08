package settlers;

import settlers.util.Pair;

import java.util.*;

public class BoardTest extends SettlersTestCase {

    private static final int[] RAND_SEEDS;

    static {
        RAND_SEEDS = new int[1000];
        for (int i = 0; i < 1000; i++)
            RAND_SEEDS[i] = i + 1;
    }

    public void testAllHexes() {
        List<Hex> hexes = Board.allHexes();
        List<TestHex> expectedHexes = prepareExpectedHexes();

        assertEquals(expectedHexes.size(), hexes.size());

        List<TestHex> notFound = new ArrayList<TestHex>();
        for (TestHex hex : expectedHexes)
            if (!containsHex(hex, hexes))
                notFound.add(hex);

        assertTrue("Not found: " + notFound, notFound.isEmpty());
    }

    public void testAllEdges() {
        doTestAllEdgesOrNodes(Board.allEdges(), "test/data/edges.txt", true);
    }

    public void testAllNodes() {
        doTestAllEdgesOrNodes(Board.allNodes(), "test/data/nodes.txt", false);
    }

    public void testCreateWithSameRandSeed() {
        for (int randSeed : RAND_SEEDS) {
            TestBoard board = TestBoard.create(new Random(randSeed));
            for (int i = 0; i < 10; i++)
                assertEquals(board, TestBoard.create(new Random(randSeed)));
        }
    }

    public void testCreateWithDifferentRandSeeds() {
        Set<TestBoard> boards = new HashSet<TestBoard>();
        for (int randSeed : RAND_SEEDS) {
            TestBoard board = TestBoard.create(new Random(randSeed));
            assertFalse(boards.contains(board));
            boards.add(board);
        }
        assertEquals(RAND_SEEDS.length, boards.size());
    }

    public void testBoardNumbers() {
        for (int randSeed : RAND_SEEDS) {
            TestBoard board = TestBoard.create(new Random(randSeed));
            Map<Hex, Integer> numbers = board.numbers;

            // -1 for desert
            assertEquals(Board.allHexes().size() - 1, numbers.size());

            int[] numberCount = new int[13];
            for (Hex hex : numbers.keySet()) {
                int mine = numbers.get(hex);
                for (Hex adjacent : Board.adjacentHexes(hex)) {
                    if (!numbers.containsKey(adjacent))
                        continue;
                    int other = numbers.get(adjacent);
                    assertFalse(mine == other);
                    assertFalse((mine == 6 || mine == 8) && (other == 6 || other == 8));
                }

                numberCount[mine]++;
            }

            assertEquals(0, numberCount[0]);
            assertEquals(0, numberCount[1]);
            assertEquals(1, numberCount[2]);
            assertEquals(0, numberCount[7]);
            assertEquals(1, numberCount[12]);
            for (int i = 3; i <= 11; i++)
                if (i != 7)
                    assertEquals(2, numberCount[i]);
        }
    }

    public void testBoardResources() {
        for (int randSeed : RAND_SEEDS) {
            TestBoard board = TestBoard.create(new Random(randSeed));
            Map<Hex, Resource> resources = board.resources;

            // -1 for desert
            assertEquals(Board.allHexes().size() - 1, resources.size());

            int[] resourceCount = new int[Resource.values().length];
            for (Resource resource : resources.values())
                resourceCount[resource.ordinal()]++;

            assertEquals(3, resourceCount[Resource.BRICK.ordinal()]);
            assertEquals(4, resourceCount[Resource.WOOL.ordinal()]);
            assertEquals(3, resourceCount[Resource.ORE.ordinal()]);
            assertEquals(4, resourceCount[Resource.GRAIN.ordinal()]);
            assertEquals(4, resourceCount[Resource.LUMBER.ordinal()]);
        }
    }



    private void doTestAllEdgesOrNodes(List<?> allEdgesOrNodes, String filename, boolean edges) {
        List<TestEdgeOrNode> expectedEdgesOrNodes = prepareExpectedEdgesOrNodes(filename);

        assertEquals(expectedEdgesOrNodes.size(), allEdgesOrNodes.size());

        for (TestEdgeOrNode eon : expectedEdgesOrNodes)
            eon.assertMapsToTheSameElement(edges);

        List<TestEdgeOrNode> notFound = new ArrayList<TestEdgeOrNode>();
        for (TestEdgeOrNode eon : expectedEdgesOrNodes)
            if (!containsEdgeOrNode(eon, allEdgesOrNodes))
                notFound.add(eon);

        assertTrue("Not found: " + notFound, notFound.isEmpty());
    }

    private static List<TestHex> prepareExpectedHexes() {
        List<TestHex> result = new ArrayList<TestHex>();
        for (String line : TestUtil.readNonEmptyLines("test/data/hexes.txt")) {
            String[] xy = line.split(" ");
            assertEquals(2, xy.length);
            result.add(new TestHex(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }
        return result;
    }

    private static List<TestEdgeOrNode> prepareExpectedEdgesOrNodes(String filename) {
        List<TestEdgeOrNode> result = new ArrayList<TestEdgeOrNode>();
        for (String line : TestUtil.readNonEmptyLines(filename)) {
            String[] edgesOrNodes = line.split("  +");
            int n = edgesOrNodes.length;
            assertTrue(0 < n && n <= 3);
            List<Pair<TestHex, Integer>> eons = new ArrayList<Pair<TestHex, Integer>>();
            for (String edge : edgesOrNodes) {
                String[] xyd = edge.split(" ");
                assertEquals(3, xyd.length);
                int x = Integer.parseInt(xyd[0]);
                int y = Integer.parseInt(xyd[1]);
                int d = Integer.parseInt(xyd[2]);
                eons.add(Pair.make(new TestHex(x, y), d));
            }
            result.add(new TestEdgeOrNode(eons));
        }
        return result;
    }

    private static boolean containsHex(TestHex needle, List<Hex> haystack) {
        for (Hex hex : haystack)
            if (needle.isEqualTo(hex))
                return true;
        return false;
    }

    private static boolean containsEdgeOrNode(TestEdgeOrNode needle, List<?> haystack) {
        for (Object edgeOrNode : haystack)
            if (needle.isEqualTo(edgeOrNode))
                return true;
        return false;
    }

    private static class TestHex {
        private final int x;
        private final int y;

        private TestHex(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private boolean isEqualTo(Hex hex) {
            return x == hex.x && y == hex.y;
        }
    }

    private static class TestEdgeOrNode {
        private final List<Pair<TestHex, Integer>> eons;

        private TestEdgeOrNode(List<Pair<TestHex, Integer>> eons) {
            this.eons = eons;
        }

        private boolean isEqualTo(Object o) {
            if (o instanceof Edge) {
                Edge edge = (Edge)o;
                for (Pair<TestHex, Integer> eon : eons)
                    if (eon.first.isEqualTo(edge.hex) && eon.second == edge.direction)
                        return true;
            } else if (o instanceof Node) {
                Node node = (Node)o;
                for (Pair<TestHex, Integer> eon : eons)
                    if (eon.first.isEqualTo(node.hex) && eon.second == node.direction)
                        return true;
            } else {
                throw new IllegalStateException("Should be either Edge or Node: " + o);
            }
            return false;
        }

        private void assertMapsToTheSameElement(boolean edges) {
            Set<Object> result = new HashSet<Object>();
            for (Pair<TestHex, Integer> eon : eons) {
                Hex hex = Board.hex(eon.first.x, eon.first.y);
                Object edgeOrNode = edges ? Board.edge(hex, eon.second) : Board.node(hex, eon.second);
                assertNotNull(edgeOrNode);
                result.add(edgeOrNode);
            }
            assertEquals(1, result.size());
        }
    }

    private static class TestBoard {
        private final Map<Hex, Resource> resources;
        private final Map<Hex, Integer> numbers;
        private final Map<Edge, Harbor> harbors;

        private TestBoard(Map<Hex, Resource> resources, Map<Hex, Integer> numbers, Map<Edge, Harbor> harbors) {
            this.resources = resources;
            this.numbers = numbers;
            this.harbors = harbors;
        }

        @SuppressWarnings("unchecked")
        private static TestBoard create(Random rnd) {
            Board board = Board.create(rnd);
            Map<Hex, Resource> resources = (Map)TestUtil.getField(board, Board.class, "resources");
            Map<Hex, Integer> numbers = (Map)TestUtil.getField(board, Board.class, "numbers");
            Map<Edge, Harbor> harbors = (Map)TestUtil.getField(board, Board.class, "harbors");
            return new TestBoard(resources, numbers, harbors);
        }

        public int hashCode() {
            return resources.hashCode() + numbers.hashCode() + harbors.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof TestBoard))
                return false;
            TestBoard b = (TestBoard)o;
            return resources.equals(b.resources) && numbers.equals(b.numbers) && harbors.equals(b.harbors);
        }
    }
}
