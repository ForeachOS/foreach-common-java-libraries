package com.foreach.spring.localization.text;

import com.foreach.spring.localization.Language;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>LocalizedTextSet is a collection of LocalizedText instances scoped to a particular group.
 * It is a special structure to be used for:
 * <ul>
 * <li>looking up text in the collection</li>
 * <li>creating fields with default values if they do not yet exist</li>
 * <li>setting a field as autogenerated upon creation or used upon first call</li>
 * </ul>
 * </p>
 * <p>A LocalizedTextSet is hooked to a LocalizedTextService that should provide the actual implementation of
 * the LocalizedText backend.  Things like creating default values, saving in database, etc.</p>
 */
public class LocalizedTextSetImpl implements LocalizedTextSet
{
	private final String application, group;
	private final LocalizedTextService localizedTextService;
	private Map<String, LocalizedText> textMap = new HashMap<String, LocalizedText>();

	/**
	 * <p>Constructs a new LocalizedTextSet based on a collection of items.  The collection may contain duplicates
	 * but in case of multiples the item with the highest index in the collection will be retained.</p>
	 *
	 * @param application          Name of the application that new items in this set should be created in.
	 * @param group                Name of the group that new items in this set should be created in.
	 * @param localizedTextService Service owning this set, where callbacks will occur based on set modifications.
	 */
	protected LocalizedTextSetImpl( String application, String group, LocalizedTextService localizedTextService ) {
		this.application = application;
		this.group = group;
		this.localizedTextService = localizedTextService;

		reload();
	}

	/**
	 * @return The application this set belongs to, new items in this set will be created in this application.
	 */
	public final String getApplication() {
		return application;
	}

	/**
	 * @return The group this set represents, new items in this set will be created in this group.
	 */
	public final String getGroup() {
		return group;
	}

	/**
	 * @return The LocalizedTextService this set is linked to.
	 */
	final LocalizedTextService getLocalizedTextService() {
		return localizedTextService;
	}

	/**
	 * @return All items in this set.
	 */
	public final Collection<LocalizedText> getItems() {
		return textMap.values();
	}

	/**
	 * Returns the value for a specific language of a text item.  If the text item is found and it is the first time
	 * it has been requested (based on the Used property of {@link LocalizedText}), this method will trigger a
	 * flagAsUsed call on the {@link LocalizedTextService} provided.
	 *
	 * @param label    Label of the text item.
	 * @param language Language for which we want the value.
	 * @return Value as a string.
	 */
	public final String getText( String label, Language language ) {
		return getText( label, language, null );
	}

	/**
	 * Returns the value for a specific language of a text item.  If the text item is found and it is the first time
	 * it has been requested (based on the Used property of {@link LocalizedText}), this method will trigger a
	 * flagAsUsed call on the {@link LocalizedTextService} provided.
	 *
	 * @param label        Label of the text item.
	 * @param language     Language for which we want the value.
	 * @param defaultValue Value to return in case the text item does not yet exist.
	 * @return Value as a string.
	 */
	public final String getText( String label, Language language, String defaultValue ) {
		LocalizedText text = textMap.get( label );

		// If the requested item doesn't exist, assume it should and create it
		if ( text == null ) {
			text = localizedTextService.saveDefaultText( application, group, label, defaultValue );
			textMap.put( text.getLabel(), text );
		}

		// Upon first use, flag the text as being used
		if ( !text.isUsed() ) {
			text.setUsed( true );
			localizedTextService.flagAsUsed( text );
		}

		return text.getFieldsForLanguage( language ).getText();
	}

	/**
	 * @param label Label of the item to search for.
	 * @return True if the item exists in this set, false if not.
	 */
	public final boolean exists( String label ) {
		return textMap.containsKey( label );
	}

	/**
	 * @return Number of items (different label) in the set.
	 */
	public final int size() {
		return textMap.size();
	}

	/**
	 * Reloads all items in this set from the backing data store.
	 */

	public final void reload() {
		HashMap<String, LocalizedText> updatedMap = new HashMap<String, LocalizedText>();
		List<LocalizedText> items = localizedTextService.getLocalizedTextItems( application, group );

		for ( LocalizedText text : items ) {
			updatedMap.put( text.getLabel(), text );
		}

		textMap = updatedMap;
	}
}
