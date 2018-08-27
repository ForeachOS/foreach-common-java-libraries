package com.foreach.across.samples.filemanager.application.web;

import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steven Gentens
 */
@Controller
@RequiredArgsConstructor
public class FileUploadController
{

	private final FileReferenceService fileReferenceService;
	private final FileReferenceRepository fileReferenceRepository;

	@GetMapping("/upload")
	public String renderForm() {
		return "th/fileManagerTest/file-upload";
	}

	@PostMapping("/uploadFile")
	public String processFile( @RequestParam(name = "file") MultipartFile file ) {
		FileReference reference = fileReferenceService.save( file );
		FileReference saved = fileReferenceRepository.findOne( reference.getId() );
		return "redirect:/upload";
	}
}
