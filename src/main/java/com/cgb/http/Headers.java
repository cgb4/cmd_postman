package com.cgb.http;

import java.util.Map;
import java.util.stream.Collectors;

public class Headers {

    private String collectionName;
    private Map<String, String> headers;

    public String getCollectionName() {
	return collectionName;
    }

    public Map<String, String> getHeaders() {
	return headers;
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("\nCollectionName: ");
	sb.append(getCollectionName());
	if (headers != null) {
	    sb.append("\nHeaders:\n" + headers.entrySet().stream()
		      .map(e -> "  " + e.getKey() + " " + e.getValue())
		      .collect(Collectors.joining("\n")));
	}
	return sb.toString();
    }
}
