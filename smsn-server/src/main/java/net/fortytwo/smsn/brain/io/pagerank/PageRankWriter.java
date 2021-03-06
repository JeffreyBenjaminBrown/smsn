package net.fortytwo.smsn.brain.io.pagerank;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import net.fortytwo.smsn.brain.io.BrainWriter;
import net.fortytwo.smsn.brain.io.Format;
import net.fortytwo.smsn.brain.model.Atom;
import net.fortytwo.smsn.brain.model.AtomList;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class PageRankWriter extends BrainWriter {

    @Override
    public List<Format> getFormats() {
        return Arrays.asList(PageRankTSVFormat.getInstance());
    }

    @Override
    public void doExport(Context context) throws IOException {

        TinkerGraph tmpGraph = new TinkerGraph();

        for (Atom a : context.getFilteredGraph().getAllAtoms()) {
            tmpGraph.addVertex(a.getId());
        }

        for (Atom a : context.getFilteredGraph().getAllAtoms()) {
            AtomList children = a.getNotes();
            Vertex outVertex = tmpGraph.getVertex(a.getId());
            while (null != children) {
                Vertex inVertex = tmpGraph.getVertex(children.getFirst().getId());
                tmpGraph.addEdge(null,
                        outVertex,
                        inVertex,
                        "link");
                children = children.getRest();
            }
        }

        PageRank<Vertex, Edge> pr = new PageRank<>(new GraphJung(tmpGraph), 0.15d);
        pr.evaluate();

        PrintStream p = new PrintStream(context.getDestStream());
        p.println("id\tscore");
        for (Vertex v : tmpGraph.getVertices()) {
            p.println(v.getId() + "\t" + pr.getVertexScore(v));
        }

        tmpGraph.shutdown();
    }
}
