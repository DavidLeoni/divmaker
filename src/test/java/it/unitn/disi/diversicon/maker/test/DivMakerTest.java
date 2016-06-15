package it.unitn.disi.diversicon.maker.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.XZInputStream;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.LMFDBUtils;
import de.tudarmstadt.ukp.lmf.transform.LMFXmlWriter;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;
import de.tudarmstadt.ukp.lmf.transform.wordnet.WNConverter;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * @since 0.1.0
 */
public class DivMakerTest {

    private static final Logger LOG = LoggerFactory.getLogger(DivMakerTest.class);

    private static final String dtdPath = "ubyLmfDTD_1.0.dtd";
    private static final String dtdVersion = "1_0";

    @Test
    public void testImportWn30() throws IOException, JWNLException {

        Dictionary dictionary = Dictionary.getDefaultResourceInstance();

        if (dictionary == null) {
            throw new RuntimeException("no dictionary!");

        }

        LexicalResource lr = new LexicalResource();

        WNConverter c = new WNConverter(new File("wn30/"),
                dictionary,
                lr,
                "3.0",
                dtdVersion);
        c.toLMF();
        LOG.info("Created lexical resource in memory!");

        File outFile = new File("target/wn30.xml");
        if (outFile.exists()) {
            outFile.delete();
        }
        lexicalResourceToXml(lr, outFile);

        String outDb = "target/wn30";
        xmlToDb(outFile, outDb);
        checkDb(outDb);

    }

    public File getDump(String str) {
        return getDump(new File(str));
    }
    /**
     * Expects a .xml file. If not found, a file .xml.xz
     *  is searched and decompressed in same folder and returned

     */
    public File getDump(File file) {
        
        if (file.getAbsolutePath()
                .endsWith(".xml")) {
            if (file.exists()){
                return file;
            } else {
                
                File xz = new File(file.getAbsolutePath() + ".xz");

                if (!xz.exists()){
                    throw new IllegalStateException("Couldn't find file " + xz.getAbsolutePath() + " to decompress!");
                }
                
                LOG.info("Decompressing " + xz.getAbsolutePath() + " ...");
                try {
                    FileInputStream fin = new FileInputStream(xz);
                    BufferedInputStream in = new BufferedInputStream(fin);
                    FileOutputStream out = new FileOutputStream(file);
                    XZInputStream xzIn = new XZInputStream(in);
                    final byte[] buffer = new byte[8192];
                    int n = 0;
                    while (-1 != (n = xzIn.read(buffer))) {
                        out.write(buffer, 0, n);
                    }
                    out.close();
                    xzIn.close();
                    LOG.info("Wrote " + file.getAbsolutePath() + " file.");
                    return file;
                } catch (Exception e) {
                    throw new RuntimeException("Error while decompressing " + file.getAbsolutePath(), e);
                }

            }

        } else {
            throw new IllegalArgumentException("Unsupported input file extension for file " + file.getAbsolutePath());
        }

    }

    @Test
    @Ignore
    public void testCreateImportWn30()
            throws IOException, JWNLException, XMLStreamException, SAXException, DocumentException {
        File outFile = new File("mywordnet.xml");
        lexicon2XML("wn30/", outFile);
    }

