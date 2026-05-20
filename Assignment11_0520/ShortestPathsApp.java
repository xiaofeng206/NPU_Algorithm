import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ShortestPathsApp extends JFrame {

    static class Edge {
        int from, to;
        long weight;

        Edge(int from, int to, long weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    static class Graph {
        int n;
        ArrayList<ArrayList<Edge>> adj;
        ArrayList<Edge> edges = new ArrayList<>();

        Graph(int n) {
            this.n = n;
            adj = new ArrayList<>();
            for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        }

        void addEdge(int u, int v, long w) {
            Edge e = new Edge(u, v, w);
            adj.get(u).add(e);
            edges.add(e);
        }
    }

    static final long INF = Long.MAX_VALUE / 4;

    static class Result {
        long[] dist;
        int[] prev;
        boolean negCycle;
    }

    private Graph g = null;
    private final JTextArea out = new JTextArea(20, 60);
    private GraphPanel graphPanel;

    public ShortestPathsApp() {
        super("Shortest Paths — Dijkstra & Bellman-Ford");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        // 範本下拉選單與按鈕
        String[] initialNames = new String[]{"範本 A: 正權範例", "範本 B: 負權但無負循環", "範本 C: 含負循環範例"};
        java.util.List<Graph> templates = new ArrayList<>();
        templates.add(templateA());
        templates.add(templateB());
        templates.add(templateC());
        JComboBox<String> templateBox = new JComboBox<>(initialNames);
        JButton loadTemplateBtn = new JButton("載入範本");
        JButton saveTemplateBtn = new JButton("儲存為範本");

        JButton inputBtn = new JButton("手動建立範例");
        JButton dijkBtn = new JButton("執行 Dijkstra");
        JButton bfBtn = new JButton("執行 Bellman-Ford");
        JButton showBtn = new JButton("顯示圖形");
        JButton clearOutputBtn = new JButton("清除右側輸出");

        top.add(templateBox);
        top.add(loadTemplateBtn);
        top.add(saveTemplateBtn);
        top.add(inputBtn);
        top.add(dijkBtn);
        top.add(bfBtn);
        top.add(showBtn);
        top.add(clearOutputBtn);

        add(top, BorderLayout.NORTH);

        out.setEditable(false);
        JScrollPane sp = new JScrollPane(out);

        // 圖形面板與文字輸出以分割面板顯示
        graphPanel = new GraphPanel();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, sp);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        loadTemplateBtn.addActionListener(e -> {
            int idx = templateBox.getSelectedIndex();
            if (idx < 0 || idx >= templates.size()) return;
            g = copyGraph(templates.get(idx));
            graphPanel.setGraph(g);
            out.append("已載入範本: " + templateBox.getSelectedItem() + "\n");
        });

        saveTemplateBtn.addActionListener(e -> {
            if (g == null) { JOptionPane.showMessageDialog(this, "目前沒有圖形可儲存"); return; }
            String name = JOptionPane.showInputDialog(this, "請輸入範本名稱:", "我的範本");
            if (name == null || name.trim().isEmpty()) return;
            templates.add(copyGraph(g));
            templateBox.addItem(name);
            out.append("已儲存為範本: " + name + "\n");
            graphPanel.repaint();
        });

        inputBtn.addActionListener(e -> inputGraphDialog());

        dijkBtn.addActionListener(e -> {
            if (!ensureGraph()) return;
            String sIdx = JOptionPane.showInputDialog(this, "來源節點 (0 起算):", "0");
            if (sIdx == null) return;
            try {
                int s = Integer.parseInt(sIdx.trim());
                boolean neg = false;
                for (Edge ed : g.edges) if (ed.weight < 0) { neg = true; break; }
                if (neg) out.append("警告：圖中有負權，Dijkstra 可能不正確。\n");
                Result r = dijkstra(g, s);
                displayResult(r);
                graphPanel.highlightFromResult(r);
                graphPanel.repaint();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "來源節點無效"); }
        });

        bfBtn.addActionListener(e -> {
            if (!ensureGraph()) return;
            String sIdx = JOptionPane.showInputDialog(this, "來源節點 (0 起算):", "0");
            if (sIdx == null) return;
            try {
                int s = Integer.parseInt(sIdx.trim());
                Result r = bellmanFord(g, s);
                displayResult(r);
                graphPanel.highlightFromResult(r);
                graphPanel.repaint();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "來源節點無效"); }
        });

        showBtn.addActionListener(e -> {
            if (g == null) { out.append("目前沒有載入圖形。\n"); return; }
            graphPanel.setGraph(g);
            out.append("圖形鄰接表:\n");
            for (int u = 0; u < g.n; u++) {
                out.append(u + " -> ");
                for (Edge ed : g.adj.get(u)) out.append("(" + ed.to + "," + ed.weight + ") ");
                out.append("\n");
            }
        });

        clearOutputBtn.addActionListener(e -> out.setText(""));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean ensureGraph() {
        if (g == null) {
            JOptionPane.showMessageDialog(this, "No graph loaded.");
            return false;
        }
        return true;
    }

    private void inputGraphDialog() {
        JTextField nField = new JTextField();
        JTextField mField = new JTextField();
        JPanel p = new JPanel(new GridLayout(0,1));
        p.add(new JLabel("節點數："));
        p.add(nField);
        p.add(new JLabel("邊數："));
        p.add(mField);
        int res = JOptionPane.showConfirmDialog(this, p, "圖形大小", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            int n = Integer.parseInt(nField.getText().trim());
            int m = Integer.parseInt(mField.getText().trim());
            Graph ng = new Graph(n);
            for (int i = 0; i < m; i++) {
                String prompt = "邊 #" + (i+1) + " 格式：起點 目標 權重（例如 0 1 5）";
                String s = JOptionPane.showInputDialog(this, prompt);
                if (s == null) {
                    // 使用者取消 — 詢問是否放棄整個建立流程
                    int c = JOptionPane.showConfirmDialog(this, "取消建立圖形？目前輸入的會被捨棄。", "確認取消", JOptionPane.YES_NO_OPTION);
                    if (c == JOptionPane.YES_OPTION) {
                        return;
                    } else {
                        i--; // 讓使用者重新輸入此條
                        continue;
                    }
                }
                s = s.trim();
                if (s.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "輸入不可為空，請重新輸入。");
                    i--; continue;
                }
                String[] parts = s.split("\\s+");
                if (parts.length < 3) { JOptionPane.showMessageDialog(this, "格式錯誤，請輸入：起點 目標 權重"); i--; continue; }
                try {
                    int u = Integer.parseInt(parts[0]);
                    int v = Integer.parseInt(parts[1]);
                    long w = Long.parseLong(parts[2]);
                    if (u < 0 || u >= n || v < 0 || v >= n) { JOptionPane.showMessageDialog(this, "頂點編號超出範圍，請重新輸入。"); i--; continue; }
                    ng.addEdge(u, v, w);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "數字格式錯誤，請輸入整數頂點與數值型權重。");
                    i--; continue;
                }
            }
            g = ng;
            if (graphPanel != null) {
                graphPanel.setGraph(g);
            }
            out.append("已建立圖形（" + n + " 個節點，" + m + " 條邊）。\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "輸入解析錯誤，請重新操作。\n" + ex.getMessage());
        }
    }

    static Result dijkstra(Graph g, int s) {
        long[] dist = new long[g.n];
        Arrays.fill(dist, INF);
        int[] prev = new int[g.n];
        Arrays.fill(prev, -1);
        dist[s] = 0;

        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        pq.add(new long[]{0, s});

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long d = cur[0];
            int u = (int) cur[1];
            if (d != dist[u]) continue;
            for (Edge e : g.adj.get(u)) {
                if (dist[e.to] > d + e.weight) {
                    dist[e.to] = d + e.weight;
                    prev[e.to] = u;
                    pq.add(new long[]{dist[e.to], e.to});
                }
            }
        }
        Result r = new Result();
        r.dist = dist;
        r.prev = prev;
        r.negCycle = false;
        return r;
    }

    static Result bellmanFord(Graph g, int s) {
        long[] dist = new long[g.n];
        Arrays.fill(dist, INF);
        int[] prev = new int[g.n];
        Arrays.fill(prev, -1);
        dist[s] = 0;

        for (int i = 0; i < g.n - 1; i++) {
            boolean changed = false;
            for (Edge e : g.edges) {
                if (dist[e.from] != INF && dist[e.to] > dist[e.from] + e.weight) {
                    dist[e.to] = dist[e.from] + e.weight;
                    prev[e.to] = e.from;
                    changed = true;
                }
            }
            if (!changed) break;
        }

        boolean neg = false;
        for (Edge e : g.edges) {
            if (dist[e.from] != INF && dist[e.to] > dist[e.from] + e.weight) {
                neg = true;
                break;
            }
        }

        Result r = new Result();
        r.dist = dist;
        r.prev = prev;
        r.negCycle = neg;
        return r;
    }

    private void displayResult(Result r) {
        if (r.negCycle) {
            out.append("Graph contains a negative-weight cycle reachable from source.\n");
            return;
        }
        for (int i = 0; i < r.dist.length; i++) {
            out.append("Vertex " + i + ": ");
            if (r.dist[i] == INF) out.append("unreachable\n");
            else {
                out.append("distance = " + r.dist[i] + " path = ");
                java.util.List<Integer> path = new ArrayList<>();
                int cur = i;
                while (cur != -1) {
                    path.add(cur);
                    cur = r.prev[cur];
                }
                Collections.reverse(path);
                out.append(path + "\n");
            }
        }
    }

    static Graph sampleGraph() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 6);
        g.addEdge(0, 3, 7);
        g.addEdge(1, 2, 5);
        g.addEdge(1, 3, 8);
        g.addEdge(1, 4, -4);
        g.addEdge(2, 1, -2);
        g.addEdge(3, 2, -3);
        g.addEdge(3, 4, 9);
        g.addEdge(4, 0, 2);
        g.addEdge(4, 2, 7);
        return g;
    }

    static Graph templateA() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 3);
        g.addEdge(2, 3, 2);
        return g;
    }

    static Graph templateB() {
        // 負權但無負循環
        Graph g = new Graph(5);
        g.addEdge(0, 1, 6);
        g.addEdge(0, 3, 7);
        g.addEdge(1, 2, 5);
        g.addEdge(1, 3, 8);
        g.addEdge(1, 4, -4);
        g.addEdge(2, 1, -2);
        g.addEdge(3, 2, -3);
        g.addEdge(3, 4, 9);
        g.addEdge(4, 0, 2);
        g.addEdge(4, 2, 7);
        return g;
    }

    static Graph templateC() {
        // 含負權且有負循環
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, -1);
        g.addEdge(2, 0, -1); // 形成負循環
        return g;
    }

    static Graph copyGraph(Graph src) {
        Graph ng = new Graph(src.n);
        for (Edge e : src.edges) ng.addEdge(e.from, e.to, e.weight);
        return ng;
    }

    // 面板用來繪製節點與邊，並支援高亮最短路徑
    class GraphPanel extends JPanel {
        Graph graph = null;
        Point[] pos = new Point[0];
        java.util.Set<String> highlight = new HashSet<>();

        GraphPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        void setGraph(Graph g) {
            this.graph = g;
            highlight.clear();
            computePositions();
            repaint();
        }

        void computePositions() {
            if (graph == null) { pos = new Point[0]; return; }
            int n = graph.n;
            pos = new Point[n];
            int w = getWidth() > 0 ? getWidth() : 800;
            int h = getHeight() > 0 ? getHeight() : 600;
            int cx = w/2, cy = h/2;
            int r = Math.min(w, h)/2 - 60;
            for (int i = 0; i < n; i++) {
                double ang = 2*Math.PI*i/n - Math.PI/2;
                int x = cx + (int)(r*Math.cos(ang));
                int y = cy + (int)(r*Math.sin(ang));
                pos[i] = new Point(x, y);
            }
        }

        void highlightFromResult(Result res) {
            highlight.clear();
            if (res == null || res.prev == null) return;
            for (int v = 0; v < res.prev.length; v++) {
                int cur = v;
                while (cur != -1 && res.prev[cur] != -1) {
                    String key = res.prev[cur] + "->" + cur;
                    highlight.add(key);
                    cur = res.prev[cur];
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g2 = (Graphics2D) g0;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (graph == null) return;
            // 每次重繪都重新計算位置以處理大小改變
            computePositions();

            // draw edges
            for (Edge e : graph.edges) {
                Point a = pos[e.from];
                Point b = pos[e.to];
                if (a == null || b == null) continue;
                String key = e.from + "->" + e.to;
                if (highlight.contains(key)) g2.setColor(Color.RED);
                else g2.setColor(Color.GRAY);
                drawArrow(g2, a.x, a.y, b.x, b.y);
                // weight label — 偏移過以避免重疊
                int mx = (a.x + b.x)/2;
                int my = (a.y + b.y)/2;
                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double len = Math.hypot(dx, dy);
                double nx = 0, ny = 0;
                if (len != 0) { nx = -dy / len; ny = dx / len; }
                // 決定偏移量（使不同邊標籤有不同偏移，減少重疊）
                int hash = Math.abs((e.from * 31 + e.to * 97) % 5); // 0..4
                int offset = (hash - 2) * 12; // -24,-12,0,12,24
                int lx = mx + (int) (nx * offset);
                int ly = my + (int) (ny * offset);
                String wstr = String.valueOf(e.weight);
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(wstr);
                int sh = fm.getAscent();
                g2.drawString(wstr, lx - sw/2, ly + sh/2);
            }

            // draw nodes
            for (int i = 0; i < graph.n; i++) {
                Point p = pos[i];
                int r = 20;
                g2.setColor(Color.ORANGE);
                g2.fillOval(p.x-r, p.y-r, r*2, r*2);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x-r, p.y-r, r*2, r*2);
                String label = String.valueOf(i);
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(label);
                int sh = fm.getAscent();
                g2.setColor(Color.BLACK);
                g2.drawString(label, p.x - sw/2, p.y + sh/2 - 2);
            }
        }

        void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.drawLine(x1, y1, x2, y2);
            double phi = Math.toRadians(20);
            int barb = 12;
            double dy = y2 - y1;
            double dx = x2 - x1;
            double theta = Math.atan2(dy, dx);
            double x, y;
            x = x2 - barb * Math.cos(theta + phi);
            y = y2 - barb * Math.sin(theta + phi);
            g2.drawLine(x2, y2, (int)x, (int)y);
            x = x2 - barb * Math.cos(theta - phi);
            y = y2 - barb * Math.sin(theta - phi);
            g2.drawLine(x2, y2, (int)x, (int)y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShortestPathsApp());
    }
}
