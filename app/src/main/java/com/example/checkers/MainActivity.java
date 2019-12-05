package com.example.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /** The LinearLayout which conatins the board. It holds basically the entirety of the activity_main.xml*/
    private LinearLayout gameBoard;

    /** A list of a Parcelable object called Cell. Cells contain information about each button's tag and id.
     * See Cell class for more info */
    private List<Cell> coordinates;

    /** A map of every Button on the board (64). The String is a string of two ints representing the position of the
     * button. For example, "00" represents the top left button and "77" represents bottom right. Integer is the id*/
    private Map<String, Integer> buttonId;

    /** Basically a subset of the buttonId map only including the buttons which have a counter on the them. */
    private Map<String, Integer> pieceLocations;

    /** The same as pieceLocations, but instead of storing the id of the cell, stores which player owns it
     * If the blue player owns it, a 1 is stored, and if the orange player owns it, a 2 is stored, as with the drawables.
     * Used to determine whether a given cell should have it's piece removed */
    private Map<String, Integer> playerLocations;

    /** Either 1 or 2, the identifying value of the player using the phone.
     * Should be 2/orange if the player created the game, 1/blue if they joined. */
    private int userID;

    /** Will toggle between 2 and 1 as play goes on */
    private int currentPlayerID;

    private Button recent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinates = new ArrayList<>();
        buttonId = new HashMap<>();
        pieceLocations = new HashMap<>();
        playerLocations = new HashMap<>();
        gameBoard = findViewById(R.id.gameLayout);
        userID = 2; /** Right here I hardcoded this as 2, but really this will be set depending on a value passed in the intent. */
        currentPlayerID = 2; // this though should always START as 2, and toggle every move
        recent = null;

        /**Iterates through activity_main.xml and creates a Cell object for each button.
         * This is necessary in order to pass the List to other activities (i.e GameActivity).
         */
        for (int x = 0; x < 8; x++) {
            LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(x);
            for (int y = 0; y < rowLayout.getChildCount(); y++) {
                Button curr = (Button) rowLayout.getChildAt(y);
                curr.setOnClickListener(this);
                Cell cell = new Cell(curr.getId(), curr.getTag().toString());
                coordinates.add(cell);
            }

        }
        // The rest of this should be in GameActivity

        //Fills the buttonId map
        for (Cell c : coordinates) {
            String xy = c.getTag().substring(7);
            buttonId.put(xy, c.getId());
        }
        fillStartBoard(0, 3);
        fillStartBoard(5, 8);

