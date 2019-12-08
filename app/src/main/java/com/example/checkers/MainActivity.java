package com.example.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MyActivity";

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
     * If a blue king owns it, a 3 is stored, and if a orange king owns it, a 4 is stored
     * Used to determine whether a given cell should have it's piece removed */
    private Map<String, Integer> playerLocations;

    /** Either 1 or 2, the identifying value of the player using the phone.
     * Should be 2/orange if the player created the game, 1/blue if they joined. */
    private int userID;

    /** Will toggle between 2 and 1 as play goes on */
    private int currentPlayerID;

    private int madeMoves;

    private Button recent;

    private boolean endTurn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinates = new ArrayList<>();
        buttonId = new HashMap<>();
        pieceLocations = new HashMap<>();
        playerLocations = new HashMap<>();
        madeMoves = 0;
        endTurn = false;
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
        //sets endTurn button onClick;
        findViewById(R.id.endTurn).setOnClickListener(this);
        //Fills the buttonId map
        for (Cell c : coordinates) {
            String yx = c.getTag().substring(7);
            buttonId.put(yx, c.getId());
        }
        fillStartBoard(0, 3);
        fillStartBoard(5, 8);

//        Intent intent = new Intent(this, GameActivity.class);
//        intent.putParcelableArrayListExtra("coordinates", (ArrayList<? extends Parcelable>) coordinates);
//        startActivity(intent);
    }

    @Override
    public void onClick(View view) { // I can clean this up later
        /**First checks if the clicked button contains a checker. If it does, that button is marked by storing it
         * in the recent button. If it does not, it checks whether recent contains a button and fills the clicked
         * button with recent's drawable. So we need to revise the code and in the if else and move it into a seperate
         * method for convenience
         */
        //NOTE : minor bug where king piece gets an extra move.
        int currPlayerId = playerLocations.get(recent.getTag().toString().substring(7));
        if (recent != null && !view.getTag().toString().equals("endTurn")) {
            Toast.makeText(this, Integer.toString(playerLocations.get(recent.getTag().toString().substring(7))), Toast.LENGTH_SHORT).show();
            if (currPlayerId == 3 || currPlayerId == 4 && !endTurn) {
                Button store = recent;
                movePiece(view);
                if (currentPlayerID % 2 != currPlayerId % 2) {
                    recent = (Button) view;
                    togglePlayers();
                    madeMoves++;
                } else {
                    recent = store;
                }
            } else {
                movePiece(view);
                endTurn = false;
                Log.i(TAG, "here");
            }
        } else if (pieceLocations.containsValue(view.getId())) { /** Here is where we're going to check current player against the user player */
            if (currentPlayerID % 2 == playerLocations.get(view.getTag().toString().substring(7)) % 2) { /** this part may be redundant once we implement the HTTP move making */
                recent = (Button) view;
            }
        }
        //Allows King piece to end a sequence of moves earlier
        if (view.getTag().toString().equals("endTurn") && madeMoves > 0) {
            madeMoves = 0;
            togglePlayers();
        }
        //Declares winner - not working yet
        if (pieceLocations.size() < 12 && checkWinner() > 0) {
            Toast.makeText(this, "Game Over Player " + checkWinner() + " wins", Toast.LENGTH_SHORT).show();
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
        if (((newY % 2 == 0 && newX % 2 == 1) || (newY % 2 == 1 && newX % 2 == 0)) && followsRules(prevCell, newCell)) { // only allow moves to brown squares
            if ((newY == oldY - 1) && (newX == oldX - 1 || newX == oldX + 1) ||
                    (newY == oldY + 1) && (newX == oldX - 1 || newX == oldX + 1)) {// only allow moves to directly diagonal squares
                if (pieceLocations.get(newCell) == null) { // makes sure potential cell is not occupied
                    int prevCellId = playerLocations.get(prevCell);
                    if (prevCellId == 3 || prevCellId == 4) {
                        endTurn = true;
                    }
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
                // Toast.makeText(this, Integer.toString(view.getId()), Toast.LENGTH_SHORT).show();
            } else if (Math.abs(newY - oldY) == 2 && Math.abs(newX - oldX) == 2) {
                String middle = "" + (oldY + ((newY - oldY) / 2)) + (oldX + ((newX - oldX) / 2));
                if (pieceLocations.containsKey(middle)) {
                    findViewById(pieceLocations.get(middle)).setForeground(null);
                    pieceLocations.remove(middle);
                    playerLocations.remove(middle);
                    moveHelper(view, prevCell, newCell);
                    togglePlayers();
                }
            }
            toKing(view, newY, newCell);
        }
    }

    public void moveHelper(View view, String prevCell, String newCell) {
        view.setForeground(recent.getForeground());
        playerLocations.put(newCell, playerLocations.get(recent.getTag().toString().substring(7)));
        pieceLocations.remove(prevCell);
        playerLocations.remove(prevCell);
        pieceLocations.put(newCell, view.getId());
        // Be careful about this
        recent.setForeground(null);
        recent = null;
    }
    //ensures that a piece moving only moves forward and follows rules of checkers
    public boolean followsRules(String prevCell, String newCell) {
        int newY = Integer.parseInt(newCell.substring(0,1));
        int newX = Integer.parseInt(newCell.substring(1));
        int oldY = Integer.parseInt(prevCell.substring(0,1));
        int oldX = Integer.parseInt(prevCell.substring(1));
        if (playerLocations.get(newCell) != null) {
            recent = null;
            return false;
        } else if (Math.abs(newY - oldY) == 2 && Math.abs(newX - oldX) == 2) { // can't jump over piece of same color
            String middle = "" + (oldY + ((newY - oldY) / 2)) + (oldX + ((newX - oldX) / 2));
            if (playerLocations.get(middle) % 2 == playerLocations.get(prevCell) % 2) {
                recent = null;
                return false;
            }
        }
        if (playerLocations.get(prevCell) == 1) {
            if (newY <= oldY) {
                return false;
            }
        } else if (playerLocations.get(prevCell) == 2) {
            if (newY >= oldY) {
                return false;
            }
        }
        return true;
    }
    //converts a regular piece to a king if it makes it to the other side
    public void toKing(View view, int newY, String newCell) {
        if (newY == 7 && playerLocations.get(newCell) == 1) {
            view.setForeground(getResources().getDrawable(R.drawable.checker_piece1_king, null));
            playerLocations.remove(newCell);
            pieceLocations.remove(newCell);
            playerLocations.put(newCell, 3);
            pieceLocations.put(newCell, view.getId());
            Toast.makeText(this, Integer.toString(playerLocations.get(newCell)), Toast.LENGTH_SHORT).show();

        } else if (newY == 0 && playerLocations.get(newCell) == 2) {
            view.setForeground(getResources().getDrawable(R.drawable.checker_piece2_king, null));
            playerLocations.remove(newCell);
            pieceLocations.remove(newCell);
            playerLocations.put(newCell, 4);
            pieceLocations.put(newCell, view.getId());
            //Toast.makeText(this, Integer.toString(playerLocations.get(newCell)), Toast.LENGTH_SHORT).show();
        }
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
    public int checkWinner() {
        if (pieceLocations.containsValue(1)) {
            for (Map.Entry<String, Integer> Entry : pieceLocations.entrySet()) {
                if (Entry.getValue() % 2 != 1) {
                    return 1;
                }
            }
        } else if (pieceLocations.containsValue(2)) {
            for (Map.Entry<String, Integer> Entry : pieceLocations.entrySet()) {
                if (Entry.getValue() % 2 != 0) {
                    return 2;
                }
            }
        }
        return 0;
    }
}



//
//    public void kingMoveHelper(View view) {
//        view.setForeground(recent.getForeground());
//        for (String str : kingPieceMoves) {
//            findViewById(pieceLocations.get(str)).setForeground(null);
//            pieceLocations.remove(str);
//            playerLocations.remove(str);
//        }
//        kingPieceMoves.clear();
//        pieceLocations.put(view.getTag().toString().substring(7), view.getId());
//        playerLocations.put(view.getTag().toString().substring(7),
//                playerLocations.get(recent.getTag().toString().substring(7)));
//        recent.setForeground(null);
//        recent = null;
//    }
//
//    public boolean kingPieceValidMove(View view) {
//        if (view.getTag().equals(recent.getTag())) {
//            return true;
//        } else {
//            String curr = view.getTag().toString().substring(7);
//            int posX = Integer.parseInt(curr.substring(0,1));
//            int posY = Integer.parseInt(curr.substring(1));
//            for (int x = -1; x <= 1; x+=2) {
//                for (int y = -1; y <= 1; y+=2) {
//                    if (y + y + posY >= 0 && x + x + posX >= 0) {
//                        String xy = "" + (x + posX) + (y + posY);
//                        String move = "" + (x + x + posX) + (y + y + posY);
//                        if (playerLocations.get(xy) != null
//                                && buttonId.get(move) != null
//                                && (playerLocations.get(move) == null
//                                || playerLocations.get(move) == getPlayerId(recent))
//                                && playerLocations.get(xy) != currentPlayerID
//                                && !kingPieceMoves.contains(xy)) {
//                            Log.i(TAG, move);
//                            kingPieceMoves.add(xy);
//                            if (!kingPieceValidMove(findViewById(buttonId.get(move)))) {
//                                kingPieceMoves.remove(xy);
//                            } else {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

//checks whether the king piece
//public boolean kingPieceCanCapture(View view) {
//    String cell = view.getTag().toString().substring(7);
//    int newX = Integer.parseInt(cell.substring(0,1));
//    int newY = Integer.parseInt(cell.substring(1));
//    for (int x = -1; x <= 1; x += 2) {
//        for (int y = -1; y <=1; y += 2) {
//            String xy = "" + (x + newX) + (y + newY);
//            String move = "" + (x + x + newX) + (y + y + newY);
//            if (!move.contains("-") && !xy.contains("-")
//                    && playerLocations.get(xy) != null && playerLocations.get(cell) != null) {
//                //Log.i(TAG, playerLocations.get(xy) + " " + playerLocations.get(cell));
//                if ((playerLocations.get(xy) % 2) != (playerLocations.get(cell) % 2)
//                        && !pieceLocations.containsKey(move)) {
//                    return true;
//                }
//            }
//        }
//    }
//    return false;
//}

// Inner of movePiece just in case we need it
//            else if ((newY == oldY - 2) && (newX == oldX - 2)) { //if the move is a jump, check to see if middle is occupied by other player
//                String middle = "" + (oldY - 1) + (oldX - 1);
//                if (pieceLocations.containsKey(middle)) {
//                    findViewById(pieceLocations.get(middle)).setForeground(null);
//                    pieceLocations.remove(middle);
//                    playerLocations.remove(middle);
//                    moveHelper(view, prevCell, newCell);
//                    togglePlayers();
//                }
//            } else if ((newY == oldY - 2) && (newX == oldX + 2)) {
//                String middle = "" + (oldY - 1) + (oldX + 1);
//                if (pieceLocations.containsKey(middle)) {
//                    findViewById(pieceLocations.get(middle)).setForeground(null);
//                    pieceLocations.remove(middle);
//                    playerLocations.remove(middle);
//                    moveHelper(view, prevCell, newCell);
//                    togglePlayers();
//                }
//            } else if ((newY == oldY + 2) && (newX == oldX - 2)) {
//                String middle = "" + (oldY + 1) + (oldX - 1);
//                if (pieceLocations.containsKey(middle)) {
//                    findViewById(pieceLocations.get(middle)).setForeground(null);
//                    pieceLocations.remove(middle);
//                    playerLocations.remove(middle);
//                    moveHelper(view, prevCell, newCell);
//                    togglePlayers();
//                }
//            } else if ((newY == oldY + 2) && (newX == oldX + 2)) {
//                String middle = "" + (oldY + 1) + (oldX + 1);
//                if (pieceLocations.containsKey(middle)) {
//                    findViewById(pieceLocations.get(middle)).setForeground(null);
//                    pieceLocations.remove(middle);
//                    playerLocations.remove(middle);
//                    moveHelper(view, prevCell, newCell);
//                    togglePlayers();
//                }
//            }
