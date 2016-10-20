package eu.kidf.diversicon.maker;

import static eu.kidf.diversicon.core.internal.Internals.checkNotBlank;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.meta.MetaData;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.LMFXmlWriter;
import de.tudarmstadt.ukp.lmf.transform.wordnet.WNConverter;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.ImportConfig;
import eu.kidf.diversicon.core.LexResPackage;
import eu.kidf.diversicon.core.exceptions.DivException;
import eu.kidf.diversicon.core.internal.Internals;
import eu.kidf.diversicon.data.DivWn31;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Creates Diversicon databases in various formats ready for packaging.
 * 
 * @since 0.1.0
 */
public class DivMaker {

    private static final Logger LOG = LoggerFactory.getLogger(DivMaker.class);

    private static final String dtdPath = Diversicons.DTD_1_0_PUBLIC_URL;
    private static final String dtdVersion = "1.0";
          
    /**
     * @since 0.1.0
     */
    private static final String DUMPS_UBY = "dumps/uby/";
    
    /**
     * @since 0.1.0
     */    
    private static final String DUMPS_DIV = "dumps/diversicon/";
    
    /**
     * version with dots, like '3.1'
     */
    private String version;
    
    LexResPackage pack;
    LexicalResource lexRes;
    List<String> errors;
    
    /**
     * @since 0.1.0
     */
    private DivMaker(){
        this.pack = new LexResPackage();
        this.errors = new ArrayList<>();
    }
    
    /**
     * Currently oonly processes Wordnet 3.1 and takes no arguments.
     * 
     * @since 0.1.0
     */
    public static void main(String args[]){
                                      
        if (args.length > 0){
            throw new DivException("Invalid arguments! Currently the program accepts no arguments at all!");
        }
        
        DivMaker m = new DivMaker();
        
        m.version = "3.1";       
        m.pack = DivWn31.of();
                
        
        m.run();
                                              
    }

    /**
     * Version without dots, like '30'
     * 
     * @since 0.1.0
     */
    private String vNoDot(){
        return version.replace(".", "");
    }
    
    /**
     * @since 0.1.0
     */
    private void run() {

        LOG.info("");
        LOG.info("");
        LOG.info("****   Going to create:\n" + pack.toString());
        LOG.info("");
        LOG.info("");
        Internals.checkLexResPackage(pack);        
        
        this.lexRes = wordnetToLexRes();        
        

        
        String h2dbName = DUMPS_DIV + lexRes.getName();        
        
        File fDumps = new File(DUMPS_DIV);
        
        if (!fDumps.exists()){
            LOG.info("Creating " + fDumps + "   ...");
            if (!fDumps.mkdirs()){
                throw new DivException("Couldn't create directory " + fDumps);
            }
            
        }
                
        
        File xmlFile = new File(DUMPS_DIV + lexRes.getName() + ".xml");
        if (xmlFile.exists()) {
            xmlFile.delete();
        }
        
        File sqlFile = new File(DUMPS_DIV + lexRes.getName() + ".sql");
        if (sqlFile.exists()) {
            sqlFile.delete();
        }

        File h2dbFile = new File(DUMPS_DIV + h2dbName + ".h2.db");
        if (h2dbFile.exists()) {
            h2dbFile.delete();
        }
                              
        LOG.info("****  Going to create XML to " + xmlFile.getAbsolutePath());
        LOG.info("****                   (make take several minutes...)");
        
        Diversicons.writeLexResToXml(lexRes, pack, xmlFile);
        
        DBConfig dbConfig = Diversicons.h2MakeDefaultFileDbConfig(h2dbName, false);
        
        Diversicons.createTables(dbConfig);
        
        Diversicon div = Diversicon.connectToDb(dbConfig);
        ImportConfig config = new ImportConfig();
        config.setAuthor("David Leoni");
        config.setDescription("Import for making Wordnet 3.1 Diversicon distribution");
        config.setFileUrls(Internals.newArrayList(xmlFile.getAbsolutePath()));
        
        div.importFiles(config); 
        
        try {
            LOG.info("****  Exporting to SQL " + sqlFile.getAbsolutePath());
            div.exportToSql(sqlFile.getAbsolutePath(), false);
        } catch (Exception ex){
            error("FAILED EXPORTING TO SQL!", ex);
        }

        try {
            LOG.info("****  Compacting db");
            Diversicons.h2Execute("SHUTDOWN COMPACT", dbConfig);
            LOG.info("****  Done compacting db.");
        } catch (Exception ex){
            error("FAILED COMPACTING THE DB!", ex);
        }

        if (!div.getSession().isOpen()){
            div.getSession().close();    
        }
        
        
        if (errors.size() > 0){
            LOG.error("");        
            LOG.error("");
            LOG.error("!!!!  DONE WITH ERRORS (look at log for more info):");
            for (String err : errors){
                LOG.error(" - " + err);
            }
            LOG.info("");
        } else {
            LOG.info("");        
            LOG.info("");
            LOG.info("****  DONE!!!   ALLELUJA!!!  ");
            LOG.info("");
        }
        
        
    }

    private void error(String msg, Exception ex) {
        errors.add(msg);
        LOG.error("");
        LOG.error("!!!!  " + msg, ex);
        LOG.error("");
    }

    /**
     * Returns a new LexicalResource
     * 
     * @since 0.1.0 
     */
    public LexicalResource wordnetToLexRes(){
        checkNotBlank(version, "Invalid version!");                                      
        
                
        File wordnetOnDisk = new File("wn" + vNoDot() + "/");
        
        Internals.checkArgument(wordnetOnDisk.exists(), 
                "I need a wordnet distribution at " + wordnetOnDisk.getAbsolutePath());
        
        Dictionary dictionary;
        try {
            dictionary = Dictionary.getResourceInstance("/net/sf/extjwnl/data/wordnet/wn"+vNoDot()+"/res_properties.xml");
        } catch (JWNLException e) {        
            throw new DivException("I need a wordnet distribution as maven package!", e);
        }

        if (dictionary == null) {
            throw new DivException("no dictionary!");   
        }

        LexicalResource lexRes = new LexicalResource();

        lexRes.setName(DivWn31.NAME);        
        lexRes.setDtdVersion(dtdVersion);
        
        WNConverter c = new WNConverter(
                "wn"+vNoDot(),
                wordnetOnDisk,
                dictionary,
                lexRes,
                version,
                dtdVersion);
        
        c.toLMF();
               
        LOG.info("Created lexical resource in memory!");
                      
        return lexRes;
        
    }


}
