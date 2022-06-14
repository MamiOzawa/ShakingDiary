package jp.ac.titech.itpro.sdl.shakingdiary;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    private Diary_Table db;
    private LineChart lineChart;
    private Button register_button;

    private ActivityResultLauncher<Intent> launcher;
    private LocalDateTime now = LocalDateTime.now();
    private int year ,month, date;
    private LineChart_setter lineChartSetter;
    private LineChartUpdateAsyncTask linechartupdateasynctask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.getApplicationContext().deleteDatabase("diary_table");//デバッグ用．起動のたびにDB消すときはここのコメントアウトを外す
        db = Diary_Table_Singleton.getInstance(getApplicationContext());
        lineChartSetter = new LineChart_setter();

        //SubActivityから結果を受け取り，非同期処理を行うlauncher
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent result_intent = result.getData();
                if(result_intent != null) {
                    Bundle extras = result_intent.getExtras();
                    if(extras != null){
                        new DateDataInsertAsyncTask(db, extras.getString("text"), extras.getInt("smile_score",0),extras.getInt("sad_score",0), extras.getInt("angry_score",0)).execute();
                    }
                }
            }
        });

        //カレンダー
        CalendarView calendar = findViewById(R.id.calendar);
        year = now.getYear(); month = now.getMonthValue();  date = now.getDayOfMonth();
        //日付を選択すると，その日付を取得する
        //さらに，その月の折れ線グラフを表示する
        calendar.setOnDateChangeListener(
                new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int date) {
                        if ((MainActivity.this.year != year) || (MainActivity.this.month != month)) {
                            MainActivity.this.year = year;
                            MainActivity.this.month = month + 1;
                            MainActivity.this.date = date;
                            lineChart = lineChartSetter.set_xAxis(lineChart, MainActivity.this.year, MainActivity.this.month);
                            linechartupdateasynctask = new LineChartUpdateAsyncTask(db, lineChart, MainActivity.this.year, MainActivity.this.month);
                            linechartupdateasynctask.setListener(createListener());
                            linechartupdateasynctask.execute();
                        } else {
                            MainActivity.this.year = year;
                            MainActivity.this.month = month + 1;
                            MainActivity.this.date = date;
                        }
                    }
                }
        );

        // 日記登録ボタン
        register_button = this.findViewById(R.id.register_button_in_main);
        register_button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("Year", year); intent.putExtra("Month", month); intent.putExtra("Date", date);
                new DateDataGetAsyncTask(db,intent).execute();
        });

        // 折れ線グラフ
        lineChart = this.findViewById(R.id.Linechart);
        lineChart = lineChartSetter.init(lineChart, year, month);
        linechartupdateasynctask = new LineChartUpdateAsyncTask(db,lineChart,year,month);
        linechartupdateasynctask.setListener(createListener());
        linechartupdateasynctask.execute();
    }

    //軸とかの設定に関わる部分
    public class LineChart_setter {
        //x軸の設定，y軸の設定，グラフ背景の描画設定を一気に
        public LineChart init(LineChart lineChart, int year, int month) {
            return set_xAxis(set_yAxis(set_info(lineChart)), year, month);
        }

        //x軸の設定
        public LineChart set_xAxis(LineChart lineChart, int year, int month) {
            String[] x_index = new String[YearMonth.of(year, month).atEndOfMonth().getDayOfMonth()];
            x_index[0] = year + " " + month + "/" + 1;
            for (int i = 1; i < x_index.length; i++) {
                x_index[i] = month + "/" + (i + 1);
            }
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(x_index));
            xAxis.setAxisMinimum(0f);
            xAxis.setAxisMaximum(YearMonth.of(year, month).atEndOfMonth().getDayOfMonth());
            lineChart.setVisibleXRangeMaximum(14f);
            return lineChart;
        }

        //y軸の設定
        public LineChart set_yAxis(LineChart lineChart) {
            YAxis yAxis = lineChart.getAxisLeft();
            lineChart.getAxisRight().setEnabled(false);
            // Y軸最大最小設定
            yAxis.setAxisMaximum(110f);
            yAxis.setAxisMinimum(-10f);
            return lineChart;
        }

        //背景の設定
        public LineChart set_info(LineChart lineChart) {
            lineChart.getDescription().setEnabled(true);
            lineChart.getDescription().setText("お気持ちグラフ");
            lineChart.setBackgroundColor(Color.WHITE);

            return lineChart;
        }
    }

    //Linechartに新たなデータをセットしたときに呼ばれるリスナー
    private LineChartUpdateAsyncTask.Listener createListener() {
        return new LineChartUpdateAsyncTask.Listener() {
            @Override
            public void onSuccess(LineChart linechart) {
                linechart.invalidate();
            }
        };
    }

    //Linechartに新たなデータをセットし，表示するTask
    public static class LineChartUpdateAsyncTask extends AsyncTask<Void, Void, Integer> {
        private Diary_Dao diary_dao;
        List<Diary_Entity> temp_entities;
        ArrayList<ILineDataSet> dataSets;
        ArrayList<Entry> smile_values;
        ArrayList<Entry> sad_values;
        ArrayList<Entry> angry_values;
        LineChart lineChart;
        private Listener listener;
        int year, month;

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        public LineChartUpdateAsyncTask(@NonNull Diary_Table db, LineChart lineChart, int year, int month) {
            this.diary_dao = db.Diary_Dao();
            this.lineChart = lineChart;
            this.year = year;
            this.month = month;
            this.smile_values = new ArrayList<>();
            this.sad_values = new ArrayList<>();
            this.angry_values = new ArrayList<>();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            dataSets = new ArrayList<>();
            //年月が合致するエンティティを全部取り出す
            temp_entities = diary_dao.getAll_by_year_and_month(year, month);
            //それぞれのスコアをList型に格納
            for(Diary_Entity entity : temp_entities){
                int x = entity.getDate();
                smile_values.add(new Entry(x-1, entity.getSmile_score()));
                sad_values.add(new Entry(x-1, entity.getSad_score()));
                angry_values.add(new Entry(x-1, entity.getAngry_score()));
            }
            //それぞれのスコアをlinechartに格納
            LineDataSet smile = new LineDataSet(smile_values, "SMILE");
            LineDataSet sad = new LineDataSet(sad_values, "SAD");
            LineDataSet angry = new LineDataSet(angry_values, "ANGRY");
            LineDataSet[] linedatasets = {smile, sad, angry};
            int[] colors = {Color.MAGENTA, Color.BLUE, Color.RED};
            for (int i = 0; i < linedatasets.length; i++) {
                linedatasets[i].setColor(colors[i]);          // 線の色
                linedatasets[i].setCircleColor(colors[i]);    // 座標の色
                linedatasets[i].setLineWidth(3f);             // 線の太さ 1f~
                linedatasets[i].setCircleRadius(4f);          // 座標の大きさ
                linedatasets[i].setDrawCircleHole(false);     // 座標を塗りつぶす→false 塗りつぶさない→true
                linedatasets[i].setValueTextSize(10f);        // データの値を記す。0fで記載なし。floatだから小数点がつく
                linedatasets[i].setDrawFilled(true);          // 線の下を塗りつぶすか否か
                linedatasets[i].setFillColor(colors[i]);      //　塗りつぶしたフィールドの色

                dataSets.add(linedatasets[i]);
            }
            LineData lineData = new LineData(dataSets);
            lineChart.setData(lineData);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if (listener != null) {
                listener.onSuccess(lineChart);
            }
        }
        interface Listener {
            void onSuccess(LineChart lineChart);
        }
    }

    //日付に紐付けて保存してあるデータを取得し，SubActivityにインテントで渡す非同期処理
    public class DateDataGetAsyncTask extends AsyncTask<Void, Void, Integer> {
        private Diary_Dao diary_dao;
        Diary_Entity temp_entity;
        Intent intent;

        public DateDataGetAsyncTask(Diary_Table db, Intent intent) {
            this.diary_dao = db.Diary_Dao();
            this.intent = intent;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            temp_entity = diary_dao.get_by_date(year, month, date);
            if(temp_entity != null) {
                intent.putExtra("text", temp_entity.getText());
                intent.putExtra("smile_score", temp_entity.getSmile_score());
                intent.putExtra("sad_score", temp_entity.getSad_score());
                intent.putExtra("angry_score", temp_entity.getAngry_score());
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            launcher.launch(intent);
        }
    }

    //SubActivityから受け取った情報をもとにデータベースへ追加/更新/削除のいずれかを行い，グラフに反映する非同期処理
    public class DateDataInsertAsyncTask extends AsyncTask<Void, Void, Integer> {
        private Diary_Dao diary_dao;
        String text;
        Integer smile_score, sad_score, angry_score;

        public DateDataInsertAsyncTask(Diary_Table db, String text, Integer smile_score, Integer sad_score, Integer angry_score) {
            this.diary_dao = db.Diary_Dao();
            this.text = text;
            this.smile_score = smile_score; this.sad_score = sad_score; this.angry_score = angry_score;
            Log.i("223l", "smile: "+ smile_score);
        }

        //フィールドが全部空っぽ（textが""かつscoreが0）ならInsert/UpdateせずDelete
        @Override
        protected Integer doInBackground(Void... params) {
            Log.i("DateDataInsertAsyncTask", "text の中身は : " + text);
            if(!(text.equals("") && smile_score==0 && sad_score==0 && angry_score==0)) {
                Diary_Entity temp_entity = new Diary_Entity(year, month, date, text, smile_score, sad_score, angry_score);
                //Insert
                long i = diary_dao.insert_or_update(temp_entity);
                Log.i("DateDataInsertAsyncTask", "Insert成功！ Insert rowId is : " + i);
                Log.i("DateDataInsertAsyncTask", "現在の日時での検索結果は" + diary_dao.get_by_date(year, month, date).getText());
            } else {
                //Delete
                diary_dao.delete(year, month,date);
                Log.i("DateDataInsertAsyncTask", "Delete成功！ Delete key is : " + year + "/" + month + "/" + date);
            }
            return 0;
        }

        //データベース更新が終わったらLinechartも更新
        @Override
        protected void onPostExecute(Integer code) {
            new LineChartUpdateAsyncTask(db, lineChart, year, month).execute();
        }
    }
}
