/**
 * Created by antt on 8/15/2019.
 */
import java.awt.*;
import javax.swing.*;


/**
 * Created by antt on 8/14/2019.
 */
public class Tetris extends JFrame {
    private JLabel statusbar;

    public Tetris() {

        initUI();
    }

    private void initUI() {

        statusbar = new JLabel(" 0");
        add(statusbar, BorderLayout.SOUTH);
        add(new BoardEx(this));

        setResizable(false);
        pack();

        setTitle("Tetris");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            JFrame ex = new Tetris();
            ex.setVisible(true);
        });
    }

    public JLabel getStatusBar() {
        return statusbar;
    }
}
