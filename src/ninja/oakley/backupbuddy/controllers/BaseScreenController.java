package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.RefreshRunnable;
import ninja.oakley.backupbuddy.project.BucketManager;
import ninja.oakley.backupbuddy.queue.DownloadRequest;
import ninja.oakley.backupbuddy.queue.UploadRequest;

public class BaseScreenController implements Initializable {

    private static final Logger logger = LogManager.getLogger(BaseScreenController.class);
    private BackupBuddy instance;

    private String prevProject;
    private String prevBucket;

    private FileChooser fileChooser;
    private DirectoryChooser dirChooser;

    @FXML
    private ComboBox<String> projectComboBox;

    @FXML
    private ComboBox<String> bucketComboBox;

    @FXML
    private TreeView<String> fileList;

    @FXML
    public ProgressBar progressBar;

    @FXML
    private SplitMenuButton actionMenu;

    public BaseScreenController(BackupBuddy instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        dirChooser.setTitle("Select a save location...");

        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileList.setShowRoot(false);
    }

    @FXML
    public void onQueueClick() {
        instance.getQueueController().openWindow();
    }

    @FXML
    public void onProjectSelect() {
        String current = projectComboBox.getValue();

        if (current == null || current.isEmpty() || current.equalsIgnoreCase(prevProject)) {
            return;
        }

        BucketManager bucketManager = instance.getProjects().get(current);
        if (bucketManager == null) {
            logger.error("No BucketManager exists for " + current);
            return;
        }

        setCurrentBucketManager(bucketManager);

        try {
            updateBucketList();
        } catch (IOException e) {
            logger.error("Couldn't load authentication " + e);
            return;
        } catch (GeneralSecurityException e) {
            logger.error("Couldn't authenticate " + e);
            return;
        }

        prevProject = current;

    }

    @FXML
    public void onAddProject() {
        if (instance.getSecondaryStage().isShowing()) {
            logger.warn("Secondary window already being used.");
            return;
        }

        instance.getAddProjectController().openWindow();
    }

    @FXML
    public void onBucketSelect() {
        String currentValue = bucketComboBox.getValue();

        if (currentValue == null || currentValue.isEmpty() || currentValue.equalsIgnoreCase(prevBucket)) {
            return;
        }

        try {
            updateFileList();
        } catch (IOException e) {
            logger.error("Couldn't load authentication " + e);
            return;
        } catch (GeneralSecurityException e) {
            logger.error("Couldn't authenticate " + e);
            return;
        }

        prevBucket = currentValue;
    }

    @FXML
    public void onAddBucket() {
        if (instance.getSecondaryStage().isShowing()) {
            logger.warn("Secondary window already being used.");
            return;
        }

        instance.getAddBucketController().openWindow();
    }

    @FXML
    public void onUploadSelect() {

        String bucketName = getCurrentBucket();
        if (isBucketSelected()) {
            logger.debug("No bucket selected.");
            return;
        }

        BucketManager manager = getCurrentBucketManager();
        if (manager == null) {
            logger.debug("No project is selected.");
            return;
        }

        List<File> list = fileChooser.showOpenMultipleDialog(instance.getPrimaryStage());
        if (list == null || list.isEmpty()) {
            return;
        }

        ListIterator<File> iter = list.listIterator();
        while (iter.hasNext()) {
            File file = iter.next();

            if (file == null) {
                return;
            }

            UploadRequest req = new UploadRequest(manager, file.toPath(), bucketName);
            instance.getRequestManager().addRequest(req);
            logger.debug("Request for upload: " + file.getAbsolutePath());
        }

    }

