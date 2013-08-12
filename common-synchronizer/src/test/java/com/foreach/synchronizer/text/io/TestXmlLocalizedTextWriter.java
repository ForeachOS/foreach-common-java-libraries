package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class TestXmlLocalizedTextWriter {

    private String expectedOutput = "<list>\n" +
            "  <text>\n" +
            "    <fields>\n" +
            "      <language>EN</language>\n" +
            "      <text>This is a test!</text>\n" +
            "    </fields>\n" +
            "    <fields>\n" +
            "      <language>NL</language>\n" +
            "      <text>Dit is een test!</text>\n" +
            "    </fields>\n" +
            "    <application>myApp</application>\n" +
            "    <group>myGroup</group>\n" +
            "    <label>my.label</label>\n" +
            "    <used>false</used>\n" +
            "    <autoGenerated>false</autoGenerated>\n" +
            "    <created>2012-01-31 23:00:00.0 UTC</created>\n" +
            "  </text>\n" +
            "</list>";

    @Before
    public void setup() {
        new LanguageConfigurator(TestLanguage.class);
    }

    @Test
    public void testWrite() throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlLocalizedTextWriter xmlLocalizedTextWriter = new XmlLocalizedTextWriter(outputStream);

        LocalizedText text1 = new LocalizedText();
        text1.setApplication("myApp");
        text1.setGroup("myGroup");
        text1.setAutoGenerated(false);
        text1.setCreated(DateUtils.parseDate("2012-02-01", "yyyy-MM-dd"));
        text1.setLabel("my.label");

        LocalizedTextFields fieldsNl = createFields("Dit is een test!", TestLanguage.NL);
        LocalizedTextFields fieldsEn = createFields("This is a test!", TestLanguage.EN);

        text1.setFieldsAsCollection(Arrays.asList(fieldsNl, fieldsEn));

        Collection<LocalizedText> localizedTexts = new ArrayList<LocalizedText>();
        localizedTexts.add(text1);

        xmlLocalizedTextWriter.write(localizedTexts);

        String actualOutput = new String(outputStream.toByteArray());

        assertEquals(expectedOutput, actualOutput);
    }

    private LocalizedTextFields createFields(String text, Language language) {
        LocalizedTextFields result = new LocalizedTextFields(language);
        result.setText(text);
        return result;
    }
}
