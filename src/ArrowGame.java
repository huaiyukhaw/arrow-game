

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
    private int maxY = 800;
    private int transX = 0;
    private Arrow activeArrow;
    private Person activePlayer, idlePlayer;
    private ArrayList<Arrow> usedArrows;
    private double ax, ay, windAcceleration;
    private Timer arrowHandler, textDeleter;
    private boolean arrowIsPulledBack, ready;
    private int updateInterval, pixelPerMeter;
    private int tempXMouse = 0;
    private int tempYMouse = 0;
    private int initArrowX = 0;
    private int initArrowY = 0;
    private String textOnDisplay;
    

    public ArrowGame(int length, int thickness, double windAcceleration, double gravity, int updateInterval, int pixelPerMeter) {

        // initialize drawPanel
        myDrawPanel = new drawPanel();
        // slider to move the frame
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

        textOnDisplay = "";

        // initialize wind acceleration and gravity
        ax = convertGravity(windAcceleration, pixelPerMeter, updateInterval);
        ay = convertGravity(gravity, pixelPerMeter, updateInterval);

        initialize(length, thickness);

        this.updateInterval = updateInterval;
        this.pixelPerMeter = pixelPerMeter;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(
                new WindowAdapter() {
                    // exit when window has closed
                    public void windowClosed(WindowEvent event) {
                        System.exit(0);
                    } // end method windowClosed
                } // end WindowAdapter inner class
        ); // end call to addWindowListener
        // create timer to make text appear for 3s
        textDeleter = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textOnDisplay = "";
                myDrawPanel.repaint();
            }
        });
        textDeleter.setRepeats(false);

        // this the timer handler that will simulate the physics of the arrow movement
        arrowHandler = new Timer(updateInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // update the ball location on each time interval based on the current ball velocity
                activeArrow.move(ax, ay);
                frameSlider.setValue((int) activeArrow.getX() - windowXSize / 2);
                double[] arrowCoords = activeArrow.getTipAndTail();
                // check hitbox
                int hit = idlePlayer.checkHit(arrowCoords[0], arrowCoords[1]);
                checkHit(arrowCoords[0], arrowCoords[1], hit);
                myDrawPanel.repaint();
                if (hit != 0) {
                    checkWinCondition();
                }
            }
        });
    }

    public static void main(String[] args) {
        // configure parameter here
        int arrowLength = 30;
        int thickness = 4;
        double gravity = 9.8; // in m/s^2 unit
        double windAcceleration = 0.0;
        int updateInterval = 10; // milliseconds
        int pixelPerMeter = 100;
        new ArrowGame(arrowLength, thickness, windAcceleration, gravity, updateInterval, pixelPerMeter);
    }

    public void checkHit(double x, double y, int hit) {
        // function to check whether arrow hit anything
        if (hit == 1) {
            idlePlayer.reduceHealth(1);
        }
        // hit ground or hit player
        if (y >= maxY || hit != 0) {
            // stop the arrow
            activeArrow.setForce(0, 0);
            arrowHandler.stop();
            usedArrows.add(activeArrow);
            // switch active player
            activePlayer.resetHand();
            Person temp = activePlayer;
            activePlayer = idlePlayer;
            idlePlayer = temp;
            ready = true;
            // create new arrow on active player hand
            activeArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), activeArrow.getLength(), activeArrow.getThickness(), -activePlayer.getBowAngle() * Math.PI / 180);
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
                    p + " won. Restart game?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (n == 0) {
                initialize(activeArrow.getLength(), activeArrow.getThickness());
                myDrawPanel.repaint();
            } else {
                System.exit(0);
            }
        }
    }

    public void initialize(int length, int thickness) {
        // function to initialize the whole game
        Random r = new Random();
        activePlayer = new Person(30, 100, maxY - 80, maxY - 30, true);
        idlePlayer = new Person(30, maxX - (r.nextInt(450) + 50), maxY - 80, maxY - 30, false);
        usedArrows = new ArrayList<>();
        activeArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), length, thickness, -activePlayer.getBowAngle() * Math.PI / 180);

        arrowIsPulledBack = false;
        ready = true;
    }

    public double convertGravity(double gravity, int pixelPerMeter, int updateInterval) {
        // convert unit of gravity from m/s^2 to pixel/frameupdate^2
        return gravity * pixelPerMeter * ((double) updateInterval / 1000 * (double) updateInterval / 1000);
    }

    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent event) {
        }//end public void mouseClicked(mouseEvent event)

        public void mousePressed(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // when mouse pressed, if the mouse is on the arrow, let player start pulling back
            if (Math.abs(activePlayer.getHandX() - tempXMouse) <= activePlayer.getHeadR()
                    && Math.abs(activePlayer.getHandY() - tempYMouse) <= activePlayer.getHeadR() && ready && !arrowIsPulledBack) {
                arrowIsPulledBack = true;
                ready = false;
                initArrowX = activePlayer.getHandX();
                initArrowY = activePlayer.getHandY();
            }
        }//end public void mousePressed(mouseEvent event)

        public void mouseDragged(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // when mouse is dragged, update the location of arrow, position of elbow
            // and bow string based on mouse movement
            if (arrowIsPulledBack && activePlayer.checkHand(tempXMouse, tempYMouse)) {
                activeArrow.setPos(tempXMouse, tempYMouse);
                activeArrow.setAngle(initArrowX - tempXMouse, initArrowY - tempYMouse);
                activePlayer.updateDrag(tempXMouse, tempYMouse);
                myDrawPanel.repaint();
            }
        }//end public void mouseDragged(mouseEvent event)

        public void mouseReleased(MouseEvent event) {
            tempXMouse = event.getX() + transX;
            tempYMouse = event.getY();
            // when mouse is released, apply force to the arrow based on how far the initial mouse location is from
            // current position then start the timer handler if the mouse position is valid
            if (arrowIsPulledBack && activePlayer.checkHand(tempXMouse, tempYMouse)) {
                tempXMouse = event.getX() + transX;
                tempYMouse = event.getY();
                activeArrow.setPos(tempXMouse, tempYMouse);
                myDrawPanel.repaint();
                activeArrow.setForce((initArrowX - tempXMouse) / 3.0, (initArrowY - tempYMouse) / 3.0);
                arrowIsPulledBack = false;
                ready = false;
                arrowHandler.start();
            }
            if (!arrowHandler.isRunning()) {
                // reset hand position if player move mouse to invalid position
                activePlayer.resetHand();
                activeArrow = new Arrow(activePlayer.getHandX(), activePlayer.getHandY(), activeArrow.getLength(), activeArrow.getThickness(), -activePlayer.getBowAngle() * Math.PI / 180);
                myDrawPanel.repaint();
            }
        }//end public void mouseReleased(mouseEvent event)

        public void mouseEntered(MouseEvent event) {
            tempXMouse = 0;
            tempYMouse = 0;
        }//end public void mouseEntered(mouseEvent event)

        public void mouseExited(MouseEvent event) {
            tempXMouse = 0;
            tempYMouse = 0;
        }//end public void mouseExited(mouseEvent event)

        public void mouseMoved(MouseEvent mouseEvent) {
        }//end public void mouseMoved(MouseEvent mouseEvent)
    }//end private class MouseHandler implements MouseListener, MouseMotionListener

    private class MyKeyListener implements KeyListener {

        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                // key event to move the frame
                case KeyEvent.VK_LEFT:
                    windAcceleration -= 0.1;
                    frameSlider.setValue(frameSlider.getValue() - 50);
//                    ax = convertGravity(windAcceleration, pixelPerMeter, updateInterval);
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    windAcceleration += 0.1;
                    frameSlider.setValue(frameSlider.getValue() + 50);
//                    ax = convertGravity(windAcceleration, pixelPerMeter, updateInterval);
                    myDrawPanel.repaint();
                    break;
                case KeyEvent.VK_DELETE:
                    initialize(activeArrow.getLength(), activeArrow.getThickness());
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
            grap.setColor(Color.BLACK);
            Graphics2D g2d = (Graphics2D) grap;
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawLine(0, maxY, maxX, maxY);
            activePlayer.draw(g2d, transX, 0);
            // if player is pulling bow, draw pulled bow strings
            if (ready || arrowIsPulledBack) {
                double[] arrowCoords = activeArrow.getTipAndTail();
                activePlayer.drawBowString(g2d, (int) arrowCoords[2], (int) arrowCoords[3]);
            } else {
                activePlayer.drawDefaultBowString(grap);
            }
            idlePlayer.draw(grap, transX, 0);
            idlePlayer.drawDefaultBowString(grap);
            activeArrow.draw(grap, transX, 0, Color.BLACK);
            // draw all past arrows, the most recent one is green colour
            for (int i = 0; i < usedArrows.size(); i++) {
                Arrow a = usedArrows.get(i);
                if (i == usedArrows.size() - 1) {
                    a.draw(grap, transX, 0, Color.GREEN);
                } else {
                    a.draw(grap, transX, 0, Color.YELLOW);
                }

            }
            // if there is any text to show, display it in upper middle
            if (!textOnDisplay.isEmpty()) {
                grap.setColor(Color.BLACK);
                Font font = new Font("TimesRoman", Font.PLAIN, 32);
                grap.setFont(font);
                FontMetrics fontMetrics = grap.getFontMetrics(font);
                int strLen = fontMetrics.stringWidth(textOnDisplay);
                grap.drawString(textOnDisplay, (this.getWidth() / 2) - (strLen) / 2, 80);
            }
        }

    }
}
