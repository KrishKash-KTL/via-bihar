import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BiharRoutingUI extends JFrame {

    private final BiharRoutingEngine engine;

    // UI Components
    private JComboBox<String> originCombo;
    private JComboBox<String> destinationCombo;
    private JRadioButton distanceRadio;
    private JRadioButton timeRadio;
    private JTextArea outputArea;
    private JLabel totalDistanceLabel;
    private JLabel totalTimeLabel;

    public BiharRoutingUI(BiharRoutingEngine engine) {
        this.engine = engine;
        initUI();
    }

    private void initUI() {
        setTitle("Bihar Regional Transit Navigator — Executive Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Global Dark Theme Colors
        Color bgDark = new Color(18, 24, 38);
        Color cardDark = new Color(30, 41, 59);
        Color accentBlue = new Color(59, 130, 246);
        Color textWhite = new Color(241, 245, 249);
        Color textMuted = new Color(148, 163, 184);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(bgDark);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -------------------------------------------------------------
        // TOP HEADER BAR
        // -------------------------------------------------------------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(cardDark);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🗺️ Bihar Regional Transit Navigator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(textWhite);

        JLabel subtitleLabel = new JLabel("A* Pathfinding & Infrastructure Optimization Engine");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(textMuted);

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(subtitleLabel);

        headerPanel.add(titleBox, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // LEFT CONTROLS PANEL (User Inputs)
        // -------------------------------------------------------------
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(cardDark);
        controlsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        controlsPanel.setPreferredSize(new Dimension(320, 0));

        // District Selectors
        String[] districtList = engine.getRegisteredDistricts();

        JLabel originLabel = createFormLabel("ORIGIN DISTRICT", textMuted);
        originCombo = new JComboBox<>(districtList);
        styleComboBox(originCombo);

        JLabel destLabel = createFormLabel("DESTINATION DISTRICT", textMuted);
        destinationCombo = new JComboBox<>(districtList);
        styleComboBox(destinationCombo);

        if (districtList.length > 1) {
            destinationCombo.setSelectedIndex(1); // Default to different district
        }

        // Optimization Metric Selection
        JLabel strategyLabel = createFormLabel("OPTIMIZATION STRATEGY", textMuted);
        distanceRadio = new JRadioButton("Shortest Distance (Km)");
        timeRadio = new JRadioButton("Fastest Travel Time (Hours)", true);
        styleRadioButton(distanceRadio, textWhite, cardDark);
        styleRadioButton(timeRadio, textWhite, cardDark);

        ButtonGroup strategyGroup = new ButtonGroup();
        strategyGroup.add(distanceRadio);
        strategyGroup.add(timeRadio);

        // Calculate Button
        JButton calculateBtn = new JButton("COMPUTE ROUTE ➔");
        calculateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calculateBtn.setForeground(Color.WHITE);
        calculateBtn.setBackground(accentBlue);
        calculateBtn.setFocusPainted(false);
        calculateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calculateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        calculateBtn.addActionListener(e -> runPathfinding());

        // Assembly of Controls
        controlsPanel.add(originLabel);
        controlsPanel.add(originCombo);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlsPanel.add(destLabel);
        controlsPanel.add(destinationCombo);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlsPanel.add(strategyLabel);
        controlsPanel.add(timeRadio);
        controlsPanel.add(distanceRadio);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        controlsPanel.add(calculateBtn);

        mainPanel.add(controlsPanel, BorderLayout.WEST);

        // -------------------------------------------------------------
        // RIGHT RESULTS PANEL (Metrics & Output Log)
        // -------------------------------------------------------------
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 15));
        resultsPanel.setOpaque(false);

        // Stat Cards Top Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setOpaque(false);

        totalDistanceLabel = new JLabel("-- Km", SwingConstants.CENTER);
        JPanel distCard = createMetricCard("TOTAL DISTANCE", totalDistanceLabel, cardDark, textWhite, textMuted);

        totalTimeLabel = new JLabel("-- hrs -- mins", SwingConstants.CENTER);
        JPanel timeCard = createMetricCard("ESTIMATED TRAVEL TIME", totalTimeLabel, cardDark, textWhite, textMuted);

        statsPanel.add(distCard);
        statsPanel.add(timeCard);

        // Itinerary Output Text Console
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setBackground(cardDark);
        outputArea.setForeground(textWhite);
        outputArea.setMargin(new Insets(15, 15, 15, 15));
        outputArea.setText("Select your origin, destination, and strategy on the left, then click 'COMPUTE ROUTE'.");

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        resultsPanel.add(statsPanel, BorderLayout.NORTH);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(resultsPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void runPathfinding() {
        String origin = (String) originCombo.getSelectedItem();
        String destination = (String) destinationCombo.getSelectedItem();
        boolean optimizeTime = timeRadio.isSelected();

        if (origin != null && origin.equalsIgnoreCase(destination)) {
            JOptionPane.showMessageDialog(this, "Origin and Destination must be different districts.", "Input Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String result = engine.getRouteItinerary(origin, destination, optimizeTime);
        outputArea.setText(result);

        // Update Stat Cards
        totalDistanceLabel.setText(String.format("%.1f Km", engine.getLastTotalDistance()));
        int hrs = (int) engine.getLastTotalTime();
        int mins = (int) Math.round((engine.getLastTotalTime() - hrs) * 60);
        totalTimeLabel.setText(String.format("%d hr %d mins", hrs, mins));
    }

    // Helper Styling Methods
    private JLabel createFormLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color bg, Color valColor, Color titleColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(titleColor);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(valColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleRadioButton(JRadioButton radio, Color fg, Color bg) {
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radio.setForeground(fg);
        radio.setBackground(bg);
        radio.setFocusPainted(false);
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
