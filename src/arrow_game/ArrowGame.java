/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arrow_game;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *
 * @author Goh Jing Xuan
 */
public class ArrowGame extends JFrame {

    private boolean testColorBlack = true;
    private boolean thickLine = false;
    private int X = 50;
    private int Y = 50;
    private int maxX = 1000;
    private int maxY = 900;
    private int minX = 0;
    private int minY = 0;

    private int temporaryStore = 0;

    private int i = 0;
    private int j = 0;

    private int theTempXMouse = 0;
    private int theTempYMouse = 0;

    private int thePrevTempXMouse = 0;
    private int thePrevTempYMouse = 0;

    private boolean drawControl = false;

    private boolean[][] screen = null;

    int headSize, bodyLength;

    drawPanel myDrawPanel;

    public ArrowGame() {
        myDrawPanel = new drawPanel();
        MyKeyListener myKeyListener = new MyKeyListener();

        this.add(myDrawPanel);
        addKeyListener(myKeyListener);
        this.setVisible(true);

        int windowXSize = maxX * 125 / 100;
        int windowYSize = maxY * 125 / 100;

        screen = new boolean[maxX][maxY];
        initializeScreen();

        this.setSize(windowXSize, windowYSize);

        MouseHandler myHandler = new MouseHandler();
        myDrawPanel.addMouseListener(myHandler);
        myDrawPanel.addMouseMotionListener(myHandler);

        myDrawPanel.setVisible(true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(
                new WindowAdapter() {
                    // exit when window has closed
                    public void windowClosed(WindowEvent event) {
                        System.exit(0);
                    } // end method windowClosed
                } // end WindowAdapter inner class
        ); // end call to addWindowListener
    }

    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent event) {
        }//end public void mouseClicked(mouseEvent event)

        public void mousePressed(MouseEvent event) {
            thePrevTempXMouse = theTempXMouse;
            thePrevTempYMouse = theTempYMouse;

            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
        }//end public void mousePressed(mouseEvent event)

        public void mouseReleased(MouseEvent event) {
            drawControl = false;
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            if (theTempXMouse > 0 && theTempXMouse < maxX && theTempYMouse > 0 && theTempYMouse < maxY) {
                screen[theTempXMouse][theTempYMouse] = true;

                myDrawPanel.repaint();
            }

        }//end public void mouseReleased(mouseEvent event)

        public void mouseEntered(MouseEvent event) {
            theTempXMouse = 0;
            theTempYMouse = 0;
        }//end public void mouseEntered(mouseEvent event)

        public void mouseExited(MouseEvent event) {
            theTempXMouse = 0;
            theTempYMouse = 0;
        }//end public void mouseExited(mouseEvent event)

        public void mouseDragged(MouseEvent event) {
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            if (theTempXMouse > 0 && theTempXMouse < maxX && theTempYMouse > 0 && theTempYMouse < maxY) {
                screen[theTempXMouse][theTempYMouse] = true;

                myDrawPanel.repaint();
            }
        }//end public void mouseDragged(mouseEvent event)

        public void mouseMoved(MouseEvent event) {
            //theTempXMouse = event.getX();
            //theTempYMouse = event.getY();
        }//end public void mouseMoved(mouseEvent event)
    }//end private class MouseHandler implements MouseListener, MouseMotionListener

    private class MyKeyListener implements KeyListener {

        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                    initializeScreen();
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_INSERT:
                    if (testColorBlack == true) {
                        testColorBlack = false;
                    } else {
                        testColorBlack = true;
                    }
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_K:
                    if (thickLine == true) {
                        thickLine = false;
                    } else {
                        thickLine = true;
                    }
                    myDrawPanel.repaint();
                    break;
            }
        }

        public void keyReleased(KeyEvent event) {
        }

        public void keyTyped(KeyEvent event) {
        }
    }

    private class drawPanel extends JPanel {

        private int width = 10;
        private int height = 10;

        public void paint(Graphics grap) {
            super.paint(grap);

            //System.out.println("Repaint");
            grap.fillOval(25, maxY - 200, headSize, headSize);
            Graphics2D g2d = (Graphics2D) grap;
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawLine(45, maxY - 160, 45, maxY-80 );
        }

    }

    private void initializeScreen() {
        headSize = 40;
        bodyLength = 80;
    }

    public static void main(String args[]) {
        new ArrowGame();
    }

}
