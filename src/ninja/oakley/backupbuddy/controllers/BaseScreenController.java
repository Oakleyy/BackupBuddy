package ninja.oakley.backupbuddy.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ninja.oakley.backupbuddy.BackupBuddy;
import ninja.oakley.backupbuddy.PreRefreshRunnable;
import ninja.oakley.backupbuddy.TreeItemDownloadRunnable;
import ninja.oakley.backupbuddy.project.CreateProjectRunnable;
import ninja.oakley.backupbuddy.project.ProjectController;
import ninja.oakley.backupbuddy.queue.UploadRequest;

public class BaseScreenController extends AbstractScreenController<AnchorPane> {

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
        fileList.setContextMenu(instance.getContextMenuController().getBase());
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

        ProjectController controller = instance.getProjects().get(current);
        if (controller == null) {
            logger.error("No BucketManager exists for " + current);
            return;
        }

        setSelectedProjectController(controller);

        refresh();

        prevProject = current;

    }

    @FXML
    public void onAddProject() {
        /*if (instance.getSecondaryStage().isShowing()) {
            logger.warn("Secondary window already being used.");
            return;
        }

        instance.getAddProjectController().openWindow();*/
        File sel = fileChooser.showOpenDialog(instance.getPrimaryStage());
        
        if(sel == null){
            return;
        }
        
        new Thread(new CreateProjectRunnable(instance, sel.toPath())).start();
        
    }

    @FXML
    public void onBucketSelect() {
        String currentValue = bucketComboBox.getValue();

        if (currentValue == null || currentValue.isEmpty() || currentValue.equalsIgnoreCase(prevBucket)) {
            return;
        }

        refresh();

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

        ProjectController controller = getSelectedProjectController();
        if (controller == null) {
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

            UploadRequest req = new UploadRequest(controller, file.toPath(), bucketName);
            instance.getRequestHandler().addRequest(req);
        }

    }

    @FXML
    public void onDownloadSelect() {

        if (isBucketSelected()) {
            logger.debug("No bucket selected.");
            return;
        }

        ProjectController controller = getSelectedProjectController();
        if (controller == null) {
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

        new Thread(new TreeItemDownloadRunnable(instance, selected, saveLocation.toPath(), fileList)).start();
    }

    @FXML
    public void onManageKeys() {

    }

    public void refresh() {
        new Thread(new PreRefreshRunnable(instance)).start();
    }

    @FXML
    public void onRefreshSelect() {
        refresh();
    }

    private boolean isBucketSelected() {
        String bucketName = getCurrentBucket();
        return bucketName == null || bucketName.isEmpty();
    }

    public void setSelectedProjectController(ProjectController projectController) {
        if (projectComboBox.getItems().contains(projectController.getProjectId())
                || instance.getProjects().containsKey(projectController.getProjectId())) {
            projectComboBox.setValue(projectController.getProjectId());
            return;
        }

    }

    public ProjectController getSelectedProjectController() {
        String value = projectComboBox.getValue();
        if(value == null) return null;
        
        return instance.getProjects().get(value);
    }

    public String getCurrentBucket() {
        return bucketComboBox.getValue();
    }

    public void updateProjectList() {
        ObservableList<String> items = FXCollections.observableArrayList(instance.getProjects().keySet());
        projectComboBox.setItems(items);
    }

    public void setBucketList(ObservableList<String> items) {
        bucketComboBox.setItems(items);
    }

    public ObservableList<String> updateBucketList() throws IOException, GeneralSecurityException {
        ObservableList<String> items = FXCollections.observableArrayList();
        List<Bucket> buckets = getSelectedProjectController().getBuckets();
        ListIterator<Bucket> iter = buckets.listIterator();

        while (iter.hasNext()) {
            Bucket bucket = iter.next();
            items.add(bucket.getName());
        }

        return items;
    }

    public void setFileList(TreeItem<String> files) {
        fileList.setRoot(files);
    }

    public TreeItem<String> updateFileList() throws IOException, GeneralSecurityException {
        String currentBucket = bucketComboBox.getValue();
        List<StorageObject> files = getSelectedProjectController().listBucket(currentBucket);
        return organize(files);
    }

    private TreeItem<String> organize(List<StorageObject> files) {
        ListIterator<StorageObject> iter = files.listIterator();
        TreeItem<String> root = new TreeItem<String>();
        Map<String, TreeItem<String>> paths = new HashMap<>();

        while (iter.hasNext()) {
            StorageObject next = iter.next();
            String st = next.getName();

            if (!st.contains("/")) {
                TreeItem<String> item = new TreeItem<String>(st);
                root.getChildren().add(item);
                continue;
            }

            int index = st.lastIndexOf('/');
            int indexS = st.lastIndexOf('/', index - 1);

            String folderName = st.substring(indexS < 0 ? 0 : indexS + 1, index + 1);
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

    @Override
    public void load() throws IOException {
        FXMLLoader baseLoader = loadFxmlFile(BaseScreenController.class, "Base.fxml");
        setController(baseLoader, this);
        base = (AnchorPane) baseLoader.load();
    }

}
