package ninja.oakley.backupbuddy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class Project {

	private String nickName;
	private String projectId;
	
	private Path filePath;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}
	
	public InputStream getInputStream() throws FileNotFoundException{
		return new FileInputStream(filePath.toFile());
	}
	
}
