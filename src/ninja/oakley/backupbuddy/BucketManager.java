package ninja.oakley.backupbuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Buckets;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;

import ninja.oakley.backupbuddy.controllers.BucketClass;
import ninja.oakley.backupbuddy.controllers.BucketLocation;

public class BucketManager {

	private Project project;

	private Storage storageService;
	private JsonFactory jsonFactory;
	private HttpTransport httpTransport;
	private InputStream credentialInputStream;

	private RequestThread uploadThread;

	private BucketManager(BucketManager.Builder builder){
		this.project = builder.project;
		this.jsonFactory = builder.jsonFactory != null ? builder.jsonFactory : JacksonFactory.getDefaultInstance();
		this.httpTransport = builder.httpTransport;
		this.credentialInputStream = builder.credentialInputStream;
	}

	public Project getProject(){
		return this.project;
	}

	public String getProjectId(){
		return this.project.getProjectId();
	}

	/**
	 * Get all of the objects that are in a bucket
	 * 
	 * @param bucketName name of the bucket you'd like parse data from
	 * @return a list of all of the objects in the bucket
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public List<StorageObject> listBucket(String bucketName) throws IOException, GeneralSecurityException {
		Storage.Objects.List listRequest = getStorage().objects().list(bucketName);

		List<StorageObject> rt = new ArrayList<StorageObject>();
		Objects objects; //Google's fancy array

		/*
		 * Use a do-while statement to allow for a more compact method. If it was just
		 * a while statement you would have to execute once before going into the loop so that server knows
		 * what entry is next
		 */
		do {
			objects = listRequest.execute();

			List<StorageObject> items = objects.getItems();
			if(items != null){
				rt.addAll(items);
			}

			listRequest.setPageToken(objects.getNextPageToken());
		} while (null != objects.getNextPageToken());

		return rt;
	}

	/**
	 *  Retrieve a bucket by a specific name
	 *  
	 * @param bucketName
	 * @return bucket requested, null if storageService is not active
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
		Storage.Buckets.Get bucketRequest = getStorage().buckets().get(bucketName);
		bucketRequest.setProjection("full");
		return bucketRequest.execute();
	}

	public Bucket createBucket(String name, BucketClass storageClass, BucketLocation location) throws IOException, GeneralSecurityException {
		System.out.println(storageClass.toString());
		return getStorage().buckets().insert(project.getProjectId(), new Bucket()
				.setName(name)
				.setLocation(location.toString())
				.setStorageClass(storageClass.toString())).execute();
	}

	/**
	 * Upload a file to the Google Cloud Storage servers
	 * Takes any type of InputStream, converts to an InputStreamContent and then uploads
	 * 
	 * @param name
	 * @param contentType
	 * @param stream
	 * @param bucketName
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void uploadStream(String bucketName, String name, String contentType, File file, MediaHttpUploaderProgressListener listener, Modifier... mod) throws IOException, GeneralSecurityException {
		InputStreamContent contentStream = new InputStreamContent(contentType, new FileInputStream(file));
		contentStream.setLength(file.length());
		/*
		 * Sets the metadata such as name, access permissions, etc.
		 */
		StorageObject objectMetadata = new StorageObject().setName(name)
				.setAcl(Arrays.asList(new ObjectAccessControl().setEntity("allUsers").setRole("READER")));

		Storage.Objects.Insert insertRequest = getStorage().objects().insert(bucketName, objectMetadata, contentStream);

		insertRequest.getMediaHttpUploader().setProgressListener(listener);
		insertRequest.execute();
	}

	public void downloadStream(String bucketName, String name, Path save, MediaHttpDownloaderProgressListener listener) throws IOException, GeneralSecurityException{
		Storage.Objects.Get getRequest = getStorage().objects().get(bucketName, name);

		getRequest.getMediaHttpDownloader().setProgressListener(listener);
		getRequest.executeMediaAndDownloadTo(new FileOutputStream(save.toFile()));
	}

	public GZIPInputStream compressStream(InputStream stream) throws IOException {
		return new GZIPInputStream(stream);
	}

	public CipherInputStream encryptStream(InputStream stream, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		rsaCipher.init(Cipher.ENCRYPT_MODE, key);
		return new CipherInputStream(stream, rsaCipher);
	}

	public List<Bucket> getBuckets() throws IOException, GeneralSecurityException{
		Storage.Buckets.List listBuckets = getStorage().buckets().list(getProjectId());
		List<Bucket> rt = new ArrayList<Bucket>();
		Buckets buckets;

		do {
			buckets = listBuckets.execute();
			rt.addAll(buckets.getItems());

			listBuckets.setPageToken(buckets.getNextPageToken());
		} while (null != buckets.getNextPageToken());

		return rt;
	}

	public Storage constructStorageService() throws IOException, GeneralSecurityException{
		HttpTransport httpTransport = getHttpTransport();
		GoogleCredential cred = GoogleCredential.fromStream(this.credentialInputStream, httpTransport, this.jsonFactory);


		if (cred.createScopedRequired()) {
			cred = cred.createScoped(StorageScopes.all());
		}

		this.storageService = new Storage.Builder(httpTransport, this.jsonFactory, cred).setApplicationName(this.getProjectId()).build();
		return storageService;
	}

	public boolean isConstructed(){
		return (storageService != null ? true : false);
	}

	public boolean isUploadThreadAlive(){
		if(uploadThread != null && uploadThread.isAlive()){
			return true;
		}
		return false;
	}

	private HttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
		return this.httpTransport != null ? this.httpTransport : GoogleNetHttpTransport.newTrustedTransport();
	}

	private Storage getStorage() throws IOException, GeneralSecurityException{
		return (storageService != null ? this.storageService : constructStorageService());
	}


	public static class Builder {

		private Project project;

		private JsonFactory jsonFactory;
		private HttpTransport httpTransport;
		private InputStream credentialInputStream;

		public Builder(){
			project = new Project();
		}

		public Builder(Project project) throws FileNotFoundException {
			this.project = project;
			this.credentialInputStream = project.getInputStream();
		}

		public Builder setJsonFactory(JsonFactory jsonFactory){
			this.jsonFactory = jsonFactory;
			return this;
		}

		public Builder setProjectId(String projectId){
			project.setProjectId(projectId);
			return this;
		}

		public Builder setHttpTransport(HttpTransport httpTransport){
			this.httpTransport = httpTransport;
			return this;
		}

		public Builder setCredentialInputStream(InputStream credentialInputStream){
			this.credentialInputStream = credentialInputStream;
			return this;
		}

		public BucketManager build(){
			return new BucketManager(this);
		}


	}
}
