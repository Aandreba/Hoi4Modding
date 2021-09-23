package org.hoi.ui;

import org.hoi.Project;
import org.hoi.graphics.Button;
import org.hoi.graphics.Window;

public class ProjectWindow extends Window {
    final private Project project;
    final private Button map;

    public ProjectWindow (Project project, int width, int height) {
        super(project.name, width, height);
        this.project = project;

        this.map = new Button("Map") {
            @Override
            public void onClick() {
                MapWindow window = new MapWindow(project, width, height);
                window.display();
                ProjectWindow.this.dispose();
            }
        };


        this.add(this.map);
    }
}
