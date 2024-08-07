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
package com.foreach.common.spring.localization.text;

import com.foreach.common.spring.localization.AbstractLocalizedFieldsObject;
import com.foreach.common.spring.localization.Language;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocalizedText extends AbstractLocalizedFieldsObject<LocalizedTextFields> implements Comparable<LocalizedText>
{
	private String application, group, label;
	private boolean used, autoGenerated;
	private Date created, updated;

	public final String getGroup() {
		return group;
	}

	public final void setGroup( String group ) {
		this.group = group;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel( String label ) {
		this.label = label;
	}

	public final boolean isUsed() {
		return used;
	}

	public final void setUsed( boolean used ) {
		this.used = used;
	}

	public final String getApplication() {
		return application;
	}

	public final void setApplication( String application ) {
		this.application = application;
	}

	public final boolean isAutoGenerated() {
		return autoGenerated;
	}

	public final void setAutoGenerated( boolean autoGenerated ) {
		this.autoGenerated = autoGenerated;
	}

	public final Date getCreated() {
		return created;
	}

	public final void setCreated( Date created ) {
		this.created = created;
	}

	public final Date getUpdated() {
		return updated;
	}

	public final void setUpdated( Date updated ) {
		this.updated = updated;
	}

	/**
	 * Creates new LocalizedFields of the required specific implementation.  This does not add the fields to
	 * the collection for this entity.
	 *
	 * @param language Language for which to create fields.
	 * @return Specific LocalizedFields implementation.
	 */
	@Override
	public final LocalizedTextFields createFields( Language language ) {
		return new LocalizedTextFields( language );
	}

	@SuppressWarnings("all")
	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		LocalizedText that = (LocalizedText) o;

		if ( application != null ? !application.equals( that.application ) : that.application != null ) {
			return false;
		}
		if ( group != null ? !group.equals( that.group ) : that.group != null ) {
			return false;
		}
		if ( label != null ? !label.equals( that.label ) : that.label != null ) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("all")
	@Override
	public int hashCode() {
		int result = application != null ? application.hashCode() : 0;
		result = 31 * result + ( group != null ? group.hashCode() : 0 );
		result = 31 * result + ( label != null ? label.hashCode() : 0 );
		return result;
	}

	@Override
	public final String toString() {
		return "{" + application + "-" + group + ": " + label + "}";
	}

	public int compareTo( LocalizedText o ) {
		String thisValue = toString();
		String thatValue = o.toString();
		return thisValue.compareTo( thatValue );
	}

	public static LocalizedTextBuilder builder() {
		return new LocalizedTextBuilder();
	}

	public static class LocalizedTextBuilder
	{
		private String application, group, label;
		private Map<Language, String> localizedTextFieldMap = new HashMap<>();

		LocalizedTextBuilder() {
		}

		public LocalizedTextBuilder group( String group ) {
			this.group = group;
			return this;
		}

		public LocalizedTextBuilder application( String application ) {
			this.application = application;
			return this;
		}

		public LocalizedTextBuilder label( String label ) {
			this.label = label;
			return this;
		}

		public LocalizedTextBuilder field( Language language, String field ) {
			localizedTextFieldMap.put( language, field );
			return this;
		}

		public LocalizedText build() {
			LocalizedText localizedText = new LocalizedText();
			localizedText.setGroup( this.group );
			localizedText.setLabel( this.label );
			localizedText.setApplication( application );
			for ( Language l : localizedTextFieldMap.keySet() ) {
				LocalizedTextFields localizedTextFields = new LocalizedTextFields( l );
				localizedTextFields.setText( localizedTextFieldMap.get( l ) );
				localizedText.addFields( localizedTextFields );
			}

			return localizedText;
		}

	}
}
