package com.alanmrace.jimzmlparser.data;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Description of a full set of steps applied to transform data stored as a 
 * double[] to an optionally compressed, optionally modified data type byte[]. 
 * 
 * @author Alan Race
 */
public class DataTransformation {

    /**
     * The set of DataTransform instances that make up the DataTransformation.
     */
    protected List<DataTransform> transformation;
    
    /**
     * Create an empty DataTransformation.
     */
    public DataTransformation() {
        transformation = new ArrayList<DataTransform>();
    }
    
    /**
     * Add a DataTransform to the DataTransformation. This will be added to the 
     * end of the list.
     * 
     * @param transform DataTransform to add
     */
    public void addTransform(DataTransform transform) {
        transformation.add(transform);
    }
    
    /**
     * Perform all steps of the DataTransformation sequentially to the supplied 
     * data, after first converting the double[] to a byte[] using 
     * {@link DataTypeTransform#convertDoublesToBytes(double[])}. 
     *  
     * @param data Data to perform the DataTransformation on
     * @return Transformed data
     * @throws DataFormatException Issue with the transformation
     */
    public byte[] performForwardTransform(double[] data) throws DataFormatException {
        byte[] byteData = DataTypeTransform.convertDoublesToBytes(data);
        
        return performForwardTransform(byteData);
    }
    
    /**
     * Perform all steps of the DataTransformation sequentially to the supplied 
     * data.
     * 
     * @param data Data to perform the DataTransformation on
     * @return Transformed data
     * @throws DataFormatException Issue with the transformation
     */
    public byte[] performForwardTransform(byte[] data) throws DataFormatException {
        byte[] transformedData = data;
        
        for(DataTransform transform : transformation) {
            transformedData = transform.forwardTransform(transformedData);
        }
        
        return transformedData;
    }
    
    /**
     * Perform all steps of the DataTransformation in reverse to the supplied 
     * data, and then convert the resulting byte[] to a double[] using 
     * {@link DataTypeTransform#convertDataToDouble(byte[], com.alanmrace.jimzmlparser.data.DataTypeTransform.DataType)}.
     * 
     * @param data Data to perform the reverse of the DataTransformation on
     * @return Transformed data 
     * @throws DataFormatException Issue with the transformation
     */
    public double[] performReverseTransform(byte[] data) throws DataFormatException {
        byte[] transformedData = data;
        
        for(DataTransform transform : transformation) {
            transformedData = transform.reverseTransform(transformedData);
        }
        
        return DataTypeTransform.convertDataToDouble(transformedData, DataTypeTransform.DataType.Double);
    }
}