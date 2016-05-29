package just.juced.justtest.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import just.juced.justtest.application.MyApplication;

/**
 * Created by juced on 04.04.2016.
 */
public class AnimationHelper {

    public static void toggleViews(Context context, final boolean show, final View view_1, final View view_2) {
        if (context == null) {
            context = MyApplication.getSingleton().getApplicationContext();
        }

        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        view_1.setVisibility(show ? View.GONE : View.VISIBLE);
        view_1.animate().setInterpolator(new FastOutSlowInInterpolator()).setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view_1.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        view_2.setVisibility(show ? View.VISIBLE : View.GONE);
        view_2.animate().setInterpolator(new FastOutSlowInInterpolator()).setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view_2.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public static void expand(final View v, int duration) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(new FastOutSlowInInterpolator());
        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static void collapse(final View v, int duration) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                }
                else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(new FastOutSlowInInterpolator());
        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static void animateOut(final View view) {
        view.setAlpha(1);
        view.animate()
                .alpha(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        //isAnimatingOut = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //isAnimatingOut = false;
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    public static void animateIn(final View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0);
        view.animate()
                .alpha(1)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(null)
                .start();
    }

    public static void animateHeight(final View v, int new_height, int duration) {
        final ViewGroup.LayoutParams params = v.getLayoutParams();

        int oldHeight = params.height;
        if (oldHeight == -2) {
            oldHeight = v.getMeasuredHeight();
        }

        ValueAnimator animator = ValueAnimator.ofInt(oldHeight, new_height);
        if (params.height != new_height) {
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.height = (Integer) valueAnimator.getAnimatedValue();
                    v.requestLayout();
                }
            });
            animator.setDuration(duration);
            animator.start();
        }
    }

    public static void animateTransparent(View view, float from, float to, int visibility) {
        if (view.getVisibility() == visibility) {
            return;
        }

        Animation animation = new AlphaAnimation(from, to);
        animation.setDuration(300);
        view.startAnimation(animation);
        view.setVisibility(visibility);
    }

    public static void animateTransparent(View view, float from, float to) {
        Animation animation = new AlphaAnimation(from, to);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

}
