package org.myorg;

import org.w3c.dom.ls.LSInput;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpServer {
    public static void run() throws IOException {
        ApiJava apiJava = new ApiJava();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        Socket clientSocket = null;
        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;

            boolean firstLine = true;
            String path = "/";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibí: " + inputLine);
                if (firstLine) {
                    path = inputLine.split(" ")[1];
                    firstLine = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            Service service = new Service() {
                @Override
                public String getBody() {
                    return "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "    <head>\n" +
                            "        <title>Form Example</title>\n" +
                            "        <meta charset=UTF-8>\n" +
                            "        <meta name=viewport content=width=device-width, initial-scale=1.0>\n" +
                            "    </head>\n" +
                            "    <body>\n" +
                            "        <h1>Form with GET</h1>\n" +
                            "        <form action=/consulta>\n" +
                            "            <label for=name>Name:</label><br>\n" +
                            "            <input type=text id=command><br><br>\n" +
                            "            <input type=button value=Submit onclick=loadGetMsg()>\n" +
                            "        </form> \n" +
                            "        <div id=getrespmsg></div>\n" +
                            "        <script>\n" +
                            "            function loadGetMsg(){\n" +
                            "                let command = document.getElementById(\"command\");\n" +
                            "                let url = \"/consulta?command=\" + command.value;\n" +
                            "                fetch (url, {method: 'GET'})\n" +
                            "                    .then(x => x.text())\n" +
                            "                    .then(y => document.getElementById(\"getrespmsg\").innerHTML = y);\n" +
                            "            }\n" +
                            "        </script>\n" +
                            "    </body>\n" +
                            "</html>";
                }

                @Override
                public List<String> getHeader() {
                    return List.of("HTTP/1.1 200 Ok", "Content-Type: text/html");
                }
            };

            Object outputLine = null;
            List<String> header = new ArrayList<>();

            if (path.startsWith("/consulta")) {
                header = List.of("HTTP/1.1 200 Ok", "Content-Type: text/plain");
                String query = path.split("=")[1];
                if (query.startsWith("Class")) {
                    String param = path.split("Class")[1];
                    outputLine = apiJava.Class(param.substring(1, param.indexOf(")")));
                } else if (query.startsWith("invoke")) {
                    String param = path.split("invoke")[1];
                    List<String> paramsFunction = Stream.of(param.split(","))
                            .map((String parameter) -> parameter.replace("(", ""))
                            .map((String parameter) -> parameter.replace(")", ""))
                            .map((String parameter) -> parameter.replace("%20", ""))
                            .map((String parameter) -> parameter.replace("%22", ""))
                            .collect(Collectors.toList());

                    outputLine = apiJava.invoke(paramsFunction.get(0),paramsFunction.get(1));
                } else if (query.startsWith("unaryInvoke") &&
                        path.split("unaryInvoke")[1].split(",").length > 4) {
                    String param = path.split("unaryInvoke")[1];
                    List<String> paramsFunction = Stream.of(param.split(","))
                            .map((String parameter) -> parameter.replace("(", ""))
                            .map((String parameter) -> parameter.replace(")", ""))
                            .map((String parameter) -> parameter.replace("%20", ""))
                            .map((String parameter) -> parameter.replace("%22", ""))
                            .collect(Collectors.toList());

                    outputLine = apiJava.unaryInvoke(paramsFunction.get(0), paramsFunction.get(1),
                            paramsFunction.get(2), paramsFunction.get(3));
                } else if (query.startsWith("binaryInvoke") &&
                        path.split("binaryInvoke")[1].split(",").length > 6) {
                    String param = path.split("binaryInvoke")[1];
                    List<String> paramsFunction = Stream.of(param.split(","))
                            .map((String parameter) -> parameter.replace("(", ""))
                            .map((String parameter) -> parameter.replace(")", ""))
                            .map((String parameter) -> parameter.replace("%20", ""))
                            .map((String parameter) -> parameter.replace("%22", ""))
                            .collect(Collectors.toList());

                    outputLine = apiJava.binaryInvoke(paramsFunction.get(0), paramsFunction.get(1),
                            paramsFunction.get(2), paramsFunction.get(3), paramsFunction.get(4), paramsFunction.get(5));
                }
            } else if (path.equals("/")){
                header = service.getHeader();
                outputLine = service.getBody();
            }

            for (String line : header) {
                out.println(line);
            }
            out.println();

            out.println("{ \"Data\":" + outputLine + " }");

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
}
