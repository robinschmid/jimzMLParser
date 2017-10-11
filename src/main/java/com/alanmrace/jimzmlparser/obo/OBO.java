package com.alanmrace.jimzmlparser.obo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to store an ontology database (including dependents) loaded from OBO format.
 * 
 * @author Alan Race
 */
public class OBO implements Serializable {

    /** Class logger. */
    private static final Logger logger = Logger.getLogger(OBO.class.getName());

    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    /** Location of the PSI MS ontology. */
    public static final String MS_OBO_URI = "http://psidev.cvs.sourceforge.net/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo";
    /** Location of the Units ontology. */
    public static final String UO_OBO_URI = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo";
    
    /** Location of the MSI ontology. */
    public static final String IMS_OBO_URI = "http://ms-imaging.org.org/download/imzml/imagingMS.obo";
    /** Full name of the MSI ontology. */
    public static final String IMS_OBO_FULLNAME = "Mass Spectrometry Imaging Ontology";
    /** Shorthand identifier for the MSI ontology. */
    public static final String IMS_OBO_ID = "IMS";
    /** Version number for the MSI ontology. */
    public static final String IMS_OBO_VERSION = "???";
    
    /** Path to the OBO file. */
    private String path;

    /** List of imported ontologies. */
    private List<OBO> imports;

    /** Dictionary of ontology terms, using their ID as the key. */
    private Map<String, OBOTerm> terms;
    
    /**
     * Singleton OBO instance.
     */
    protected static final OBO obo = new OBO("imagingMS.obo");

    private String obopath = "C:\\Users\\r_schm33\\Programmieren\\Git\\jimzMLParser\\src\\main\\resources\\obo\\";
    /**
     * Generate ontology database from the specified .obo file. 
     * If the obo file specifies imports, then load those imports from resources
     * associated with the project. The path of the import is ignored, with only 
     * the final name considered as the location of the resource.
     * 
     * @param path Location of the .obo file to load the ontology from.
     */
    public OBO(String path) {
        this.path = path;

        imports = new ArrayList<OBO>();
        terms = new HashMap<String, OBOTerm>();

        String resourcePath = path;
        
        // Strip off the URL details if they exist
        if (resourcePath.contains("http://") || resourcePath.contains("https://")) {
            resourcePath = resourcePath.substring(resourcePath.lastIndexOf("/") + 1).toLowerCase();
        }
        	resourcePath = obopath+resourcePath;

        logger.log(Level.FINER, "Parsing OBO /obo/{0}", resourcePath);

        try {
//      InputStream is = OBO.class.getResourceAsStream("/obo/" + resourcePath);
        InputStream is = new FileInputStream(resourcePath);

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);

        String curLine;
        OBOTerm curTerm = null;
        boolean processingTerms = false;

            while ((curLine = in.readLine()) != null) {
                // Skip empty lines
                if (curLine.trim().isEmpty()) {
                    continue;
                }

                if (curLine.trim().equals("[Term]")) {
                    // Process a term
                    processingTerms = true;

                    // Get the ID
                    curLine = in.readLine();
                    // TODO: Requires that the first tag in the term is the ID tag 
                    int indexOfColon = curLine.indexOf(":");
                    String id = curLine.substring(indexOfColon + 1).trim();

                    curTerm = new OBOTerm(id);

                    terms.put(id, curTerm);
                } else if (curLine.trim().equals("[Typedef]")) {
                    processingTerms = false;
                } else if (curTerm != null && processingTerms) {
                    curTerm.parse(curLine);
                } else {
                    // TODO: Add in header information
                    int locationOfColon = curLine.indexOf(":");
                    String tag = curLine.substring(0, locationOfColon).trim();
                    String value = curLine.substring(locationOfColon + 1).trim().toLowerCase();

                    if (tag != null && value != null
                            && "import".equals(tag)) {

                        imports.add(new OBO(value));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OBO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Process relationships
        for (OBOTerm term : terms.values()) {
            Collection<String> is_a = term.getIsA();

            for (String id : is_a) {
                OBOTerm parentTerm = getTerm(id);

                if (parentTerm == null) {
                    System.err.println("Haven't found " + id);
                } else {
                    parentTerm.addChild(term);
                    term.addParent(parentTerm);
                }
            }

            Collection<String> part_of = term.getPartOf();

            for (String id : part_of) {
                OBOTerm parentTerm = getTerm(id);

                if (parentTerm == null) {
                    System.err.println("Haven't found " + id);
                } else {
                    parentTerm.addChild(term);
                    term.addParent(parentTerm);
                }
            }

            // Units
            for(String unitName : term.unitList) {
            //if (term.getUnitName() != null) {
                term.addUnit(getTerm(unitName));
            }
        }
    }
    
    /**
     * Static function to load in the imagingMS.obo file stored as a project resource.
     * 
     * @return Loaded ontology
     */
    public static OBO getOBO() {
        return obo;
    }
    
    /**
     * Return all imported ontologys.
     * 
     * @return List of imported ontologys
     */
    public List<OBO> getImports() {
        return this.imports;
    }
    
    /**
     * Return a complete list of ontology terms present within this ontolgoy.
     * 
     * @return All ontology terms
     */
    public Collection<OBOTerm> getTerms() {
        return this.terms.values();
    }

    /**
     * Get the term from the ontology with the ID id.
     * If no term is found with exactly the id specified, then return null.
     * 
     * @param id ID of the ontology term
     * @return Ontology term if found, null otherwise
     */
    public OBOTerm getTerm(String id) {
        if (id == null) {
            return null;
        }

        OBOTerm term = terms.get(id);

        if (term == null) {
            for (OBO parent : imports) {
                term = parent.getTerm(id);

                if (term != null) {
                    break;
                }
            }
        }

        return term;
    }

    @Override
    public String toString() {
        return path;
    }
}
