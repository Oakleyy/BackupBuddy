package ninja.oakley.backupbuddy.controllers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.util.Callback;

public abstract class AbstractScreenController<T> implements Initializable {

    protected T base;

    public T getBase() {
        return base;
    }

    public abstract void load() throws IOException;

    /**
     * Get the FXMLLoader for a specific file
     *
     * @param clazz
     *            class you want to search from
     * @param name
     *            of the file
     * @return FXMLLoader loaded from file
     * @throws IOException
     */
    protected static FXMLLoader loadFxmlFile(Class<?> clazz, String name) throws IOException {
        return new FXMLLoader(clazz.getResource(name));
    }

    /**
     * Allows you to initialize your own controller for an FXML file
     *
     * @return loader with the controller assigned
     */
    protected static FXMLLoader setController(FXMLLoader loader, Object obj) {
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> paramClass) {
                return obj;
            }
        });
        return loader;
    }

}
