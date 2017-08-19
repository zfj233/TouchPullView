package com.zfj.android.touchpulldemo;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private float mTouchMoveStartY = 0;
    public static final float TOUCH_MOVE_MAX_Y = 600;
    private TouchPullView mTouchPullView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTouchPullView = (TouchPullView) findViewById(R.id.touch_pull_view);
        findViewById(R.id.activity_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMoveStartY = event.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float y = event.getY();
                        //向下滑动
                        if (y >= mTouchMoveStartY) {
                            float moveSize = y - mTouchMoveStartY;
                            float progress = moveSize >= TOUCH_MOVE_MAX_Y ?
                                    1 : moveSize / TOUCH_MOVE_MAX_Y;
                            mTouchPullView.setProgress(progress);
                        }

                        return true;
                    case MotionEvent.ACTION_UP:
                        mTouchPullView.release();
                        return true;
                    default:
                        break;

                }
                return false;

            }
        });
    }
}
