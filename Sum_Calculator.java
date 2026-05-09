import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Sum_Calculator {
    public static void main (String[] args) {
        // Frame.
        JFrame frame = new JFrame("Sum Calculator");
        frame.setSize(300,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4,2,10,10));

        // Components.
        JLabel label1 = new JLabel(" Number 1:");
        JTextField textField1 = new JTextField();

        JLabel label2 = new JLabel(" Number 2:");
        JTextField textField2 = new JTextField();

        JButton btnCalculate = new JButton("Calculate");
        JLabel labelResult = new JLabel(" Result: ");
        labelResult.setFont(new Font("Arial", Font.BOLD, 14));

        // Logic Addition.
        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double num1 = Double.parseDouble(textField1.getText());
                    double num2 = Double.parseDouble(textField2.getText());
                    double sum = num1 + num2;

                    labelResult.setText(" Result: " + sum);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers!");
                }
            }
        });

        frame.add(label1);
        frame.add(textField1);
        frame.add(label2);
        frame.add(textField2);
        frame.add(new JLabel(""));
        frame.add(btnCalculate);
        frame.add(new JLabel(""));
        frame.add(labelResult);

        frame.setVisible(true);
    }
}
