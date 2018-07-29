package com.proger.ruslan.advancedact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

public class Introducing extends AppCompatActivity {

    //знакомство с приложением и показ слайдов при первом открытии

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introducing);

        getSupportActionBar().hide();

        ViewPager viewPager = (ViewPager) findViewById(R.id.vpager);
        if (viewPager != null) {
            viewPager.setAdapter(new SimplePagerAdapter(this));
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //установка собственной модели презентации

                    ResourcesModel customPagerEnum = ResourcesModel.values()[position];

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        getWindow().setStatusBarColor(getResources().getColor(customPagerEnum.getColor()));
                    }

                    Button btn = (Button) findViewById(R.id.begin);

                    if (btn != null)
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick (View view) {
                                startActivity (new Intent (getApplicationContext(), MainActivity.class));

                                SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext().getString (R.string.APP_PREFERENCES), Context.MODE_PRIVATE).edit();
                                editor.putBoolean ("isInit", false);
                                editor.commit();

                                finish ();
                            }
                        });

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void getStarted (View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));

        finish();
    }

    class SimplePagerAdapter extends PagerAdapter {

        private Context mContext;

        public SimplePagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            // получаем ресурсы (название и макет) соответствующий позиции в адаптере
            ResourcesModel resources = ResourcesModel.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            // инициализируем экран ViewPager'а в соответствии с позицией
            ViewGroup layout = (ViewGroup) inflater.inflate(
                    resources.getLayoutResourceId(), viewGroup, false);
            viewGroup.addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int position, Object view) {
            viewGroup.removeView((View) view);
        }

        @Override
        public int getCount() {
            // кличество элементов в адаптере соответствует
            // количеству значений в enum классе
            return ResourcesModel.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // получаем название соответствующее позиции в адаптере
            ResourcesModel customPagerEnum = ResourcesModel.values()[position];

            return mContext.getString(customPagerEnum.getTitleResourceId());
        }
    }
}

