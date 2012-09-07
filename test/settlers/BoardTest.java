package settlers;

import settlers.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoardTest extends SettlersTestCase {

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
}
