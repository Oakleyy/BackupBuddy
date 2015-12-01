package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface Request {

    public void execute() throws IOException, GeneralSecurityException;
    
    public double getProgress();

    public void setProgress(double progress);
}
