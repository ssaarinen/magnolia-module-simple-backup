package org.sevensource.magnolia.backup.descriptor;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class SimpleBackupJobFileDescriptor {
	
	private static final String LOGFILE			= "simplebackup.log";
	private static final String JOBFILE			= "simplebackup.job.xml";
	
	private final BackupJobDescriptor descriptor;
	
	public SimpleBackupJobFileDescriptor() {
		this(new BackupJobDescriptor());
	}
	
	public SimpleBackupJobFileDescriptor(BackupJobDescriptor backupJobDescriptor) {
		this.descriptor = backupJobDescriptor;
	}
	
	public void addWorkspace(String workspace) {
		descriptor.addWorkspace(workspace);
	}
	
	public void addWorkspaceItem(String workspace, String nodePath, Path backupItemFile ) {
		
		final WorkspaceItemDescriptor wsid = new WorkspaceItemDescriptor(backupItemFile, nodePath);
		
		if(descriptor.getWorkspaceDescriptor(workspace) == null) {
			addWorkspace(workspace);
		}
		
		descriptor.getWorkspaceDescriptor(workspace).addWorkspaceItem(wsid);
	}
	
	public List<WorkspaceDescriptor> getWorkspaces() {
		return descriptor.getWorkspaces();
	}
	
	public void log(String message, Path basePath) {
		writeToFile(message, basePath.resolve(LOGFILE));
	}
	
	public void serialize(Path basePath) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		serialize(os);
		writeToFile(new String(os.toByteArray(), StandardCharsets.UTF_8), basePath.resolve(JOBFILE));
	}
	
	public void serialize(OutputStream os) {
		try {
		    final JAXBContext contextObj = JAXBContext.newInstance(BackupJobDescriptor.class);
		    final Marshaller marshallerObj = contextObj.createMarshaller();  
		    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    marshallerObj.marshal(descriptor, os);
		} catch(JAXBException jaxex) {
			throw new RuntimeException("Cannot serialize to XML", jaxex);
		}
	}
	
	public static SimpleBackupJobFileDescriptor deserialize(InputStream is) {
		try {
			final JAXBContext contextObj = JAXBContext.newInstance(BackupJobDescriptor.class);
			final Unmarshaller unmarshaller = contextObj.createUnmarshaller();
			final BackupJobDescriptor o = (BackupJobDescriptor) unmarshaller.unmarshal(is);
			return new SimpleBackupJobFileDescriptor(o);			
		} catch(JAXBException e) {
			throw new IllegalArgumentException("Cannot parse job descriptor XML", e);
		}

	}
	
	public static SimpleBackupJobFileDescriptor deserialize(Path basePath) {
		final Path jobFile = basePath.resolve(JOBFILE);
		
		try (final FileInputStream fis = new FileInputStream(jobFile.toFile());) {
			return deserialize(fis);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot read from file", e);
		} catch (IOException e) {
			throw new RuntimeException("IOException", e);
		}
	}
	
	private static void writeToFile(String message, Path file) {
		
		try (final FileOutputStream fos = new FileOutputStream(file.toFile(), true);
				final Writer oswriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				final BufferedWriter out = new BufferedWriter(oswriter);) {
			
			out.write(message);
			out.newLine();
			out.flush();
			oswriter.flush();
			fos.flush();
			
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot write to file", e);
		} catch (IOException e) {
			throw new RuntimeException("IOException", e);
		}
	}
}
