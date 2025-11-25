package com.jakir.multiselectdragselectanchorselect;

import android.view.View;

import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * Created by JAKIR HOSSAIN on 11/23/2025.
 ***********************************************************************************************/

public class AnimHelper {

    // Real physics spring animation (attractive icon pop)
    public static void animateSpring(View view, boolean fastAnimation) {

        float startScale = fastAnimation ? 0.8f : 0.05f;   // start size
        float stiffness = fastAnimation ? SpringForce.STIFFNESS_MEDIUM :          // slower / softer
                SpringForce.STIFFNESS_LOW;                // faster / sharper

        // more bounce
        float damping = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;  // controlled bounce

        int fadeDuration = fastAnimation ? 100 : 150;      // fade-in time

        // Set initial state
        view.setScaleX(startScale);
        view.setScaleY(startScale);
        view.setAlpha(0f);

        // Fade-in animation
        view.animate().alpha(1f).setDuration(fadeDuration).start();

        // Spring X
        SpringAnimation springX = new SpringAnimation(view, SpringAnimation.SCALE_X, 1f);
        springX.getSpring().setStiffness(stiffness);
        springX.getSpring().setDampingRatio(damping);

        // Spring Y
        SpringAnimation springY = new SpringAnimation(view, SpringAnimation.SCALE_Y, 1f);
        springY.getSpring().setStiffness(stiffness);
        springY.getSpring().setDampingRatio(damping);

        springX.start();
        springY.start();
    }

    // Attractive show/hide animation (flexible)
    public static void animateFancy(View view, boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
            view.setScaleX(0.4f);
            view.setScaleY(0.4f);
            view.setAlpha(0f);

            view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).setInterpolator(new android.view.animation.OvershootInterpolator(1.3f)) // little bounce
                    .start()
            ;
        } else {
            view.animate().scaleX(0.6f).scaleY(0.6f).alpha(0f).setDuration(160).setInterpolator(new android.view.animation.AccelerateInterpolator()).withEndAction(() -> view.setVisibility(View.GONE)).start();
        }
    }
}
