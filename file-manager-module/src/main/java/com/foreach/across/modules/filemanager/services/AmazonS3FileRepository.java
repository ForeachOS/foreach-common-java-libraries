/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.filemanager.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * FileRepository which stores its files in an Amazon S3 bucket.
 * Optionally takes a {@link PathGenerator} for generating a folder structure when uploading a non-named file.
 *
 * @author Sander Van Loock, Arne Vandamme
 * @since 1.4.0
 */
@Slf4j
public class AmazonS3FileRepository implements FileRepository, FileManagerAware
{
	private final String repositoryId;
	private final String bucketName;
	private final AmazonS3 amazonS3Client;
	private final PathGenerator pathGenerator;

	@Setter
	private FileManager fileManager;

	@Builder
	public AmazonS3FileRepository( @NonNull String repositoryId, @NonNull AmazonS3 amazonS3, @NonNull String bucketName, PathGenerator pathGenerator ) {
		this.amazonS3Client = amazonS3;
		this.repositoryId = repositoryId;
		this.bucketName = bucketName;
		this.pathGenerator = pathGenerator;
	}

	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	@Override
	public FileDescriptor createFile() {
		FileDescriptor descriptor = buildNewDescriptor( null );
		try {
			if ( descriptor.getFolderId() != null ) {
				createFolder( descriptor.getFolderId() );
			}
			//create empty file
			amazonS3Client.putObject( bucketName, buildAwsPath( descriptor ), "" );
			getAsFile( descriptor );
		}
		catch ( AmazonServiceException a ) {
			LOG.error( "Unable to create new file on Amazon", a );
			throw new FileStorageException( a );
		}

		return descriptor;
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		FileDescriptor descriptor = save( file );
		if ( !file.delete() ) {
			LOG.warn( "File {} was copied into the AmazonS3FileRepository but could not be deleted", file );
		}
		return descriptor;
	}

	@Override
	public FileDescriptor save( File file ) {
		FileDescriptor descriptor = buildNewDescriptor( file.getName() );
		try {
			if ( descriptor.getFolderId() != null ) {
				createFolder( descriptor.getFolderId() );
			}
			amazonS3Client.putObject( bucketName, buildAwsPath( descriptor ), file );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to save file on Amazon", e );
			throw new FileStorageException( e );
		}
		return descriptor;
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		FileDescriptor descriptor = buildNewDescriptor( null );
		save( descriptor, inputStream, true );
		return descriptor;
	}

	@Override
	public void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting ) {
		if ( !StringUtils.equals( repositoryId, target.getRepositoryId() ) ) {
			throw new IllegalArgumentException(
					"Invalid file descriptor. File repository " + target.getRepositoryId() +
							" can not persist a file for the provided descriptor: " + target.getUri() );
		}

		if ( !replaceExisting && exists( target ) ) {
			throw new IllegalArgumentException( "Unable to save file to the given descriptor: " + target.getUri() + ". File already exists." );
		}

		try {
			PutObjectRequest putObjectRequest =
					new PutObjectRequest( bucketName, buildAwsPath( target ), inputStream, new ObjectMetadata() );
			if ( target.getFolderId() != null ) {
				createFolder( target.getFolderId() );
			}
			amazonS3Client.putObject( putObjectRequest );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to save file on Amazon", e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		try {
			amazonS3Client.deleteObject( bucketName, buildAwsPath( descriptor ) );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to delete file on Amazon", e );
			return false;
		}
		return true;
	}

	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		return new S3OutputStream( this, descriptor );
	}

	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		try {
			S3Object object = amazonS3Client.getObject( bucketName, buildAwsPath( descriptor ) );
			return object.getObjectContent();
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to get file {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );
		try {
			File localFile = fileManager.createTempFile();
			FileUtils.forceMkdir( localFile.getParentFile() );
			amazonS3Client.getObject( new GetObjectRequest( bucketName, buildAwsPath( descriptor ) ), localFile );
			return localFile;
		}
		catch ( IOException e ) {
			throw new FileStorageException( e );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to get file {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		try {
			return amazonS3Client.doesObjectExist( bucketName, buildAwsPath( descriptor ) );
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to check file existance {} on Amazon", descriptor, e );
			throw new FileStorageException( e );
		}
	}

	@Override
	public boolean move( FileDescriptor source, FileDescriptor target ) {
		String renamedRep = target.getRepositoryId();
		String originalRep = source.getRepositoryId();
		if ( !StringUtils.equals( originalRep, renamedRep ) ) {
			throw new IllegalArgumentException( "Repository id of the target is different from the source." );
		}
		try {
			if ( target.getFolderId() != null ) {
				createFolder( target.getFolderId() );
			}
			amazonS3Client.copyObject( bucketName, buildAwsPath( source ), bucketName, buildAwsPath( target ) );
			delete( source );
			return true;
		}
		catch ( AmazonServiceException e ) {
			LOG.error( "Unable to move {} to {} on Amazon", source, target, e );
			return false;
		}
	}

	private FileDescriptor buildNewDescriptor( String proposedName ) {
		String extension = FilenameUtils.getExtension( proposedName );
		String fileName = UUID.randomUUID() + ( !StringUtils.isBlank( extension ) ? "." + extension : "" );

		String path = pathGenerator != null ? pathGenerator.generatePath() : null;

		return FileDescriptor.of( repositoryId, path, fileName );
	}

	private void assertValidDescriptor( FileDescriptor descriptor ) {
		if ( !StringUtils.equals( getRepositoryId(), descriptor.getRepositoryId() ) ) {
			throw new FileStorageException( String.format(
					"Attempt to use a FileDescriptor of repository %s on repository %s", descriptor.getRepositoryId(),
					getRepositoryId() ) );
		}
	}

	private String buildAwsPath( FileDescriptor descriptor ) {
		String result;
		if ( descriptor.getFolderId() != null ) {
			result = Paths.get( descriptor.getFolderId(), descriptor.getFileId() ).toString();
		}
		else {
			result = Paths.get( descriptor.getFileId() ).toString();
		}

		return result.replace( "\\", "/" );
	}

	//found on https://stackoverflow.com/questions/11491304/amazon-web-services-aws-s3-java-create-a-sub-directory-object
	private void createFolder( String folderName ) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength( 0 );

		// create empty content
		InputStream emptyContent = new ByteArrayInputStream( new byte[0] );

		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, folderName + "/", emptyContent, metadata );

		// send request to S3 to create folder
		amazonS3Client.putObject( putObjectRequest );
	}
}
