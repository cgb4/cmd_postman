package com.cgb.http;

import java.util.*;

import java.util.stream.Collectors;
import java.io.IOException;
import java.net.http.*;

public class HttpCLI {

    public static void main(String [] args) {
	HttpCLI cli = new HttpCLI();
	cli.run();
    }

    private String PROMPT = "HCL> ";
    private Scanner scanner;
    private CgHttpClient cgHttpClient;
    private Headers headers;
    private Set<Spec> specs;
    private Map<String, List<String>> aliases;
    
    private HttpCLI() {

	aliases = new HashMap<>();
	
	cgHttpClient = new CgHttpClient();
	specs = new HashSet<>();
	
	try {
  
            scanner = new Scanner(System.in);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
    
    private void run() {

	String cmdTxt = "";

	while (!cmdTxt.equalsIgnoreCase("quit")
	       && !cmdTxt.equalsIgnoreCase("done")
	       && !cmdTxt.equalsIgnoreCase("exit")) {
	    
	    System.out.print(PROMPT);

	    cmdTxt = scanner.nextLine().trim();

	    processCmd(cmdTxt);
	}
    }

    private void processCmd(String cmdTxt) {

	try {
	    List<String> args = Arrays.asList(cmdTxt.split(" "));

	    if (!args.isEmpty()) {

		processCmdList(args);
		/*
		String cmd = args.get(0);
		if (cmd.equalsIgnoreCase("alias")) {
		    handleAlias(args);
		} else if (cmd.equalsIgnoreCase("list")) {
		    handleList(args);
		} else if (cmd.equalsIgnoreCase("load")) {
		    handleLoad(args);
		} else if (cmd.equalsIgnoreCase("submit")) {
		    handleSubmit(args);
		} else if (cmd.equalsIgnoreCase("show")) {
		    handleShow(args);
		} else if (cmd.equalsIgnoreCase("exit")
			   || cmd.equalsIgnoreCase("quit")) {
		} else if (aliases.containsKey(cmd)) {
		    processCmd(aliases.get(cmd));
		} else {
		    System.out.println("Unknown command: " + args);	    
		}
		*/
	    }
	} catch(Exception e) {
	    System.out.println("Error: " + e);
	}
    }

    private void processCmdList(List<String> cmdList) throws IOException {
	String cmd = cmdList.get(0);
	if (cmd.equalsIgnoreCase("alias")) {
	    handleAlias(cmdList);
	} else if (cmd.equalsIgnoreCase("list")) {
	    handleList(cmdList);
	} else if (cmd.equalsIgnoreCase("load")) {
	    handleLoad(cmdList);
	} else if (cmd.equalsIgnoreCase("submit")) {
	    handleSubmit(cmdList);
	} else if (cmd.equalsIgnoreCase("show")) {
	    handleShow(cmdList);
	} else if (cmd.equalsIgnoreCase("exit")
		   || cmd.equalsIgnoreCase("quit")) {
	} else if (aliases.containsKey(cmd)) {
	    processCmdList(aliases.get(cmd));
	} else {
	    System.out.println("Unknown command: " + cmdList);	    
	}
    }
    
    private void handleAlias(List<String> args) {
	if (args.size() > 1) {
	    if (args.get(1).indexOf("=") < 0) {
		System.out.println("Missing '='");
		return;
	    }
	    
	    // Add alias:
	    List<String> eqSplit = Arrays.asList(args.get(1).split("="));

	    if (eqSplit.size() < 2) {
		System.out.println("Invalid alias.");
		return;
	    }

	    
	    // args.set(0, eqSplit.get(1));
	    List<String> cmd = new ArrayList<>();

	    String first = eqSplit.get(1);

	    if (first.startsWith("\"")) {
		first = first.substring(1);
	    }
	    
	    cmd.add(first);
	    for (int i=2; i<args.size(); i++) {
		String token = args.get(i);
		if (i == args.size() - 1) {
		    if (token.endsWith("\"")) {
			args.set(i, token.substring(0, token.length() - 1));
		    }
		}
		
		cmd.add(args.get(i));
	    }
	    
	    aliases.put(eqSplit.get(0), cmd);
	} else {
	    System.out.println(aliases.entrySet()
			       .stream()
			       .map(e->e.getKey() + "=" + e.getValue())
			       .collect(Collectors.joining("\n")));
	}
    }
    
    private void handleList(List<String> args) {
	if (specs.isEmpty()) {
	    System.out.println("No requests loaded");
	} else if (args.size() > 1) {

	    String byArg = args.get(1);
	    
	    if (byArg.equalsIgnoreCase("by_name")) {
		System.out.println(specs.stream()
				   .map(Spec::getRequestName)
				   .collect(Collectors.joining("\n")));
	    } else if (byArg.equalsIgnoreCase("by_id")) {
		System.out.println(specs.stream()
				   .map(spec -> String.valueOf(spec.getId()))
				   .collect(Collectors.joining("\n")));
	    } else if (byArg.equalsIgnoreCase("by_all")) {
		System.out.println(specs.stream()
				   .map(Spec::getAll)
				   .collect(Collectors.joining("\n")));
	    }
	} else {
	    System.out.println(specs.stream()
			       .map(Spec::getRequestName)
			       .collect(Collectors.joining("\n")));
	}
    }

    private void handleLoad(List<String> args) throws IOException {
	if (args.size() > 1) {
	    String path = args.get(1);
	    headers = cgHttpClient.loadHeaders(path);
	    specs = cgHttpClient.loadSpecs(path, headers);
	} else {
	    System.out.println("Please specify a directory path");
	}
    }

    private void handleShow(List<String> args) {
	if (args.size() > 1) {
	    String name = args.get(1);
	    
	    Optional<Spec> option = specs.stream()
		.filter(spec -> spec.hasName(name))
		.findAny();
				       
	    if (option.isPresent()) {
		System.out.println(option.get().toString());
	    } else {
		System.out.println("No '" + name + "' request.");
	    }
	}
    }
    
    private void handleSubmit(List<String> args) {
	if (args.size() > 1) {

	    String name = args.get(1);
	    
	    Optional<Spec> optional = specs.stream()
		.filter(spec -> spec.hasName(name))
		.findFirst();

	    if (optional.isPresent()) {
		HttpResponse<String> response = cgHttpClient.submit(optional.get());

		System.out.println("Status: " + response.statusCode());
	    } else {
		System.out.println("No '" + name + "' spec exists.");
	    }
	}
    }
}
