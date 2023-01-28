package com.cgb.http;

import java.io.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;
import com.fasterxml.jackson.databind.*;

public class CgHttpClient {

    private HttpClient client = HttpClient.newHttpClient();
    
    public static void main(String [] args) throws Exception {
	
	CgHttpClient cgHttpClient = new CgHttpClient();

	Headers headers = cgHttpClient.loadHeaders(".");
	Set<Spec> specs = cgHttpClient.loadSpecs(".", headers);

	Set<HttpResponse<String>> results = specs.stream()
	    .map(spec -> cgHttpClient.submit(spec))
	    .collect(Collectors.toSet());

	results.stream().forEach(response -> {
		System.out.println("Status: " + response.statusCode());
	});
    }

    public Headers loadHeaders(String dir) throws IOException {
	ObjectMapper mapper = new ObjectMapper();
	return Stream.of(new File(dir).listFiles())
	    .filter(file -> file.isFile()
		    && file.getName().equals("headers.json"))

	    .map(file -> parseHeaders(file, mapper))
	    .findFirst()
	    .orElse(new Headers());
    }
    
    public Set<Spec> loadSpecs(String dir, Headers headers)
	throws IOException {
	ObjectMapper mapper = new ObjectMapper();

	Set<Spec> specs = Stream.of(new File(dir).listFiles())
	    .filter(file -> !file.isDirectory())
	    .filter(file -> file.getName().endsWith(".json"))
	    .filter(file -> !file.getName().equals("headers.json"))
	    .map(file -> parseFileSpec(file, mapper, headers))
	    .collect(Collectors.toSet());

	// Get requests with duplicate names:
	Map<String, Long> duplicates = specs.stream()
            .collect(Collectors.collectingAndThen(Collectors.groupingBy(Spec::getRequestName, Collectors.counting()),
				       m -> { m.values().removeIf(v -> v <= 1L); return m; }));
	
	if (!duplicates.isEmpty()) {
	    System.out.println("Found requests with duplicate names: " + duplicates);
	    specs.clear();
	}

	return specs;
    }

    private Headers parseHeaders(File file,
					     ObjectMapper mapper) {
	try {
	    return mapper.readValue(file, Headers.class);
	} catch(IOException ioe) {
	    throw new RuntimeException(ioe);
	}
    }
    
    private Spec parseFileSpec(File file, ObjectMapper mapper,
			       Headers headers) {
	try {
	    Spec spec = mapper.readValue(file, Spec.class);
	    spec.mergeHeaders(headers);
	    return spec;
	} catch(IOException ioe) {
	    throw new RuntimeException(ioe);
	}
    }
    
    private Set<File> listFiles(String dir) {
	return Stream.of(new File(dir).listFiles())
	    .filter(file -> !file.isDirectory())
	    .filter(file -> file.getName().endsWith(".json"))
	    .collect(Collectors.toSet());
    }

    public HttpResponse<String> submit(Spec spec) {
	try {
	    HttpRequest request = spec.buildRequest();
	    return client.send(request, BodyHandlers.ofString());
	} catch(Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
}
