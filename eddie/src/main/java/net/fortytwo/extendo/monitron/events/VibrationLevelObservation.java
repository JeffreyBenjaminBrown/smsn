package net.fortytwo.extendo.monitron.events;

import net.fortytwo.extendo.monitron.MonitronEventHandler;
import net.fortytwo.extendo.monitron.data.GaussianData;
import net.fortytwo.extendo.ontologies.MonitronOntology;
import net.fortytwo.extendo.ontologies.OMOntology;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class VibrationLevelObservation extends Observation {

    public VibrationLevelObservation(final MonitronEventHandler context,
                                     final URI sensor,
                                     final GaussianData data) {
        super(context, sensor, data);

        addStatement(d, event, RDF.TYPE, MonitronOntology.VIBRATION_LEVEL_OBSERVATION);
        addStatement(d, event, OMOntology.OBSERVED_PROPERTY, MonitronOntology.VIBRATION_LEVEL);

        Literal value = vf.createLiteral(data.getMaxValue());
        addStatement(d, result, OMOntology.VALUE, value);
        // TODO: add units
    }
}
