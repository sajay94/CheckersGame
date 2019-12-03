package com.example.checkers;


import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

//    private List<Integer>  buttonId;
    private Map<String, Integer> buttonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonId = new HashMap<>();
        Bundle extras = getIntent().getExtras();
        List<Cell> coors = extras.getParcelableArrayList("coordinates");

        for (Cell c : coors) {
            String xy = c.getTag().substring(7);
            buttonId.put(xy, c.getId());
        }
        fillStartBoard(0, 3);
        fillStartBoard(5, 8);

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
        b.setForeground(getResources().getDrawable(R.drawable.checker_piece1, null));
    }

}
