package it.unitn.disi.diversicon.maker.test;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.transform.LMFXmlWriter;
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
    public void testImportWn30() throws IOException, JWNLException{               
         
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();
                       
        if (dictionary == null){
            throw new RuntimeException("no dictionary!");
            
        }
        
        LexicalResource lr = new LexicalResource();
        
         WNConverter c = new WNConverter(new File("wn30/"), 
                 dictionary, 
                 lr, 
                 "3.0", 
                 dtdVersion);
         c.toLMF();
         File outFile = new File("mywordnet.xml");
         if (outFile.exists()){
             outFile.delete();
         }
         writeIt(lr, outFile);
         
    }
    
    @Test
    @Ignore
    public void testCreateImportWn30() throws IOException, JWNLException, XMLStreamException, SAXException, DocumentException{
        File outFile = new File("mywordnet.xml");
        lexicon2XML("wn30/", outFile);
    }
    
    public File lexicon2XML(String source, File lmfXML)
            throws IOException, XMLStreamException, SAXException, DocumentException, JWNLException
        {

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

            return writeIt(lexicalResource, lmfXML);

        }

    private File writeIt(LexicalResource lexicalResource, File lmfXML)  {
        try {
        LMFXmlWriter xmlWriter = new LMFXmlWriter(lmfXML.getAbsolutePath(), dtdPath);
        xmlWriter.writeElement(lexicalResource);
        xmlWriter.writeEndDocument();

        System.out.println("temp file saved: " + lmfXML.getAbsolutePath());

        return lmfXML;
        } catch (Exception ex){
            throw new RuntimeException("Error while writing LMF XML!", ex);
        }
    }
}
