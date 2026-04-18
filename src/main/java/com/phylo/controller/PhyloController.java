package com.phylo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.springframework.http.ResponseEntity; // ADDED: Missing import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PhyloController {

    // -------- SAVE FASTA --------
    @GetMapping("/fasta")
    public String fasta(@RequestParam("data") String data) throws Exception {
        String content = data.replace("\\n", "\n").replace("\r", "");
        FileWriter writer = new FileWriter("input.fasta", false);
        writer.write(content);
        writer.close();
        return "FASTA saved";
    }

    // -------- ALIGN --------
    @GetMapping("/align") // ADDED: Missing annotation
    public ResponseEntity<String> align(@RequestParam String tool) { // FIXED: Was "blic"
        try {
            // Use /bin/sh -c to handle the file redirection '>'
            String command = tool.equalsIgnoreCase("mafft") 
                ? "mafft input.fasta > aligned.fasta" 
                : "muscle -in input.fasta -out aligned.fasta";

            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            pb.directory(new File("/app")); 
            Process process = pb.start();
            
            int exitCode = process.waitFor(); 
            
            if (exitCode == 0) {
                return ResponseEntity.ok("Alignment Finished");
            } else {
                return ResponseEntity.status(500).body("Alignment tool failed with exit code " + exitCode);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Java Error: " + e.getMessage());
        }
    }

    // -------- TREE --------
    @GetMapping("/tree")
    public String tree() throws Exception {
        return buildTree("nj");
    }

    // -------- SIMILARITY TREE --------
    @GetMapping("/similarityTree")
    public String similarityTree(@RequestParam(defaultValue = "nj") String method) throws Exception {
        return buildTree(method);
    }

    // -------- CORE TREE BUILDER --------
    private String buildTree(String method) throws Exception {
        File aligned = new File("aligned.fasta");

        if (!aligned.exists() || aligned.length() == 0) {
            return "ERROR: aligned.fasta missing";
        }

        ProcessBuilder pb;
        if (method.equalsIgnoreCase("upgma")) {
            pb = new ProcessBuilder("FastTree", "-noml", "aligned.fasta");
        } else {
            pb = new ProcessBuilder("FastTree", "aligned.fasta");
        }

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder tree = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains("(")) {
                tree.append(line);
            }
        }
        process.waitFor();
        return tree.toString();
    }
}
