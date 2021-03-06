package com.alanmrace.jimzmlparser.mzml;

import com.alanmrace.jimzmlparser.exceptions.InvalidXPathException;
import com.alanmrace.jimzmlparser.exceptions.UnfollowableXPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class implementing the basic functionality for a list tag in MzML.
 *
 * @author Alan Race
 * @param <T> The type of MzMLTag within this list.
 *
 * @see MzMLIDContentList
 * @see BinaryDataArrayList
 * @see CVList
 * @see DataProcessing
 * @see PrecursorList
 * @see ProductList
 * @see ReferenceableParamGroupList
 * @see SampleList
 * @see SelectedIonList
 * @see SourceFileRefList
 * @see TargetList
 */
public abstract class MzMLContentList<T extends MzMLTag>
        extends MzMLContentWithChildren implements MzMLTagList<T> {

    /**
     * The list of MzMLTags.
     */
    protected List<T> list = null;

    /**
     * Create an empty list, used by subclasses.
     */
    protected MzMLContentList() {
//        list = new ArrayList<T>();
    }

    /**
     * Create an empty list with the specified initial capacity.
     *
     * @param count Initial capacity
     */
    public MzMLContentList(int count) {
//        list = new ArrayList<T>(count);
    }

    /**
     * Copy constructor for list, shallow copy.
     *
     * @param contentList List to copy
     */
    public MzMLContentList(MzMLContentList<T> contentList) {
        this(contentList.size());

        for (T item : contentList) {
            add(item);
        }
    }

    @Override
    public void add(T item) {
        if (item instanceof MzMLContent) {
            item.setParent(this);
        }
        
        if (list instanceof ArrayList) {
            list.add(item);
        } else if (list != null) {
            list = new ArrayList<T>(list);
            list.add(item);
        } else {
            list = Collections.singletonList(item);
        }
    }

    @Override
    public T get(int index) {
        if(list == null)
            return null;
        
        return list.get(index);
    }

    @Override
    public T remove(int index) {
        if(list == null)
            return null;
        
        return list.remove(index);
    }
    
    @Override
    public boolean remove(T item) {
        if(list == null)
            return false;
        
        return list.remove(item);
    }

    @Override
    public int indexOf(T item) {
        if(list == null)
            return -1;
        
        return list.indexOf(item);
    }

    @Override
    public int size() {
        if(list == null)
            return 0;
        
        return list.size();
    }

    @Override
    public void addChildrenToCollection(Collection<MzMLTag> children) {
        if (list != null) {
            children.addAll(list);
        }
    }

    @Override
    protected void addTagSpecificElementsAtXPathToCollection(Collection<MzMLTag> elements, String fullXPath, String currentXPath) throws InvalidXPathException {
        if (!fullXPath.equals(currentXPath) && list == null) {
            throw new UnfollowableXPathException("No " + getTagName() + " exists, so cannot go to " + fullXPath, fullXPath, currentXPath);
        }
        
        if (size() > 0) {
            // Get the first element from the list so that we can use it to get 
            // the name of the tag.
            T firstElement = list.get(0);

            if (currentXPath.startsWith("/" + firstElement.getTagName())) {
                for (T item : list) {
                    item.addElementsAtXPathToCollection(elements, fullXPath, currentXPath);
                }
            }
        }
    }

    @Override
    public String getXMLAttributeText() {
        String attributeText = super.getXMLAttributeText();
        
        if(!attributeText.isEmpty())
            attributeText += " ";
        
        return attributeText + "count=\"" + size() + "\"";
    }

//    @Override
//    protected void outputXMLContent(MzMLWritable output, int indent) throws IOException {
//        int counter = 0;
//        
//        for (T item : this) {
//            // 0 index for indexed content, but 1 for ordered content
//            if(item instanceof MzMLIndexedContentWithParams)
//                ((MzMLIndexedContentWithParams) item).outputXML(output, indent, counter++);
//            else if(item instanceof MzMLOrderedContentWithParams)
//                ((MzMLOrderedContentWithParams) item).outputXML(output, indent, ++counter);
//            else
//                item.outputXML(output, indent);
//        }
//    }

    @Override
    public String toString() {
        return getTagName();
    }

    @Override
    public Iterator<T> iterator() {
        if(list == null)
            return Collections.emptyIterator();
        
        return list.iterator();
    }

    @Override
    public boolean contains(T item) {
        if(list == null)
            return false;
        
        return list.contains(item);
    }
    
    @Override
    public void clear() {
        if(list != null)
            list.clear();
    }
}
