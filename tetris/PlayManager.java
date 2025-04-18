package tetris;


// in this draw the play area
// manages tetrominoes
// handles gameplay action
// deleting lines , adding score etc

import mino.*;

import java.awt.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class PlayManager {

    // 25 *12
    // 25 * 20
    final int WIDTH = 300;
    final int HEIGHT = 550;

    public static int right_x;
    public static int left_x;
    public static int top_y;
    public static int bottom_y;

    // MINO
    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;

    Mino nextMino;  // created another mino and its x and y below
    final int NEXT_MINO_X;
    final int NEXT_MINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>(); // put the inactive mino in the static block

    // others

    public static int dropInterval = 40;
    public boolean gameOver;

    // effects

    boolean effectsCounterOn;

    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    int level = 1;
    int lines;
    int score;


    public PlayManager() {


        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2); // 1150/2  - 300/2
        right_x = left_x + WIDTH;
        top_y = 30;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXT_MINO_X = right_x + 180;
        NEXT_MINO_Y = top_y + 450;


        // set the starting mino

        currentMino = minoPicker();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = new Mino_Bar();
        nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);
    }

    private Mino minoPicker() {
        Mino mino = null;
        int i = new Random().nextInt(7); // used random class to generate mino (0-6)

        switch (i) {
            case 0:
                mino = new Mino_L1();
                break;
            case 1:
                mino = new Mino_L2();
                break;
            case 2:
                mino = new Mino_Square();
                break;
            case 3:
                mino = new Mino_Bar();
                break;
            case 4:
                mino = new Mino_T();
                break;
            case 5:
                mino = new Mino_Z1();
                break;
            case 6:
                mino = new Mino_Z2();
                break;
        }
        return mino;
    }

    public void update() {
        // check if the current is active
        if(KeyHandler.gamestart == false)
        {

        }
        else{
            if (currentMino.active == false) {
                // if not active add that in static block
                staticBlocks.add(currentMino.b[0]);
                staticBlocks.add(currentMino.b[1]);
                staticBlocks.add(currentMino.b[2]);
                staticBlocks.add(currentMino.b[3]);

                // game over check
                if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
// this means the current mino immediately collided a block and couldn't move at all
                    // so its x and y are same as next mino
                    // no space left so
                    // game is over
                    gameOver = true;
                    GamePanel.music.stop();
                    GamePanel.se.play(2,false);
                }

                currentMino.deactivating = false; // reset

                // replace the current mino with the nextMino

                currentMino = nextMino;
                currentMino.setXY(MINO_START_X, MINO_START_Y);
                nextMino = minoPicker();
                nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);

                // check if a mino is inactive and check if line can be deleted
                checkDelete();
            } else {
                currentMino.update();
            }
        }

    }

    private void checkDelete() {

        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount =0;

        // so maximum number of blocks are 12 if one line got 12 blocks we can delete line
        while (x < right_x && y < bottom_y) {

            for (int i = 0; i < PlayManager.staticBlocks.size(); i++) { // scanning
                if (staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    // increase the count
                    blockCount++;

                }
            }

            x += Block.SIZE;
            if (x == right_x) {

                // line filled and can be deleted
                if (blockCount == 12) {

                    effectsCounterOn = true;
                    effectY.add(y);

                    for (int i = staticBlocks.size() - 1; i > -1; i--) {
                        // remove all the blocks in current y line
                        if (staticBlocks.get(i).y == y) {
                            staticBlocks.remove(i);
                        }

                    }
                    lineCount++;
                    lines++;
                    // to increase level + speed after every 10 line

                    if(lines % 10 == 0 &&  dropInterval >1){
                        level++;
                        if(dropInterval >10){
                            dropInterval -=10;
                        }
                        else {
                            dropInterval -=1;
                        }
                    }

                    // a line has deleted so need to move down blocks
                    for (int i = 0; i < staticBlocks.size(); i++) {

                        // if a block is above the current y , move it down by the block size

                        if (staticBlocks.get(i).y < y) {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }
                blockCount = 0; // reset when x reaches the right x because it goes to the next row
                x = left_x;
                y += Block.SIZE;
            }
        }
        if(lineCount>0){
            GamePanel.se.play(1,false);
            int singleLineScore = 10 * level;
            score+=singleLineScore * lineCount;
        }
    }

    public void draw(Graphics2D g2) {


        // draw play area frame
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 2, WIDTH + 8, HEIGHT + 8);

        // another frame

        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("Next", x + 70, y + 60);

        // score
        g2.drawRect(x,top_y,250,300);
        x += 40;
        y = top_y +90;
        g2.drawString("Level : "+level,x,y);
        y+=70;
        g2.drawString("Lines : "+lines,x,y);
        y+=70;
        g2.drawString("Score : "+score,x,y);




        // draw the current Mino

        if (currentMino != null) {
            currentMino.draw(g2);
        }

        nextMino.draw(g2);  // show next mino

        // draw static blocks

        for (int i = 0; i < staticBlocks.size(); i++) { // scan the list and draw one by one
            staticBlocks.get(i).draw(g2);
        }

        if (effectsCounterOn) {
            effectCounter++;
            g2.setColor(Color.RED);

            for (int i = 0; i < effectY.size(); i++) {
                g2.fillRect(left_x, effectY.get(i), WIDTH, Block.SIZE);
            }
            if (effectCounter == 10) {

                effectsCounterOn = false;
                effectCounter = 0;
                effectY.clear();

            }

        }
        //Draw PowerUp notifier
        if(GamePanel.powerupInProgress){
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            g2.drawString("Powerup In Progress", 50, 200);
        }
        else if(GamePanel.powerupused){
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            g2.drawString("Powerup on cooldown", 50, 200);
        }
        else if(!GamePanel.powerupInProgress){
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            g2.drawString("Press S to use slowdown!", 50, 200);
        }



        // draw pause or gameover

        g2.setColor(Color.YELLOW);
        g2.setFont(g2.getFont().deriveFont(50f));
        if (gameOver) {
            x = left_x + 15;
            y = top_y + 320;
            g2.drawString("GAME OVER", x, y);

        } else if (KeyHandler.pausePressed) {
            x = left_x + 70;
            y = top_y + 320;
            g2.drawString("Paused", x, y);
        }
         x = 35;
         y = top_y +320;
         g2.setColor(Color.WHITE);
         g2.setFont(new Font("TimesNewRoman",Font.ITALIC,60));
         g2.drawString("Tetris in Java",x,y);

    }



}
