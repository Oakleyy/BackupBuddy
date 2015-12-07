package ninja.oakley.backupbuddy.queue;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ninja.oakley.backupbuddy.BackupBuddy;

public interface Request {

    public void execute(BackupBuddy instance) throws IOException, GeneralSecurityException;

    public double getProgress();

    public void setProgress(double progress);
}
