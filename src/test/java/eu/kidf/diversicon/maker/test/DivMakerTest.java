package eu.kidf.diversicon.maker.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.LMFXmlWriter;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;

/**
 * 
 * TODO this class is full of useless stuff, do clean up!
 * 
 * @since 0.1.0
 */
public class DivMakerTest {

    private static final Logger LOG = LoggerFactory.getLogger(DivMakerTest.class);

    private static final String dtdPath = "ubyLmfDTD_1.0.dtd";
    private static final String dtdVersion = "1_0";

    private static final String DUMPS_DIVERSICON = "dumps/diversicon/";

    private static final String TARGET_DIV = "target/diversicon/";

    private static final String WN30_DIV = "div-wn30";

    @BeforeClass
    public static void beforeClass(){
        try {
            Files.createDirectories(Paths.get("target", "div"));            
        } catch (IOException e) {        
            throw new RuntimeException("Something went wrong!", e);
        }        
    }    

   



   
    public File lexicalResourceToXml(LexicalResource lexicalResource, File lmfXML) {
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
    @Ignore
    public void testXmlDumpToDb() {
        File xmlDump = Tests.getDump(DUMPS_DIVERSICON + WN30_DIV + ".xml");
        String outDb = TARGET_DIV + "wn30-from-dump";
        xmlToDb(xmlDump, outDb);

        // checkDb(outDb);
    }

    @Test
    @Ignore
    public void testDbToSql() {
        DBConfig dbConfig = Diversicons.h2MakeDefaultInMemoryDbConfig("mydb", false);
        Diversicons.dropCreateTables(dbConfig);
        Diversicon div = Diversicon.connectToDb(dbConfig);
        div.importXml("dumps/uby/uby-wn30.xml.xz");
        String zipFilePath = TARGET_DIV + WN30_DIV + ".sql.zip";
        div.exportToSql(zipFilePath, true);
        assertTrue(new File(zipFilePath).exists());
        div.getSession().close();
    }

    
    @Test    
    @Ignore
    public void testConnectToDb() {
        DBConfig dbConfig = Diversicons.h2MakeDefaultFileDbConfig("~/Da/prj/diversicon/dumps/div-wn31", true);
        Diversicon div = Diversicon.connectToDb(dbConfig);
                
        div.getSession().close();
        
        Diversicons.h2Execute("SHUTDOWN COMPACT", dbConfig);
    }
    
    
    @Test
    @Ignore
    public void testRestoreFileDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        Diversicons.h2RestoreSql(DUMPS_DIVERSICON + WN30_DIV + ".zip",
                    Diversicons.h2MakeDefaultFileDbConfig(tempDir.toString() + "/temp-db", true));
    }

    @Test
    @Ignore
    public void testRestoreZipToInMemoryDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        Diversicons.h2RestoreSql(DUMPS_DIVERSICON + WN30_DIV + ".zip", 
                Diversicons.h2MakeDefaultInMemoryDbConfig(tempDir.toString() + "/temp-db", false));
    }
    
    @Test
    @Ignore
    public void testRestoreSqlToInMemoryDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        Diversicons.h2RestoreSql(DUMPS_DIVERSICON + WN30_DIV + ".sql", 
                Diversicons.h2MakeDefaultInMemoryDbConfig(tempDir.toString() + "/temp-db", false));
    }

   
    public void checkDb(String outDbPath) {
        File outDb = new File(outDbPath + ".h2.db");
        assertTrue(outDb.exists());
        assertTrue(outDb.length() > 1000000);
    }    

    public void xmlToDb(File inputXml, String outDb) {

        try {
            if (!inputXml.exists()) {
                throw new RuntimeException("Input xml doesn't exist! Path is: " + inputXml.getAbsolutePath());
            }

            LOG.info("Going to populate H2 DB " + outDb + "...");

            DBConfig dbConfig = Diversicons.h2MakeDefaultFileDbConfig(outDb, false);

            Diversicons.dropCreateTables(dbConfig);

            XMLToDBTransformer dbWriter = new XMLToDBTransformer(dbConfig);

            dbWriter.transform(inputXml, WN30_DIV);

            LOG.info("db saved: " + outDb);

        } catch (Exception ex) {
            throw new RuntimeException("Error while writing db!", ex);
        }
    }
}
