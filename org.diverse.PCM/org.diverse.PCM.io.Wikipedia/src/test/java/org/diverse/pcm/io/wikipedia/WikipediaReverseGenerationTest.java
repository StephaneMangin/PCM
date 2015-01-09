package org.diverse.pcm.io.wikipedia;

import org.diverse.pcm.api.java.AbstractFeature;
import org.diverse.pcm.api.java.Feature;
import org.diverse.pcm.api.java.PCM;
import org.diverse.pcm.api.java.Product;
import org.diverse.pcm.io.wikipedia.export.*;
import org.diverse.pcm.io.wikipedia.pcm.*;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import static scala.collection.JavaConversions.*;

/**
 * Created by StephaneMangin on 07/01/15.
 */
public class WikipediaReverseGenerationTest {


    private WikipediaPageMiner miner = new WikipediaPageMiner();
    private String resources_path = getCurrentFolderPath() + "/resources/";
    private File file = new File(resources_path + "Comparison_test.txt");

    int nbFeatures = 200;
    int nbProducts = 500;
    private String code;
    private String preprocessedCode;
    private Page page;
    private PCMModelExporter pcmExporter;
    private WikiTextExporter wikitextExporter;
    private Map<PCM, String> pcmsMap;
    private List<PCM> pcms0;
    private List<PCM> pcms1;
    /*
        Create a fake pcm nbFeatures*nbProducts

        This method manage the generation of a "fake" comparison file
       which is composed of product named "P1" and feature named "F1"
       The particularity is that cell's value is named "P1F1"
       which is quite simple to test

     */
    private void createBrutalPCM() throws IOException {

        FileWriter filew = new FileWriter(file);
        filew.write("{| class=\"wikitable sortable\"\n");
        for(int i=1; i <= nbFeatures; i++){
            filew.write("! F"+i+"\n");
        }
        filew.write("|-\n");

        for(int j=1; j <= nbProducts; j++){
            filew.write("|- P"+j);
            for(int i=1; i <= nbFeatures; i++){
                filew.write(" || P" + j + "F" + i);
            }
            filew.write("\n|-\n");
        }
        filew.write("|}");
        filew.close();
    }

    /*
        Transform a list of PCM models into wikitext (markdown language for Wikipedia articles

     */
    @Before
    public void setUp() throws Exception {
        // TODO: check for sonar to mesure performance
        createBrutalPCM();
        pcmExporter = new PCMModelExporter();
        wikitextExporter = new WikiTextExporter();
        pcms0 = new ArrayList<PCM>();
        pcms1 = new ArrayList<PCM>();

        try {
            code = loadFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        preprocessedCode = miner.preprocess(code);
        page = miner.parse(preprocessedCode);

        // Preload the pcms content
        pcms0 = seqAsJavaList(pcmExporter.export(page));
        for (PCM pcm : pcms0) {
            preprocessedCode = miner.preprocess(wikitextExporter.toWikiText(pcm));
            page = miner.parse(preprocessedCode);
            pcms1.add(pcmExporter.export(page).head());
        }
    }

    private String getCurrentFolderPath() {
        String path = "";
        try {
            path = new java.io.File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /* Manage a file opening and returns its content
    
     */
    private String loadFile(File f) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        StringWriter out = new StringWriter();
        int b;
        while ((b=in.read()) != -1)
            out.write(b);
        out.flush();
        out.close();
        in.close();
        return out.toString();
    }

    /*
        Compare pcms from those before and after reverse parsing

     */
/*    @Test
    public void ReverseWikitextTest() {

        System.out.println("pcms0");
        for (PCM pcm: pcms0) {
            System.out.println("pcm");
            System.out.println(pcm.getProducts());
            for (Product product: pcm.getProducts()) {
                System.out.println(product.getName());
            }
        }
        System.out.println("pcms1");
        for (PCM pcm: pcms0) {
            System.out.println("pcm");
            System.out.println(pcm.getProducts());
            for (Product product: pcm.getProducts()) {
                System.out.println(product.getName());
            }
        }
        //assertTrue(pcms0.equals(pcms1));

    }*/

    public boolean comparePCM(PCM pcm1, PCM pcm2){
        if(pcm1.getFeatures().size() != pcm2.getFeatures().size()) {
            return false;
        }
        if(!pcm1.getFeatures().containsAll(pcm2.getFeatures()) ||
                !pcm2.getFeatures().containsAll(pcm1.getFeatures())){
            return false;
        }
        if(pcm1.getProducts().size() != pcm2.getProducts().size()){
            return false;
        }
        if(!pcm1.getProducts().containsAll(pcm2.getProducts()) ||
                !pcm2.getProducts().containsAll(pcm1.getProducts())){
            return false;
        }

        Iterator itfeature1 = pcm1.getFeatures().iterator();
        Iterator itfeature2 = pcm2.getFeatures().iterator();
        Iterator itproduct1 = pcm1.getProducts().iterator();
        Iterator itproduct2 = pcm2.getProducts().iterator();

        while(itfeature1.hasNext()){
            while (itfeature2.hasNext()){
                while(itproduct1.hasNext()){
                    while (itproduct2.hasNext()){
                        Product product1 = (Product)itproduct1.next();
                        AbstractFeature feature1 = (Feature)itfeature1.next();
                        Product product2 = (Product)itproduct2.next();
                        AbstractFeature feature2 = (Feature)itfeature2.next();
                        if(product1.getCell(feature1).getContent() != product2.getCell(feature2).getContent()){
                            return false;
                        }
                    }
                }

            }
        }
        return true;
    }
}
