package com.foreach.spring.enums;

/**
 * EnumUtils contains some utility routines to find specific enums if their classes
 * implement Idlookup or CodeLookup.
 */
public class EnumUtils
{
	protected EnumUtils()
	{
	}

	/**
	 * @param clazz an Enum class implementing IdLookup&lt;I&gt;
	 * @param id    an instance of type I
	 * @return the instance e of class clazz such that e.getId().equals( id )
	 */
	public static final <I, E extends Enum<E> & IdLookup<I>> E getById( Class<E> clazz, I id )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getId().equals( id ) ) {
				return e;
			}
		}
		return null;
	}

	/**
	 * @param clazz an Enum class implementing CodeLookup&lt;S&gt;
	 * @param code  an instance of type S
	 * @return the instance e of class clazz such that e.getCode().equals( code ), unless S is String,
	 * in which case equalsIgnoreCase is used instead of equals().
	 */
	public static final <S, E extends Enum<E> & CodeLookup<S>> E getByCode( Class<E> clazz, S code )
	{
		if( code instanceof String ) {
			return getByCaseInsensitiveString( clazz, code );
		}

		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getCode().equals( code ) ) {
				return e;
			}
		}
		return null;
	}

	private static <S, E extends Enum<E> & CodeLookup<S>> E getByCaseInsensitiveString( Class<E> clazz, S code )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( ((String) e.getCode()).equalsIgnoreCase( (String) code ) ) {
				return e;
			}
		}
		return null;
	}
}
