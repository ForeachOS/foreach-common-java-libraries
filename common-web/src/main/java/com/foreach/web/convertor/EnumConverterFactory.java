package com.foreach.web.convertor;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;
import com.foreach.utils.IdLookup;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * EnumConverterFactory is an implementation of the Spring 3 ConverterFactory interface
 * that generates Converters from String to Enum classes implementing IdLookup or CodeLookup.
 * <p/>
 * The conversion is a two step process: first a String is converted to the parameter type of
 * IdLookup or CodeLookup, then that value is used to determine the enum with that id or code.
 * <p/>
 * Each EnumConverterFactory instance must be provided with a conversion service
 * that can convert String to all the parameter types that require conversion.
 * <p/>
 * When converting to an Enum class implementing both IdLookup and CodeLookup,
 * the IdLookup is attempted first.
 */

public class EnumConverterFactory implements ConverterFactory<String, Enum> {
    // Try to not get in an infinite loop here...

    private ConversionService conversionService;

    /**
     * Set the conversionService. This service must be able to convert String to all
     * the parameter types used.
     */
    public final void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Get a converter instance for the specified enum class.
     *
     * @param targetType the Enum class being converted to.
     * @return a converter implementing the Spring Converter interface
     *         that converts String to the specified enum class.
     */
    public final <E extends Enum> Converter<String, E> getConverter(Class<E> targetType) {
        return new EnumConverter(targetType);
    }

    private final class EnumConverter<E extends Enum> implements Converter<String, E> {

        private Class<E> enumType;

        public EnumConverter(Class<E> enumType) {
            this.enumType = enumType;
        }

        public E convert(String source) {

            if (IdLookup.class.isAssignableFrom(enumType)) {

                Class intermediateType = lookupMethodParameterClass(enumType, IdLookup.class);
                E attempt = tryConvertUsingMethod(source, intermediateType, "getById");

                if (attempt != null) {
                    return attempt;
                }
            }

            if (CodeLookup.class.isAssignableFrom(enumType)) {

                Class intermediateType = lookupMethodParameterClass(enumType, CodeLookup.class);

                E attempt = tryConvertUsingMethod(source, intermediateType, "getByCode");

                if (attempt != null) {
                    return attempt;
                }
            }

            return null;
        }

        /**
         * Find the type parameter of the argument class for the specified lookupInterface,
         * so for Foo implements LookUp<Bar>, return Bar.Class;
         */
        private Class lookupMethodParameterClass(Class targetClass, Class lookupInterface) {
            Type[] ts = targetClass.getGenericInterfaces();

            for (Type t : ts) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) t;
                    if (pt.getRawType().equals(lookupInterface)) {
                        return (Class) pt.getActualTypeArguments()[0];
                    }
                }
            }

            return null;
        }

        private E tryConvertUsingMethod(String source, Class intermediateType, String lookupMethodName) {
            try {

                Object id = source;

                if (!String.class.isAssignableFrom(intermediateType)) {
                    id = conversionService.convert(source, TypeDescriptor.valueOf(String.class),
                            TypeDescriptor.valueOf(intermediateType));
                }

                Method m = com.foreach.utils.EnumUtils.class.getMethod(lookupMethodName, Class.class, Object.class);

                return (E) m.invoke(EnumUtils.class, enumType, id);
            } catch (NoSuchMethodException nsme) {
            } catch (IllegalAccessException iae) {
            } catch (InvocationTargetException ite) {
            }
            return null;
        }
    }
}

