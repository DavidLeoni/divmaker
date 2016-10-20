package eu.kidf.diversicon.maker.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.XZInputStream;

public class Tests {
    
    private static final Logger LOG = LoggerFactory.getLogger(Tests.class);    
    
    /**
     * Expects a .xml file. If not found, a file .xml.xz
     * is searched and decompressed in same folder and returned
     * 
     */
    public static File getDump(File file) {

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
    
    public static File getDump(String str) {
        return getDump(new File(str));
    }
}
