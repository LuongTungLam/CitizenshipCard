package com.biometrics.cmnd.application;

import com.biometrics.cmnd.controller.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;
    private double x, y;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver){
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent readyEvent) {
        Stage stage = readyEvent.stage;
        Parent p = fxWeaver.loadView(LoginBioSSO.class);
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        p.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });
        p.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - x);
            stage.setY(e.getScreenY() - y);
        });
        stage.show();
    }
}
