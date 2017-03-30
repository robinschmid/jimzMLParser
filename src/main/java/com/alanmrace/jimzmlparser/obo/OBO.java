package com.alanmrace.jimzmlparser.obo;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OBO implements Serializable {

    private static final Logger logger = Logger.getLogger(OBO.class.getName());

    private static final long serialVersionUID = 1L;

    private String path;

    private ArrayList<OBO> imports;

    private HashMap<String, OBOTerm> terms;

    public OBO(String path) {
        this.path = path;

        imports = new ArrayList<OBO>();
        terms = new HashMap<String, OBOTerm>();

        // Strip off the URL details if they exist
        if(path.contains("http://")) {
            path = path.substring(path.lastIndexOf("/")+1).toLowerCase();
        }
        
        logger.log(Level.FINER, "Parsing OBO /obo/{0}", path);
        
        InputStream is = OBO.class.getResourceAsStream("/obo/" + path);
        
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);

        String curLine;
        OBOTerm curTerm = null;
        boolean processingTerms = false;

        try {
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
                    String value = curLine.substring(locationOfColon+1).trim().toLowerCase();

//                    System.out.println("Tag: " + tag);
//                    System.out.println("Value: " + value);
                    
                    if (tag != null && value != null) {
//                        String tag = splitLine[0].trim();
//                        String value = splitLine[1].trim();

                        if (tag.equals("import")) {
//							if(!value.contains("\\")) {
//								String parent = oboFile.getParent();
//								
//								if(parent != null)
//									value = parent + File.separator + value;
//							}

							//System.out.println(value);
                            
                            
                            imports.add(new OBO(value));
                        }
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
            if (term.getUnitName() != null) {
                term.addUnit(getTerm(term.getUnitName()));
            }
        }
    }

    public static OBO getOBO() {
        return new OBO("imagingMS.obo");
    }

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

//	@Override
//	@JsonIgnore
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> child = new Vector<TreeNode>();
//		child.addAll(imports);
//		child.addAll(terms.values());
//		
//		return child.elements();
//	}
//
//	@Override
//	@JsonIgnore
//	public boolean getAllowsChildren() {
//		return true;
//	}
//
//	@Override
//	@JsonIgnore
//	public TreeNode getChildAt(int childIndex) {
//		if(childIndex < imports.size())
//			return imports.get(childIndex);
//		
//		return (TreeNode) terms.values().toArray()[childIndex - imports.size()];
//	}
//
//	@Override
//	@JsonIgnore
//	public int getChildCount() {
//		return imports.size() + terms.size();
//	}
//
//	@Override
//	@JsonIgnore
//	public int getIndex(TreeNode node) {
//		if(node instanceof OBO)
//			return imports.indexOf(node);
//		
//		// TODO:
//		
//		return 0;
//	}
//
//	@Override
//	@JsonIgnore
//	public TreeNode getParent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	@JsonIgnore
//	public boolean isLeaf() {
//		// TODO Auto-generated method stub
//		return ((imports.size() + terms.size()) == 0);
//	}
    public String toString() {
        return path;
    }
}
