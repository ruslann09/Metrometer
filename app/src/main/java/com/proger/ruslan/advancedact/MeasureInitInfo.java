package com.proger.ruslan.advancedact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class MeasureInitInfo extends AppCompatActivity {

    //создаём переменные для далльнейшей инициализации
    private Intent info;
    private TextView type, percent_of_quality;
    private EditText time, dots, perimeter, area, note, place;
    private Button edit, delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_init_info);

        try {

            //ставим новый заголовок для активности
            setTitle(getString(R.string.measureinfoactivityname));

            //инициализируем созданные переменные для динамического управления в коде
            type = (TextView) findViewById(R.id.type_measure);
            time = (EditText) findViewById(R.id.time_measure);
            dots = (EditText) findViewById(R.id.numsDots);
            perimeter = (EditText) findViewById(R.id.perimeter_measure);
            area = (EditText) findViewById(R.id.area_measure);
            note = (EditText) findViewById(R.id.note_measure);
            place = (EditText) findViewById(R.id.place_of_measure);
            percent_of_quality = (TextView) findViewById(R.id.percent_of_quality_measure);

            edit = (Button) findViewById(R.id.edit_measure);
            delete = (Button) findViewById(R.id.delete_measure);

            info = getIntent();

            DataMatches match = (DataMatches) info.getSerializableExtra("TheMatch");

            //выставляем значения существующих свойств

            type.setText(String.valueOf(match.getType()));
            dots.setText(String.valueOf(match.getDots()));
            perimeter.setText(String.valueOf(match.getPerimeter()));
            area.setText(String.valueOf(match.getArea()));
            note.setText(String.valueOf(match.getNote()));
            place.setText(String.valueOf(match.getPlace()));
            percent_of_quality.setText(String.valueOf((double)Math.round(match.getPercent_of_quality() * 100) / 100) + " %");

            //ставим слушатели кнопкам
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //выполняем проверку на первое открытие активности пользователем
                    infoWindowGetStarted();
                    selectTheEditAction();
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    infoWindowGetStarted();

                    //получаем id записи на которую нажал пользователь на странице всех записеё
                    long id = getIntent().getLongExtra("id", -1);

                    try {
                        //удалаем выбранную запись из БД
                        MainActivity.dataBaseController.delete(id);
                        Toast.makeText(getApplicationContext(), id + "", Toast.LENGTH_LONG).show();
                        updateList();

                        //выходим из активности
                        finish();
                    } catch (Exception e) {
                    }
                }
            });

            DataMatches date = (DataMatches) info.getSerializableExtra("TheMatch");

            //стави задний фон активности для уникальности записи
            ((LinearLayout) findViewById(R.id.back_measure)).setBackgroundColor(Color.argb(80,
                    255 % date.getDots() + 1, (int) ((date.getArea() + 1) % 255), (int) ((date.getPerimeter() + 1) % 255)));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh.mm.ss.SSS");

            try {
                if (date != null)
                    time.setText(date.getDate());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.unknown_wrong + " : " + e.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.unknown_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void infoWindowGetStarted () {
        if (getSharedPreferences(getApplicationContext().getString(R.string.APP_PREFERENCES), Context.MODE_PRIVATE).getBoolean("isInitMeasureInfo", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MeasureInitInfo.this);
            builder.setTitle(R.string.edit_measure_info).setMessage(R.string.accelerator_dialog)
                    .setCancelable(false)
                    .setNegativeButton(R.string.understand, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //проверяем первичное вхождение на данный вид активности и в зависимисти от этого выводим диалог
                            SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext().
                                    getString (R.string.APP_PREFERENCES), Context.MODE_PRIVATE).edit();
                            editor.putBoolean ("isInitMeasureInfo", false);
                            editor.commit();

                            dialog.cancel();
                        }
                    }).create().show();
        }
    }

    private void selectTheEditAction () {
        final String[] mChooseCats = { getString(R.string.save), getString(R.string.edit)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasureInitInfo.this);
        builder.setTitle(R.string.selectTheEthalone)
                .setCancelable(false)

                // добавляем одну кнопку для закрытия диалога
                .setNeutralButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })

                // добавляем переключатели
                .setSingleChoiceItems(mChooseCats, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                dialog.cancel();

                                switch (item) {
                                    case 0:
                                            try {
                                                DataMatches match = (DataMatches) info.getSerializableExtra("TheMatch");

                                                //инициализируем изменения в выбранной записи

                                                String type_edited = "" + type.getText().toString();
                                                String time_edited = time.getText().toString();
                                                int dots_edited = Integer.parseInt(dots.getText().toString());
                                                double perimeter_edited = Double.parseDouble(perimeter.getText().toString());
                                                double area_edited = Double.parseDouble(area.getText().toString());
                                                String place_edited = place.getText().toString();
                                                String note_edited = note.getText().toString();
                                                double percent_of_quality_edited = 1 + Double.parseDouble(percent_of_quality.getText().toString());

                                                long id = getIntent().getLongExtra("id", -1);

                                                if (match != null) {
                                                    DataMatches newMatch = new DataMatches(id, area_edited, perimeter_edited,
                                                            dots_edited, match.getDotsRelatives(), time_edited, type_edited,
                                                            place_edited, note_edited, percent_of_quality_edited);

                                                    //выставляем новые свойства выбранной записи

                                                    Toast.makeText(getApplicationContext(), "Updated successfully", Toast.LENGTH_LONG).show ();

                                                    MainActivity.dataBaseController.update(newMatch);

                                                    updateList();
                                                }
                                            } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(), R.string.unknown_wrong + " : " + e.toString(), Toast.LENGTH_LONG).show ();
                                            }
                                        break;
                                    case 1:
                                        long id = getIntent().getLongExtra("id", -1);
                                        DataMatches md = MainActivity.dataBaseController.select(id);

                                        switch (md.getType()) {
                                            case AcceleratorModel.TYPE:
                                                Intent accel = new Intent(getApplicationContext(), AcceleratorModel.class);
                                                accel.putExtra("Matches", md);
                                                startActivityForResult(accel, MainActivity.UPDATE_DATARAW);
                                                break;
                                            case GPSLocation.TYPE:
                                                Intent gps = new Intent(getApplicationContext(), GPSLocation.class);
                                                gps.putExtra("Matches", md);
                                                startActivityForResult(gps, MainActivity.UPDATE_DATARAW);
                                                break;
                                        }
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show ();
    }

    private void updateList () {
        MainActivity.measuringAdapter.setArrayMyData(MainActivity.dataBaseController.selectAll());
        MainActivity.measuringAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case MainActivity.UPDATE_ACTIVITY:
                    DataMatches camMatch = (DataMatches) data.getExtras().getSerializable("Matches");
                    MainActivity.dataBaseController.insert(camMatch);
                    updateList();
                    break;
                case MainActivity.MAP_MEASURING:
                    DataMatches mapMatch = (DataMatches) data.getExtras().getSerializable("Matches");
                    MainActivity.dataBaseController.insert(mapMatch);
                    updateList();
                    break;
                case MainActivity.UPDATE_DATARAW:
                    DataMatches upMatch = (DataMatches) data.getExtras().getSerializable("Matches");
                    MainActivity.dataBaseController.update(upMatch);
                    updateList();
                    break;
            }
        }
    }
}