//        Intent intent = new Intent(this, GameActivity.class);
//        intent.putParcelableArrayListExtra("coordinates", (ArrayList<? extends Parcelable>) coordinates);
//        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        /**First checks if the clicked button contains a checker. If it does, that button is marked by storing it
         * in the recent button. If it does not, it checks whether recent contains a button and fills the clicked
         * button with recent's drawable. So we need to revise the code and in the if else and move it into a seperate
         * method for convenience
         */
        if (pieceLocations.containsValue(view.getId())) { /** Here is where we're going to check current player against the user player */
            if (currentPlayerID == playerLocations.get(view.getTag().toString().substring(7))) { /** this part may be redundant once we implement the HTTP move making */
                recent = (Button) view;
                // Toast.makeText(this, "Recent set", Toast.LENGTH_SHORT).show();
            }
        } else if (recent != null) {
            movePiece(view);
        }
    }
    /** Logic for moving a piece from one xy to another, including capturing other pieces.*/
    public void movePiece(View view) {
        // Toast below shows the clicked button's id for debugging / me figuring this out
        // Toast.makeText(this, newCell, Toast.LENGTH_SHORT).show();
        String newCell = view.getTag().toString().substring(7); //holds the id of the potential cell to be moved to
        String prevCell = recent.getTag().toString().substring(7); // id of current cell
        int newY = Integer.parseInt(newCell.substring(0,1));
        int newX = Integer.parseInt(newCell.substring(1));
        int oldY = Integer.parseInt(prevCell.substring(0,1));
        int oldX = Integer.parseInt(prevCell.substring(1));
        if ((newY % 2 == 0 && newX % 2 == 1) || (newY % 2 == 1 && newX % 2 == 0)) { // only allow moves to brown squares
            if ((newY == oldY - 1) && (newX == oldX - 1 || newX == oldX + 1) ||
                    (newY == oldY + 1) && (newX == oldX - 1 || newX == oldX + 1)) { // only allow moves to directly diagonal squares
                moveHelper(view, prevCell, newCell);
                togglePlayers();
                // Toast.makeText(this, Integer.toString(view.getId()), Toast.LENGTH_SHORT).show();
            } else if ((newY == oldY - 2) && (newX == oldX - 2)) { //if the move is a jump, check to see if middle is occupied by other player
                String middle = "" + (oldY - 1) + (oldX - 1);
                if (pieceLocations.containsKey(middle)) {
                    findViewById(pieceLocations.get(middle)).setForeground(null);
                    pieceLocations.remove(middle);
                    playerLocations.remove(middle);
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
            } else if ((newY == oldY - 2) && (newX == oldX + 2)) {
                String middle = "" + (oldY - 1) + (oldX + 1);
                if (pieceLocations.containsKey(middle)) {
                    findViewById(pieceLocations.get(middle)).setForeground(null);
                    pieceLocations.remove(middle);
                    playerLocations.remove(middle);
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
            } else if ((newY == oldY + 2) && (newX == oldX - 2)) {
                String middle = "" + (oldY + 1) + (oldX - 1);
                if (pieceLocations.containsKey(middle)) {
                    findViewById(pieceLocations.get(middle)).setForeground(null);
                    pieceLocations.remove(middle);
                    playerLocations.remove(middle);
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
            } else if ((newY == oldY + 2) && (newX == oldX + 2)) {
                String middle = "" + (oldY + 1) + (oldX + 1);
                if (pieceLocations.containsKey(middle)) {
                    findViewById(pieceLocations.get(middle)).setForeground(null);
                    pieceLocations.remove(middle);
                    playerLocations.remove(middle);
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
            }
        }
    }

    public void moveHelper(View view, String prevCell, String newCell) {
        view.setForeground(recent.getForeground());
        pieceLocations.remove(prevCell);
        playerLocations.remove(prevCell);
        pieceLocations.put(newCell, view.getId());
        playerLocations.put(newCell, currentPlayerID); // Be careful about this
        recent.setForeground(null);
        recent = null;
    }
    /** flip flop the current players, call every time a move is successfully made */
    public void togglePlayers() {
        if (currentPlayerID == 1) {
            currentPlayerID = 2;
            TextView current = findViewById(R.id.currentPlayer);
            current.setText(Integer.toString(currentPlayerID));

        } else {
            currentPlayerID = 1;
            TextView current = findViewById(R.id.currentPlayer);
            current.setText(Integer.toString(currentPlayerID));
        }
    }
    /** Fills the the board by placing pieces on both sides. */
    public void fillStartBoard(int start, int end) {
        for (int x = start; x < end; x++) {
            for (int y = 0; y < 8; y++) {
                if ((x % 2 == 0 && y % 2 == 1) || (x % 2 == 1 && y % 2 == 0)) {
                    fillCell(x , y);
                }
            }
        }
    }
    /** Fills an individual with the piece drawable. Should only be called at the beginning of the game. */
    public void fillCell(int x, int y) {
        String tag = "" + x + y;
        int id = buttonId.get(tag);
        Button b = findViewById(id);
        if (x < 4) {
            b.setForeground(getResources().getDrawable(R.drawable.checker_piece1, null));
            pieceLocations.put(tag, id);
            playerLocations.put(tag, 1);
        } else {
            b.setForeground(getResources().getDrawable(R.drawable.checker_piece2, null));
            pieceLocations.put(tag, id);
            playerLocations.put(tag, 2);
        }
    }
}
