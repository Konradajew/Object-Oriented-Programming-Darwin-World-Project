package project;

import project.gui.App;
import javafx.application.Application;

public class Main {
    public static void main(String[] args){

        try {
            System.out.println("System started");

            Application.launch(App.class, args);

            System.out.println("System finished");
        } catch ( IllegalArgumentException ex) {
            System.err.println(ex);

        }
    }
}
