package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestLocalFileRepository
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	private static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
	private static final String ROOT_DIR = Paths.get( TEMP_DIR, UUID.randomUUID().toString() ).toString();

	private static final File FILE_ONE = new File( TEMP_DIR, UUID.randomUUID().toString() );

	private LocalFileRepository fileRepository;

	@Before
	public void create() throws IOException {
		cleanup();

		assertFalse( FILE_ONE.exists() );

		try (FileWriter w = new FileWriter( FILE_ONE )) {
			IOUtils.copy( RES_TEXTFILE.getInputStream(), w );
			w.flush();
		}

		fileRepository = new LocalFileRepository( "default", ROOT_DIR );
	}

	@After
	public void cleanup() {
		try {
			if ( FILE_ONE.exists() ) {
				FILE_ONE.delete();
			}

			FileUtils.deleteDirectory( new File( ROOT_DIR ) );
		}
		catch ( Exception e ) {
			System.err.println( "Unit test could not cleanup files nicely" );
		}
	}

	@Test
	public void moveIntoDeletesTheOriginalFile() {
		assertTrue( FILE_ONE.exists() );

		FileDescriptor descriptor = fileRepository.moveInto( FILE_ONE );
		assertNotNull( descriptor );
		assertTrue( fileRepository.exists( descriptor ) );

		assertFalse( FILE_ONE.exists() );

		assertTrue( fileRepository.delete( descriptor ) );
		assertFalse( fileRepository.exists( descriptor ) );
	}

	@Test
	public void savingFileAlwaysCreatesANewFile() {
		FileDescriptor descriptorOne = fileRepository.save( FILE_ONE );
		FileDescriptor descriptorTwo = fileRepository.save( FILE_ONE );

		assertNotEquals( descriptorOne, descriptorTwo );

		File one = fileRepository.getAsFile( descriptorOne );
		File two = fileRepository.getAsFile( descriptorTwo );

		assertNotEquals( one, two );
		assertTrue( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		fileRepository.delete( descriptorOne );
		assertFalse( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		assertFalse( one.exists() );
		assertTrue( two.exists() );
	}

	@Test
	public void savingInputStreamAlwaysCreatesANewFile() throws IOException {
		FileDescriptor descriptorOne = fileRepository.save( RES_TEXTFILE.getInputStream() );
		FileDescriptor descriptorTwo = fileRepository.save( RES_TEXTFILE.getInputStream() );

		assertNotEquals( descriptorOne, descriptorTwo );

		File one = fileRepository.getAsFile( descriptorOne );
		File two = fileRepository.getAsFile( descriptorTwo );

		assertNotEquals( one, two );
		assertTrue( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		fileRepository.delete( descriptorOne );
		assertFalse( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		assertFalse( one.exists() );
		assertTrue( two.exists() );
	}

	@Test
	public void readInputStream() throws IOException {
		FileDescriptor descriptor = fileRepository.save( RES_TEXTFILE.getInputStream() );

		assertEquals( "some dummy text", read( descriptor ) );
	}

	@Test
	public void writeToNewFile() throws IOException {
		FileDescriptor descriptor = fileRepository.createFile();

		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			assertNotNull( is );
		}

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "original text" );
				pw.flush();
			}
			os.flush();
		}

		assertEquals( "original text", read( descriptor ) );
	}

	@Test
	public void modifyExistingFile() throws IOException {
		FileDescriptor descriptor = fileRepository.save( FILE_ONE );

		assertEquals( "some dummy text", read( descriptor ) );

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "modified text" );
				pw.flush();
			}
			os.flush();
		}

		assertEquals( "modified text", read( descriptor ) );
	}

	private String read( FileDescriptor descriptor ) throws IOException {
		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			char[] text = new char[1024];
			IOUtils.read( new InputStreamReader( is ), text );

			return new String( text ).trim();
		}
	}
}
