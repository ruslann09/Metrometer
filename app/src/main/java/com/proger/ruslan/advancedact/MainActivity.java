package com.proger.ruslan.advancedact;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //основные переменные для работы с листом отображения записей и возможностью их изменения

    public static final int UPDATE_ACTIVITY = 0, MAP_MEASURING = 1, UPDATE_DATARAW = 2;

    public static DataBaseMatches dataBaseController;
    public static myListAdapter measuringAdapter;
    private View category;
    private final int REQUEST_CODE_PERMISSION_CAMERA_USING = 58975;

    //работа с отображением информации о записи

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView (R.layout.activity_main);

        //установка кастомного тайтла
        setTitle (getString(R.string.main_activity_name));

        //инициализация Базы Данных

        dataBaseController = new DataBaseMatches(getApplicationContext());
        measuringAdapter = new myListAdapter(getApplicationContext(),dataBaseController.selectAll());

        //инициализация листа отображения записей

        ListView listView = (ListView) findViewById(R.id.results);
        listView.setAdapter(measuringAdapter);

        //установка видимого бара

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //плавающая точка для мнгновенного начала сессии
        //в зависимости от разрешенных параметров ставим новую запись или запрашиваем права

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(view, R.string.snack_bar_add_measure, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    startActivityForResult(new Intent(getApplicationContext(), AcceleratorModel.class), UPDATE_ACTIVITY);
                }else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA_USING);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //ставим бар навигации

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //устанавливаем собственный цвет статус бара

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    //для нажатия точки возврата

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //метод обработки запросов прав

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA_USING:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(new Intent (getApplicationContext(), AcceleratorModel.class), 0);
                break;
        }
    }

    //реализация управления навигационным баром

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myMatches:
//                startActivityForResult(new Intent (MainActivity.this, AcceleratorModel.class), 0);
                break;
            case R.id.make_new_matches:
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(new Intent(getApplicationContext(), AcceleratorModel.class), UPDATE_ACTIVITY);
                else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA_USING);
                break;
            case R.id.find_my_matches_on_map:
                startActivityForResult(new Intent (MainActivity.this, GPSLocation.class), MAP_MEASURING);
                break;
            case R.id.take_on_location_checker:
                startActivity(new Intent (MainActivity.this, MapActivity.class));
                break;
            case R.id.nav_callback:
                startActivity (new Intent (getApplicationContext(), DevelopersSupport.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //результаты контекстного меню

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.go_to_my_matches);
        menu.add(0, 7, 0, R.string.location_checker_enabled);
        menu.add(0, 8, 0, R.string.find_the_measures_on_map);
        menu.add (0, 1, 0, R.string.make_a_new_match);
        menu.add (0, 2, 1, R.string.title_activity_introducing);
        menu.add(0, 5, 3, R.string.delete_all);
        menu.add(0, 6, 3, R.string.quite);

        return super.onCreateOptionsMenu(menu);
    }

    //открытие и подготовка контекстного меню

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    //выбор элемента большого контекстного меню

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
            case 1:
                startActivityForResult(new Intent (MainActivity.this, AcceleratorModel.class), UPDATE_ACTIVITY);
                break;
            case 2:
                startActivity(new Intent(getApplicationContext(), Introducing.class));
                break;
            case 3:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case 5:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.Warning).setMessage(R.string.all_will_deleted)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                dataBaseController.deleteAll();
                                updateList();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
                break;
            case 6:
                finish ();
                break;
            case 7:
                startActivity(new Intent (getApplicationContext(), MapActivity.class));
                break;
            case 8:
                startActivityForResult(new Intent (getApplicationContext(), GPSLocation.class), MAP_MEASURING);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //создание контекстного меню

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        category = view;
    }

    //выбор элемента локального контекстного меню

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        long id = Integer.parseInt((((TextView) category.findViewById(R.id.id)).getText().toString()).substring(8,
                (((TextView) category.findViewById(R.id.id)).getText().toString()).length() - 1));

        switch(item.getItemId()) {
            case R.id.edit:
                try {
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

                } catch (Exception e) {Toast.makeText(this, "Error" + e, Toast.LENGTH_LONG).show ();}
                return true;
            case R.id.delete:
                dataBaseController.delete (id);
                Toast.makeText(getApplicationContext(), id + "", Toast.LENGTH_LONG).show();
                updateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //обновление отображения

    private void updateList () {
        measuringAdapter.setArrayMyData(dataBaseController.selectAll());
        measuringAdapter.notifyDataSetChanged();
    }

    //вазврат из сессии в главную активность с результатом

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case UPDATE_ACTIVITY:
                    DataMatches camMatch = (DataMatches) data.getExtras().getSerializable("Matches");
//                    if (requestCode == UPDATE_ACTIVITY)
//                        dataBaseController.update(md);
//                    else
                        dataBaseController.insert(camMatch);
                    updateList();
                    break;
                case MAP_MEASURING:
                    DataMatches mapMatch = (DataMatches) data.getExtras().getSerializable("Matches");
                    dataBaseController.insert(mapMatch);
                    updateList();
                    break;
                case UPDATE_DATARAW:
                    DataMatches upMatch = (DataMatches) data.getExtras().getSerializable("Matches");
                    dataBaseController.update(upMatch);
                    updateList();
                    break;
            }
        }
    }

    public static long TIME_INTERVAL_FOR_EXIT = 1500;
    private long lastTimeBackPressed;

    @Override
    public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
        if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if(System.currentTimeMillis() - lastTimeBackPressed < TIME_INTERVAL_FOR_EXIT) {
                finish();
            }
            else {
                lastTimeBackPressed = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), R.string.tapAgainToExit, Toast.LENGTH_SHORT).show ();
            }
            return true;
        } else {
            return super.onKeyDown(pKeyCode, pEvent);
        }
    }

    //работа с отображением элементов ListView

    class myListAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<DataMatches> arrayMyMatches;
        public myListAdapter (Context ctx, ArrayList<DataMatches> arr) {
            mLayoutInflater = LayoutInflater.from(ctx);
            setArrayMyData(arr);
        }
        public ArrayList<DataMatches> getArrayMyData() {
            return arrayMyMatches;
        }
        public void setArrayMyData(ArrayList<DataMatches> arrayMyData) {
            this.arrayMyMatches = arrayMyData;
        }
        public int getCount () {
            return arrayMyMatches.size();
        }
        public Object getItem (int position) {
            return position;
        }
        public long getItemId (int position) {
            DataMatches md = arrayMyMatches.get(position);
            if (md != null) {
                return md.getId();
            }
            return 0;
        }

        //получение элемента ListView и его отправка в активность данных

        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.measuring_data_row, null);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent infoIntent = new Intent(getApplication().getApplicationContext(), MeasureInitInfo.class);

                                infoIntent.putExtra("TheMatch", arrayMyMatches.get(position));

                                long id = Integer.parseInt((((TextView) view.findViewById(R.id.id)).getText().toString()).substring(8,
                                        (((TextView) view.findViewById(R.id.id)).getText().toString()).length() - 1));

                                infoIntent.putExtra("id", id);

                                startActivity(infoIntent);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.unknown_wrong), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    registerForContextMenu(convertView);
                }

                DataMatches md = arrayMyMatches.get(position);