    /**
     * Copied from ubycreate-gpl
     */
    public File lexicon2XML(String source, File lmfXML)
            throws IOException, XMLStreamException, SAXException, DocumentException, JWNLException {

        String lexicalResourceName = "WordNet_3.0_eng";

        /* Dumping lexical into a file */

        LexicalResource lexicalResource = null;

        File wnPath = new File(source);
        Dictionary extWordnet;
        extWordnet = Dictionary
                               .getInstance(IOUtils.toInputStream(
                                       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                               + "<jwnl_properties language=\"en\">"
                                               + "  <version publisher=\"Princeton\" number=\"3.0\" language=\"en\"/>"
                                               + "  <dictionary class=\"net.sf.extjwnl.dictionary.FileBackedDictionary\">"
                                               + "    <param name=\"morphological_processor\" value=\"net.sf.extjwnl.dictionary.morph.DefaultMorphologicalProcessor\">"
                                               + "      <param name=\"operations\">"
                                               + "        <param value=\"net.sf.extjwnl.dictionary.morph.LookupExceptionsOperation\"/>"
                                               + "        <param value=\"net.sf.extjwnl.dictionary.morph.DetachSuffixesOperation\">"
                                               + "          <param name=\"noun\" value=\"|s=|ses=s|xes=x|zes=z|ches=ch|shes=sh|men=man|ies=y|\"/>"
                                               + "          <param name=\"verb\" value=\"|s=|ies=y|es=e|es=|ed=e|ed=|ing=e|ing=|\"/>"
                                               + "          <param name=\"adjective\" value=\"|er=|est=|er=e|est=e|\"/>"
                                               + "          <param name=\"operations\">"
                                               + "            <param value=\"net.sf.extjwnl.dictionary.morph.LookupIndexWordOperation\"/>"
                                               + "            <param value=\"net.sf.extjwnl.dictionary.morph.LookupExceptionsOperation\"/>"
                                               + "          </param>"
                                               + "        </param>"
                                               + "        <param value=\"net.sf.extjwnl.dictionary.morph.TokenizerOperation\">"
                                               + "          <param name=\"delimiters\">"
                                               + "            <param value=\" \"/>"
                                               + "            <param value=\"-\"/>"
                                               + "          </param>"
                                               + "          <param name=\"token_operations\">"
                                               + "            <param value=\"net.sf.extjwnl.dictionary.morph.LookupIndexWordOperation\"/>"
                                               + "            <param value=\"net.sf.extjwnl.dictionary.morph.LookupExceptionsOperation\"/>"
                                               + "            <param value=\"net.sf.extjwnl.dictionary.morph.DetachSuffixesOperation\">"
                                               + "              <param name=\"noun\" value=\"|s=|ses=s|xes=x|zes=z|ches=ch|shes=sh|men=man|ies=y|\"/>"
                                               + "              <param name=\"verb\" value=\"|s=|ies=y|es=e|es=|ed=e|ed=|ing=e|ing=|\"/>"
                                               + "              <param name=\"adjective\" value=\"|er=|est=|er=e|est=e|\"/>"
                                               + "              <param name=\"operations\">"
                                               + "                <param value=\"net.sf.extjwnl.dictionary.morph.LookupIndexWordOperation\"/>"
                                               + "                <param value=\"net.sf.extjwnl.dictionary.morph.LookupExceptionsOperation\"/>"
                                               + "              </param>"
                                               + "            </param>"
                                               + "          </param>"
                                               + "        </param>"
                                               + "      </param>"
                                               + "    </param>"
                                               + "    <param name=\"dictionary_element_factory\" value=\"net.sf.extjwnl.princeton.data.PrincetonWN17FileDictionaryElementFactory\"/>"
                                               + "    <param name=\"file_manager\" value=\"net.sf.extjwnl.dictionary.file_manager.FileManagerImpl\">"
                                               + "      <param name=\"file_type\" value=\"net.sf.extjwnl.princeton.file.PrincetonRandomAccessDictionaryFile\"/>"
                                               + "      <param name=\"dictionary_path\" value=\""
                                               + wnPath.getAbsolutePath() + "\"/>"
                                               + "    </param>"
                                               + "  </dictionary>"
                                               + "</jwnl_properties>"));

        WNConverter converterWN = new WNConverter(wnPath, extWordnet, new LexicalResource(),
                lexicalResourceName, dtdVersion);
        converterWN.toLMF();
        lexicalResource = converterWN.getLexicalResource();

        return lexicalResourceToXml(lexicalResource, lmfXML);

    }

    private File lexicalResourceToXml(LexicalResource lexicalResource, File lmfXML) {
        try {
            LOG.info("Going to create XML...");

            LMFXmlWriter xmlWriter = new LMFXmlWriter(lmfXML.getAbsolutePath(), dtdPath);
            xmlWriter.writeElement(lexicalResource);
            xmlWriter.writeEndDocument();

            LOG.info("xml file saved: " + lmfXML.getAbsolutePath());

            return lmfXML;
        } catch (Exception ex) {
            throw new RuntimeException("Error while writing LMF XML!", ex);
        }
    }

    @Test
    public void testXmlDumpToDb() {
        File xmlDump = getDump("dumps/wn30.xml");
        String outDb = "target/wn30-from-dump";        
        xmlToDb(xmlDump, outDb);
        checkDb(outDb);
    }

    private void checkDb(String outDbPath) {                       
        File outDb = new File(outDbPath + ".h2.db");
        assertTrue(outDb.exists());
        assertTrue(outDb.length() > 1000000);
    }

    private void xmlToDb(File inputXml, String outDb) {       
        
        try {
            if (!inputXml.exists()) {
                throw new RuntimeException("Input xml doesn't exist! Path is: " + inputXml.getAbsolutePath());
            }

            
            LOG.info("Going to populate H2 DB " + outDb + "...");

            DBConfig dbConfig = new DBConfig();
            dbConfig.setDb_vendor("de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect");
            dbConfig.setJdbc_driver_class("org.h2.Driver");
            dbConfig.setJdbc_url("jdbc:h2:file:" + outDb);
            dbConfig.setUser("root");
            dbConfig.setPassword("pass");

            LMFDBUtils.createTables(dbConfig);

            XMLToDBTransformer dbWriter = new XMLToDBTransformer(dbConfig);

            dbWriter.transform(inputXml, "wn30");

            LOG.info("db saved: " + outDb);

        } catch (Exception ex) {
            throw new RuntimeException("Error while writing db!", ex);
        }
    }
}
