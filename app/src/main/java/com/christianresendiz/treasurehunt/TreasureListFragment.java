package com.christianresendiz.treasurehunt;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

public class TreasureListFragment extends Fragment {

    TextView t1;
    TextView t2;
    TextView t3;
    TextView instruct;
    private Random rand = new Random();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.treasure_list_fragment, container, false);

        instruct = (TextView) view.findViewById(R.id.instruct);
        t1 = (TextView) view.findViewById(R.id.t1);
        t2 = (TextView) view.findViewById(R.id.t2);
        t3 = (TextView) view.findViewById(R.id.t3);

        return view;
    }

    public static String[] fortunes = {

            //Colors
            "RED",
            "BLUE",
            "PURPLE",
            "PINK",
            "ORANGE",
            "YELLOW",
            "GREEN",
            "BLACK",
            "WHITE",
            "BROWN",

            //Body
            "MAN",
            "FACE",
            "MUSCLE",
            "ARM",
            "HAIR",
            "LEG",
            "HAND",
            "EAR",
            "EYE",
            "FINGER",
            "GIRL",
            "BEAUTY",
            "NOSE",
            "SKIN",
            "PERSON",

            //Surroundings
            "WINDOW",
            "TELEVISION",
            "GADGET",
            "PLANT",
            "NATURE",
            "FLOWER",
            "HOUSE",
            "DOOR",
            "KEYBOARD",
            "TREE",
            "BIRD",
            "WRITING",
            "WALL",
            "ROOM",
            "DOG",
            "CAT",
            "CAR",
            "DARKNESS",
            "TECHNOLOGY",
            "COMPUTER",
            "GLASSES",
            "WOOD",
            "LAPTOP",
            "MOBILE PHONE",
            "CHAIR",
    };

    public String getFortune(int dif){
        if(dif == 1)
            return fortunes[rand.nextInt(10)];
        else if(dif ==2)
            return fortunes[rand.nextInt(25)];
        else
            return fortunes[rand.nextInt(fortunes.length)];
    }

    public void checkDup(int dif){
        do{
            t1.setText(getFortune(dif));
            t2.setText(getFortune(dif));
            t3.setText(getFortune(dif));
        }
        while(t1.getText().equals(t2.getText()) || t1.getText().equals(t3.getText()) || t2.getText().equals(t3.getText()) || t1.getText().equals(t3.getText()) && t2.getText().equals(t3.getText()));
    }
    public void getFortunes(int dif){
        t1.setText(getFortune(dif));
        t2.setText(getFortune(dif));
        t3.setText(getFortune(dif));
        checkDup(dif);
    }

    public void resetFlags(){
        t1.setPaintFlags(t1.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        t2.setPaintFlags(t2.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        t3.setPaintFlags(t3.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
    }

    public void colorText(){
        //Treasure 1 color
        if(t1.getText().equals("RED"))
            t1.setTextColor(getResources().getColor(R.color.red));
        else if(t1.getText().equals("BLUE"))
            t1.setTextColor(getResources().getColor(R.color.blue));
        else if(t1.getText().equals("YELLOW"))
            t1.setTextColor(getResources().getColor(R.color.yellow));
        else if(t1.getText().equals("GREEN"))
            t1.setTextColor(getResources().getColor(R.color.green));
        else if(t1.getText().equals("ORANGE"))
            t1.setTextColor(getResources().getColor(R.color.orange));
        else  if(t1.getText().equals("PURPLE"))
            t1.setTextColor(getResources().getColor(R.color.purple));
        else if(t1.getText().equals("PINK"))
            t1.setTextColor(getResources().getColor(R.color.pink));
        else if(t1.getText().equals("BLACK"))
            t1.setTextColor(getResources().getColor(R.color.black));
        else if(t1.getText().equals("WHITE"))
            t1.setTextColor(getResources().getColor(R.color.white));
        else if(t1.getText().equals("BROWN"))
            t1.setTextColor(getResources().getColor(R.color.brown));
        else{
            int r1 = rand.nextInt(155) + 100;
            int g1 = rand.nextInt(155) + 100;
            int b1 = rand.nextInt(155) + 100;
            int randomColor1 = Color.rgb(r1,g1,b1);
            t1.setTextColor(randomColor1);
        }

        //Treasure 2 color
        if(t2.getText().equals("RED"))
            t2.setTextColor(getResources().getColor(R.color.red));
        else if(t2.getText().equals("BLUE"))
            t2.setTextColor(getResources().getColor(R.color.blue));
        else if(t2.getText().equals("YELLOW"))
            t2.setTextColor(getResources().getColor(R.color.yellow));
        else if(t2.getText().equals("GREEN"))
            t2.setTextColor(getResources().getColor(R.color.green));
        else if(t2.getText().equals("ORANGE"))
            t2.setTextColor(getResources().getColor(R.color.orange));
        else if(t2.getText().equals("PURPLE"))
            t2.setTextColor(getResources().getColor(R.color.purple));
        else if(t2.getText().equals("PINK"))
            t2.setTextColor(getResources().getColor(R.color.pink));
        else if(t2.getText().equals("BLACK"))
            t2.setTextColor(getResources().getColor(R.color.black));
        else if(t2.getText().equals("WHITE"))
            t2.setTextColor(getResources().getColor(R.color.white));
        else if(t2.getText().equals("BROWN"))
            t2.setTextColor(getResources().getColor(R.color.brown));
        else{
            int r2 = rand.nextInt(155) + 100;
            int g2 = rand.nextInt(155) + 100;
            int b2 = rand.nextInt(155) + 100;
            int randomColor2 = Color.rgb(r2,g2,b2);
            t2.setTextColor(randomColor2);
        }

        //Treasure 3 color
        if(t3.getText().equals("RED"))
            t3.setTextColor(getResources().getColor(R.color.red));
        else if(t3.getText().equals("BLUE"))
            t3.setTextColor(getResources().getColor(R.color.blue));
        else if(t3.getText().equals("YELLOW"))
            t3.setTextColor(getResources().getColor(R.color.yellow));
        else if(t3.getText().equals("GREEN"))
            t3.setTextColor(getResources().getColor(R.color.green));
        else if(t3.getText().equals("ORANGE"))
            t3.setTextColor(getResources().getColor(R.color.orange));
        else if(t3.getText().equals("PURPLE"))
            t3.setTextColor(getResources().getColor(R.color.purple));
        else if(t3.getText().equals("PINK"))
            t3.setTextColor(getResources().getColor(R.color.pink));
        else if(t3.getText().equals("BLACK"))
            t3.setTextColor(getResources().getColor(R.color.black));
        else if(t3.getText().equals("WHITE"))
            t3.setTextColor(getResources().getColor(R.color.white));
        else if(t3.getText().equals("BROWN"))
            t3.setTextColor(getResources().getColor(R.color.brown));
        else{
            int r3 = rand.nextInt(155) + 100;
            int g3 = rand.nextInt(155) + 100;
            int b3 = rand.nextInt(155) + 100;
            int randomColor3 = Color.rgb(r3,g3,b3);
            t3.setTextColor(randomColor3);
        }
    }
}
