package com.biometrics.cmnd.common.nxView;

import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.swing.NFaceView;
import java.awt.Dimension;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingNode;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class FaceViewLittle  extends SwingNode {
    private final ObjectProperty<NFace> face = new SimpleObjectProperty();
    private NFaceView faceView;

    public FaceViewLittle() {
        SwingUtilities.invokeLater(() -> {
            this.faceView = new NFaceView();
            this.faceView.setFace((NFace)null);
            this.faceView.setAutofit(true);
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(20);
            scrollPane.setHorizontalScrollBarPolicy(30);
            scrollPane.setMinimumSize(new Dimension(100, 100));
            scrollPane.setPreferredSize(new Dimension(500, 500));
            scrollPane.setViewportView(this.faceView);
            this.setContent(scrollPane);
        });
    }

    public final NFace getFace() {
        return (NFace)this.face.get();
    }

    public final void setFace(NFace value) {
        this.face.setValue(value);
        SwingUtilities.invokeLater(() -> {
            this.faceView.setFace(value);
        });
    }

    public ObjectProperty<NFace> faceProperty() {
        return this.face;
    }
}
