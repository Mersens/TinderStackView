package com.mersens.tinderstackview.view;

import android.animation.Animator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mersens.tinderstackview.R;
import com.mersens.tinderstackview.entity.User;
import com.mersens.tinderstackview.utils.DensityUtil;

/**
 * Created by Mersens on 2016/12/1.
 */

public class TinderCardView extends FrameLayout implements View.OnTouchListener {
    private static final int PADDINGVALUE=16;
    private static final float CARD_ROTATION_DEGREES = 40.0f;
    public static final int DURATIONTIME=300;
    private ImageView iv;
    private TextView tv_name;
    private ImageView iv_tips;
    private int padding;
    private float downX;
    private float downY;
    private float newX;
    private float newY;
    private float dX;
    private float dY;
    private float rightBoundary;
    private float leftBoundary;
    private int screenWidth;
    private OnLoadMoreListener listener;

    public TinderCardView(Context context) {
        this(context,null);
    }

    public TinderCardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public TinderCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){

        if(!isInEditMode()){
            inflate(context,R.layout.cardview,this);
            screenWidth=DensityUtil.getScreenWidth(context);
            leftBoundary =  screenWidth * (1.0f/6.0f);
            rightBoundary = screenWidth * (5.0f/6.0f);
            iv=(ImageView) findViewById(R.id.iv);
            tv_name=(TextView) findViewById(R.id.tv_name);
            iv_tips=(ImageView)findViewById(R.id.iv_tips);
            padding = DensityUtil.dip2px(context, PADDINGVALUE);
            setOnTouchListener(this);

        }

    }

    @Override
    public boolean onTouch(final View view, MotionEvent motionEvent) {
        TinderStackLayout tinderStackLayout = ((TinderStackLayout) view.getParent());
        TinderCardView topCard = (TinderCardView) tinderStackLayout.getChildAt(tinderStackLayout.getChildCount() - 1);
        if (topCard.equals(view)) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = motionEvent.getX();
                    downY = motionEvent.getY();
                    view.clearAnimation();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    newX = motionEvent.getX();
                    newY = motionEvent.getY();
                    dX = newX - downX;
                    dY = newY - downY;
                    float posX = view.getX() + dX;
                    view.setX(view.getX() + dX);
                    view.setY(view.getY() + dY);
                    float rotation = (CARD_ROTATION_DEGREES * (posX)) / screenWidth;
                    int halfCardHeight = (view.getHeight() / 2);
                    if(downY < halfCardHeight - (2*padding)){
                        view.setRotation(rotation);
                    } else {
                        view.setRotation(-rotation);
                    }
                    float alpha = (posX - padding) / (screenWidth * 0.3f);
                    if(alpha>0){
                        iv_tips.setAlpha(alpha);
                        iv_tips.setImageResource(R.drawable.ic_like);
                    }else{
                        iv_tips.setAlpha(-alpha);
                        iv_tips.setImageResource(R.drawable.ic_nope);

                    }

                    return true;
                case MotionEvent.ACTION_UP:
                    if(isBeyondLeftBoundary(view)){
                        removeCard(view, -(screenWidth * 2));
                    }
                    else if(isBeyondRightBoundary(view)){
                        removeCard(view,(screenWidth * 2));

                    }else{
                        resetCard(view);
                    }


                    return true;
                default :
                    return super.onTouchEvent(motionEvent);
            }
        }
        return super.onTouchEvent(motionEvent);

    }

    private boolean isBeyondLeftBoundary(View view){
        return (view.getX() + (view.getWidth() / 2) < leftBoundary);
    }

    private boolean isBeyondRightBoundary(View view){
        return (view.getX() + (view.getWidth() / 2) > rightBoundary);
    }

    private void removeCard(final View view, int xPos){
        view.animate()
                .x(xPos)
                .y(0)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(DURATIONTIME)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                       ViewGroup viewGroup = (ViewGroup) view.getParent();
                        if(viewGroup != null) {
                            viewGroup.removeView(view);
                        }
                        int count=viewGroup.getChildCount();
                        if(count==1 && listener!=null){
                            listener.onLoad();
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }


    private void resetCard(final View view){

        view.animate()
                .x(0)
                .y(0)
                .rotation(0)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(DURATIONTIME);
        iv_tips.setAlpha(0f);

    }

    public void bind(User u){
        if(u==null){
            return;
        }
        if(!TextUtils.isEmpty(u.getAvatarUrl())){
            Glide.with(iv.getContext())
                    .load(u.getAvatarUrl())
                    .into(iv);
        }
        if(!TextUtils.isEmpty(u.getName())){
            tv_name.setText(u.getName());
        }
    }

    public interface OnLoadMoreListener{
        void onLoad();
    }



    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        this.listener=listener;
    }
}
