package com.song.watchview;

import java.util.Calendar;

import com.song.watchview.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint({ "DrawAllocation", "Recycle" })
public class WatchView extends View {

    /**
     * 默认最小的尺寸
     */
    private static final int DEFAULT_SIZE = 1000;

    /**
     * 本方法在java代码中使用
     * 
     * @param context
     */
    public WatchView(Context context) {
        super(context);
        start();
    }

    /**
     * 本方法在xml中使用
     * 
     * @param context
     * @param attrs
     */
    public WatchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        start();
    }

    /**
     * 本方法也在xml中使用,可以使用style
     * 
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public WatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // attrs 中存了设定的值,R.styleable.TestView取出这一组值,
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TestView, defStyleAttr, 0);
        Drawable drawable = array.getDrawable(R.styleable.TestView_src);
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        color = array.getColor(R.styleable.TestView_hour_color, Color.WHITE);
        array.recycle();
        start();
    }

    private int color;
    private Bitmap bitmap;

    private void start() {
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_cheetah);
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    // 子线程中,提醒刷新
                    postInvalidate();
                    // 主线程中刷新
                    // invalidate();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 绘制
     * 
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        drawWatch(canvas);
    }

    private void drawWatch(Canvas canvas) {
        // 让canvas入栈，保存当前的信息
        canvas.save();
        float temp = Math.min(getWidth() / 200.0f, getHeight() / 200.0f);
        // 将画布以200位基准缩放，那么无论view宽高多大，都可以看做宽200，高200的view
        canvas.scale(temp, temp);
        // view填充黑色
        canvas.drawColor(Color.BLACK);
        {
            // 坐标100,100为圆心，半径为100的画圆，并且设置背景图片
            Paint paint = new Paint();
            // 给paint设置渲染背景图片，画出的圆带有背景图片，使用REPEAT模式渲染
            paint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
            canvas.drawCircle(100, 100, 100, paint);
        }
        // 画表的白色边界
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        // 设置画笔抗锯齿
        paint.setAntiAlias(true);
        canvas.drawCircle(100, 100, 100, paint);
        // 保存当前canvas状态
        canvas.save();
        // 画表刻度，总共有12个刻度，其中4个粗刻度
        for (int i = 0; i < 12; i++) {
            if (i % 3 == 0) {
                paint.setStrokeWidth(3);
            } else {
                paint.setStrokeWidth(1);
            }
            canvas.drawLine(100, 0, 100, 10, paint);
            // 画完一个刻度后，顺时针旋转canvas30度，然后画下一个刻度
            canvas.rotate(30, 100, 100);
        }
        // 回到上次保存的canvas状态，未旋转
        canvas.restore();
        Calendar calendar = Calendar.getInstance();
        // 同上
        canvas.save();
        // 绘制时针
        // 以100,100为基准旋转，一个小时旋转30度，当前的时刻乘以30就是时针位置
        canvas.rotate(30 * calendar.get(Calendar.HOUR) + 30 * calendar.get(Calendar.MINUTE) / 60.0f, 100, 100);
        Path path = new Path();
        // 绘制菱形时针
        path.moveTo(100, 30);
        path.lineTo(110, 100);
        path.lineTo(100, 110);
        path.lineTo(90, 100);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);
        canvas.restore();
        canvas.save();
        // 绘制分针
        // 以100,100位基准，一分钟旋转6度，当前的时刻乘以6就是分针位置
        canvas.rotate(6 * calendar.get(Calendar.MINUTE) + 6 * calendar.get(Calendar.SECOND) / 60.0f, 100, 100);
        Path path1 = new Path();
        path1.moveTo(100, 10);
        path1.lineTo(105, 100);
        path1.lineTo(100, 110);
        path1.lineTo(95, 100);
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        canvas.drawPath(path1, paint);
        canvas.restore();
        canvas.save();
        // 绘制秒针
        // 以100,100位基准，一秒钟旋转6度，当前的时刻乘以6就是秒针位置
        canvas.rotate(6 * calendar.get(Calendar.SECOND) + 6 * calendar.get(Calendar.MILLISECOND) / 1000.0f, 100, 100);
        paint.setColor(Color.RED);
        canvas.drawLine(100, 10, 100, 110, paint);
        canvas.restore();
        // 当前canvas栈还保存数据，将所有的栈数据清空
        canvas.restore();
    }

    /**
     * 计算尺寸
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:// 在一定范围内,尽可能展示完全,在wrap_content时,传入
                widthSize = Math.min(widthSize, DEFAULT_SIZE);
                break;
            case MeasureSpec.EXACTLY:// 规定大小,在match_parent或指定尺寸的时候.
                break;
            case MeasureSpec.UNSPECIFIED:// 未知大小,完全展示,在ListView子控件的高度等
                widthSize = DEFAULT_SIZE;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST:// 在一定范围内,尽可能展示完全,在wrap_content时,传入
                heightSize = Math.min(heightSize, DEFAULT_SIZE);
                break;
            case MeasureSpec.EXACTLY:// 规定大小,在match_parent或指定尺寸的时候.
                break;
            case MeasureSpec.UNSPECIFIED:// 未知大小,完全展示,在ListView子控件的高度等
                heightSize = DEFAULT_SIZE;
                break;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = Math.min(widthSize, heightSize);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(widthSize, heightSize);
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
