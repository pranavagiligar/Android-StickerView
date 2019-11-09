package com.greentown.emmarpublicity.stickerview;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.greentown.emmarpublicity.R;
import com.greentown.emmarpublicity.storage.UserPreference;

public abstract class StickerView extends FrameLayout {

    public static final String TAG = "StickerView";
    private ImageView iv_flip;
    public BorderView iv_border;
    private ImageView iv_scale;
    private ImageView iv_horizontal;
    private ImageView iv_verticle;
//    ImageView iv_delete;

    // For scalling
    private float scale_orgX = -1, scale_orgY = -1;
    // For moving
    private float move_orgX = -1, move_orgY = -1;

    private double centerX, centerY;

    private final static int BUTTON_SIZE_DP = 32;
    public final static int SELF_SIZE_DP = 100;

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        iv_border = new BorderView(context);
        iv_scale = new ImageView(context);
        iv_flip = new ImageView(context);
        iv_horizontal = new ImageView(context);
        iv_verticle = new ImageView(context);

        iv_scale.setImageResource(R.drawable.icon_resize);
        iv_horizontal.setImageResource(R.mipmap.ic_horizontal_resize_round);
        iv_verticle.setImageResource(R.mipmap.ic_veretical_resize_round);

        setTag("DraggableViewGroup");
        iv_border.setTag("iv_border");
        iv_scale.setTag("iv_scale");
        iv_horizontal.setTag("iv_horizontal");
        iv_verticle.setTag("iv_verticle");

        int margin = convertDpToPixel(BUTTON_SIZE_DP, getContext()) / 2;
        int size = convertDpToPixel(SELF_SIZE_DP, getContext());

        LayoutParams this_params =
                new LayoutParams(
                        size,
                        size
                );
        this_params.gravity = Gravity.CENTER;

        LayoutParams iv_main_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        iv_main_params.setMargins(margin, margin, margin, margin);

        LayoutParams iv_border_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        iv_border_params.setMargins(margin, margin, margin, margin);

        LayoutParams iv_scale_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_scale_params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        LayoutParams iv_horizontal_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_horizontal_params.gravity = Gravity.CENTER | Gravity.RIGHT;

        LayoutParams iv_verticle_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_verticle_params.gravity = Gravity.CENTER | Gravity.BOTTOM;

        setLayoutParams(this_params);
        addView(getMainView(), iv_main_params);
        addView(iv_border, iv_border_params);
        addView(iv_scale, iv_scale_params);
        addView(iv_horizontal, iv_horizontal_params);
        addView(iv_verticle, iv_verticle_params);

        setOnTouchListener(mTouchListener);
        iv_scale.setOnTouchListener(mTouchListener);
        iv_horizontal.setOnTouchListener(mTouchListener);
        iv_verticle.setOnTouchListener(mTouchListener);
    }

    protected abstract View getMainView();

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (view.getTag().equals("DraggableViewGroup")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "sticker view action down");
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "sticker view action move");
                        float offsetX = event.getRawX() - move_orgX;
                        float offsetY = event.getRawY() - move_orgY;
                        StickerView.this.setX(StickerView.this.getX() + offsetX);
                        StickerView.this.setY(StickerView.this.getY() + offsetY);
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        break;
                }
            } else if (view.getTag().equals("iv_scale")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "iv_scale action down");

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        centerX = StickerView.this.getX() +
                                ((View) StickerView.this.getParent()).getX() +
                                (float) StickerView.this.getWidth() / 2;

                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                ((View) StickerView.this.getParent()).getY() +
                                statusBarHeight +
                                (float) StickerView.this.getHeight() / 2;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "iv_scale action move");

                        double angle_diff = Math.abs(
                                Math.atan2(event.getRawY() - scale_orgY, event.getRawX() - scale_orgX)
                                        - Math.atan2(scale_orgY - centerY, scale_orgX - centerX)) * 180 / Math.PI;

                        Log.v(TAG, "angle_diff: " + angle_diff);

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        int size = convertDpToPixel(SELF_SIZE_DP, getContext());
                        if (length2 > length1
                                && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                        ) {
                            //scale up
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            StickerView.this.getLayoutParams().width += offset;
                            StickerView.this.getLayoutParams().height += offset;
                            onScaling(true);
                        } else if (length2 < length1
                                && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                                && StickerView.this.getLayoutParams().width > size / 2
                                && StickerView.this.getLayoutParams().height > size / 2) {
                            //scale down
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            StickerView.this.getLayoutParams().width -= offset;
                            StickerView.this.getLayoutParams().height -= offset;
                            onScaling(false);
                        }

                        //rotate
