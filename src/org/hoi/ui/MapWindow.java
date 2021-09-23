package org.hoi.ui;

import org.hoi.Project;
import org.hoi.classes.history.Country;
import org.hoi.classes.history.State;
import org.hoi.classes.utils.ImageResult;
import org.hoi.graphics.Button;
import org.hoi.graphics.Window;
import org.hoi.various.ImagePanel;
import org.hoi.various.collection.KeyedValues;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapWindow extends Window {
    final public Project project;
    private Country selectedCountry;
    private State selectedState;

    private Button back;
    private JList<Country> countryList;
    final private ImagePanel map;

    public MapWindow (Project project, int width, int height) {
        super(project.name, width, height);
        this.setLayout(new BorderLayout());
        this.project = project;

        this.map = new ImagePanel(this.project.getStateMap(x -> Color.gray));
        this.back = new Button("Back") {
            @Override
            public void onClick() {
                ProjectWindow window = new ProjectWindow(project, getWidth(), getHeight());
                window.display();
                MapWindow.this.dispose();
            }
        };

        listSetup();
        mapSetup();
    }

    private void updateMap () {
        this.map.setImage(this.project.getStateMap(x -> {
            Color color = this.selectedCountry == null ? Color.gray : (this.selectedCountry == x.owner ? x.owner.color : (x.isClaimedBy(this.selectedCountry) | x.isCoreOf(this.selectedCountry) ? this.selectedCountry.color.brighter() : Color.gray));
            return x == selectedState ? color.darker() : color;
        }));
    }

    private void listSetup () {
        java.util.List<Country> countries = this.project.getCountries();
        countries.sort(Comparator.comparing(x -> x.tag));

        DefaultListModel<Country> dlm = new DefaultListModel();
        dlm.addAll(countries);

        this.countryList = new JList<Country>(dlm);
        this.countryList.addListSelectionListener(x -> {
            this.selectedCountry = this.countryList.getSelectedValue();
            updateMap();
        });

        this.add(new JScrollPane(this.countryList), BorderLayout.EAST);
    }

    private void mapSetup () {
        final int[] mousePos = new int[2];

        this.map.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 3 && selectedCountry != null) { // LEFT CLICK
                    project.setOwner(selectedState, selectedCountry);
                    updateMap();
                    return;
                }

                if (e.getButton() != 1) {
                    return;
                }

                Point intPoint = e.getPoint();
                Point2D.Float point = new Point2D.Float(intPoint.x, intPoint.y);

                point.x *= (float) map.getTransformedWidth() / map.getWidth();
                point.y *= (float) map.getTransformedHeight() / map.getHeight();

                point.x -= map.getDeltaX();
                point.y -= map.getDeltaY();

                point.x += (map.getImageWidth() - map.getTransformedWidth()) / 2f;
                point.y += (map.getImageHeight() - map.getTransformedHeight()) / 2f;

                java.util.List<State> candidates = project.getStates().stream().filter(x -> x.getBounds().contains(point)).collect(Collectors.toList());
                if (candidates.size() == 1) {
                    selectedState = candidates.get(0);
                } else {
                    selectedState = candidates.stream().filter(x -> {
                        ImageResult result = x.getImage();
                        int localX = (int) (point.x - result.point.x);
                        int localY = (int) (point.y - result.point.y);

                        return (result.img.getRGB(localX, localY) & 0xFF) != 0;
                    }).findFirst().orElse(null);
                }

                updateMap();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        this.map.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
                    map.addDelta(-20f * e.getX() / map.getWidth() + 10f, -20f * e.getY() / map.getHeight() + 10f);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos[0] = e.getX();
                mousePos[1] = e.getY();
            }
        });

        this.map.addMouseWheelListener(x -> {
            this.map.addZoom(- (float) x.getPreciseWheelRotation() / 10f);
            this.map.addDelta(-20f * mousePos[0] / this.map.getWidth() + 10f, -20f * mousePos[1] / this.map.getHeight() + 10f);
        });

        this.add(this.map, BorderLayout.CENTER);
    }
}
