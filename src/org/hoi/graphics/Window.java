package org.hoi.graphics;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Window extends Frame {
    final private Listener listener;

    public Window (String title, int width, int height) {
        super(title);
        this.setSize(width, height);
        this.setLayout(new FlowLayout());

        this.listener = new Listener();
        this.addWindowListener(this.listener);
    }

    public void display () {
        this.setVisible(true);
    }

    private class Listener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            Window.this.setVisible(false);
            Window.this.dispose();
        }
    }
}
