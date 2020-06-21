

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class ArrowGame extends JFrame {

    final drawPanel myDrawPanel;
    final JSlider frameSlider;
    private int maxX = 2500;
    private int maxY = 900;
    private int transX = 0;
    private Arrow currentArrow;
    private Person activePlayer, idlePlayer;
    private ArrayList<Arrow> prevArrows;
    private double ax, accelerationY;
    private Timer arrowHandler;
    private boolean arrowIsPulledBack, ready;
    private int tempXMouse = 0;
    private int tempYMouse = 0;
    private int initArrowX = 0;
    private int initArrowY = 0;


    public ArrowGame(int length, int thickness, double gravity, int updateInterval, int pixelPerMeter) {

        // initialize drawPanel
        myDrawPanel = new drawPanel();
        // slider to move the frame
        myDrawPanel.setBackground(Color.BLACK);
        frameSlider = new JSlider(0, 1000, 0);
        frameSlider.setFocusable(false);
        frameSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                transX = frameSlider.getValue();
                myDrawPanel.repaint();
            }
        });
        this.add(myDrawPanel);
        this.add(frameSlider, BorderLayout.SOUTH);
        this.setVisible(true);

        MyKeyListener myKeyListener = new MyKeyListener();
        addKeyListener(myKeyListener);

        int windowXSize = 1500;
        int windowYSize = 1000;

        this.setSize(windowXSize, windowYSize);

        MouseHandler myMouseHandler = new MouseHandler();
        myDrawPanel.addMouseListener(myMouseHandler);
        myDrawPanel.addMouseMotionListener(myMouseHandler);

        myDrawPanel.setVisible(true);


        // initialize gravity
        accelerationY = convertGravity(gravity, pixelPerMeter, updateInterval);

        initialize(length, thickness);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(
                new WindowAdapter() {
                    // exit when window has closed
                    public void windowClosed(WindowEvent event) {
                        System.exit(0);
                    } // end method windowClosed
                } // end WindowAdapter inner class
        ); // end call to addWindowListener

        // arrow movement
        arrowHandler = new Timer(updateInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // update arrow location
                currentArrow.move(ax, accelerationY);
                frameSlider.setValue((int) currentArrow.getX() - windowXSize / 2);
                double[] arrowCoords = currentArrow.getHeadAndTail();
                // check hit for person
                int hit = idlePlayer.checkHit(arrowCoords[0], arrowCoords[1]);
                checkHit(arrowCoords[0], arrowCoords[1], hit);
                myDrawPanel.repaint();
                if (hit != 0) {
                    checkWinCondition();
                }
            }
        });
    }

    public void checkHit(double x, double y, int hit) {
        // function to check whether arrow hit anything
        if (hit == 1) {
            idlePlayer.reduceHealth(1);
        }
        // hit ground or hit player
        if (y >= maxY || hit != 0) {
            // stop the arrow
            currentArrow.setForce(0, 0);
            arrowHandler.stop();
            prevArrows.add(currentArrow);
            // switch active player
            activePlayer.resetHand();
            Person temp = activePlayer;
            activePlayer = idlePlayer;
            idlePlayer = temp;
            ready = true;
            // create new arrow on active player hand
            currentArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), currentArrow.getLength(), currentArrow.getThickness(), -activePlayer.getBowAngle() * Math.PI / 180);
            // move the frame to show the active player
            frameSlider.setValue(activePlayer.getHandX() - this.getWidth() / 2);
        }
    }

    public void checkWinCondition() {
        // if hit check if the player health is 0 or not
        if (activePlayer.getHealth() <= 0) {
            // if win, display message box to ask if player wants to restart game
            int n;
            String p;
            Object[] options = {"Yes", "No"};
            p = activePlayer.isLeft() ? "Player 2" : "Player 1";

            n = JOptionPane.showOptionDialog(null,
                    p + " won. Do you wnat to start again?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (n == 0) {
                initialize(currentArrow.getLength(), currentArrow.getThickness());
                myDrawPanel.repaint();
            } else {
                System.exit(0);
            }
        }
    }

    public void initialize(int length, int thickness) {
        // function to initialize the whole game
        Random r = new Random();
        activePlayer = new Person(50, 100, maxY - 175, maxY - 60, true);
//        random location of P2
        idlePlayer = new Person(50, maxX - (r.nextInt(1000-300) + 300), maxY - 175, maxY - 60, false);
        prevArrows = new ArrayList<>();
        currentArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), length, thickness, -activePlayer.getBowAngle() * Math.PI / 180);

        arrowIsPulledBack = false;
        ready = true;
    }

    public double convertGravity(double gravity, int pixelPerMeter, int updateInterval) {
        // convert unit of gravity from m/s^2 to pixel/frameupdate^2
        return gravity * pixelPerMeter * ((double) updateInterval / 1000 * (double) updateInterval / 1000);
    }

    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // check if the mouse is on the arrow, let player start pulling back
            if (Math.abs(activePlayer.getHandX() - tempXMouse) <= activePlayer.getHeadR()
                    && Math.abs(activePlayer.getHandY() - tempYMouse) <= activePlayer.getHeadR() && ready && !arrowIsPulledBack) {
                arrowIsPulledBack = true;
                ready = false;
                initArrowX = activePlayer.getHandX();
                initArrowY = activePlayer.getHandY();
            }
        }

        public void mouseDragged(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // when mouse is dragged, update the location of arrow, position of elbow
            // and bow string based on mouse movement
            if (arrowIsPulledBack && activePlayer.checkHand(tempXMouse, tempYMouse)) {
                currentArrow.setPos(tempXMouse, tempYMouse);
                currentArrow.setAngle(initArrowX - tempXMouse, initArrowY - tempYMouse);
                activePlayer.updateDrag(tempXMouse, tempYMouse);
                myDrawPanel.repaint();
            }
        }

        public void mouseReleased(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // when mouse is released, apply force to the arrow based on how far the initial mouse location is from
            // current position then start the timer handler if the mouse position is valid
            if (arrowIsPulledBack && activePlayer.checkHand(tempXMouse, tempYMouse)) {
                tempXMouse = event.getX() + transX;
                tempYMouse = event.getY();
                currentArrow.setPos(tempXMouse, tempYMouse);
                myDrawPanel.repaint();
                currentArrow.setForce((initArrowX - tempXMouse) / 3.0, (initArrowY - tempYMouse) / 3.0);
                arrowIsPulledBack = false;
                ready = false;
                arrowHandler.start();
            }
            if (!arrowHandler.isRunning()) {
                // reset hand position if player move mouse to invalid position
                activePlayer.resetHand();
                currentArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), currentArrow.getLength(), currentArrow.getThickness(), -activePlayer.getBowAngle() * Math.PI / 180);
                myDrawPanel.repaint();
            }
        }

        public void mouseEntered(MouseEvent event) {
            tempXMouse = 0;
            tempYMouse = 0;
        }

        public void mouseExited(MouseEvent event) {
            tempXMouse = 0;
            tempYMouse = 0;
        }

        public void mouseMoved(MouseEvent mouseEvent) {
        }
    }

    private class MyKeyListener implements KeyListener {

        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                // move the frame
                case KeyEvent.VK_LEFT:
                    frameSlider.setValue(frameSlider.getValue() - 50);
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    frameSlider.setValue(frameSlider.getValue() + 50);
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_DELETE:
                    initialize(currentArrow.getLength(), currentArrow.getThickness());
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

        public void paint(Graphics grap) {
            super.paint(grap);
            grap.setColor(Color.WHITE);
            Graphics2D g2d = (Graphics2D) grap;
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawLine(0, maxY, maxX, maxY);
            activePlayer.draw(g2d, transX, 0);
            // if player is pulling bow, draw pulled bow strings
            if (ready || arrowIsPulledBack) {
                double[] arrowCoords = currentArrow.getHeadAndTail();
                activePlayer.drawBowString(g2d, (int) arrowCoords[2], (int) arrowCoords[3]);
            } else {
                activePlayer.drawDefaultBowString(grap);
            }
            idlePlayer.draw(grap, transX, 0);
            idlePlayer.drawDefaultBowString(grap);
            currentArrow.draw(grap, transX, 0, Color.WHITE);
            // draw all previous arrows, the most recent one is green colour
            for (int i = 0; i < prevArrows.size(); i++) {
                Arrow a = prevArrows.get(i);
                if (i == prevArrows.size() - 1) {
                    a.draw(grap, transX, 0, Color.GREEN);
                } else {
                    a.draw(grap, transX, 0, Color.YELLOW);
                }
            }
        }
    }

    public static void main(String[] args) {
        // configure parameter here
        int arrowLength = 50;
        int thickness = 6;
        double gravity = 9.8; // in m/s^2 unit
        int updateInterval = 10; // milliseconds
        int pixelPerMeter = 100;
        new ArrowGame(arrowLength, thickness, gravity, updateInterval, pixelPerMeter);
    }
}
