package it.unitn.disi.diversicon.maker.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.junit.BeforeClass;
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
public class DivMakerUbyTest {

    private static final Logger LOG = LoggerFactory.getLogger(DivMakerUbyTest.class);

    private static final String dtdPath = "ubyLmfDTD_1.0.dtd";
    private static final String dtdVersion = "1_0";

    private static final String DUMPS_UBY = "dumps/uby/";
    
    private static final String WN30_UBY = "uby-wn30";

    private static final String TARGET_UBY = "target/uby/";

    @BeforeClass
    public void beforeClass(){
        try {
            Files.createDirectories(Paths.get("target", "uby"));            
        } catch (IOException e) {        
            throw new RuntimeException("Something went wrong!", e);
        }        
    }
    
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

        File outFile = new File(TARGET_UBY  + WN30_UBY + ".xml");
        if (outFile.exists()) {
            outFile.delete();
        }
        lexicalResourceToXml(lr, outFile);

        String outDb = TARGET_UBY + WN30_UBY;
        xmlToDb(outFile, outDb);
        checkDb(outDb);

    }

    public File getDump(String str) {
        return getDump(new File(str));
    }

    /**
     * Expects a .xml file. If not found, a file .xml.xz
     * is searched and decompressed in same folder and returned
     * 
     */
    public File getDump(File file) {

        if (file.getAbsolutePath()
                .endsWith(".xml")
                || file.getAbsolutePath()
                       .endsWith(".h2.db")) {
            if (file.exists()) {
                return file;
            } else {

                File xz = new File(file.getAbsolutePath() + ".xz");

                if (!xz.exists()) {
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
        File xmlDump = getDump(DUMPS_UBY + WN30_UBY + ".xml");
        String outDb = TARGET_UBY + WN30_UBY + "-from-dump";
        xmlToDb(xmlDump, outDb);

        // checkDb(outDb);
    }

    @Test
    public void testDbToSql() {
        dbToSql(getDefaultH2FileDbConfig(DUMPS_UBY + WN30_UBY ), TARGET_UBY + WN30_UBY);
    }

    @Test
    public void testRestoreFileDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        restoreH2Db(DUMPS_UBY + WN30_UBY + ".zip", 
                getDefaultH2FileDbConfig(tempDir.toString() + "/temp-db"));
    }

    @Test
    public void testRestoreZipToInMemoryDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        restoreH2Db(DUMPS_UBY + WN30_UBY + ".zip", getDefaultH2InMemoryDbConfig(tempDir.toString() + "/temp-db"));
    }
    
    @Test
    public void testRestoreSqlToInMemoryDb() throws IOException {
        Path tempDir = Files.createTempDirectory("divmaker-test");
        restoreH2Db(DUMPS_UBY + WN30_UBY + ".sql", getDefaultH2InMemoryDbConfig(tempDir.toString() + "/temp-db"));
    }


    static InputStream getInputStream(File file) {

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {            
            throw new RuntimeException("Error while getting input stream!", ex);
        }
        String absPath = file.getAbsolutePath();
        if (absPath
                .endsWith(".zip")) {
            try {
                ZipInputStream zin = new ZipInputStream(fileInputStream);
                for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
                    if (!e.getName().endsWith(".sql")){
                        throw new RuntimeException("Expected .sql file inside zip, found instead "
                                + e.getName());
                    }
                    return zin;
                }
                throw new EOFException("Cannot find file!");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (absPath.endsWith(".sql")){
            return fileInputStream; 
        } else {
            throw new RuntimeException("Expected .sql or .zip file, found instead " + file.getAbsolutePath());
        }
    }

    public void restoreH2Db(String inFile, DBConfig outDb) {

        Date start = new Date();

        LOG.info("Restoring database...");
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Something went wrong!", ex);
        }
        // InputStream in = getClass().getResourceAsStream(inFile + ".zip");
        InputStream in;
        try {
            in = getInputStream(new File(inFile));// new
                                                  // FileInputStream(inFile);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Something went wrong!", ex);
        }

        if (in == null) {
            throw new RuntimeException("Please add the file script.sql to the classpath, package "
                    + getClass().getPackage()
                                .getName());
        } else {
            Connection conn = null;
            Statement stat = null;
            ResultSet rs = null;
            try {
                /* from http://www.h2database.com/html/performance.html#fast_import
                    SET LOG 0 (disabling the transaction log)
                    SET CACHE_SIZE (a large cache is faster)
                    SET LOCK_MODE 0 (disable locking)
                    SET UNDO_LOG 0 (disable the session undo log)
                */
                String saveVars = ""
                 + "  SET @DIV_SAVED_LOG @LOG;"
                 + "  SET @DIV_SAVED_CACHE_SIZE @CACHE_SIZE;"
                 + "  SET @DIV_SAVED_LOCK_MODE @LOCK_MODE;"
                 + "  SET @DIV_SAVED_UNDO_LOG @UNDO_LOG;";
                
                 String setFastOptions = 
                 "    SET @LOG 0;"
                 + "  SET @CACHE_SIZE 65536;"
                 + "  SET @LOCK_MODE 0;"
                 + "  SET @UNDO_LOG 0;";
                 
                 String restoreSavedVars = ""
                         + "  SET @LOG @DIV_SAVED_LOG;"
                         + "  SET @CACHE_SIZE @DIV_SAVED_CACHE_SIZE;"
                         + "  SET @LOCK_MODE @DIV_SAVED_LOCK_MODE;"
                         + "  SET @UNDO_LOG @DIV_SAVED_UNDO_LOG;";
                
                conn = DriverManager.getConnection(outDb.getJdbc_url());
                stat = conn.createStatement();
                stat.execute(saveVars);
                stat.execute(setFastOptions);
                RunScript.execute(conn, new InputStreamReader(in));
                stat.execute(restoreSavedVars);
                Date end = new Date();
                LOG.info("Done restoring database " + outDb.getJdbc_url());
                LOG.info("Elapsed time: " + Math.ceil(((end.getTime() - start.getTime()) / 1000)) + "s");

                // TODO do check
            } catch (SQLException e) {
                throw new RuntimeException("Something went wrong!", e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while closing result set", ex);
                    }
                }
                if (stat != null) {
                    try {
                        stat.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while closing Statement", ex);
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while closing connection", ex);
                    }
                }

            }
        }
    }

    private void dbToSql(DBConfig inDbConfig, String outPath) {
        LOG.info("Backing up database");
        // used to get DB location
        // Statement stmt = conn.createStatement();

        // Configuration cfg = HibernateConnect.getConfiguration(dbConfig);

        // SessionFactory sessionFactory = cfg.buildSessionFactory(
        // new ServiceRegistryBuilder().applySettings(
        // cfg.getProperties()).buildServiceRegistry());

        // used to get DB url
        String sqlPath = outPath + ".zip";

        String[] bkp = { "-url", inDbConfig.getJdbc_url(),
                "-user", inDbConfig.getUser(), "-password", inDbConfig.getPassword(),
                "-script", sqlPath,
                "-options",
                "compression",
                "zip" };
        try {
            Script.main(bkp);
        } catch (SQLException ex) {
            throw new RuntimeException("Error while exporting to sql!", ex);
        }
        LOG.info("Done backing up database to " + sqlPath);
    }

    private void checkDb(String outDbPath) {
        File outDb = new File(outDbPath + ".h2.db");
        assertTrue(outDb.exists());
        assertTrue(outDb.length() > 1000000);
    }

    private DBConfig getDefaultH2FileDbConfig(String filePath) {
        DBConfig ret = new DBConfig();
        ret.setDb_vendor("de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect");
        ret.setJdbc_driver_class("org.h2.Driver");
        ret.setJdbc_url("jdbc:h2:file:" + filePath);
        ret.setUser("root");
        ret.setPassword("pass");
        return ret;
    }

    /**
     * 
     * @param dbName
     *            Uniquely identifies the db among all in-memort dbs.
     */
    private DBConfig getDefaultH2InMemoryDbConfig(String dbName) {
        DBConfig ret = new DBConfig();
        ret.setDb_vendor("de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect");
        ret.setJdbc_driver_class("org.h2.Driver");
        ret.setJdbc_url("jdbc:h2:mem:" + dbName);
        ret.setUser("root");
        ret.setPassword("pass");
        return ret;
    }

    private void xmlToDb(File inputXml, String outDb) {

        try {
            if (!inputXml.exists()) {
                throw new RuntimeException("Input xml doesn't exist! Path is: " + inputXml.getAbsolutePath());
            }

            LOG.info("Going to populate H2 DB " + outDb + "...");

            DBConfig dbConfig = getDefaultH2FileDbConfig(outDb);

            LMFDBUtils.createTables(dbConfig);

            XMLToDBTransformer dbWriter = new XMLToDBTransformer(dbConfig);

            dbWriter.transform(inputXml, WN30_UBY);

            LOG.info("db saved: " + outDb);

        } catch (Exception ex) {
            throw new RuntimeException("Error while writing db!", ex);
        }
    }
}
