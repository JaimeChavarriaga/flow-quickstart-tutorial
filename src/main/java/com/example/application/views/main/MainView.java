package com.example.application.views.main;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    Map map = new Map();

    // Add draggable marker
    MarkerFeature marker = new MarkerFeature();

    MarkerFeature myLocation = null;

    public MainView() {
        VerticalLayout todosList = new VerticalLayout();
        TextField taskField = new TextField();
        Button addButton = new Button("Add");
        addButton.addClickListener(click -> {
            Checkbox checkbox = new Checkbox(taskField.getValue());
            todosList.add(checkbox);
        });
        addButton.addClickShortcut(Key.ENTER);

        
        // Set user projection to EPSG:3857
        // Map.setUserProjection("EPSG:3857");
        map.setCenter(new Coordinate(-74.06461250120446, 4.629689071349958));
        map.setZoom(18);

        // Ask permission to use the location adn pass
        // location to the server
        getElement().executeJs("""
            var el = this;
            // https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/watchPosition
            navigator.geolocation.watchPosition(
              position => {
                el.$server.updateMyLocation(position.coords.latitude,position.coords.longitude);
              },
              error => {
                 // those ever happen for the great developers :-)
              },
              {enableHighAccuracy: true, timeout: 5000, maximumAge: 1000 }
            );
        """);


        marker.setId("draggable-marker");
        marker.setDraggable(true);
        marker.setText("Drag me");
        map.getFeatureLayer().addFeature(marker);

        // Listen to marker drop event
        map.addFeatureDropListener(event -> {
            MarkerFeature droppedMarker = (MarkerFeature) event.getFeature();
            Coordinate startCoordinates = event.getStartCoordinate();
            Coordinate endCoordinates = event.getCoordinate();

            Notification.show(
                    "Marker \"" + droppedMarker.getId() + "\" dragged from "
                            + startCoordinates + " to " + endCoordinates);
        });

        add(
                new H1("Vaadin Todo"),
                map,
                new HorizontalLayout(
                        taskField,
                        addButton
                ),
                todosList

        );
    }

    /**
     * Called by the browser on new geolocation updates
     * @param lat latitude
     * @param lon longitude
     */
    @ClientCallable
    private void updateMyLocation(double lat, double lon) {
        if(myLocation == null) {
            myLocation = new MarkerFeature();
            map.getFeatureLayer().addFeature(myLocation);
        }
        Coordinate coordinate = new Coordinate(lon, lat);

        myLocation.setCoordinates(coordinate);
        map.setCenter(coordinate);

        marker.setCoordinates(coordinate);
    }

}
