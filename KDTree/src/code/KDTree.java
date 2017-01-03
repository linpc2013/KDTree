package code;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/*
 * KDTree implementation
 * Features :
 * K-Dimension
 * Search : Range Search, Nearest NeighBor Search
 * Insert : SingleNode insert, Points set insert (Split by median using median of medians and presort)
 * Refer to https://en.wikipedia.org/wiki/K-d_tree
 * Author : linpc2013
 *
 * */
// K - Dimension Point
class HyperPoint {
    double[] coords;
    int K = 0;

    public HyperPoint(double[] crds) {
        if (crds == null)
            throw new NullPointerException("");
        K = crds.length;
        coords = new double[K];
        for (int i = 0; i < K; i++)
            coords[i] = crds[i];
    }

    public HyperPoint(HyperPoint p) {
        this(p.coords);
    }

    public boolean equals(HyperPoint p) {
        if (K != p.K)
            throw new IllegalArgumentException("");
        for (int i = 0; i < K; i++)
            if (p.coords[i] != coords[i])
                return false;
        return true;
    }

    // Euclidean Distance
    public double distanceTo(HyperPoint p) {
        return Math.sqrt(squareDistanceTo(p));
    }

    public double squareDistanceTo(HyperPoint p) {
        if (K != p.K)
            throw new IllegalArgumentException("");
        double res = 0;
        for (int i = 0; i < K; i++)
            res += (coords[i] - p.coords[i]) * (coords[i] - p.coords[i]);
        return res;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < K; i++)
            sb.append(coords[i] + " ");
        return sb.toString();
    }
}

// K - Dimension Space

class HyperSpace {
    HyperPoint min, max;
    int K = 0;

    public HyperSpace(HyperPoint min, HyperPoint max) {
        if (min == null || max == null)
            throw new NullPointerException("");
        K = min.K;
        if (K == 0 || K != max.K)
            throw new IllegalArgumentException("");
        this.min = new HyperPoint(min);
        this.max = new HyperPoint(max);
    }

    // Detect whether intersects with other HyperSpace or not
    public boolean intersects(HyperSpace p) {
        for (int i = 0; i < K; i++)
            if (min.coords[i] > p.max.coords[i] || max.coords[i] < p.min.coords[i])
                return false;
        return true;
    }

    public boolean contains(HyperPoint p) {
        if (K != p.K)
            throw new IllegalArgumentException("");
        for (int i = 0; i < K; i++)
            if (min.coords[i] > p.coords[i] || p.coords[i] > max.coords[i])
                return false;
        return true;
    }

    // The square of Euclidean Distance
    public double squareDistanceTo(HyperPoint p) {
        if (K != p.K)
            throw new IllegalArgumentException("");
        double res = 0;
        for (int i = 0; i < K; i++)
            if (min.coords[i] > p.coords[i])
                res += (min.coords[i] - p.coords[i]) * (min.coords[i] - p.coords[i]);
            else if (p.coords[i] > max.coords[i])
                res += (p.coords[i] - max.coords[i]) * (p.coords[i] - max.coords[i]);
        return res;
    }

    // Euclidean Distance
    public double distanceTo(HyperPoint p) {
        return Math.sqrt(squareDistanceTo(p));
    }

    public String toString() {
        return min.toString() + "->" + max.toString();
    }
}

public class KDTree {
    class Node {
        // HyperSpace hs is used to accelerate range search
        HyperSpace hs;
        // Current spliting node
        HyperPoint p;
        Node left, right;

        public Node(HyperSpace hs, HyperPoint p) {
            this.hs = hs;
            this.p = p;
            left = right = null;
        }
    }

    Node root;
    int K = 2;
    double RANGE = 1.0;
    // HyperPoint min, max are determined the range of KDTree Space
    HyperPoint min, max;

    public KDTree(int K) {
        this.K = K;
        root = null;
        double[] vals = new double[K];
        min = new HyperPoint(vals);
        for (int i = 0; i < K; i++)
            vals[i] = RANGE;
        max = new HyperPoint(vals);
    }

    public KDTree(int K, HyperPoint min, HyperPoint max) {
        this.K = K;
        this.min = min;
        this.max = max;
        root = null;
    }

    /*
     * Single Node insertion just like binary search tree but be careful to the
     * cycle of coordinate
     */
    public void insert(HyperPoint p) {
        HyperPoint hmin = new HyperPoint(min);
        HyperPoint hmax = new HyperPoint(max);
        root = insert(root, p, hmin, hmax, 0);
    }

    private Node insert(Node r, HyperPoint p, HyperPoint hmin, HyperPoint hmax, int depth) {
        if (r == null)
            return new Node(new HyperSpace(hmin, hmax), p);
        int k = depth % K;
        double pivot = r.p.coords[k];
        if (p.coords[k] < pivot) {
            hmax.coords[k] = pivot;
            r.left = insert(r.left, p, hmin, hmax, depth + 1);
        } else {
            hmin.coords[k] = pivot;
            r.right = insert(r.right, p, hmin, hmax, depth + 1);
        }
        return r;
    }

