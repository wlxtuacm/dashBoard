package com.atc0194.huge_homework;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

/**
 * Title:DashBoardView
 * Description:油量仪表盘和里程数的绘制和更新
 * Created by atc0194
 * Date: 2019/9/02
 */
public class MassDashBoardView extends View {

    private Paint paint , tmpPaint , textPaint ,  strokePain;
    private RectF rect;
    private int backGroundColor;    //背景色
    private float pointLength;      //指针长度
    private float per ;             //指数百分比
    private float perPoint ;        //缓存(变化中)指针百分比
    private float perOld ;          //变化前指针百分比
    private float length ;          //仪表盘半径
    private float r ;
    private int   mileage;

    public MassDashBoardView(Context context) {
        super(context);
        init();
    }

    public MassDashBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width / 2 / 4 * 5;
        initIndex(width / 2 );
        //优化组件高度
        setMeasuredDimension(width, height);
    }



    private void initIndex(int specSize) {
        backGroundColor = Color.WHITE;
        r = specSize;
        length = r  / 4 * 3;
        pointLength =  - (float) (r *  0.6);
        per = 0;
        perOld = 0;
    }


    private void init() {
        paint = new Paint();
        rect = new RectF();
        textPaint = new Paint();
        tmpPaint = new Paint();
        strokePain = new Paint();
    }

    public MassDashBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setR(float r) {
        this.r = r;
        this.length = r  / 4 * 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        //颜色指示的环
        initRing(canvas);
        //刻度文字
        initScale(canvas);
        //指针
        initPointer(canvas);
        //提示内容
        initText(canvas);
    }

    private void initText(Canvas canvas) {
        //抗锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);

        float rIndex = length ;

        //设置文字展示的圆环
        paint.setColor(Color.parseColor("#eeeeee"));
        paint.setShader(null);
        paint.setShadowLayer(5, 0, 0, 0x54000000);
        rect = new RectF( - (rIndex/ 3 ), - (rIndex / 3), rIndex / 3, rIndex / 3);
        canvas.drawArc(rect, 0, 360, true, paint);

        paint.clearShadowLayer();

        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2f , r);


        textPaint.setStrokeWidth(1);
        textPaint.setAntiAlias(true);

        textPaint.setTextSize(43);
        textPaint.setColor(Color.parseColor("#fc6555"));
        textPaint.setTextAlign(Paint.Align.RIGHT);


        //判断指数变化及颜色设定

        if (mileage < 100000){
            textPaint.setColor(Color.parseColor("#79d062"));
        }else if (mileage < 500000) {
            textPaint.setColor(Color.parseColor("#f5a623"));
        }else {
            textPaint.setColor(Color.parseColor("#ff6450"));
        }

        float swidth = textPaint.measureText(String.valueOf(mileage));
        //计算偏移量 是的数字和百分号整体居中显示
        swidth =   (swidth - (swidth + 22) / 2 + 14);


        canvas.translate( swidth , 0);
        canvas.drawText("" + mileage, 0, 0, textPaint);

        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);

        //canvas.drawText("%" , 0, 0, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#999999"));


        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2  , r + length / 3 /2 );
        canvas.drawText("km" , 0, 0, textPaint);
    }


    public void setBackGroundColor(int color){
        this.backGroundColor = color;
    }

    public void setPointLength1(float pointLength1){
        this.pointLength = -length * pointLength1 ;
    }

    private void initScale(Canvas canvas) {
        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);
        paint.setColor(Color.parseColor("#999999"));

        tmpPaint = new Paint(paint); //小刻度画笔对象
        tmpPaint.setStrokeWidth(1);
        tmpPaint.setTextSize(25);
        tmpPaint.setTextAlign(Paint.Align.CENTER);



        canvas.rotate(-90,0f,0f);

        float  y = length;
        y = - y;
        int count = 16; //总刻度数
        paint.setColor(backGroundColor);

        float tempRou = 180 / 16f;

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        //绘制刻度和百分比
        for (int i = 0 ; i <= count ; i++){
            if(i == 0) {
                canvas.drawText(String.valueOf(0),0, y - 20f, tmpPaint);
            } else if (i  == 2 || i == 4 || i == 8 ) {
                canvas.drawText("1/"  + String.valueOf(16 / i) , 0, y - 20f, tmpPaint);
            } else if(i == 16) {
                canvas.drawText(String.valueOf(1),0, y - 20f, tmpPaint);
            } else if(i == 12 ) {
                canvas.drawText("3/4",0, y - 20f, tmpPaint);
            }

            canvas.drawLine(0f, y , 0, y + length / 15, paint);


            canvas.rotate(tempRou,0f,0f);
        }

    }


    private void initPointer(Canvas canvas) {
        paint.setColor(Color.BLACK);


        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);
        float change;

        if (perPoint < 1 ){
            change = perPoint * 180;
        }else {
            change = 180;
        }

        //根据参数得到旋转角度
        canvas.rotate(-90 + change,0f,0f);

        //绘制三角形形成指针
        Path path = new Path();
        path.moveTo(0 , pointLength);
        path.lineTo(-10 , 0);
        path.lineTo(10,0);
        path.lineTo(0 , pointLength);
        path.close();

        canvas.drawPath(path, paint);

    }

    private void initRing(Canvas canvas) {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);


        //前100红黄渐变圆环
        paint.setStyle(Paint.Style.FILL);
        int[] colors = {Color.parseColor("#F95A37"), Color.parseColor("#f9cf45"), Color.parseColor("#2EFE64")};
        float[] positions = {0.5f - 10f/180f * 0.5f, 0.5f + 0.5f * 1f / 4f, 1.0f};
        SweepGradient sweepGradient = new SweepGradient(0, 0, colors, positions);
        paint.setShader(sweepGradient);
        rect = new RectF( -length, -length, length, length);
        canvas.drawArc(rect, 170, 10f + 180f / 6f * 5f, true, paint);



        //100之后绿色渐变圆环
        paint.setStyle(Paint.Style.FILL);
        canvas.rotate(10,0f,0f);
        int[] colors2 = {Color.parseColor("#2EFE64"), Color.parseColor("#01DF3A")};
        float[] positions2 = {0.5f + 0.5f * ( 1.08f / 4f), 1.0f};
        sweepGradient = new SweepGradient(0, 0, colors2, positions2);
        paint.setShader(sweepGradient);
        rect = new RectF( -length, -length, length, length);
        canvas.drawArc(rect, 180f + 180f * (140f / 180f), 180f / 6 + 10, true, paint);



        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);

        strokePain = new Paint(paint);

        strokePain.setColor(0x3f979797);
        strokePain.setStrokeWidth(10);
        strokePain.setShader(null);
        strokePain.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rect, 170, 200, true, strokePain);



        canvas.restore();
        canvas.save();
        canvas.translate(canvas.getWidth()/2, r);

        //底边水平
        paint.setShader(null);
        paint.setColor(backGroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(-length  , (float) (Math.sin(Math.toRadians(10) ) * length /3f * 2f), length  ,  (float) (Math.sin(Math.toRadians(10)) * length  + 100) , paint);
        canvas.drawRect(-length  , (float) (Math.sin(Math.toRadians(10) ) * length /3f * 2f), length  ,  (float) (Math.sin(Math.toRadians(10) ) * length /3f * 2f) , strokePain);


        //内部背景色填充
        paint.setColor(backGroundColor);
        paint.setShader(null);
        rect = new RectF( - (length - length / 3f  - 2), -(length / 3f * 2f - 2), length - length / 3f -2 , length / 3f * 2f - 2);
        canvas.drawArc(rect, 170, 200, true, strokePain);
        canvas.drawArc(rect, 0, 360, true, paint);



    }



    public void cgangePer(float per ){
        this.perOld = this.per;
        per /= 100;
        this.per = per;
        ValueAnimator va =  ValueAnimator.ofFloat(perOld,per);
        va.setDuration(1000);
        va.setInterpolator(new OvershootInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                perPoint = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        va.start();

    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
}

