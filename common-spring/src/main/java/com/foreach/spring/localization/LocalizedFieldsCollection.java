package com.foreach.spring.localization;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection wrapper around a set of LocalizedFields.  The collection interfaces allows iteration,
 * removing and adding items.
 *
 * @param <Base> Implementation of LocalizedFields.
 */
public final class LocalizedFieldsCollection<Base extends LocalizedFields> implements Collection<Base>
{
	private final Map<String, Base> fieldsMap;

	LocalizedFieldsCollection( Map<String, Base> fieldsMap ) {
		this.fieldsMap = fieldsMap;
	}

	public int size() {
		return fieldsMap.size();
	}

	public boolean isEmpty() {
		return fieldsMap.isEmpty();
	}

	public boolean contains( Object o ) {
		return fieldsMap.containsValue( o );
	}

	public Iterator<Base> iterator() {
		return fieldsMap.values().iterator();
	}

	public Object[] toArray() {
		return fieldsMap.values().toArray();
	}

	public <T> T[] toArray( T[] a ) {
		return fieldsMap.values().toArray( a );
	}

	public boolean add( Base base ) {
		if ( base.getLanguage() != null ) {
			fieldsMap.put( base.getLanguage().getCode(), base );
			return true;
		}
		return false;
	}

	public boolean remove( Object o ) {
		boolean found = false;
		String languageCode = null;

		for ( Map.Entry<String, Base> entry : fieldsMap.entrySet() ) {
			if ( entry.getValue().equals( o ) ) {
				languageCode = entry.getKey();
				found = true;
			}
		}

		Base base = found ? fieldsMap.remove( languageCode ) : null;

		return base != null;
	}

	public boolean containsAll( Collection<?> c ) {
		return fieldsMap.entrySet().containsAll( c );
	}

	public boolean addAll( Collection<? extends Base> c ) {
		for ( Base b : c ) {
			add( b );
		}

		return true;
	}

	public boolean removeAll( Collection<?> c ) {
		boolean success = true;

		for ( Object o : c ) {
			if ( !remove( o ) ) {
				success = false;
			}
		}

		return success;
	}

	public boolean retainAll( Collection<?> c ) {
		return fieldsMap.entrySet().retainAll( c );
	}

	public void clear() {
		fieldsMap.clear();
	}
}