    // Presort method
    // Inner class SortComparator is used for presort of points set
    class SortComparator implements Comparator<HyperPoint> {
        int k;

        public void setK(int k) {
            this.k = k;
        }

        @Override
        public int compare(HyperPoint o1, HyperPoint o2) {
            if (o1.coords[k] > o2.coords[k])
                return 1;
            else if (o1.coords[k] == o2.coords[k])
                return 0;
            return -1;
        }
    }

    public void insertByPreSort(HyperPoint[] points) {
        int num = points.length;
        HyperPoint hmin = new HyperPoint(min);
        HyperPoint hmax = new HyperPoint(max);
        // k presort points set
        HyperPoint[][] kpoints = new HyperPoint[K][];
        SortComparator sc = new SortComparator();
        // Presort
        for (int k = 0; k < K; k++) {
            sc.setK(k);
            Arrays.sort(points, sc);
            kpoints[k] = points.clone();
        }
        Vector<HyperPoint> avails = new Vector<HyperPoint>();
        for (int i = 0; i < num; i++)
            avails.add(kpoints[0][i]);
        root = insertByPreSort(root, kpoints, hmin, hmax, 0, avails);
    }

    private Node insertByPreSort(Node r, HyperPoint[][] kpoints, HyperPoint hmin, HyperPoint hmax, int depth, Vector<HyperPoint> avails) {
        int num = avails.size();
        if (num == 0)
            return null;
        else {
            int k = depth % K;
            if (num == 1)
                return new Node(new HyperSpace(hmin, hmax), avails.get(0));
            int mid = (num - 1) / 2;
            if (r == null)
                r = new Node(new HyperSpace(hmin, hmax), avails.get(mid));
            HyperPoint hmid1 = new HyperPoint(hmax);
            hmid1.coords[k] = kpoints[k][mid].coords[k];
            // Splitting current points set
            HashMap<HyperPoint, Integer> split = new HashMap<HyperPoint, Integer>();
            for (int p = 0; p < num; p++)
                if (p < mid)
                    split.put(avails.get(p), 0);
                else if (p > mid)
                    split.put(avails.get(p), 1);
            int k1 = (depth + 1) % K;
            // Generating left and right branch available points set
            Vector<HyperPoint> left = new Vector<HyperPoint>(), right = new Vector<HyperPoint>();
            for (HyperPoint p : kpoints[k1])
                if (split.containsKey(p))
                    if (split.get(p) == 0)
                        left.addElement(p);
                    else
                        right.addElement(p);
            // Recursive Split
            r.left = insertByPreSort(r.left, kpoints, hmin, hmid1, depth + 1, left);
            HyperPoint hmid2 = new HyperPoint(hmin);
            hmid1.coords[k] = kpoints[k][mid].coords[k];
            r.right = insertByPreSort(r.right, kpoints, hmid2, hmax, depth + 1, right);
            return r;
        }
    }
    //

    public void insertByMedianFinding(HyperPoint[] points) {
        int num = points.length;
        HyperPoint hmin = new HyperPoint(min);
        HyperPoint hmax = new HyperPoint(max);
        root = insertByMedianFinding(root, points, hmin, hmax, 0, 0, num - 1);
    }

    // quickSort partition function
    private int partition(HyperPoint[] points, int k, int beg, int end) {
        HyperPoint pivot = points[beg];
        int i = beg, j = end + 1;
        while (true) {
            while (++i <= end && points[i].coords[k] < pivot.coords[k])
                ;
            while (--j > beg && points[j].coords[k] >= pivot.coords[k])
                ;
            if (i < j) {
                HyperPoint temp = points[i];
                points[i] = points[j];
                points[j] = temp;
            } else
                break;
        }
        points[beg] = points[j];
        points[j] = pivot;
        return j;
    }

    // median of medians algorithm
    // Refer to https://en.wikipedia.org/wiki/Median_of_medians
    private int findMedian(HyperPoint[] points, int k, int beg, int end) {
        if (beg > end)
            return -1;
        else if (beg == end)
            return beg;
        int mid = (beg + end) / 2;
        int i = beg, j = end;
        while (true) {
            int t = partition(points, k, i, j);
            if (t == mid)
                return t;
            else if (t > mid)
                j = t - 1;
            else
                i = t + 1;
        }
    }

