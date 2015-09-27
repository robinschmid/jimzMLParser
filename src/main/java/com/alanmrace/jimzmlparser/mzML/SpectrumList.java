package com.alanmrace.jimzmlparser.mzML;

import com.alanmrace.jimzmlparser.util.XMLHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;

public class SpectrumList extends MzMLContent implements Iterable<Spectrum>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private ArrayList<Spectrum> spectrumList;
	private DataProcessing defaultDataProcessingRef;
	
	public SpectrumList(int count, DataProcessing defaultDataProcessingRef) {
		spectrumList = new ArrayList<Spectrum>(count);
		this.defaultDataProcessingRef = defaultDataProcessingRef;
	}
	
	public SpectrumList(SpectrumList spectrumList, ReferenceableParamGroupList rpgList, DataProcessingList dpList, 
			SourceFileList sourceFileList, InstrumentConfigurationList icList) {
		this.spectrumList = new ArrayList<Spectrum>(spectrumList.size());
		
		for(Spectrum spectrum : spectrumList) 
			this.spectrumList.add(new Spectrum(spectrum, rpgList, dpList, sourceFileList, icList));
		
		if(spectrumList.defaultDataProcessingRef != null && dpList != null) {
			for(DataProcessing dp : dpList) {
				if(spectrumList.defaultDataProcessingRef.getID().equals(dp.getID())) {
					defaultDataProcessingRef = dp;
				}
			}
		}
	}
	
	public void setDefaultDataProcessingRef(DataProcessing dp) {
		this.defaultDataProcessingRef = dp;
	}
	
	public DataProcessing getDefaultDataProcessingRef() {
		return defaultDataProcessingRef;
	}
	
	public int size() {
		return spectrumList.size();
	}
	
	public void addSpectrum(Spectrum spectrum) {
		spectrum.setParent(this);
		
		spectrumList.add(spectrum);
	}
	
	public Spectrum getSpectrum(int index) {
		if(index >= spectrumList.size())
			return null;
		
		return spectrumList.get(index);
	}
        
        public Spectrum getSpectrum(String id) {
            for(Spectrum spectrum : spectrumList)
                if(spectrum.getID().equals(id))
                    return spectrum;
            
            return null;
        }
	
	public void removeSpectrum(Spectrum spectrum) {
		spectrumList.remove(spectrum);
	}
	
	public void outputXML(BufferedWriter output, int indent) throws IOException {
		MzMLContent.indent(output, indent);
		output.write("<spectrumList");
		output.write(" count=\"" + spectrumList.size() + "\"");
		output.write(" defaultDataProcessingRef=\"" + XMLHelper.ensureSafeXML(defaultDataProcessingRef.getID()) + "\"");
		output.write(">\n");
		
		int index = 0;
		
		for(Spectrum spectrum : spectrumList)
			spectrum.outputXML(output, indent+1, index++);
		
		MzMLContent.indent(output, indent);
		output.write("</spectrumList>\n");
	}

	@Override
	public Iterator<Spectrum> iterator() {
		return spectrumList.iterator();
	}
	
	public String toString() {
		return "spectrumList: defaultDataProcessingRef=\"" + defaultDataProcessingRef.getID() + "\"";
	}
	
//	@Override
//	public int getChildCount() {
//		// 
//		return size();
//	}
//	
//	@Override
//	public int getIndex(TreeNode childNode) {
//		return spectrumList.indexOf(childNode);
//	}
//	
//	@Override
//	public TreeNode getChildAt(int index) {
//		return spectrumList.get(index);
//	}
//	
//	@Override
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> children = new Vector<TreeNode>();
//		children.addAll(spectrumList);
//		
//		return children.elements();
//	}
}
