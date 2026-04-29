import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DistanceTransformGUI extends JFrame {
    private static final int EDGE_THRESHOLD = 80;
    
    private JLabel imageLabel;
    private BufferedImage sourceImage;
    private BufferedImage currentDisplayImage;
    private int[][] grayArray;
    private float[][] distArray;
    
    private JComboBox<String> modeComboBox;
    private JButton loadButton, processButton, stepButton, resetButton;
    private JLabel statusLabel;
    private JLabel stepInfo;
    private int currentStep = 0;
    private boolean isProcessing = false;
    
    public DistanceTransformGUI() {
        setTitle("Distance Transform - 1D/2D Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Top Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        loadButton = new JButton("Load Image");
        loadButton.addActionListener(e -> loadImage());
        
        modeComboBox = new JComboBox<>(new String[]{"Mode: 1D Scan", "Mode: 2D Scan"});
        
        processButton = new JButton("Process");
        processButton.addActionListener(e -> processImage());
        processButton.setEnabled(false);
        
        stepButton = new JButton("Next Step");
        stepButton.addActionListener(e -> nextStep());
        stepButton.setEnabled(false);
        
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> reset());
        
        controlPanel.add(loadButton);
        controlPanel.add(new JLabel("Scan Mode:"));
        controlPanel.add(modeComboBox);
        controlPanel.add(processButton);
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);
        
        // Center Image Panel
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        
        // Bottom Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        statusLabel = new JLabel("Status: Ready");
        stepInfo = new JLabel("Step: 0");
        
        infoPanel.add(statusLabel);
        infoPanel.add(stepInfo);
        
        // Layout
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "bmp"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                sourceImage = ImageIO.read(selectedFile);
                
                if (sourceImage == null) {
                    JOptionPane.showMessageDialog(this, "Unsupported image format", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Scale image if too large
                if (sourceImage.getWidth() > 800 || sourceImage.getHeight() > 600) {
                    double scale = Math.min(800.0 / sourceImage.getWidth(), 600.0 / sourceImage.getHeight());
                    sourceImage = scaleImage(sourceImage, scale);
                }
                
                currentDisplayImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                grayArray = toGrayscale(sourceImage);
                
                displayImage(sourceImage);
                processButton.setEnabled(true);
                statusLabel.setText("Status: Image loaded. Press 'Process' to start.");
                reset();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processImage() {
        if (sourceImage == null) return;
        
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        
        distArray = new float[height][width];
        float infinity = Float.POSITIVE_INFINITY;
        
        // Initialize: dark pixels have distance 0
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isTargetPixel = grayArray[y][x] < EDGE_THRESHOLD;
                distArray[y][x] = isTargetPixel ? 0.0f : infinity;
            }
        }
        
        isProcessing = true;
        currentStep = 0;
        stepButton.setEnabled(true);
        statusLabel.setText("Status: Processing started. Click 'Next Step' to proceed.");
        stepInfo.setText("Step: 0 - Initialization complete");
        
        // Show initial state
        displayDistanceMap(distArray);
    }
    
    private void nextStep() {
        if (!isProcessing || sourceImage == null) return;
        
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        String mode = (String) modeComboBox.getSelectedItem();
        
        currentStep++;
        
        if (mode.equals("Mode: 1D Scan")) {
            performForward1D(distArray, width, height);
            displayDistanceMap(distArray);
            stepInfo.setText("Step: " + currentStep + " - Forward Scan Complete");
            
            currentStep++;
            performBackward1D(distArray, width, height);
            displayDistanceMap(distArray);
            stepInfo.setText("Step: " + currentStep + " - Backward Scan Complete");
            
            statusLabel.setText("Status: 1D Scan finished!");
            stepButton.setEnabled(false);
        } else {
            if (currentStep == 1) {
                performForward2D(distArray, width, height);
                displayDistanceMap(distArray);
                stepInfo.setText("Step: 1 - Forward Pass (Top-Left to Bottom-Right)");
            } else if (currentStep == 2) {
                performBackward2D(distArray, width, height);
                displayDistanceMap(distArray);
                stepInfo.setText("Step: 2 - Backward Pass (Bottom-Right to Top-Left)");
                
                statusLabel.setText("Status: 2D Scan finished!");
                stepButton.setEnabled(false);
            }
        }
    }
    
    private void performForward1D(float[][] dist, int width, int height) {
        // Forward pass: 1D L1 norm - Horizontal scan only
        // Process each row: left to right
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                if (dist[y][x] != 0.0f) { // Skip feature pixels
                    dist[y][x] = Math.min(dist[y][x], dist[y][x - 1] + 1.0f);
                }
            }
        }
        
        // Vertical scan: top to bottom
        for (int x = 0; x < width; x++) {
            for (int y = 1; y < height; y++) {
                if (dist[y][x] != 0.0f) { // Skip feature pixels
                    dist[y][x] = Math.min(dist[y][x], dist[y - 1][x] + 1.0f);
                }
            }
        }
    }
    
    private void performBackward1D(float[][] dist, int width, int height) {
        // Backward pass: 1D L1 norm - Horizontal scan only
        // Process each row: right to left
        for (int y = 0; y < height; y++) {
            for (int x = width - 2; x >= 0; x--) {
                if (dist[y][x] != 0.0f) { // Skip feature pixels
                    dist[y][x] = Math.min(dist[y][x], dist[y][x + 1] + 1.0f);
                }
            }
        }
        
        // Vertical scan: bottom to top
        for (int x = 0; x < width; x++) {
            for (int y = height - 2; y >= 0; y--) {
                if (dist[y][x] != 0.0f) { // Skip feature pixels
                    dist[y][x] = Math.min(dist[y][x], dist[y + 1][x] + 1.0f);
                }
            }
        }
    }
    
    private void performForward2D(float[][] dist, int width, int height) {
        // Forward pass: top-left to bottom-right
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dist[y][x] == 0.0f) continue;
                
                float minDist = dist[y][x];
                if (y > 0) minDist = Math.min(minDist, dist[y - 1][x] + 1.0f);
                if (x > 0) minDist = Math.min(minDist, dist[y][x - 1] + 1.0f);
                if (y > 0 && x > 0) minDist = Math.min(minDist, dist[y - 1][x - 1] + 1.4142135f);
                if (y > 0 && x < width - 1) minDist = Math.min(minDist, dist[y - 1][x + 1] + 1.4142135f);
                dist[y][x] = minDist;
            }
        }
    }
    
    private void performBackward2D(float[][] dist, int width, int height) {
        // Backward pass: bottom-right to top-left
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                if (dist[y][x] == 0.0f) continue;
                
                float minDist = dist[y][x];
                if (y < height - 1) minDist = Math.min(minDist, dist[y + 1][x] + 1.0f);
                if (x < width - 1) minDist = Math.min(minDist, dist[y][x + 1] + 1.0f);
                if (y < height - 1 && x > 0) minDist = Math.min(minDist, dist[y + 1][x - 1] + 1.4142135f);
                if (y < height - 1 && x < width - 1) minDist = Math.min(minDist, dist[y + 1][x + 1] + 1.4142135f);
                dist[y][x] = minDist;
            }
        }
    }
    
    private void displayDistanceMap(float[][] dist) {
        if (currentDisplayImage == null) return;
        
        int width = currentDisplayImage.getWidth();
        int height = currentDisplayImage.getHeight();
        
        float maxDist = 0.0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!Float.isInfinite(dist[y][x])) {
                    maxDist = Math.max(maxDist, dist[y][x]);
                }
            }
        }
        
        if (maxDist == 0.0f) maxDist = 1.0f;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = Float.isInfinite(dist[y][x]) ? 0 : Math.round((dist[y][x] / maxDist) * 255.0f);
                value = Math.max(0, Math.min(255, value));
                int rgb = (value << 16) | (value << 8) | value;
                currentDisplayImage.setRGB(x, y, rgb);
            }
        }
        
        displayImage(currentDisplayImage);
    }
    
    private void displayImage(BufferedImage image) {
        ImageIcon icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
    }
    
    private void reset() {
        currentStep = 0;
        isProcessing = false;
        stepButton.setEnabled(false);
        stepInfo.setText("Step: 0");
        if (sourceImage != null) {
            displayImage(sourceImage);
            statusLabel.setText("Status: Ready. Press 'Process' to start.");
        }
    }
    
    private int[][] toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] gray = new int[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                gray[y][x] = Math.round((0.299f * red) + (0.587f * green) + (0.114f * blue));
            }
        }
        
        return gray;
    }
    
    private BufferedImage scaleImage(BufferedImage img, double scale) {
        int newWidth = (int) (img.getWidth() * scale);
        int newHeight = (int) (img.getHeight() * scale);
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return scaled;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DistanceTransformGUI gui = new DistanceTransformGUI();
            gui.setVisible(true);
        });
    }
}
