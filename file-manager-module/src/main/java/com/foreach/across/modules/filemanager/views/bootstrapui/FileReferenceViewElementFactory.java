package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactory;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnBootstrapUI
@Component
public class FileReferenceViewElementFactory implements EntityViewElementBuilderFactory
{
	public final static String FILE_REFERENCE_CONTROL = FileReferenceViewElementFactory.class.getName() + ".fileReferenceControl";

	@Override
	public boolean supports( String viewElementType ) {
		return StringUtils.equals( FILE_REFERENCE_CONTROL, viewElementType );
	}

	@Override
	public ViewElementBuilder createBuilder( EntityPropertyDescriptor entityPropertyDescriptor, ViewElementMode viewElementMode, String s ) {
		return new FileReferenceViewElementBuilder();
	}
}
