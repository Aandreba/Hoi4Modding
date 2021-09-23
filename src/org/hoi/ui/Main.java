package org.hoi.ui;

import org.hoi.Project;
import org.hoi.various.Config;
import org.hoi.graphics.Button;
import org.hoi.graphics.Window;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class Main {
    final public static Window WINDOW = new Window("Hello world", 900, 900);

    final public static Button SET_HOI4_FOLDER = new Button("Reload game data") {
        @Override
        public void onClick () {
            JFileChooser dialog = new JFileChooser(Config.getDefaultDir());
            dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dialog.showOpenDialog(WINDOW);

            try {
                File selected = dialog.getSelectedFile();
                Config.setHoi4Dir(selected);
                Config.loadHoi4Data();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    final public static Button NEW_PROJECT_BUTTON = new Button ("New Project") {
        @Override
        public void onClick() {
            Window dialog = new Window("New Project", 200, 100);
            TextField name = new TextField();
            name.setColumns(10);

            Button create = new Button("Create") {
                @Override
                public void onClick() {
                    createWindow(dialog, name);
                }
            };

            dialog.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        createWindow(dialog, name);
                    }

                    System.out.println(e.getKeyCode());
                }
            });

            dialog.add(name);
            dialog.add(create);
            dialog.setVisible(true);
        }

        private void createWindow (Window dialog, TextField name) {
            Project project = new Project(name.getText());
            ProjectWindow window = new ProjectWindow(project, 900, 900);

            window.setVisible(true);
            dialog.dispose();
            WINDOW.dispose();
        }
    };

    static {
        WINDOW.add(SET_HOI4_FOLDER);
        WINDOW.add(NEW_PROJECT_BUTTON);
    }

    public static void main (String... args) {
        WINDOW.display();
    }
}
