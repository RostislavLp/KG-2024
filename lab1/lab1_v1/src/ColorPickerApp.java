import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorPickerApp extends JFrame {
    private JTextField rgbRField, rgbGField, rgbBField;
    private JTextField cmykCField, cmykMField, cmykYField, cmykKField;
    private JTextField hlsHField, hlsLField, hlsSField;
    private JPanel colorDisplay;
    private JSlider rSlider, gSlider, bSlider;

    public ColorPickerApp() {
        setTitle("Color Picker");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));

        // Color display panel
        colorDisplay = new JPanel();
        colorDisplay.setPreferredSize(new Dimension(600, 100));
        add(colorDisplay);

        // RGB Inputs
        JPanel rgbPanel = new JPanel();
        rgbPanel.setBorder(BorderFactory.createTitledBorder("RGB"));
        rgbRField = createTextField(rgbPanel, "R:");
        rgbGField = createTextField(rgbPanel, "G:");
        rgbBField = createTextField(rgbPanel, "B:");
        add(rgbPanel);

        // Sliders for RGB
        JPanel sliderPanel = new JPanel();
        sliderPanel.setBorder(BorderFactory.createTitledBorder("Adjust Color"));
        rSlider = createSlider(sliderPanel, "R", 255);
        gSlider = createSlider(sliderPanel, "G", 255);
        bSlider = createSlider(sliderPanel, "B", 255);
        add(sliderPanel);

        // CMYK Inputs
        JPanel cmykPanel = new JPanel();
        cmykPanel.setBorder(BorderFactory.createTitledBorder("CMYK"));
        cmykCField = createTextField(cmykPanel, "C:");
        cmykMField = createTextField(cmykPanel, "M:");
        cmykYField = createTextField(cmykPanel, "Y:");
        cmykKField = createTextField(cmykPanel, "K:");
        add(cmykPanel);

        // HLS Inputs
        JPanel hlsPanel = new JPanel();
        hlsPanel.setBorder(BorderFactory.createTitledBorder("HLS"));
        hlsHField = createTextField(hlsPanel, "H:");
        hlsLField = createTextField(hlsPanel, "L:");
        hlsSField = createTextField(hlsPanel, "S:");
        add(hlsPanel);

        // Set initial color
        updateColor(255, 0, 0); // Initial color Red

        // Button to choose color from palette
        JButton chooseColorButton = new JButton("Choose Color");
        chooseColorButton.addActionListener(e -> chooseColor());
        add(chooseColorButton);

        setVisible(true);
    }

    private JTextField createTextField(JPanel panel, String label) {
        JLabel jLabel = new JLabel(label);
        JTextField textField = new JTextField(5);
        textField.addActionListener(new ColorChangeListener());
        panel.add(jLabel);
        panel.add(textField);
        return textField;
    }

    private JSlider createSlider(JPanel panel, String label, int max) {
        JSlider slider = new JSlider(0, max);
        slider.addChangeListener(e -> updateFromSliders());
        JLabel jLabel = new JLabel(label + ": ");

        panel.add(jLabel);
        panel.add(slider);

        return slider;
    }

    private void updateFromSliders() {
        int r = rSlider.getValue();
        int g = gSlider.getValue();
        int b = bSlider.getValue();

        updateColor(r, g, b);
    }

    private void chooseColor() {
        Color selectedColor = JColorChooser.showDialog(this, "Choose a Color", colorDisplay.getBackground());

        if (selectedColor != null) {
            int r = selectedColor.getRed();
            int g = selectedColor.getGreen();
            int b = selectedColor.getBlue();
            rSlider.setValue(r);
            gSlider.setValue(g);
            bSlider.setValue(b);
            updateColor(r, g, b);
        }
    }
    private class ColorChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int r = Integer.parseInt(rgbRField.getText());
                int g = Integer.parseInt(rgbGField.getText());
                int b = Integer.parseInt(rgbBField.getText());
                updateColor(r, g, b);
                rSlider.setValue(r);
                gSlider.setValue(g);
                bSlider.setValue(b);
            } catch (NumberFormatException ex) {
                // Handle invalid input
            }
        }
    }

    private void updateColor(int r, int g, int b) {
        // Update RGB display
        rgbRField.setText(String.valueOf(r));
        rgbGField.setText(String.valueOf(g));
        rgbBField.setText(String.valueOf(b));

        // Convert to CMYK
        float[] cmyk = rgbToCmyk(r, g, b);
        cmykCField.setText(String.format("%.2f", cmyk[0]));
        cmykMField.setText(String.format("%.2f", cmyk[1]));
        cmykYField.setText(String.format("%.2f", cmyk[2]));
        cmykKField.setText(String.format("%.2f", cmyk[3]));

        // Convert to HLS
        float[] hls = rgbToHls(r, g, b);
        hlsHField.setText(String.format("%.2f", hls[0]));
        hlsLField.setText(String.format("%.2f", hls[1]));
        hlsSField.setText(String.format("%.2f", hls[2]));

        // Update color display
        colorDisplay.setBackground(new Color(r, g, b));
    }

    private float[] rgbToCmyk(int r, int g, int b) {
        float c = 1 - (r / 255f);
        float m = 1 - (g / 255f);
        float y = 1 - (b / 255f);

        float k = Math.min(c, Math.min(m, y));

        if (k < 1) {
            c = (c - k) / (1 - k);
            m = (m - k) / (1 - k);
            y = (y - k) / (1 - k);
        } else {
            c = m = y = 0;
        }

        return new float[]{c, m, y, k};
    }

    private float[] rgbToHls(int r, int g, int b) {
        float rNorm = r / 255f;
        float gNorm = g / 255f;
        float bNorm = b / 255f;

        float max = Math.max(rNorm, Math.max(gNorm, bNorm));
        float min = Math.min(rNorm, Math.min(gNorm, bNorm));

        float h = 0;
        float l = (max + min) / 2;

        if (max == min) {
            h = 0; // achromatic
        } else {
            float delta = max - min;
            if (max == rNorm) {
                h = (gNorm - bNorm) / delta + (gNorm < bNorm ? 6 : 0);
            } else if (max == gNorm) {
                h = (bNorm - rNorm) / delta + 2;
            } else {
                h = (rNorm - gNorm) / delta + 4;
            }
            h /= 6;
        }

        float s = l == 0 || l == 1 ? 0 : (max - l) / Math.min(l, 1 - l);

        return new float[]{h * 360, l * 100, s * 100}; // H in degrees; L and S in percentage
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorPickerApp::new);
    }
}
