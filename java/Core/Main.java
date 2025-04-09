package Core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ui.ConsoleUI;

/**
 * Main application class for the custom console. This is the entry point of the
 * application.
 */
public class Main extends Application {

	@SuppressWarnings("exports")
	@Override
	public void start(Stage primaryStage) {
		ConsoleUI consoleUI = new ConsoleUI();
		consoleUI.setStyle("-fx-background-color: rgba(30,30,35,0.95);");

		Scene scene = new Scene(consoleUI, 800, 600);
		scene.getStylesheets().add(getClass().getResource("/ui/ConsoleStyle.css").toExternalForm());

		try {
			Image icon = new Image(getClass().getResourceAsStream("/ui/ico.png"));
			primaryStage.getIcons().add(icon);
		} catch (Exception e) {
			System.err.println("Failed to load application icon: " + e.getMessage());
		}

		primaryStage.setTitle("ConsoleX");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Set focus to the input field after showing the window
		consoleUI.focusInput();
	}

	public static void main(String[] args) {
		launch(args);
	}
}