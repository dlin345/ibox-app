package edu.csupomona.cs585.ibox;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveServiceProvider;

public class GoogleDriveFileSyncManagerIntegrationTest {

	private GoogleDriveFileSyncManager fileSyncManager;
	private java.io.File localFile;
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws IOException {
		fileSyncManager = new GoogleDriveFileSyncManager(
        		GoogleDriveServiceProvider.get().getGoogleDriveClient());
		
		localFile = tempFolder.newFile();
		new WatchDir(localFile.getParentFile().toPath(), fileSyncManager);		
	}
	
	@After
	public void tearDown() throws IOException, FileNotFoundException {
		try {
			fileSyncManager.deleteFile(localFile);
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}
	}
	
//	@Test
	public void implIntegAddFile_NewFile_ShouldPass() throws IOException {
		System.out.println("\nRunning implIntegAddFile_NewFile_ShouldPass...");
		String googleDriveFileName = null;
		
		fileSyncManager.addFile(localFile);
		
		// get first 100 files from Google Drive
	    Files.List request = (fileSyncManager.service).files().list();
	    FileList files = request.execute();
	    
        // check if added file is in list
        for(File file : files.getItems()) {
			if (file.getTitle().equals(localFile.getName())) {
				googleDriveFileName = file.getTitle();
			}
		}    
		Assert.assertEquals(localFile.getName(), googleDriveFileName);
	}
	
//	@Test
	public void implIntegGetFileId_ExistentFile_ShouldPass() {
		System.out.println("\nRunning implIntegGetFileId_ExistentFile_ShouldPass...");
		String localFileID = null;
		String googleFileID = null;
		
		try {
			// set up existing file in Google Drive
			fileSyncManager.addFile(localFile);
			
			localFileID = fileSyncManager.getFileId(localFile.getName());
			
			// get first 100 files from Google Drive
		    Files.List request = (fileSyncManager.service).files().list();
		    FileList files = request.execute();
	        
	        // check if added file ID is in list
	        for(File file : files.getItems()) {
				if (file.getId().equals(localFileID)) {
					googleFileID = file.getId();
				}
			}
		} catch (IOException e) {

		}
		Assert.assertEquals(localFileID, googleFileID);
	}

//	@Test
	public void implIntegGetFileId_NonexistentFile_ShouldReturnNull() {
		System.out.println("\nRunning implIntegGetFileId_NonexistentFile_ShouldReturnNull...");
		String localFileName = localFile.getName();
		String googleFileID = null;
		
		String localFileID = fileSyncManager.getFileId(localFileName);;
		
		try {			
			// get first 100 files from Google Drive
		    Files.List request = (fileSyncManager.service).files().list();
		    FileList files = request.execute();
	        
	        // check if file ID is in list, expect file ID not in list
	        for(File file : files.getItems()) {
				if (file.getId().equals(localFileID)) {
					googleFileID = file.getId();
				}
			}
		} catch (IOException e) {

		}
		Assert.assertNull(localFileID);
		Assert.assertEquals(localFileID, googleFileID);
	}
	
//	@Test
	public void implIntegUpdateFile_NewFile_ShouldPass() throws IOException {
		System.out.println("\nRunning implIntegUpdateFile_NewFile_ShouldPass...");
		String googleDriveFileName = null;
		
		fileSyncManager.updateFile(localFile);
		
		// get first 100 files from Google Drive
	    Files.List request = (fileSyncManager.service).files().list();
	    FileList files = request.execute();
        
        // check if updated file is in list
        for(File file : files.getItems()) {
			if (file.getTitle().equals(localFile.getName())) {
				googleDriveFileName = file.getTitle();
			}
		}
		Assert.assertEquals(localFile.getName(), googleDriveFileName);
	}
	
//	@Test
	public void implIntegUpdateFile_ExistentFile_ShouldPass() throws IOException {
		System.out.println("\nRunning implIntegUpdateFile_ExistentFile_ShouldPass...");
		String googleDriveFileName = null;
		
		// set up existing file in Google Drive
		fileSyncManager.addFile(localFile);
		
		fileSyncManager.updateFile(localFile);
		
		// get first 100 files from Google Drive
	    Files.List request = (fileSyncManager.service).files().list();
	    FileList files = request.execute();
        
        // check if updated existing file is in list
        for(File file : files.getItems()) {
			if (file.getTitle().equals(localFile.getName())) {
				googleDriveFileName = file.getTitle();
			}
		}
		Assert.assertEquals(localFile.getName(), googleDriveFileName);
	}
	
//	@Test
	public void implIntegDeleteFile_ExistentFile_ShouldPass() throws IOException {
		System.out.println("\nRunning implIntegDeleteFile_ExistentFile_ShouldPass...");
		String googleDriveFileName = null;
		
		// set up existing file in Google Drive
		fileSyncManager.addFile(localFile);
		
		fileSyncManager.deleteFile(localFile);
		
		// get first 100 files from Google Drive
	    Files.List request = (fileSyncManager.service).files().list();
	    FileList files = request.execute();
        
        // check if deleted file is in list, expect file not in list
        for(File file : files.getItems()) {
			if (file.getTitle().equals(localFile.getName())) {
				googleDriveFileName = file.getTitle();
			}
		}
        Assert.assertNull(googleDriveFileName);
	}
	
//	@Test
	public void implIntegDeleteFile_NonexistentFile_ShouldThrowException() throws IOException {
		System.out.println("\nRunning implIntegDeleteFile_NonexistentFile_ShouldThrowException...");
		try {
			fileSyncManager.deleteFile(localFile);
			Assert.fail("Expected to throw FileNotFoundException");
		} catch (FileNotFoundException e) {
			
		}
	}
	
}
