package com.alanmrace.jimzmlparser.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.alanmrace.jimzmlparser.mzML.MzML;
import com.alanmrace.jimzmlparser.obo.OBO;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MzMLHandler extends MzMLHeaderHandler {

    private static final Logger logger = Logger.getLogger(MzMLHandler.class.getName());

    protected boolean processingBinary;

    protected File temporaryBinaryFile;
//    protected BinaryDataStorage dataStorage;
    protected DataOutputStream temporaryFileStream;
    protected StringBuffer binaryData;
    protected long offset = 0;

    byte[] temp;
    ArrayList<Byte> uncompressedData;

//	private int[] order;
    public MzMLHandler(OBO obo, File temporaryBinaryFile) throws FileNotFoundException {
        super(obo);

        binaryData = new StringBuffer();
        this.temporaryBinaryFile = temporaryBinaryFile;
        this.dataStorage = new BinaryDataStorage(temporaryBinaryFile);

        temporaryFileStream = new DataOutputStream(new FileOutputStream(temporaryBinaryFile));
    }

    public static MzML parsemzML(String filename) throws FileNotFoundException {
        OBO obo = new OBO("imagingMS.obo");

        File tmpFile = new File(filename.substring(0, filename.lastIndexOf('.')) + ".tmp");
        tmpFile.deleteOnExit();

        // Parse mzML
        MzMLHandler handler = new MzMLHandler(obo, tmpFile);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            File file = new File(filename);

            //parse the file and also register this class for call backs
            sp.parse(file, handler);

        } catch (SAXException | ParserConfigurationException | IOException se) {
            logger.log(Level.SEVERE, null, se);
        }

        handler.getmzML().setOBO(obo);

        return handler.getmzML();
    }

    public void deleteTemporaryFile() {
        try {
            temporaryFileStream.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }

        temporaryFileStream = null;
        temporaryBinaryFile.delete();
    }

//	public void setPixelOrder(int[] order) {
//		this.order = order;
//	}
//	
//	public void setIBDStream(DataOutputStream ibdStream, long offset) {
//		this.ibdStream = ibdStream;
//		this.offset = offset;
//	}
    //Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("binary")) {
            binaryData.setLength(0);

            processingBinary = true;
        } else {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (processingBinary) {
            binaryData.append(ch, start, length);
        } else {
            super.characters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("binary")) {
            // Convert the data from base 64
            byte[] processedData = DatatypeConverter.parseBase64Binary(binaryData.toString());

            int lengthToWrite = processedData.length;

            if (currentBinaryDataArray.isCompressed()) {
                Inflater decompressor = new Inflater();
                decompressor.setInput(processedData);

                if (uncompressedData == null) {
                    uncompressedData = new ArrayList<Byte>(2 ^ 20);
                }

                lengthToWrite = 0;
                int uncompressed = 0;

                if (temp == null) {
                    temp = new byte[1048576]; // 2^20
                }
                do {
                    try {
                        uncompressed = decompressor.inflate(temp);

                        for (int i = 0; i < uncompressed; i++) {
                            if (uncompressedData.size() <= lengthToWrite) {
                                uncompressedData.add(temp[i]);
                                lengthToWrite++;
                            } else {
                                uncompressedData.set(lengthToWrite++, temp[i]);
                            }
                        }
                    } catch (DataFormatException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }

                    //lengthToWrite += uncompressed;
                } while (uncompressed != 0);

                if (processedData.length < lengthToWrite) {
                    processedData = new byte[lengthToWrite];
                }

                for (int i = 0; i < lengthToWrite; i++) {
                    processedData[i] = uncompressedData.get(i);
                }
                //System.out.println("Processed: " + length);
                decompressor.end();
            }

            try {
                temporaryFileStream.write(processedData, 0, lengthToWrite);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            currentBinaryDataArray.setDataLocation(new DataLocation(dataStorage, offset, lengthToWrite));

//                        if(true)
//                            throw new RuntimeException("Removed code - won't work");
//			Binary binary = new Binary(dataStorage, offset, lengthToWrite, currentBinaryDataArray.getCVParamOrChild(BinaryDataArray.dataTypeID));
//			currentBinaryDataArray.setBinary(binary);
            offset += lengthToWrite;
            processingBinary = false;
        } else {
            super.endElement(uri, localName, qName);
        }
//		} else if(qName.equals("mzML"))
//			if(binaryDataStream != null) {
//				try {
//					binaryDataStream.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

    }
}
