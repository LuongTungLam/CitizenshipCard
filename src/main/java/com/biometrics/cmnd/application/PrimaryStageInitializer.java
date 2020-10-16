package com.biometrics.cmnd.application;

import com.biometrics.cmnd.controller.CitizenshipCard;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver){
        this.fxWeaver = fxWeaver;
    }


    @Override
    public void onApplicationEvent(StageReadyEvent readyEvent) {
        Stage stage = readyEvent.stage;
        Scene scene = new Scene(fxWeaver.loadView(CitizenshipCard.class),740,529);
        stage.setScene(scene);
        stage.show();
    }
}
