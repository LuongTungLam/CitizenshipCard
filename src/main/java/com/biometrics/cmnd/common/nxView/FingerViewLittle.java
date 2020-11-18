package com.biometrics.cmnd.common.nxView;

import com.neurotec.biometrics.NFPosition;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NFrictionRidge;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.biometrics.swing.NFingerViewBase.ShownImage;
import com.neurotec.util.NIndexPair;

import java.awt.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingNode;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class FingerViewLittle extends SwingNode {
    private final ObjectProperty<NFinger> finger = new SimpleObjectProperty();
    private NFingerView fingerView;

    public FingerViewLittle() {
        SwingUtilities.invokeLater(() -> {
            this.fingerView = new NFingerView();
            this.fingerView.setFinger((NFrictionRidge) null);
            this.fingerView.setAutofit(true);
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBorder(null);
            scrollPane.setBackground(null);
            scrollPane.setMinimumSize(new Dimension(100, 100));
            scrollPane.setPreferredSize(new Dimension(500, 500));
            scrollPane.setViewportView(this.fingerView);
            this.setContent(scrollPane);
        });
    }

    public final NFinger getFinger() {
        return (NFinger) this.finger.get();
    }

    public final void setFinger(NFinger value) {
        this.finger.set(value);
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setFinger(value);
        });
    }

    public final void setPosition(NFPosition position) {
//        this.finger.get().setPosition(position);
        SwingUtilities.invokeLater(() -> {
            this.fingerView.getFinger().setPosition(position);
        });
    }

    public final NFPosition getPosition() {
        return null;
    }

    public ObjectProperty<NFinger> fingerProperty() {
        return this.finger;
    }

    public void setShownImage(ShownImage shownImage) {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setShownImage(shownImage);
        });
    }

    public void setScale(double scale) {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setScale(scale);
        });
    }

    public void setMateMinutiae(NIndexPair[] matedMinutiae) {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setMatedMinutiae(matedMinutiae);
        });
    }

    public NIndexPair[] getTree() {
        NIndexPair[] tree = new NIndexPair[0];
        RunnableFuture<NIndexPair[]> rf = new FutureTask(() -> {
            return this.fingerView.getMatedMinutiae();
        });
        SwingUtilities.invokeLater(rf);

        try {
            tree = (NIndexPair[]) rf.get();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return tree;
    }

    public void setTree(NIndexPair[] tree) {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setTree(tree);
        });
    }

    public void setMatedMinutiaeIndex(int matedMinutiaeIndex) {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.setMatedMinutiaIndex(matedMinutiaeIndex);
        });
    }

    public void prePareTree() {
        SwingUtilities.invokeLater(() -> {
            this.fingerView.prepareTree();
        });
    }
}
