package net.fortytwo.smsn.brain.rdf.classes;

import net.fortytwo.smsn.brain.model.Atom;
import net.fortytwo.smsn.brain.rdf.AtomClass;
import net.fortytwo.smsn.brain.rdf.AtomCollection;
import net.fortytwo.smsn.brain.rdf.AtomRegex;
import net.fortytwo.smsn.brain.rdf.RDFizationContext;
import net.fortytwo.smsn.brain.rdf.classes.collections.DocumentAboutTopicCollection;
import net.fortytwo.smsn.brain.rdf.classes.collections.DocumentCollection;
import net.fortytwo.smsn.brain.rdf.classes.collections.GenericCollection;
import net.fortytwo.smsn.brain.rdf.classes.collections.PersonCollection;
import net.fortytwo.smsn.brain.rdf.classes.collections.QuotedValueCollection;
import net.fortytwo.smsn.rdf.vocab.DBpediaOntology;
import net.fortytwo.smsn.rdf.vocab.FOAF;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Person extends AtomClass {

    public Person() {
        super(
                "person",
                // note: value regex currently excludes names which begin with special characters
                // (e.g. Chinese or certain European names)
                Pattern.compile("[A-Z].{1,49}"),
                null,
                new AtomRegex(Arrays.asList(
                        new AtomRegex.El(new NickHandler(),
                                AtomRegex.Modifier.ZeroOrOne, AKAReference.class),
                        new AtomRegex.El(new PageHandler(),
                                AtomRegex.Modifier.ZeroOrMore, WebPage.class),

                        new AtomRegex.El(new DocumentsAboutTopicHandler(),
                                AtomRegex.Modifier.ZeroOrOne, DocumentAboutTopicCollection.class),

                        new AtomRegex.El(new MadeHandler(),
                                AtomRegex.Modifier.ZeroOrOne, WorksCollection.class),
                        new AtomRegex.El(new QuotationHandler(),
                                AtomRegex.Modifier.ZeroOrOne, QuotedValueCollection.class),
                        new AtomRegex.El(new InterestHandler(),
                                AtomRegex.Modifier.ZeroOrOne, InterestsCollection.class),
                        new AtomRegex.El(new KnowsHandler(),
                                AtomRegex.Modifier.ZeroOrMore, SocialNetworkCollection.class),
                        new AtomRegex.El(new BirthdayHandler(),
                                AtomRegex.Modifier.ZeroOrOne, DatedEvent.Birthday.class),
                        new AtomRegex.El(new AttendedEventsHandler(),
                                AtomRegex.Modifier.ZeroOrOne, PersonalEventsCollection.class),
                        new AtomRegex.El(null,
                                AtomRegex.Modifier.ZeroOrOne, PersonalStuffCollection.class),
                        // TODO: when the person passed away
                        // TODO: the person's contact information
                        // TODO: things mentioned by the person
                        new AtomRegex.El(null,
                                AtomRegex.Modifier.ZeroOrMore)
                )));
    }

    @Override
    protected boolean isCollectionClass() {
        return false;
    }

    @Override
    public IRI toRDF(Atom a, RDFizationContext context) throws RDFHandlerException {
        ValueFactory vf = context.getValueFactory();
        RDFHandler handler = context.getHandler();

        IRI self = handleTypeAndAlias(a, context, FOAF.PERSON);
        handler.handleStatement(vf.createStatement(self, FOAF.NAME, vf.createLiteral(a.getValue())));

        return self;
    }

    private static class MadeHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            ValueFactory vf = context.getValueFactory();
            IRI objectIRI = context.iriOf(object);
            context.getHandler().handleStatement(vf.createStatement(
                    // note: dc:creator is recommended only for simple textual names
                    objectIRI, FOAF.MAKER, context.getSubjectIri()));
        }
    }

    private static class QuotationHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            // TODO
        }
    }

    private static class InterestHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            ValueFactory vf = context.getValueFactory();
            IRI objectIRI = context.iriOf(object);
            context.getHandler().handleStatement(vf.createStatement(
                    context.getSubjectIri(), FOAF.INTEREST, objectIRI));
        }
    }

    private static class KnowsHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            ValueFactory vf = context.getValueFactory();
            IRI objectIRI = context.iriOf(object);
            context.getHandler().handleStatement(vf.createStatement(
                    context.getSubjectIri(), FOAF.KNOWS, objectIRI));
        }
    }

    private static class BirthdayHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            // technically, this is a misuse of foaf:birthday, which expects
            // string values of the form mm-dd, eg. '12-31', as opposed to
            // Extend-o-Brain's structured date values, which are represented
            // using the Event Ontology.
            // Nevertheless, foaf:birthday is used here because of its
            // appropriateness for the "birthday" meaning and its association with
            // foaf:Person, the RDF type associated with the EoB Person type.

            context.getHandler().handleStatement(
                    context.getValueFactory().createStatement(
                            context.getSubjectIri(), FOAF.BIRTHDAY, context.iriOf(object)));
        }
    }

    private static class ThingsOwnedHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            context.getHandler().handleStatement(
                    context.getValueFactory().createStatement(
                            context.iriOf(object), DBpediaOntology.owner, context.getSubjectIri()));
        }
    }

    public static class AttendedEventsHandler implements FieldHandler {
        @Override
        public void handle(Atom object, RDFizationContext context) throws RDFHandlerException {
            // TODO
        }
    }

    public static class WorksCollection extends DocumentCollection {
        public WorksCollection() {
            super();
            name = "works-collection";
            valueRegex = Pattern.compile("(some (books|papers|works) by .+)" +
                    "|(some of .+ (books|papers|works))");
        }
    }

    public static class InterestsCollection extends GenericCollection {
        public InterestsCollection() {
            super();
            name = "interests-collection";
            valueRegex = Pattern.compile("some things .+ like[sd]?");
        }
    }

    public static class SocialNetworkCollection extends PersonCollection {
        public SocialNetworkCollection() {
            super();
            name = "social-network-collection";
            valueRegex = Pattern.compile(".+ (social network|friends|family)");
        }
    }

    public static class PersonalEventsCollection extends AtomCollection {

        public PersonalEventsCollection() {
            super("personal-events",
                    Pattern.compile(".+ (logs|events)"),
                    null,
                    new AtomRegex(Arrays.asList(
                            new AtomRegex.El(null,
                                    AtomRegex.Modifier.ZeroOrMore, GenericCollection.class),
                            new AtomRegex.El(null,
                                    AtomRegex.Modifier.ZeroOrMore))));
        }
    }

    public static class PersonalStuffCollection extends AtomCollection {

        public PersonalStuffCollection() {
            super("personal-stuff",
                    Pattern.compile("(my|.+'s) stuff"),
                    null,
                    new AtomRegex(Arrays.asList(
                            new AtomRegex.El(new ThingsOwnedHandler(),
                                    AtomRegex.Modifier.ZeroOrOne, BelongingsCollection.class),
                            new AtomRegex.El(null,
                                    AtomRegex.Modifier.ZeroOrMore))));
        }
    }

    public static class BelongingsCollection extends GenericCollection {
        public BelongingsCollection() {
            super();
            name = "belongings-collection";
            valueRegex = Pattern.compile("(my|.+'s) belongings");
        }
    }
}
