package com.agrisoft.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationHelper {

    public static void fadeIn(Node node) {
        fadeIn(node, 500);
    }

    public static void fadeIn(Node node, int durationMillis) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMillis), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();
    }

    public static void slideIn(Node node, double startX, double endX) {
        slideIn(node, startX, endX, 500);
    }

    public static void slideIn(Node node, double startX, double endX, int durationMillis) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMillis), node);
        translateTransition.setFromX(startX);
        translateTransition.setToX(endX);
        translateTransition.setCycleCount(1);
        translateTransition.play();
    }

    public static void pulse(Node node) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), node);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> fadeIn.play());
        fadeIn.setOnFinished(e -> fadeOut.play());

        fadeOut.play();
    }
}