package com.example.kissthefrog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * @author Hami Gündogdu
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FROG_ID = 212121;
    private int punkte =0;
    private int round =1;
    private int countdown=10;
    private int highscore;
    ViewGroup container;
    TextView pointsTv,roundTv,countdownTv,help,highscoreTv;
    private ImageView frog;
    private Random rnd = new Random();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countdown();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        help = findViewById(R.id.help);
        pointsTv = findViewById(R.id.points);
        roundTv = findViewById(R.id.round);
        countdownTv = findViewById(R.id.countdown);
        highscoreTv = findViewById(R.id.highscore);

        findViewById(R.id.help).setOnClickListener(this);

        showStartFragment();
    }
    // starGame -> newGame -> initRound
    private void newGame(){
        initRound();
    }

    private void initRound() {
        countdown=10; // 10 sec
        container = findViewById(R.id.container);
        container.removeAllViews();
        WimmelView wv = new WimmelView(this);
        container.addView(wv,ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        // anzahl stöhrbilder.
        // erhöht sich nach jeder Runde
        wv.setImageCount(8*(10+round));
        frog = new ImageView(this);
        frog.setId(FROG_ID);
        frog.setImageResource(R.drawable.frog);
        //verhindert die scalierung
        frog.setScaleType(ImageView.ScaleType.CENTER);
        //pixeldichte
        float scale = getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Math.round(64*scale),Math.round(61*scale));
        // dient dazu, dass die Ränder nicht ignoriert werden.
        lp.gravity = Gravity.TOP + Gravity.LEFT;
        lp.leftMargin = rnd.nextInt(container.getWidth()-64);
        lp.topMargin = rnd.nextInt(container.getHeight()-61);
        frog.setOnClickListener(this);
        // setze frosch in cointer ein
        container.addView(frog, lp);
        // round-50 dient dazu, dass es jede runde schneller abläuft
        handler.postDelayed(runnable,1000-round*50);
        update();
    }
    // update alles
    private void update() {
        fillTextView( pointsTv,Integer.toString(punkte)+" ");
        fillTextView( roundTv," "+Integer.toString(round));
        fillTextView( countdownTv,Integer.toString(countdown*1000)+" ");
        fillTextView(highscoreTv,Integer.toString(highscore));
        loadHighscore();
    }

    private void loadHighscore() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        highscore = sp.getInt("highscore", 0);
    }
    // save Highscore
    private void saveHighscore(int points) {
        highscore=points;
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt("highscore", highscore);
        e.commit();
    }

    private void fillTextView(TextView name, String text) {
        name.setText(text);
    }



    //start screen
    private void showStartFragment(){
        ViewGroup container = findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.fragment_start, null)); // show xml start fragment
        container.findViewById(R.id.start).setOnClickListener(this);
    }
    // End text.
    // When time is up.
    private void showGameOverFrgment() {
        round = 1;
        punkte= 0;
        ViewGroup container =  findViewById(R.id.container);
        container.addView( getLayoutInflater().inflate(R.layout.fragment_gameover, null) );//show xml gameover fragment
        container.findViewById(R.id.play_again).setOnClickListener(this);
    }

    /**
     * Check oncklick
     * @param view
     */
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.start) {
            startGame();
        } else if(view.getId()==R.id.play_again) {
            showStartFragment();
        }else if(view.getId()==FROG_ID) {
            kissFrog();
        }else if(view.getId()==R.id.help) {
            showTutorial();
        }

    }

    /**
     * Wenn spieler den Frosch anklickt
     * Toast anzeigen, Punkt geben
     * Nexte Runde startem
     */
    private void kissFrog() {
        handler.removeCallbacks(runnable);
        showToast(R.string.kissed);
        punkte += countdown*1000;
        round++;
        initRound();
    }

    /**
     * Custom Toast
     * @param stringResId
     *
     */
    private void showToast(int stringResId) {
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        TextView textView = new TextView(this);
        textView.setText(stringResId);
        textView.setTextColor(getResources().getColor(R.color.points));
        textView.setTextSize(48f);
        toast.setView(textView);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/jandamanateesolid.ttf");
        textView.setTypeface(typeface);
        toast.show();
    }

    /**
     * Spiel erklärung
     * Dialog öffnen
     * Game starten
     */
    private void showTutorial() {
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_tutorial);
        dialog.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startGame();
            }
        });
        dialog.show();
    }

    // game countdown
    private void countdown(){
        countdown--;
        update();
        if(countdown<=0){
            frog.setOnClickListener(null);
            if(punkte>highscore) {
                saveHighscore(punkte);
                update();
            }
            showGameOverFrgment();
        }else {
            handler.postDelayed(runnable,1000-round*50);
        }
    }

    /**
     * Start game
     */
    private void startGame() {
        newGame();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
