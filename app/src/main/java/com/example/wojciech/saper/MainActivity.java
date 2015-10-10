package com.example.wojciech.saper;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import  android.widget.Button;
import java.util.Random;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static Button buttons[][] = new Button[8][8];
    private ImageButton imageButton;
    private TextView textView;
    private ToggleButton flagBombs;
    private TextView counterBombs;

    private static boolean EndGameFlag = false;
    private boolean StartGame = false;


    private static class FieldBombs {
        private static int bombs[][] = new int[10][10];
        private static boolean ispressed[][] = new boolean[10][10];
        private static int counter;

        public FieldBombs() {
            generatebombs();
            counter = 0;
        }

        public static void updateCounter() {
            int c = 0;
            for (int i = 0; i < 8; ++i) {
                for (int j = 0; j < 8; ++j) {
                    if ((ispressed[i + 1][j + 1] && !isSetFlag(buttons[i][j])) || !buttons[i][j].isClickable()) {
                        ++c;
                    }
                }
            }
            counter = c;
        }

        private static boolean findvalue(int arr[], int value) {
            int k = 0;
            for (int anArr : arr) {
                if (anArr != value) {
                    ++k;
                }
            }
            return k != 10;

        }

        private static int countbombs(int[][] M, int row, int col) {
            int n = 0; // number of bombs
            for (int i = row - 1; i < row + 2; ++i) {
                for (int j = col - 1; j < col + 2; ++j) {
                    if (i == row && j == col)
                        continue;
                    if (M[i][j] == -1)
                        ++n;
                }
            }
            return n;
        }

        private static void generatematrix(int arrbombs[]) {

            /// -2 - poza macierza
            /// -1 bomba
            /// 0 brak bomby w sasiedztwie
            /// 1 ... 8 ilosc bomb
            // wypelnienie macierzy wartosciami -1
            for (int i = 0; i < 10; ++i) {
                for (int j = 0; j < 10; ++j) {
                    bombs[i][j] = -2;
                    ispressed[i][j] = false;
                }
            }
            // wypelnienie macierzy bombami
            for (int arrbomb : arrbombs) bombs[1 + arrbomb / 8][arrbomb % 8 + 1] = -1;

            for (int i = 1; i < 9; ++i) {
                for (int j = 1; j < 9; ++j) {
                    if (bombs[i][j] != -1)
                        bombs[i][j] = countbombs(bombs, i, j);
                }
            }
        }

        private static void generatebombs() {
            Random random = new Random(System.currentTimeMillis());
            int counterbombs = 0;
            double P = 1. / 64.;
            int nc = 0; //number of cell
            int[] arr = new int[10]; // arr of bombs

            while (counterbombs < 10) {
                if (random.nextDouble() < P) {
                    while (findvalue(arr, nc)) {
                        ++nc;
                        nc %= 64;
                    }
                    arr[counterbombs++] = nc;
                }
                ++nc;
                nc %= 64;
            }
            generatematrix(arr);
        }

    }

    private static class MyAsyncTaskTimer extends AsyncTask<Long, Integer, Integer> {
        private Activity activity;

        public MyAsyncTaskTimer(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Long... params) {
            while (!EndGameFlag) {
                publishProgress((int) (System.currentTimeMillis() - params[0]) / 1000);
                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0] == 999)
                EndGameFlag = true;
            ((TextView) activity.findViewById(R.id.textView)).setText(String.format("%03d", values[0]));
        }
    }


    private static class MyHandlerSelectedButton extends Handler {
        public Button button;

        public MyHandlerSelectedButton(Button button) {
            this.button = button;
        }

        @Override
        public void handleMessage(Message msg) {
            button.setPressed(true);
        }
    }

    private static class MyThreadSelectedFiled implements Runnable {
        private MyHandlerSelectedButton handler;

        public MyThreadSelectedFiled(MyHandlerSelectedButton handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            if (handler.button != null)
                handler.sendEmptyMessage(0);
        }
    }

    public void newGame(View view) {
        StartGame = false;
        EndGameFlag = false;
        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    public class Mylistener implements View.OnClickListener {
        private int i;
        private int j;

        Mylistener(int k, int l) {
            i = k;
            j = l;
        }

        @Override
        public void onClick(View v) {
            if (isSetFlag(buttons[i][j]) && !flagBombs.isChecked()) {
                counterBombs.setText(String.format("%02d", Integer.valueOf(counterBombs.getText().toString()) + 1));
                buttons[i][j].setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (!flagBombs.isChecked()) {
                if (FieldBombs.bombs[i + 1][j + 1] == 0) {
                    //currentbutton = buttons[i][j];
                    lookForZero(i + 1, j + 1);
                    MyHandlerSelectedButton myHandlerSelectedButton = new MyHandlerSelectedButton(buttons[i][j]);
                    MyThreadSelectedFiled myThreadSelectedFiled = new MyThreadSelectedFiled(myHandlerSelectedButton);
                    Thread thread = new Thread(myThreadSelectedFiled);
                    thread.start();
                } else if (FieldBombs.bombs[i + 1][j + 1] != -1) {
                    MySetButton(buttons[i][j], FieldBombs.bombs[i + 1][j + 1]);
                } else {
                    buttons[i][j].setText(String.valueOf('*'));
                    buttons[i][j].setBackgroundColor(0xFFFF171B);
                    lost();
                }
            } else {
                counterBombs.setText(String.format("%02d", Integer.valueOf(counterBombs.getText().toString()) - 1));
                FieldBombs.ispressed[i + 1][j + 1] = true;
                buttons[i][j].setCompoundDrawablesWithIntrinsicBounds(R.mipmap.flag, 0, 0, 0);
            }
            FieldBombs.updateCounter();
            if (isWin()) {
                EndGameFlag = true;
                setNotclickableAllButton();
                imageButton = (ImageButton) findViewById(R.id.imageButton);
                imageButton.setImageResource(R.drawable.win);
                int place;
                if((place = checkBestScores(Integer.valueOf(textView.getText().toString()))) != 0){
                    Intent intent = new Intent(getApplication(),BestScoresActivity.class);
                    intent.putExtra("Place",place);
                    intent.putExtra("Score",Integer.valueOf(textView.getText().toString()));
                    startActivity(intent);
                }
            }
        }
    }

    private int checkBestScores(int newBestScore) {
        for(int i = 1;i < 4;++i){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if(sp.getInt("Rank"+i,999) > newBestScore){
                return i;
            }
        }
        return 0;
    }

    private void setNotclickableAllButton() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                buttons[i][j].setClickable(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        EndGameFlag = false;
        StartGame = false;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        android.widget.GridLayout linearLayout = (android.widget.GridLayout) findViewById(R.id.gird_layout);
        new FieldBombs();
        for (int j = 0; j < 8; ++j) {
            for (int i = 0; i < 8; ++i) {
                buttons[j][i] = (Button) linearLayout.getChildAt(i + j * 8);
                buttons[j][i].setOnClickListener(new MainActivity.Mylistener(j, i));
            }
        }
        textView = (TextView) findViewById(R.id.textView);
        counterBombs = (TextView) findViewById(R.id.textView_2);
        textView.setText(String.format("%03d", 0));
        counterBombs.setText(String.format("%02d", 10));
        flagBombs = (ToggleButton) findViewById(R.id.flag);
        return true;
    }

    private void lost() {
        EndGameFlag = true;
        int i = 0;
        int k = 0;
        while (i < 10) {
            if (FieldBombs.bombs[k / 8 + 1][k % 8 + 1] == -1) {

                buttons[k / 8][k % 8].setText(String.valueOf('*'));
                buttons[k / 8][k % 8].setPressed(true);
                ++i;
            } else if (buttons[k / 8][k % 8].getCompoundDrawables()[0] != null) {
                buttons[k / 8][k % 8].setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
            ++k;
        }
        setNotclickableAllButton();
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setImageResource(R.drawable.lost);
    }

    private void lookForZero(int i, int j) {
        if (FieldBombs.bombs[i][j] == -2 || FieldBombs.bombs[i][j] == -1)
            return;
        if (FieldBombs.ispressed[i][j])
            return;
        if (FieldBombs.bombs[i][j] == 0) {
            FieldBombs.ispressed[i][j] = true;
            lookForZero(i + 1, j + 1);
            lookForZero(i - 1, j - 1);
            lookForZero(i + 1, j - 1);
            lookForZero(i - 1, j + 1);
            lookForZero(i + 1, j);
            lookForZero(i - 1, j);
            lookForZero(i, j + 1);
            lookForZero(i, j - 1);
        }
        MySetButton(buttons[i - 1][j - 1], FieldBombs.bombs[i][j]);
    }

    private void MySetButton(Button b, int i) {
        if (i == 0 && !b.isPressed()) {
            b.setPressed(true);
        } else if (!b.isTextSelectable() && i != 0) {
            b.setText(String.valueOf(i));
        }
        if (b.isClickable())
            b.setClickable(false);
        switch (i) {
            case 1:
                b.setTextColor(0xFF3021FF);
                break;
            case 2:
                b.setTextColor(0xFF05C800);
                break;
            case 3:
                b.setTextColor(0xFFFF171B);
                break;
            case 4:
                b.setTextColor(0xFFD71595);
                break;
            case 5:
                b.setTextColor(0xFF170B57);
                break;
            case 6:
                b.setTextColor(0xFFFF8712);
                break;
            case 7:
                b.setTextColor(0xFF268B00);
                break;
            case 8:
                b.setTextColor(0xFFB7001B);
                break;
        }
        FieldBombs.updateCounter();
    }

    private boolean isWin() {
        if (EndGameFlag)
            return false;
        // start thread timer
        if (!StartGame && FieldBombs.counter > 0) {
            StartGame = true;
            MyAsyncTaskTimer myAsyncTaskTimer = new MyAsyncTaskTimer(this);

            myAsyncTaskTimer.execute(System.currentTimeMillis());
        }
        // if counter equals 54 you won
        return FieldBombs.counter == 54;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.exit:
                EndGameFlag = true;
                finish();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this,BestScoresActivity.class);
                intent.putExtra("view",1);
                startActivity(intent);
                EndGameFlag = true;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static boolean isSetFlag(Button button){
        if(button.getCompoundDrawables()[0] != null)
            return true;
        else
            return false;
    }


}