    @FXML
    public void onDownloadSelect() {

        if (isBucketSelected()) {
            logger.debug("No bucket selected.");
            return;
        }

        BucketManager manager = getCurrentBucketManager();
        if (manager == null) {
            logger.debug("No project is selected.");
            return;
        }

        List<TreeItem<String>> selected = fileList.getSelectionModel().getSelectedItems();

        if (selected == null || selected.isEmpty()) {
            logger.debug("No files selected for download.");
            return;
        }

        File saveLocation = dirChooser.showDialog(instance.getPrimaryStage().getOwner());
        if (saveLocation == null || !saveLocation.isDirectory()) {
            logger.debug("No directory selected.");
            return;
        }

        ListIterator<TreeItem<String>> iter = selected.listIterator();
        while (iter.hasNext()) {
            TreeItem<String> next = iter.next();
            TreeItem<String> parent = next.getParent();
            String path = "";

            while (parent != null && parent != fileList.getRoot()) {
                path = parent.getValue() + "/" + path;
                parent = parent.getParent();
            }

            path += next.getValue();

            logger.info(path);

            DownloadRequest req = new DownloadRequest(manager, path,
                    Paths.get(saveLocation.getAbsolutePath(), next.getValue()), getCurrentBucket());
            instance.getRequestManager().addRequest(req);
        }
    }

    @FXML
    public void onRefreshSelect() {
        new Thread(new RefreshRunnable(instance));

    }

    private boolean isBucketSelected() {
        String bucketName = getCurrentBucket();
        return bucketName == null || bucketName.isEmpty();
    }

    public void setCurrentBucketManager(BucketManager bucketManager) {
        if (projectComboBox.getItems().contains(bucketManager.getProjectId())
                || instance.getProjects().containsKey(bucketManager.getProjectId())) {
            projectComboBox.setValue(bucketManager.getProjectId());
            return;
        }

    }

    public BucketManager getCurrentBucketManager() {
        return instance.getProjects().get(projectComboBox.getValue());
    }

    public String getCurrentBucket() {
        return bucketComboBox.getValue();
    }

    public void updateProjectList() {
        ObservableList<String> items = FXCollections.observableArrayList(instance.getProjects().keySet());
        projectComboBox.setItems(items);
    }

    public void updateBucketList() throws IOException, GeneralSecurityException {
        ObservableList<String> items = FXCollections.observableArrayList();
        List<Bucket> buckets = getCurrentBucketManager().getBuckets();
        ListIterator<Bucket> iter = buckets.listIterator();

        while (iter.hasNext()) {
            Bucket bucket = iter.next();
            items.add(bucket.getName());
        }

        bucketComboBox.setItems(items);
    }

    public void updateFileList() throws IOException, GeneralSecurityException {
        String currentBucket = bucketComboBox.getValue();
        List<StorageObject> files = getCurrentBucketManager().listBucket(currentBucket);
        TreeItem<String> root = organize(files);
        fileList.setRoot(root);
    }

    private TreeItem<String> organize(List<StorageObject> files) {
        ListIterator<StorageObject> iter = files.listIterator();
        TreeItem<String> root = new TreeItem<String>();
        Map<String, TreeItem<String>> paths = new HashMap<>();

        while (iter.hasNext()) {
            StorageObject next = iter.next();
            String st = next.getName();

            if (!st.contains("/")) {
                root.getChildren().add(new TreeItem<String>(st));
                continue;
            }

            int index = st.lastIndexOf('/');
            int indexS = st.lastIndexOf('/', index - 1);

            String folderName = st.substring(indexS < 0 ? 0 : indexS + 1, index);
            if (!st.endsWith("/")) {
                String path = st.substring(0, index + 1);
                String name = st.substring(index + 1);

                TreeItem<String> folder = paths.getOrDefault(path, new TreeItem<String>(folderName));
                folder.getChildren().add(new TreeItem<String>(name));
                continue;
            }
            paths.putIfAbsent(st, new TreeItem<String>(folderName));
        }

        Iterator<Entry<String, TreeItem<String>>> set = paths.entrySet().iterator();
        while (set.hasNext()) {
            Entry<String, TreeItem<String>> next = set.next();
            String path = next.getKey();

            int index = path.lastIndexOf('/', path.length() - 2);
            if (index >= 0) {
                String shortPath = path.substring(0, index + 1);
                TreeItem<String> folder = paths.get(shortPath);
                folder.getChildren().add(next.getValue());
                continue;
            }
            root.getChildren().add(next.getValue());
        }

        return root;
    }

    public void clearFileList() {
        fileList.setRoot(new TreeItem<String>());
    }

}