//            int textColor = Color.rgb(255 - (int)md.getId ()*70%255, 255 - (int)md.getId ()*150%255, 255 - (int)md.getId ()*200%255);

                TextView id = (TextView) convertView.findViewById(R.id.id);
                TextView date = (TextView) convertView.findViewById(R.id.date);
                TextView area = (TextView) convertView.findViewById(R.id.area);
                TextView perimeter = (TextView) convertView.findViewById(R.id.perimeter);
                TextView dots = (TextView) convertView.findViewById(R.id.dots);

                if (position % 2 == 0)
                    convertView.findViewById(R.id.wrapper).setBackgroundColor(Color.parseColor("#e0baff"));
                else
                    convertView.findViewById(R.id.wrapper).setBackgroundColor(Color.parseColor("#ffcbb8"));

                id.setText(getString(R.string.bd_id) + "\n|" + String.valueOf(md.getId()) + "|");
                date.setText(String.valueOf(md.getDate()));
                area.setText(String.valueOf((double) Math.round(md.getArea() * 1000) / 1000) + " " + getString(R.string.meter));
                perimeter.setText(String.valueOf((double) Math.round(md.getPerimeter() * 1000) / 1000) + " " + getString(R.string.meter));
                dots.setText(String.valueOf(md.getDots()));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.unknown_wrong), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
    }
}
