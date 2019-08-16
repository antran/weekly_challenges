import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;


/**
 * Created by antt on 8/14/2019.
 */
public class BoardEx extends JPanel
        implements Runnable, KeyListener {

    private final int B_WIDTH = 180;
    private final int B_HEIGHT = 360;
    private final JLabel statusbar;
    private final Tetris parent;
    private int DELAY = 300;
    private final int R = 20;
    private final int C = 10;

    private Thread animator;
    BufferedImage tiles;
    private int[][] fields = new int[R][C];
    int[][] figures =  {
            { 1,3,5,7 }, // I
            { 2,4,5,7 }, // S
            { 3,5,4,6 }, // Z
            { 3,5,4,7 }, // T
            { 2,3,5,7 }, // L
            { 3,5,7,6 }, // J
            { 2,3,4,5 }  // 0
    };

    Point[] a = new Point[4];
    Point[] b= new Point[4];
    private int dx = 0;
    private int color = 0;
    private boolean rotate = false;
    private boolean newFig = true;
    private boolean toBottom = false;
    private int score = 0;

    public BoardEx(Tetris parent) {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        addKeyListener(this);
        setFocusable(true);
        this.parent = parent;
        statusbar =  parent.getStatusBar();
        initBoard();
    }

    private void initBoard() {
        for (int i = 0; i < a.length; i++) a[i] = new Point();
        for (int i = 0; i < b.length; i++) b[i] = new Point();
        try {
            tiles = ImageIO.read(new File("src/main/resources/tiles.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < R; i++)
            for (int j = 0; j < C; j++)
                fields[i][j] = 0;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        animator = new Thread(this);
        animator.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawStar(g);
    }

    private void drawStar(Graphics g) {
        //draw figure
        for (int i = 0; i < 4; i++) {
            g.drawImage(tile(color), a[i].x * 18, a[i].y * 18, null);
        }
        int count = 0;
        for (int i = 0; i < R; i++)
            for (int j = 0; j < C; j++) {
                if (fields[i][j] == 0) continue;
                g.drawImage(tile(fields[i][j]), j * 18, i * 18, null);
                count++;
            }
        for (int i = 0; i < C; i++) {
            g.drawLine(i*18, 0, i*18, B_HEIGHT);
        }
        for (int j = 0; j < R; j++) {
            g.drawLine(0, j*18, B_WIDTH, j*18 );
        }
        if (parent.getStatusBar() != null) parent.getStatusBar().setText(" " + String.valueOf(score));
        Toolkit.getDefaultToolkit().sync();
    }

    private Image tile(int n) {
        return tiles.getSubimage(n*18,0,18,18);
    }

    private boolean check() {
        //r,c, field;
        for (int i = 0; i < 4; i++) {
            if (a[i].x < 0 || a[i].x >= C) return false;
            if (a[i].y >= R) return false;
            if(fields[a[i].y][a[i].x] != 0) return false;
        }
        return true;
    }
    @Override
    public void run() {
        long beforeTime, timeDiff;
        beforeTime = System.currentTimeMillis();
        Random ran = new Random(System.currentTimeMillis());
        color = (Math.abs(ran.nextInt()) % 7) + 1;
        while (true) {
            for (int i = 0; i < 4; i++) {
                b[i].x = a[i].x;
                b[i].y = a[i].y;
            }

            // Move
            for (int i = 0; i < 4; i++) {
                a[i].x += dx;
            }
            if (!check()) {
                for (int i = 0; i < 4; i++) {
                    a[i].x = b[i].x;
                    a[i].y = b[i].y;
                }
            }

            // rotate
            if (rotate) {
                Point pivot = a[1];
                for(int i = 0; i < 4; i++) {
                    int dx = a[i].y - pivot.y;
                    int dy = a[i].x - pivot.x;
                    a[i].x = pivot.x - dx;
                    a[i].y = pivot.y + dy;
                }
            }
            if (!check()) {
                for (int i = 0; i < 4; i++) {
                    a[i].x = b[i].x;
                    a[i].y = b[i].y;
                }
            }

            // Move down
            timeDiff = System.currentTimeMillis() - beforeTime;
            if (timeDiff >= DELAY || toBottom) {
                for (int i = 0; i < 4; i++) {
                    a[i].y += 1;
                }
                if (!check()) {
                    for (int i = 0; i < 4; i++) {
                        a[i].x = b[i].x;
                        a[i].y = b[i].y;
                    }
                    for (int i = 0; i < 4; i++) {
                        fields[a[i].y][a[i].x] = color;
                    }
                    newFig = true;
                    toBottom = false;
                    // end game
                    for (int i = 0; i < 4; i++)
                        if (a[i].y == 0) {
                            JOptionPane.showMessageDialog(this, "Game Over!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return ;
                        }
                }
                //check line
                int k = R - 1;
                for (int i = R - 1; i >= 0; i--) {
                    int count = 0;
                    for (int j = 0; j < C; j++) {
                        if (fields[i][j] != 0) count++;
                        fields[k][j] = fields[i][j];
                    }
                    if (count < C) k--;
                    else score++;
                }
//                statusbar.setText(String.valueOf(score));
                beforeTime = System.currentTimeMillis();
                DELAY = 500;
            }
            // new figure
            if (newFig) {
                int f = Math.abs(ran.nextInt()) % 7;
                color = (Math.abs(ran.nextInt()) % 7) + 1;
                for (int i = 0; i < 4; i++) {
                    a[i].x = figures[f][i] % 2;
                    a[i].y = figures[f][i] / 2;
                }
                newFig = false;
            }

            dx = 0; rotate = false;
            repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                String msg = String.format("Thread interrupted: %s", e.getMessage());
                JOptionPane.showMessageDialog(this, msg, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) dx = -1;
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = 1;
        else if (e.getKeyCode() == KeyEvent.VK_UP) rotate = true;
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) DELAY = 30;
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) toBottom = true;
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        if (e.getKeyCode() == KeyEvent.VK_DOWN) timer.setDelay(DELAY);
        return;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        return;
    }

    private static class Point {
        int x = 0;
        int y = 0;

    }

}