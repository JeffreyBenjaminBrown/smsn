package net.fortytwo.myotherbrain.notes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class NoteParserTest {
    private NoteParser syntax;

    @Before
    public void setUp() throws Exception {
        syntax = new NoteParser();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExample1() throws Exception {
        List<Note> notes = syntax.parse(NoteParser.class.getResourceAsStream("tinkernotes-example-1.txt")).getChildren();
        assertEquals(7, notes.size());

        Note indentation = notes.get(1);
        assertNull(indentation.getId());
        assertEquals("indentation", indentation.getValue());
        assertEquals("and this", indentation.getChildren()
                .get(2).getChildren()
                .get(0).getChildren()
                .get(0).getChildren()
                .get(0).getValue());

        Note atts = notes.get(3);
        assertEquals("http://example.org/ns/attributes", atts.getAlias());
        assertEquals(0.75f, atts.getWeight());

        Note ws = notes.get(4);
        assertEquals(5, ws.getChildren().size());
        assertEquals("newlines can be preserved with triple braces {{{\n" +
                "like this.\n" +
                "Use as many lines of text as you need.\n" +
                "}}}", ws.getChildren().get(2).getValue());
        assertEquals("leading and trailing whitespace are ignored", ws.getChildren().get(3).getValue());

        Note ids = notes.get(5);
        assertEquals("ids", ids.getValue());
        assertEquals("0txXBm", ids.getChildren().get(0).getId());
        assertEquals("cE85nD", ids.getChildren().get(1).getId());
    }

    @Test
    public void testExample2() throws Exception {
        Note root = syntax.parse(NoteParser.class.getResourceAsStream("tinkernotes-example-2.txt"));

        assertEquals("http://example.org/ns/top-level-attributes-are-allowed", root.getAlias());
        assertEquals(1.0f, root.getWeight());
        assertEquals(0.75f, root.getSharability());

        assertEquals(1, root.getChildren().size());
        assertEquals(1, root.getChildren().get(0).getChildren().size());
    }

    @Test(expected = NoteParser.NoteParsingException.class)
    public void testEmptyNotesNotAllowed() throws Exception {
        readNotes("* ");
    }

    @Test
    public void testEmptyAliasAttributeIsAllowed() throws Exception {
        readNotes("@alias ");
    }

    @Test(expected = NoteParser.NoteParsingException.class)
    public void testEmptyWeightAttributeNotAllowed() throws Exception {
        readNotes("@weight ");
    }

    @Test(expected = NoteParser.NoteParsingException.class)
    public void testEmptySharabilityAttributeNotAllowed() throws Exception {
        readNotes("@sharability ");
    }

    private List<Note> readNotes(final String s) throws IOException, NoteParser.NoteParsingException {
        InputStream in = new ByteArrayInputStream(s.getBytes());
        try {
            return syntax.parse(in).getChildren();
        } finally {
            in.close();
        }
    }
}