//                        double angle = Math.atan2(event.getRawY() - centerY, event.getRawX() - centerX) * 180 / Math.PI;
//                        Log.v(TAG, "log angle: " + angle);
//                        setRotation((float) angle - 45);
//                        Log.v(TAG, "getRotation(): " + getRotation());
//                        onRotating();

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        postInvalidate();
                        requestLayout();
                        break;
                }
            } else if (view.getTag().equals("iv_horizontal")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "iv_horizontal action down");

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        centerX = StickerView.this.getX() +
                                ((View) StickerView.this.getParent()).getX()/* +
                                (float) StickerView.this.getWidth() / 2*/;

                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                ((View) StickerView.this.getParent()).getY() +
                                statusBarHeight/* +
                                (float) StickerView.this.getHeight()*/;
//                        centerX = StickerView.this.getX();
//                        centerY = StickerView.this.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "iv_horizontal action move");

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        if (length2 > length1) {
                            //scale up
                            double offset = Math.round(Math.abs(event.getRawX() - scale_orgX));
                            StickerView.this.getLayoutParams().width += offset;
                            onScaling(true);
                        } else if (length2 < length1) {
                            //scale down
                            double offset = Math.round(Math.abs(event.getRawX() - scale_orgX));
                            StickerView.this.getLayoutParams().width -= offset;
                            onScaling(false);
                        }

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();
                        postInvalidate();
                        requestLayout();
                        break;
                }
            } else if (view.getTag().equals("iv_verticle")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.v(TAG, "iv_verticle action down");

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        centerX = StickerView.this.getX() +
                                ((View) StickerView.this.getParent()).getX()/* +
                                (float) StickerView.this.getWidth()*/;

                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                ((View) StickerView.this.getParent()).getY() +
                                statusBarHeight /*+
                                (float) StickerView.this.getHeight() / 2*/;
//                        centerX = StickerView.this.getX();
//                        centerY = StickerView.this.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "iv_verticle action move");

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        if (length2 > length1) {
                            //scale up
                            double offset = Math.round(Math.abs(event.getRawY() - scale_orgY));
                            StickerView.this.getLayoutParams().height += offset;
                            onScaling(true);
                        } else if (length2 < length1) {
                            //scale down
                            double offset = Math.round(Math.abs(event.getRawY() - scale_orgY));
                            StickerView.this.getLayoutParams().height -= offset;
                            onScaling(false);
                        }
                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();
                        postInvalidate();
                        requestLayout();
                        break;
                }
            }
            return true;
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }

    protected View getImageViewFlip() {
        return iv_flip;
    }

    protected void onScaling(boolean scaleUp) {
    }

    public void setControlsVisibility(boolean isVisible) {
        if(!isVisible) {
            iv_border.setVisibility(View.GONE);
//            iv_delete.setVisibility(View.GONE);
            iv_flip.setVisibility(View.GONE);
            iv_scale.setVisibility(View.GONE);
            iv_horizontal.setVisibility(View.GONE);
            iv_verticle.setVisibility(View.GONE);
        }else{
            iv_border.setVisibility(View.VISIBLE);
//            iv_delete.setVisibility(View.VISIBLE);
            iv_flip.setVisibility(View.VISIBLE);
            iv_scale.setVisibility(View.VISIBLE);
            iv_horizontal.setVisibility(View.VISIBLE);
            iv_verticle.setVisibility(View.VISIBLE);
        }

    }

    private class BorderView extends View {

        private Context context;

        public BorderView(Context context) {
            super(context);
            this.context = context;
        }

        public BorderView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
        }

        public BorderView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.context = context;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Draw sticker border
            LayoutParams params = (LayoutParams) this.getLayoutParams();

            Log.v(TAG, "params.leftMargin: " + params.leftMargin);

            Rect border = new Rect();
            border.left = this.getLeft() - params.leftMargin;
            border.top = this.getTop() - params.topMargin;
            border.right = this.getRight() - params.rightMargin;
            border.bottom = this.getBottom() - params.bottomMargin;
            Paint borderPaint = new Paint();
            UserPreference userPref = new UserPreference(context);
            borderPaint.setStrokeWidth(userPref.getMarkerSize());
            borderPaint.setColor(userPref.getMarkerColor());
            borderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(border, borderPaint);
        }
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }
}
