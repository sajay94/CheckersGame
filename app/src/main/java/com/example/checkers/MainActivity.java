package com.example.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
     * button. For example, "00" represents the top left button and "77" represents bottom left. Integer is the id*/
    private Map<String, Integer> buttonId;

    /** Basically a subset of the buttonId map only including the buttons which have a counter on the them. */
    private Map<String, Integer> pieceLocations;

    private Button recent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinates = new ArrayList<>();
        buttonId = new HashMap<>();
        pieceLocations = new HashMap<>();
        gameBoard = findViewById(R.id.gameLayout);
        recent = null;

        /**Iterates through activity_main.xml and creates a Cell object for each button.
         * This is necessary in order to pass the List to other activities (i.e GameActivity).
         *
         */
        for (int x = 0; x < gameBoard.getChildCount(); x++) {
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
        if (pieceLocations.containsValue(view.getId())) {
            recent = (Button) view;
            Toast.makeText(this, "Recent set", Toast.LENGTH_SHORT).show();
        } else if (recent != null) {
            view.setForeground(recent.getForeground());
            pieceLocations.remove(recent.getTag().toString().substring(7));
            pieceLocations.put(view.getTag().toString().substring(7), view.getId());
            recent.setForeground(null);
            recent = null;
        }
    }
    public void fillStartBoard(int start, int end) {
        for (int x = start; x < end; x++) {
            for (int y = 0; y < 8; y++) {
                if ((x % 2 == 0 && y % 2 == 1) || (x % 2 == 1 && y % 2 == 0)) {
                    fillCell(x , y);
                }
            }
        }
    }

    public void fillCell(int x, int y) {
        String tag = "" + x + y;
        int id = buttonId.get(tag);
        Button b = findViewById(id);
        if (x < 4) {
            b.setForeground(getResources().getDrawable(R.drawable.checker_piece1, null));
            pieceLocations.put(tag, id);
        } else {
            b.setForeground(getResources().getDrawable(R.drawable.checker_piece2, null));
            pieceLocations.put(tag, id);
        }
    }
}
