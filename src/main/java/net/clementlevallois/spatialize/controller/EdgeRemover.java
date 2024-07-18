/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package net.clementlevallois.spatialize.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

/**
 *
 * @author LEVALLOIS
 */
public class EdgeRemover {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Path gexfPath = Path.of("C:\\Users\\levallois\\open\\nocode-app-functions\\MapsOfScience\\data\\maps\\20230607\\spatialized.gexf");
        EdgeRemover er = new EdgeRemover();
        er.removeAllEdges(gexfPath);
    }

    public void removeAllEdges(Path path) throws IOException {
        String gexf = Files.readString(path);
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
        gm.getGraph().clearEdges();
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        ExporterGEXF exporterGexf = (ExporterGEXF) ec.getExporter("gexf");
        exporterGexf.setWorkspace(projectController.getCurrentWorkspace());
        exporterGexf.setExportDynamic(false);
        exporterGexf.setExportPosition(true);
        exporterGexf.setExportSize(true);
        exporterGexf.setExportColors(true);
        exporterGexf.setExportMeta(true);

        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, exporterGexf);
        stringWriter.close();
        String resultGexf = stringWriter.toString();
        Files.writeString(Path.of("edges-removed.gexf"), resultGexf);
    }

}
