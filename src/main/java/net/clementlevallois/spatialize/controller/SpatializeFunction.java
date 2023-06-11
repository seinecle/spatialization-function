package net.clementlevallois.spatialize.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import net.clementlevallois.utils.Clock;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.plugin.ExporterGEXF;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterGEXF;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author LEVALLOIS
 */
public class SpatializeFunction {

    private Clock clock;
    private static boolean silentClock = true;

    public static void main(String[] args) throws IOException {
        SpatializeFunction spat = new SpatializeFunction();
        Path path = Path.of("C:\\Users\\levallois\\OneDrive - Aescra Emlyon Business School\\Bureau\\tests\\cowo--min-occ-4-words-removed.gexf");
        String gexf = Files.readString(path);
        spat.spatialize(gexf, 5);
    }

    public String spatialize(String gexf, int maxTimeRunningInSeconds) throws IOException {
        clock = new Clock("clocking the layout",true);
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        projectController.newProject();
        Container container = null;


        FileImporter fi = new ImporterGEXF();
        container = importController.importFile(new StringReader(gexf), fi);
        container.closeLoader();

        DefaultProcessor processor = new DefaultProcessor();
        processor.setWorkspace(projectController.getCurrentWorkspace());
        processor.setContainers(new ContainerUnloader[]{container.getUnloader()});
        processor.process();
        GraphModel gm = graphController.getGraphModel();

        ForceAtlas2Builder layoutBuilder = new ForceAtlas2Builder();
        ForceAtlas2 layout = new ForceAtlas2(layoutBuilder);
        layout.setGraphModel(gm);
        layout.resetPropertiesValues();

        int threads = Runtime.getRuntime().availableProcessors() * 2 - 1;

        layout.setScalingRatio(5d);
        layout.setThreadsCount(threads);
        layout.setAdjustSizes(Boolean.TRUE);
        layout.setJitterTolerance(2d);
        layout.setBarnesHutOptimize(Boolean.TRUE);
        layout.setBarnesHutTheta(2d);

        layout.initAlgo();
        int counterLoops = 1;
        long start = System.currentTimeMillis();
        for (int i = 0; layout.canAlgo(); i++) {
            layout.goAlgo();
            long now = System.currentTimeMillis();
            if ((now - start) / 1000 > maxTimeRunningInSeconds) {
                break;
            }
        }
        layout.endAlgo();

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        ExporterGEXF exporterGexf = (ExporterGEXF) ec.getExporter("gexf");
        exporterGexf.setWorkspace(projectController.getCurrentWorkspace());
        exporterGexf.setExportDynamic(true);
        exporterGexf.setExportPosition(true);
        exporterGexf.setExportSize(true);
        exporterGexf.setExportColors(true);
        exporterGexf.setExportMeta(true);

        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, exporterGexf);
        stringWriter.close();
        String resultGexf = stringWriter.toString();
        clock.closeAndPrintClock();
        return resultGexf;
    }
}
