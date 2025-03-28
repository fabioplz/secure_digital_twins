package app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Push
@PWA(name = "Breathe Simulator Web version", shortName = "Breathe", iconPath = "icons/bicon.svg")
@Theme("my-theme")
public class Main implements AppShellConfigurator {
    private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

    }

}