package com.alanmrace.jimzmlparser.exceptions;

/**
 * Exception occurred during the parsing of an mzML file, due to an 
 * invalid mzML file.
 * 
 * @author Alan Race
 */
public class InvalidMzML extends RuntimeException implements ParseIssue {

    /**
     * Serialisation version ID.
     */
    private static final long serialVersionUID = -5265931318748556126L;

    /**
     * Set up InvalidMzML with a message.
     * 
     * @param message Description of the exception
     */
    public InvalidMzML(String message) {
        super(message);
    }

    @Override
    public String getIssueTitle() {
        return this.getMessage();
    }

    @Override
    public String getIssueMessage() {
        return this.getMessage();
    }

    @Override
    public IssueLevel getIssueLevel() {
        return IssueLevel.SEVERE;
    }
}
