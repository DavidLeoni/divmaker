package it.unitn.disi.diversicon.maker;

import static it.unitn.disi.diversicon.internal.Internals.checkNotBlank;
import it.unitn.disi.diversicon.data.DivWn31;
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
import it.disi.unitn.diversicon.exceptions.DivException;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.LexResPackage;
import it.unitn.disi.diversicon.internal.Internals;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Creates Diversicon databases in various formats ready for packaging.
 * 
 * @since 0.1.0
 */
public class DivMaker {

    private static final Logger LOG = LoggerFactory.getLogger(DivMaker.class);

    private static final String dtdPath = "ubyLmfDTD_1.0.dtd";
    private static final String dtdVersion = "1_0";
          
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
        this.lexRes.setName(DivWn31.NAME);
        

        
        String h2dbName = DUMPS_DIV + lexRes.getName();        
        
        File fDumps = new File(DUMPS_DIV);
        
        if (!fDumps.exists()){
            LOG.info("Creating " + fDumps + "   ...");
            if (!fDumps.mkdirs()){
                throw new DivException("Couldn't create directory " + fDumps);
            }
            
        }
        
        File xmlDirectFile = new File(DUMPS_DIV + lexRes.getName() + "-direct.xml");
        if (xmlDirectFile.exists()) {
            xmlDirectFile.delete();
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
                              
        LOG.info("****  Going to directly create XML to " + xmlDirectFile.getAbsolutePath());
        
        Diversicons.writeLexResToXml(lexRes, pack, xmlDirectFile);
        
        DBConfig dbConfig = Diversicons.makeDefaultH2FileDbConfig(h2dbName, false);
        
        Diversicon div = Diversicon.connectToDb(dbConfig);
        div.importResource(lexRes, pack, false); 
        
        LOG.info("****  Recreating XML to " + xmlFile.getAbsolutePath());
        
        try {
            div.exportToXml(xmlFile.getAbsolutePath(), lexRes.getName(), false);
        } catch (Exception ex){
            error("FAILED EXPORTING TO XML!", ex);
        }

        try {
            LOG.info("****  Exporting to SQL " + sqlFile.getAbsolutePath());
            div.exportToSql(sqlFile.getAbsolutePath(), false);
        } catch (Exception ex){
            error("FAILED EXPORTING TO SQL!", ex);
        }

        try {
        LOG.info("****  Compacting db");
        SQLQuery q = div.getSession().createSQLQuery("SHUTDOWN COMPACT");
        q.executeUpdate();
        LOG.info("****  Done compacting db.");
        } catch (Exception ex){
            error("FAILED COMPACTING THE DB!", ex);
        }
        
        div.getSession().close();
        
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
