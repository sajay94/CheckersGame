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

    private LinearLayout gameBoard;
    private List<Cell> coordinates;
    private Map<String, Integer> buttonId;
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

        for (int x = 0; x < gameBoard.getChildCount(); x++) {
            LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(x);
            for (int y = 0; y < rowLayout.getChildCount(); y++) {
                Button curr = (Button) rowLayout.getChildAt(y);
                curr.setOnClickListener(this);
                Cell cell = new Cell(curr.getId(), curr.getTag().toString());
                coordinates.add(cell);
            }

        }
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
