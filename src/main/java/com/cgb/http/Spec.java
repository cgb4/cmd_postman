package com.cgb.http;

public class Spec extends HttpReq {
    private int id;
    private String collectionName;
    private String requestName;

    public Spec() {
    }

    public int getId() {
	return id;
    }

    public String getRequestName() {
	return requestName;
    }

    public String getCollectionName() {
	return collectionName;
    }

    public boolean hasName(String name) {
	return requestName.equalsIgnoreCase(name);
    }

    public String getAll() {
	StringBuilder sb = new StringBuilder();
	sb.append(String.valueOf(getId()));
	sb.append("  ");
	sb.append(getCollectionName());
	sb.append("  ");
	sb.append(getRequestName());
	return sb.toString();
    }
    
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Id: ");
	sb.append(String.valueOf(getId()));
	sb.append("\nCollectionName: ");
	sb.append(getCollectionName());
	sb.append("\nRequestName: ");
	sb.append(getRequestName());
	sb.append(super.toString());
	return sb.toString();
    }
}
