package org.diverse.pcm.io.wikipedia;

import org.diverse.pcm.api.java.PCM;
import org.diverse.pcm.io.wikipedia.export.PCMModelExporter;
import org.diverse.pcm.io.wikipedia.export.WikiTextExporter;
import org.diverse.pcm.io.wikipedia.pcm.Page;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static scala.collection.JavaConversions.seqAsJavaList;

/**
 * Created by StephaneMangin on 07/01/15.
 */
public class WikipediaReverseGenerationTest {


    private WikipediaPageMiner miner;
    private String resources_path;
    private File file;

    private String code;
    private String preprocessedCode;
    private Page page;
    private PCMModelExporter pcmExporter;
    private WikiTextExporter wikitextExporter;
    private List<String> pcmsWiki;
    private List<PCM> pcms0;
    private List<PCM> pcms1;

    @Before
    public void initialize(){
        miner = new WikipediaPageMiner();
        resources_path = getCurrentFolderPath() + "/resources/";
        file = new File(resources_path + "Comparison_test.txt");
        pcmExporter = new PCMModelExporter();
        wikitextExporter = new WikiTextExporter();
        pcmsWiki = new ArrayList<String>();
        pcms0 = new ArrayList<PCM>();
        pcms1 = new ArrayList<PCM>();
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
        while ((b=in.read()) != -1) out.write(b);
        out.flush();
        out.close();
        in.close();
        return out.toString();
    }

    @Test
    public void ReverseWikitextTest() {
        try {
            code = loadFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        preprocessedCode = miner.preprocess(code);
        page = miner.parse(preprocessedCode);
        pcms0 = seqAsJavaList(pcmExporter.export(page));
        // Transform a list of PCM models into wikitext (markdown language for Wikipedia articles)
        for (PCM pcm : pcms0) pcmsWiki.add(wikitextExporter.toWikiText(pcm));
        //System.out.println(pcmsWiki.);
        for (String wikiText: pcmsWiki) {
            preprocessedCode = miner.preprocess(wikiText);
            page = miner.parse(preprocessedCode);
            pcms1.add(pcmExporter.export(page).head());
        }
        assertEquals(pcms0.size(), pcms1.size());
        for(int i=0;i<pcms0.size();i++) assertEquals(pcms0.get(i), pcms1.get(i));

    }
}
