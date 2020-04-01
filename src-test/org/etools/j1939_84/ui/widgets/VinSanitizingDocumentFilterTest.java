/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.widgets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.etools.j1939_84.utils.VinDecoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for the {@link VinSanitizingDocumentFilter}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VinSanitizingDocumentFilterTest {

    @Mock
    private Document document;

    @Mock
    private DocumentFilter.FilterBypass filterBypass;

    private VinSanitizingDocumentFilter instance;

    @Mock
    private VinDecoder vinDecoder;

    @Before
    public void setUp() {
        instance = new VinSanitizingDocumentFilter(vinDecoder);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(document, filterBypass, vinDecoder);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testInsertString() throws BadLocationException {
        when(vinDecoder.sanitize("a")).thenReturn("A");
        when(document.getLength()).thenReturn(10);
        when(filterBypass.getDocument()).thenReturn(document);

        instance.insertString(filterBypass, 1, "a", null);

        verify(vinDecoder).sanitize("a");
        verify(filterBypass).insertString(1, "A", null);
        verify(filterBypass).getDocument();
        verify(document).getLength();
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testInsertStringTooLong() throws BadLocationException {
        when(vinDecoder.sanitize("a")).thenReturn("A");
        when(document.getLength()).thenReturn(17);
        when(filterBypass.getDocument()).thenReturn(document);

        instance.insertString(filterBypass, 1, "a", null);

        verify(vinDecoder).sanitize("a");
        verify(filterBypass).insertString(1, "", null);
        verify(filterBypass).getDocument();
        verify(document).getLength();
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testReplace() throws BadLocationException {
        when(vinDecoder.sanitize("a")).thenReturn("A");
        when(document.getLength()).thenReturn(10);
        when(filterBypass.getDocument()).thenReturn(document);

        instance.replace(filterBypass, 1, 2, "a", null);

        verify(vinDecoder).sanitize("a");
        verify(filterBypass).replace(1, 2, "A", null);
        verify(filterBypass).getDocument();
        verify(document).getLength();
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testReplaceTooLong() throws BadLocationException {
        when(vinDecoder.sanitize("a")).thenReturn("A");
        when(document.getLength()).thenReturn(17);
        when(filterBypass.getDocument()).thenReturn(document);

        instance.replace(filterBypass, 1, 2, "a", null);

        verify(vinDecoder).sanitize("a");
        verify(filterBypass).replace(1, 2, "", null);
        verify(filterBypass).getDocument();
        verify(document).getLength();
    }

}
