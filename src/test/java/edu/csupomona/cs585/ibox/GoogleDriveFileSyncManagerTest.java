package edu.csupomona.cs585.ibox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.*;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;

public class GoogleDriveFileSyncManagerTest {
	
	private GoogleDriveFileSyncManager googleDriveFileSyncManager;
	private java.io.File localFile;
	private File googleFile;
	
	private String googleFileID;
	
	private Drive service;
	private Files files;
	private Drive.Files.Insert insert;
	private List request;
	private Drive.Files.Update update;
	private Drive.Files.Delete delete;
	private java.util.List<File> googleFileList;
	private FileList fileList;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws IOException {
		googleFileID = "Google-File-ID";
		
		service = mock(Drive.class);
		files = mock(Files.class);
		insert = mock(Drive.Files.Insert.class);
		request = mock(List.class);
		update = mock(Drive.Files.Update.class);
		delete = mock(Drive.Files.Delete.class);
		
		googleDriveFileSyncManager = new GoogleDriveFileSyncManager(service);
		localFile = tempFolder.newFile("Local-File.txt");
		
		googleFile = new File();
		googleFileList = new ArrayList<File>();
		googleFileList.add(googleFile);
		
		googleFile.setId(googleFileID);
		googleFile.setTitle(localFile.getName());
		
		fileList = new FileList();
		fileList.setItems(googleFileList);
	}
	
	@Test
	public void testAddFile_ValidFile_ShouldPass() throws IOException {
		when(service.files()).thenReturn(files);
		when(files.insert(any(File.class), any(FileContent.class))).thenReturn(insert);
		when(insert.execute()).thenReturn(googleFile);
		
		googleDriveFileSyncManager.addFile(localFile);
		
		Assert.assertEquals(googleFileID, googleFile.getId());
		verify(files).insert(any(File.class), any(FileContent.class));
		verify(insert).execute();		
	}
	
	@Test
	public void testGetFileId_ExistentFile_ShouldPass() throws IOException {
		String fileName = localFile.getName();

		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		Assert.assertEquals(googleFileID, googleDriveFileSyncManager.getFileId(fileName));
	}
	
	@Test
	public void testGetFileId_NonexistentFile_ShouldReturnNull() throws IOException {
		String fileName = "Non-existent-File-Name.txt";

		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		Assert.assertNull(googleDriveFileSyncManager.getFileId(fileName));
	}
	
	@Test
	public void testGetFileId_NullFile_ShouldReturnNull() throws IOException {
		String fileName = null;	

		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		Assert.assertNull(googleDriveFileSyncManager.getFileId(fileName));
	}
	
	@Test
	public void testUpdateFile_ExistentFile_ShouldPass() throws IOException {		
		// for getID()
		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		// for updateFile()
		when(service.files()).thenReturn(files);
		when(files.update(any(String.class), any(File.class), any(FileContent.class))).thenReturn(update);
		when(update.execute()).thenReturn(googleFile);
		
		googleDriveFileSyncManager.updateFile(localFile);
		
		Assert.assertEquals(googleFileID, googleFile.getId());
		verify(update).execute();
	}
	
	@Test
	public void testUpdateFile_NewFile_ShouldPass() throws IOException {
		java.io.File newLocalFile = tempFolder.newFile("New-Local-File.txt");
		
		// for getID()
		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		// for addFile()
		when(service.files()).thenReturn(files);
		when(files.insert(any(File.class), any(FileContent.class))).thenReturn(insert);
		when(insert.execute()).thenReturn(googleFile);
		
		googleDriveFileSyncManager.updateFile(newLocalFile);
		
		Assert.assertEquals(googleFileID, googleFile.getId());
		verify(request).execute();
	}
	
	@Test
	public void testDeleteFile_ExistentFile_ShouldPass() throws IOException {
		// for getID()
		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		// for deleteFile()
		when(service.files()).thenReturn(files);
		when(files.delete(any(String.class))).thenReturn(delete);
		when(delete.execute()).thenReturn(null);
		
		googleDriveFileSyncManager.deleteFile(localFile);
		verify(delete).execute();
	}
	
	@Test
	public void testDeleteFile_NonexistentFile_ShouldThrowException() throws IOException {
		java.io.File newLocalFile2 = tempFolder.newFile("New-Local-File.txt");
		
		// for getID()
		when(service.files()).thenReturn(files);
		when(files.list()).thenReturn(request);
		when(request.execute()).thenReturn(fileList);
		
		try {
			googleDriveFileSyncManager.deleteFile(newLocalFile2);
			Assert.fail("Expected to throw FileNotFoundException.");
		} catch (FileNotFoundException e) {
			
		}
	}

}