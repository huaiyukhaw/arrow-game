/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arrow_game;

/**
 *
 * @author Goh Jing Xuan
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class ArrowGame extends JFrame {

    private boolean testColorBlack = true;
    private boolean thickLine = false;
    private int X = 50;
    private int Y = 50;
    private int maxX = 1500;
    private int maxY = 900;
    private int minX = 0;
    private int minY = 0;
    int headSize, bodyLength, xx, yy;

    private int temporaryStore = 0;

    private int i = 0;
    private int j = 0;

    private int theTempXMouse = 0;
    private int theTempYMouse = 0;

    private int thePrevTempXMouse = 0;
    private int thePrevTempYMouse = 0;

    private int ballPosX;
    private int ballPosY;
    private boolean inCircle;
    private double gravity = 9.8;
    private double energyLost = 0.8;
    private int ballDropTime = 10;
    private final int height = 8;
    private double xVelocity;
    private double yVelocity;
    private double accelerationX;
    private double accelerationY;
    private final double pixelsPerMeter = 100;
    private double wind = 0.0;
    private JLabel label;
    private double ballHeight = 0.0;
    ArrayList<int[]> prevArrowPointHead = new ArrayList<>();
    ArrayList<int[]> prevArrowPointTail = new ArrayList<>();

    final drawPanel myDrawPanel;

    boolean start = true;

    Timer ballDrop;

    public ArrowGame() {
        myDrawPanel = new drawPanel();
        MyKeyListener myKeyListener = new MyKeyListener();
        this.add(myDrawPanel);
        addKeyListener(myKeyListener);
        this.setVisible(true);

        int windowXSize = maxX * 125 / 100;
        int windowYSize = maxY * 125 / 100;
        initializeScreen();

        this.setSize(windowXSize, windowYSize);

//      create mouse handler for ball movement
        MouseHandler myHandler = new MouseHandler();
        myDrawPanel.addMouseListener(myHandler);
        myDrawPanel.addMouseMotionListener(myHandler);

        myDrawPanel.setVisible(true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

//      convert the acceleration in pixels to meters
        accelerationX = convertGravity(wind, (int) pixelsPerMeter, ballDropTime);
        accelerationY = convertGravity(gravity, (int) pixelsPerMeter, ballDropTime);

//      timer to start the ball movement
        ballDrop = new Timer(ballDropTime, new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (ballPosX < 0 || ballPosX > maxX || ballPosY < 0 || ballPosY > maxY) {
                    yVelocity = 0;
                    xVelocity = 0;
                } else {
                    ballPosX += xVelocity;
                    ballPosY += yVelocity;
//                calculate the height of ball at that particular time
                    ballHeight = Math.abs(ballPosY - maxY) / pixelsPerMeter;
                    double prevVX = xVelocity;
                    double prevVY = yVelocity;
//                wind acceleration
                    xVelocity += accelerationX;
//                gravity acceleration
                    yVelocity += accelerationY;
                }

//                if ball hit the left and right border
                if (ballPosX < 0 || ballPosX > maxX || ballPosY < 0 || ballPosY > maxY) {
                    if (ballPosX < 0) {
                        ballPosX = 0;
                    } else if (ballPosX > maxX) {
                        ballPosX = maxX;
                    }
//                if ball hit the bottom and top border
                    if (ballPosY < 0) {
                        ballPosY = 0;
                    } else if (ballPosY > maxY) {
                        ballPosY = maxY;
                    }
                    ballDrop.stop();
                    int[] headArr = new int[]{xx, yy};
                    prevArrowPointHead.add(headArr);
                    int[] tailArr = new int[]{ballPosX, ballPosY};
                    prevArrowPointTail.add(tailArr);
                    initializeScreen();
                }
//                update the value in the JLabel
                repaint();
            }
        });

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
//            determine if user clicked on the ball
            start = false;
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            double tempX = (theTempXMouse - ballPosX) / (xx - ballPosX);
            double tempY = (theTempYMouse - ballPosY) / (yy - ballPosY);
//            double mousePos = Math.sqrt(Math.pow(theTempXMouse - ballPosX, 2) + Math.pow(theTempYMouse - ballPosY, 2));
            if (tempX == tempY) {
                inCircle = true;
                thePrevTempXMouse = theTempXMouse;
                thePrevTempYMouse = theTempYMouse;
            } else {
                inCircle = false;
            }

        }//end public void mousePressed(mouseEvent event)

        public void mouseReleased(MouseEvent event) {
//            once the mouse is released, start the timer for the ball movement
            if (inCircle) {
                theTempXMouse = event.getX();
                theTempYMouse = event.getY();
                int distX = theTempXMouse - thePrevTempXMouse;
                int distY = theTempYMouse - thePrevTempYMouse;
                xVelocity = distX * 2;
                yVelocity = distY * 2;
                ballDrop.start();
            }

        }//end public void mouseReleased(mouseEvent event)

        public void mouseEntered(MouseEvent event) {
//            theTempXMouse = 0;
//            theTempYMouse = 0;
        }//end public void mouseEntered(mouseEvent event)

        public void mouseExited(MouseEvent event) {
//            theTempXMouse = 0;
//            theTempYMouse = 0;
        }//end public void mouseExited(mouseEvent event)

        public void mouseDragged(MouseEvent event) {
//            throwing the ball
//            if user clicked on the ball update the position of the ball
            if (inCircle) {
                thePrevTempXMouse = theTempXMouse;
                thePrevTempYMouse = theTempYMouse;
                theTempXMouse = event.getX();
                theTempYMouse = event.getY();
                ballPosX = theTempXMouse;
                ballPosY = theTempYMouse;
//                to make sure the ball if not outside the border
                if (ballPosX > maxX) {
                    ballPosX = maxX;
                } else if (ballPosX < 0) {
                    ballPosX = 0;
                }
                if (ballPosY > maxY) {
                    ballPosY = maxY;
                } else if (ballPosY < 0) {
                    ballPosY = 0;
                }
                myDrawPanel.repaint();
            }
        }//end public void mouseDragged(mouseEvent event)

        public void mouseMoved(MouseEvent event) {
        }//end public void mouseMoved(mouseEvent event)
    }//end private class MouseHandler implements MouseListener, MouseMotionListener

    private class MyKeyListener implements KeyListener {

        public void keyPressed(KeyEvent event) {
//            contorl the wind acceleration
            switch (event.getKeyCode()) {
                case KeyEvent.VK_RIGHT:
                    wind -= 0.1;
                    break;
                case KeyEvent.VK_LEFT:
                    wind += 0.1;
                    break;
                case KeyEvent.VK_DELETE:
                    initializeScreen();
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
            //System.out.println("Repaint");
            int startX = 25;
            int startY = maxY - 175;
//              head
            grap.fillOval(startX, startY, headSize, headSize);
            Graphics2D g2d = (Graphics2D) grap;
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawLine(startX + 25, startY + 20, startX + 25, startY + 25 + bodyLength);
//            legs
            g2d.drawLine(startX + 25, startY + 25 + bodyLength, 10, maxY);
            g2d.drawLine(startX + 25, startY + 25 + bodyLength, startX + 25 + (startX + 25 - 10), maxY);
//            hand
            g2d.drawLine(startX + 25, startY + 25 + bodyLength / 2, 10, startY + 25 + bodyLength / 2);
            g2d.drawLine(startX + 25, startY + 25 + bodyLength / 2, startX + 25 + (startX + 25 - 10), startY + 25 + bodyLength / 2);

            grap.setColor(Color.BLACK);
            double angles = Math.atan2(xVelocity, yVelocity);
            if (start == false) {
                xx = (int) (ballPosX + (100 * Math.cos(angles)));
                yy = (int) (ballPosY + (100 * Math.sin(angles)));
            }

            int headX = xx;
            int headY = yy;
            int tailX = ballPosX;
            int tailY = ballPosY;
            //            land
            grap.drawLine(0, maxY, maxX, maxY);
            grap.setColor(Color.RED);
            grap.drawLine(headX, headY, tailX, tailY);
            if (prevArrowPointHead.size() > 0) {
                for (int i = 0; i < prevArrowPointHead.size(); i++) {
                    int[] headPoint = prevArrowPointHead.get(i);
                    int[] tailPoint = prevArrowPointTail.get(i);
                    if (i == prevArrowPointHead.size() - 1) {
                        grap.setColor(Color.GREEN);
                    } else {
                        grap.setColor(Color.ORANGE);
                    }
                    grap.drawLine(headPoint[0], headPoint[1], tailPoint[0], tailPoint[1]);
                }
            }

            //System.out.println("BLACK " + i + "  " + j);
        }
    }

//    initialize the size and position of ball
    private void initializeScreen() {

        start=true;
        ballPosX = 25 + 25 + (25 + 25 - 10);
        ballPosY = maxY - 90;
        xx = (int) (ballPosX + (100 * Math.cos(15)));
        yy = (int) (ballPosY + (100 * Math.sin(15)));

        headSize = 50;
        bodyLength = 125;
    }

    public double convertGravity(double gravity, int pixelPerMeter, int ballDropTime) {
        // convert unit of gravity from m/s^2 to pixel/frameupdate^2
        return gravity * pixelPerMeter * ((double) ballDropTime / 1000 * (double) ballDropTime / 1000);
    }

    public static void main(String args[]) {
        new ArrowGame();
    }
}
