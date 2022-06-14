package jp.ac.titech.itpro.sdl.shakingdiary;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class GraphActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getdate_from_yyyy_mm(int year, int month) {
        // オブジェクト生成
        LocalDate localDate = LocalDate.of(year, month, 1);
        // 日数を取得
        return localDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Intent intent = getIntent();

        LocalDate now = LocalDate.now();

        LineChart lineChart = findViewById(R.id.Linechart);
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("感情変化の推移グラフ");
        lineChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = lineChart.getXAxis();
        lineChart.setVisibleXRangeMaximum(7f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter());

        String[] entities = intent.getStringArrayExtra("entities");
        String s = "";

        Button switch_button = this.findViewById(R.id.switch_button_in_graph);
        switch_button.setOnClickListener(v -> { if(! (getIntent() == null)) finish(); });

        for (String entity: entities) {
            s = s + entity+ "\n";
        }

    }
}