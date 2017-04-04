package com.alanmrace.jimzmlparser.mzml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class SelectedIon extends MzMLContentWithParams implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static String ionSelectionAttributeID = "MS:1000455"; // Required child (1+)

    public SelectedIon() {
        super();
    }

    public SelectedIon(SelectedIon si, ReferenceableParamGroupList rpgList) {
        super(si, rpgList);
    }

    @Override
    public ArrayList<OBOTermInclusion> getListOfRequiredCVParams() {
        ArrayList<OBOTermInclusion> required = new ArrayList<OBOTermInclusion>();
        required.add(new OBOTermInclusion(ionSelectionAttributeID, false, true, false));

        return required;
    }

    @Override
    public void outputXML(BufferedWriter output, int indent) throws IOException {
        MzMLContent.indent(output, indent);
        output.write("<selectedIon>\n");

        super.outputXML(output, indent + 1);

        MzMLContent.indent(output, indent);
        output.write("</selectedIon>\n");
    }

    @Override
    public String toString() {
        return "selectedIon";
    }

    @Override
    public String getTagName() {
        return "selectedIon";
    }

}
