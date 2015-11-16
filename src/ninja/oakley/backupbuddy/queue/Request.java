package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javafx.scene.control.ProgressBar;

public interface Request {

    public void execute() throws IOException, GeneralSecurityException;

    public void setProgressBar(ProgressBar bar);
}
