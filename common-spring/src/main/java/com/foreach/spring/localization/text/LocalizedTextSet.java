package com.foreach.spring.localization.text;

import com.foreach.spring.localization.Language;

import java.util.Collection;

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
public interface LocalizedTextSet
{
	/**
	 * @return The application this set belongs to, new items in this set will be created in this application.
	 */
	String getApplication();

	/**
	 * @return The group this set represents, new items in this set will be created in this group.
	 */
	String getGroup();

	/**
	 * @return All items in this set.
	 */
	Collection<LocalizedText> getItems();

	/**
	 * Returns the value for a specific language of a text item.  If the text item is found and it is the first time
	 * it has been requested (based on the Used property of {@link LocalizedText}), this method will trigger a
	 * flagAsUsed call on the {@link LocalizedTextService} provided.
	 *
	 * @param label    Label of the text item.
	 * @param language Language for which we want the value.
	 * @return Value as a string.
	 */
	String getText( String label, Language language );

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
	String getText( String label, Language language, String defaultValue );

	/**
	 * @param label Label of the item to search for.
	 * @return True if the item exists in this set, false if not.
	 */
	boolean exists( String label );

	/**
	 * @return Number of items (different label) in the set.
	 */
	int size();

	/**
	 * Reloads all items in this set from the backing data store.
	 */
	void reload();
}