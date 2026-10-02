import javax.swing.*;                       // Swing GUI classes: JFrame, JPanel, Timer, SwingUtilities
import java.awt.*;                          // drawing classes: Graphics, Color, Font, Point, Dimension
import java.awt.event.*;                    // event classes: KeyEvent, ActionEvent, listeners
import java.util.ArrayDeque;                // a list we can add/remove at both ends quickly
import java.util.Deque;                     // the interface type for ArrayDeque
import java.util.Random;                    // random numbers for food placement

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    static final int N = 20;                // the board is N x N cells
    static final int CELL = 24;             // each cell is 24 x 24 pixels

    static final int START_DELAY = 180;     // ms per step at the start (bigger = slower)
    static final int MIN_DELAY = 55;        // fastest the game is allowed to get
    static final int SPEEDUP = 8;           // how many ms faster each food makes the game
    static final int GROWTH = 1;            // how many segments the snake gains per food

    private final Deque<Point> snake = new ArrayDeque<>();  // snake body; first = head, last = tail
    private final Random rnd = new Random();                // random number generator
    private final Timer timer = new Timer(START_DELAY, this); // game loop, starts at the slow speed
    private Point food;                     // current food position (in grid cells)
    private int dx, dy;                     // direction of movement (-1, 0, or 1 on each axis)
    private int nextDx, nextDy;             // direction the player has requested for the next step
    private int score, best;                // current score and best score this session
    private int pendingGrowth;              // how many more segments the snake still has to grow
    private boolean started, gameOver;
    // game state flags

    public SnakeGame() {                    // constructor: runs once when the panel is created
        setPreferredSize(new Dimension(N * CELL, N * CELL + 30)); // board plus a 30px strip for the score
        setBackground(new Color(0x1e2420)); // dark background
        setFocusable(true);                 // lets this panel receive keyboard focus
        addKeyListener(this);               // send key events to this class
        reset();                            // set up a fresh game
    }

    private void reset() {                  // puts everything back to the starting state
        snake.clear();                      // remove old snake segments
        snake.add(new Point(10, 10));       // head
        snake.add(new Point(9, 10));        // body
        snake.add(new Point(8, 10));        // tail
        dx = 1;                             // start moving right
        dy = 0;                             // no vertical movement
        nextDx = 1;                         // requested direction also starts as right
        nextDy = 0;
        score = 0;                          // reset the score
        pendingGrowth = 0;                  // no growth waiting
        started = false;                    // waiting for the first key press
        gameOver = false;                   // not game over
        timer.setDelay(START_DELAY);        // back to the slow starting speed
        placeFood();                        // put food somewhere
        timer.stop();                       // make sure the loop isn't running
        repaint();                          // redraw the screen
    }

    private void placeFood() {              // picks a random empty cell for the food
        do {
            food = new Point(rnd.nextInt(N), rnd.nextInt(N)); // random cell
        } while (snake.contains(food));     // retry if it landed on the snake
    }

    @Override
    public void actionPerformed(ActionEvent e) {   // called every tick: one game step
        dx = nextDx;                        // lock in the requested direction for this step
        dy = nextDy;
        Point head = snake.peekFirst();     // current head
        Point next = new Point(head.x + dx, head.y + dy); // where the head will be next

        boolean hitWall = next.x < 0 || next.y < 0 || next.x >= N || next.y >= N; // outside the board?
        if (hitWall || snake.contains(next)) {     // hit a wall or itself
            gameOver = true;                // mark game over
            started = false;                // no longer running
            best = Math.max(best, score);   // update the best score
            timer.stop();                   // stop the game loop
        } else {                            // safe move
            snake.addFirst(next);           // add the new head
            if (next.equals(food)) {        // ate the food?
                score++;                    // +1 point
                pendingGrowth += GROWTH;    // queue up growth
                // new delay = start delay minus a bit per food, but never below the minimum
                timer.setDelay(Math.max(MIN_DELAY, START_DELAY - score * SPEEDUP));
                placeFood();                // spawn new food
            }
            if (pendingGrowth > 0) {        // still need to grow?
                pendingGrowth--;            // keep the tail this step (snake gets longer)
            } else {
                snake.removeLast();         // otherwise drop the tail (length stays the same)
            }
        }
        repaint();                          // redraw after every step
    }

    @Override
    protected void paintComponent(Graphics g) {    // Swing calls this whenever the panel needs drawing
        super.paintComponent(g);            // clear with the background color
        Graphics2D g2 = (Graphics2D) g;     // Graphics2D has nicer drawing features
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth edges

        g2.setColor(new Color(0xff7a5c));   // food color
        g2.fillOval(food.x * CELL + 3, food.y * CELL + 3, CELL - 6, CELL - 6); // food circle

        boolean head = true;                // first segment is the head
        for (Point p : snake) {             // every segment, head to tail
            g2.setColor(head ? new Color(0x8be0b0) : new Color(0x5fc088)); // lighter head
            g2.fillRoundRect(p.x * CELL + 1, p.y * CELL + 1, CELL - 2, CELL - 2, 8, 8); // rounded square
            head = false;                   // the rest are body
        }

        g2.setColor(Color.WHITE);           // white text
        g2.setFont(new Font("SansSerif", Font.BOLD, 14)); // score font
        int level = 1 + (START_DELAY - timer.getDelay()) / SPEEDUP; // speed level shown to the player
        g2.drawString("Score: " + score + "    Best: " + best + "    Speed: " + level, 8, N * CELL + 20);

        g2.setFont(new Font("SansSerif", Font.BOLD, 18)); // message font
        if (gameOver) {
            drawCentered(g2, "Game over! Press an arrow key to retry");
        } else if (!started) {
            drawCentered(g2, "Arrow keys / WASD to start");
        }
    }

    private void drawCentered(Graphics2D g2, String text) {   // helper: centered text on a dark box
        FontMetrics fm = g2.getFontMetrics();                  // for measuring text
        int x = (N * CELL - fm.stringWidth(text)) / 2;         // left edge so it's centered
        g2.setColor(new Color(0, 0, 0, 150));                  // semi-transparent black
        g2.fillRect(x - 10, N * CELL / 2 - 24, fm.stringWidth(text) + 20, 36); // background box
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, N * CELL / 2);                  // the text itself
    }

    @Override
    public void keyPressed(KeyEvent e) {    // called whenever a key goes down
        int ndx = dx, ndy = dy;             // start from the current direction
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> { ndx = 0; ndy = -1; }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> { ndx = 0; ndy = 1; }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> { ndx = -1; ndy = 0; }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> { ndx = 1; ndy = 0; }
            default -> { return; }          // ignore other keys
        }
        if (gameOver) reset();              // a direction key after game over starts a new game
        if (ndx + dx != 0 || ndy + dy != 0) {  // block 180-degree reversals
            nextDx = ndx;
            nextDy = ndy;
        }
        if (!started) {                     // first key press of a game
            started = true;
            timer.start();                  // start the game loop
        }
    }

    @Override public void keyReleased(KeyEvent e) {}   // required by KeyListener, unused
    @Override public void keyTyped(KeyEvent e) {}      // required by KeyListener, unused

    public static void main(String[] args) {           // program entry point
        SwingUtilities.invokeLater(() -> {            // build the GUI on Swing's thread
            JFrame frame = new JFrame("Tiny Snake");   // the window
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closing quits the program
            frame.add(new SnakeGame());                // put the game inside the window
            frame.pack();                              // size window to fit the panel
            frame.setResizable(false);                 // fixed size
            frame.setLocationRelativeTo(null);         // center on screen
            frame.setVisible(true);                    // show it
        });
    }
}