    private Node insertByMedianFinding(Node r, HyperPoint[] points, HyperPoint hmin, HyperPoint hmax, int depth, int i, int j) {
        if (i > j)
            return null;
        else if (i == j)
            return new Node(new HyperSpace(hmin, hmax), points[i]);
        int k = depth % K;
        // Find the index of median
        int t = findMedian(points, k, i, j);
        HyperPoint p = points[t];
        if (r == null)
            r = new Node(new HyperSpace(hmin, hmax), p);
        double pivot = p.coords[k];
        HyperPoint hmid1 = new HyperPoint(hmax);
        hmid1.coords[k] = p.coords[k];
        r.left = insertByMedianFinding(r.left, points, hmin, hmid1, depth + 1, i, t - 1);
        HyperPoint hmid2 = new HyperPoint(hmin);
        hmid2.coords[k] = pivot;
        r.right = insertByMedianFinding(r.right, points, hmid2, hmax, depth + 1, t + 1, j);
        return r;
    }

    /*
     * Nearest Neighbor Finding Record the the node of current best, and
     * continue check the the distance between current node and input node, if
     * distance is smaller, then update current best. Using pruning strategy to
     * prune left or right branch of current node. If input node is smaller than
     * current node r in the x coordinate, then algorithm will check left
     * branch. But only if the hypersphere whose center is input node and radius
     * is current minimal distance intersects with right branch, algorithm check
     * the right branch.
     */
    // current best node
    HyperPoint nmin;
    // current minimal distance
    double ndist;

    public HyperPoint nearestPoint(HyperPoint p) {
        if (root == null)
            return null;
        nmin = root.p;
        ndist = nmin.squareDistanceTo(p);
        nearestPoint(root, p, 0);
        return nmin;
    }

    private void nearestPoint(Node r, HyperPoint p, int depth) {
        if (r == null)
            return;
        double dist = r.p.squareDistanceTo(p);
        // update current best
        if (dist < ndist) {
            nmin = r.p;
            ndist = dist;
        }
        int k = depth % K;
        double pivot = r.p.coords[k];
        if (p.coords[k] < pivot) {
            nearestPoint(r.left, p, depth + 1);
            // Hyper space intersect with right branch
            if (p.coords[k] + Math.sqrt(ndist) >= pivot)
                nearestPoint(r.right, p, depth + 1);
        } else {
            nearestPoint(r.right, p, depth + 1);
            if (p.coords[k] - Math.sqrt(ndist) <= pivot)
                nearestPoint(r.left, p, depth + 1);
        }
    }

    /*
     * Range Search A simple implementation using recursion if current node's
     * hyperSpace doesn't intersect with required range, then current node will
     * be ignore. Otherwise, check the left or right son of current node.
     */
    public Set<HyperPoint> rangeQuery(HyperSpace hs) {
        Set<HyperPoint> res = new HashSet<HyperPoint>();
        rangeQuery(root, hs, res);
        return res;
    }

    private void rangeQuery(Node r, HyperSpace hs, Set<HyperPoint> res) {
        // If current node r is null or doesn't intersect with hs, then return
        if (r == null || !r.hs.intersects(hs))
            return;
        if (hs.contains(r.p))
            res.add(r.p);
        // recursively check the left, right branch of current node
        rangeQuery(r.left, hs, res);
        rangeQuery(r.right, hs, res);
    }

    // Test Code
    public static void main(String[] args) {
        double[][] ps = { { 0.2, 0.3, 0.4 }, { 0.3, 0.4, 0.5 }, { 0.1, 0.7, 0.3 }, { 0.1, 0.2, 0.9 } };
        int num = ps.length;
        HyperPoint[] hps = new HyperPoint[num];
        for (int i = 0; i < num; i++)
            hps[i] = new HyperPoint(ps[i]);
        double[][] range = { { 0, 0, 0 }, { 1, 1, 1 } };
        HyperPoint min = new HyperPoint(range[0]);
        HyperPoint max = new HyperPoint(range[1]);
        int K = range[0].length;
        KDTree kd = new KDTree(K, min, max);
        // Insert
        // ---------------------------------------
        // I. Single Point Insert
        // for (int i = 0; i < num; i++)
        // kd.insert(hps[i]);

        // II. Insert Points set by O(n) Median Find Algorithm
        kd.insertByMedianFinding(hps);

        // III. Using PreSort to fast insert Point Set
        // kd.insertByPreSort(hps);
        double[] ps4 = { 1, 1, 1 };
        HyperPoint hp4 = new HyperPoint(ps4);

        // Nearest Point search
        // ---------------------------------------
        // hp5 should be { 0.3, 0.4, 0.5 }
        HyperPoint hp5 = kd.nearestPoint(hp4);

        // Range search
        // ---------------------------------------
        // qu should contain { { 0.2, 0.3, 0.4 }, { 0.3, 0.4, 0.5 }}
        double[][] range1 = { { 0, 0, 0 }, { 0.5, 0.5, 0.5 } };
        Set<HyperPoint> qu = kd.rangeQuery(new HyperSpace(new HyperPoint(range1[0]), new HyperPoint(range1[1])));
    }
}
