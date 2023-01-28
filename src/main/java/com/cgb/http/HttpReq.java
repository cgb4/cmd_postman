package com.cgb.http;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;

public class HttpReq {
    private String type;
    private String url;
    private JsonNode jsonBody;
    private Map<String, String> headers;

    public HttpReq() {
	headers = new HashMap<>();
    }
    
    public String getType() {
	return type;
    }

    public String getUrl() {
	return url;
    }

    public JsonNode getJsonBody() {
	return jsonBody;
    }

    public Map<String, String> getHeaders() {
	return headers;
    }

    public void mergeHeaders(Headers oHeaders) {
	if (oHeaders == null) {
	    return;
	} else {
	    oHeaders.getHeaders()
		.entrySet()
		.stream()
		.filter(e -> !headers.containsKey(e.getKey()))
		.forEach(e -> { headers.put(e.getKey(), e.getValue()); });
	}
    }
    
    public String toString() {
	StringBuilder sb = new StringBuilder();
	if (headers != null) {
	    sb.append("\nHeaders:\n" + headers.entrySet().stream()
		      .map(e -> "  " + e.getKey() + " " + e.getValue())
		      .collect(Collectors.joining("\n")));
	}
	sb.append("\nJsonBody:\n  ");
	sb.append(jsonBody);
	return sb.toString();
    }

    public boolean isGet() {
	return type.equals("GET");
    }

    public boolean isPost() {
	return type.equals("POST");
    }

    public boolean isPut() {
	return type.equals("PUT");
    }

    public boolean isDelete() {
	return type.equals("DELETE");
    }

    public HttpRequest buildRequest() {

	try {
	    HttpRequest.Builder builder = HttpRequest
		.newBuilder()
		.version(Version.HTTP_1_1)
		.timeout(Duration.ofSeconds(20))
		.uri(new URI(getUrl()));
	    
	    if (isGet()) {
		builder.GET();
	    } else if (isPost()) {
		builder.POST(BodyPublishers.ofString(jsonBody.toString()));
	    } else if (isPut()) {
		builder.PUT(BodyPublishers.ofString(jsonBody.toString()));
	    } else if (isDelete()) {
		builder.DELETE();
	    } else {
		throw new RuntimeException("Unsupported type: "
					   + getType());
	    }
	    return builder.build();
	} catch(URISyntaxException use) {
	    throw new RuntimeException(use);
	}
    }    
}
