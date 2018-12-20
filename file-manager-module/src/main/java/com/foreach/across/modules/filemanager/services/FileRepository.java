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

import com.foreach.across.modules.filemanager.business.FileDescriptor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for a single file repository, allowing storing and getting of a single file.
 */
public interface FileRepository
{
	/**
	 * @return The unique id of the file repository.
	 */
	String getRepositoryId();

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 *
	 * @return FileDescriptor instance.
	 */
	FileDescriptor createFile();

	/**
	 * Moves a File into the repository, once the file is fully available in the
	 * repository an attempt will be made to delete the original file.
	 * <p/>
	 * No exception will be thrown if the delete fails as the new instance will be
	 * available in the repository already and this is considered the important part
	 * of the transaction.
	 *
	 * @param file File instance to move into the repository.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor moveInto( File file );

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param file File instance to save in the repository.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( File file );

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param inputStream InputStream of the file content.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( InputStream inputStream );

	/**
	 * Stores a file for a specified {@link FileDescriptor} in the repository.
	 * The repository defined in the file descriptor should match the {@link FileRepository} the method is executed for.
	 * <p/>
	 * If the file may not be replaced and a file exists, an exception will be thrown.
	 *
	 * @param target            FileDescriptor that should be used for the file.
	 * @param inputStream       InputStream of the file content.
	 * @param replaceExisting Whether an existing file at the same location should be replaced.
	 * @return FileDescriptor instance.
	 */
	void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting );

	/**
	 * Deletes a file from the repository.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if delete was successful, false if not (delete failed or file did not exist).
	 */
	boolean delete( FileDescriptor descriptor );

	/**
	 * Get an OutputStream that can be used to update the contents of the file.
	 * No matter the repository implementation, writes to the OutputStream should
	 * replace the existing contents.
	 * <p>
	 * Depending on the implementation, this can happen on close(), flush() or instantaneously.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return OutputStream that can be used to update the contents of the file.
	 */
	OutputStream getOutputStream( FileDescriptor descriptor );

	/**
	 * Get an InputStream to read the contents of the file.
	 * Reading the stream *should not be done more than once* to ensure compatibility
	 * across FileRepository implementations.  Once a stream has been consumed, a new
	 * InputStream should be requested of the repository.
	 * <p>
	 * If the file does not exist null will be returned.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return InputStream for the contents of the file.
	 */
	InputStream getInputStream( FileDescriptor descriptor );

	/**
	 * Gets the file contents as a File instance.  This file *should not be used for writing*
	 * as the file instance might be a local cached instance depending on the implementation.
	 * If you want to write directly you should use
	 * {@link #getOutputStream(FileDescriptor)}.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return File instance.
	 */
	File getAsFile( FileDescriptor descriptor );

	/**
	 * Checks if a descriptor actually points to an existing file.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if the file exists.
	 */
	boolean exists( FileDescriptor descriptor );

	/**
	 * Moves the file "original" into "renamed", which may not exist yet.
	 * </p>
	 * An IllegalArgumentException will be thrown if the two are not in the same repository.
	 *
	 * @param source FileDescriptor instance of the original file.
	 * @param target FileDescriptor instance of the wanted resulting file.
	 * @return True if the file was successfully moved.
	 */
	boolean move( FileDescriptor source, FileDescriptor target );
}
