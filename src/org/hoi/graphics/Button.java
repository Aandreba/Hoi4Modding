package org.hoi.graphics;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class Button extends java.awt.Button {
    final private Listener listener;

    public Button (String label) throws HeadlessException {
        super(label);
        this.listener = new Listener();
        this.addActionListener(this.listener);
    }

    public abstract void onClick ();

    private class Listener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            onClick();
        }
    }
}
