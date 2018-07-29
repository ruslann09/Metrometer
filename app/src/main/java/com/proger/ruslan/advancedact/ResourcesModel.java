package com.proger.ruslan.advancedact;

public enum ResourcesModel {

    // создаем 3 перечисления с тайтлом и макетом
    // для удобной работы в адаптере
    FIRST_SCREEN(R.string.txt_screen_1, R.layout.intro_first_view, R.color.FirstIntroSlide),
    SECOND_SCREEN(R.string.txt_screen_2, R.layout.intro_second_view, R.color.SecondIntroSlide),
    THIRD_SCREEN(R.string.txt_screen_3, R.layout.intro_third_view, R.color.ThirdIntroSlide);

    private int mTitleResourceId;
    private int mLayoutResourceId;
    private int color;

    ResourcesModel(int titleResId, int layoutResId, int color) {
        mTitleResourceId = titleResId;
        mLayoutResourceId = layoutResId;
        this.color = color;
    }

    public int getTitleResourceId() {
        return mTitleResourceId;
    }

    public int getLayoutResourceId() {
        return mLayoutResourceId;
    }

    public int getColor () {return color;}
}