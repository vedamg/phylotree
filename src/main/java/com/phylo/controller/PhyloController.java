package com.phylo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

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
    @GetMapping("/align")
    public String align(@RequestParam(value = "tool", defaultValue = "mafft") String tool) throws Exception {

        String input = "input.fasta";
        String output = "aligned.fasta";

        File inFile = new File(input);
        if (!inFile.exists()) {
            return "ERROR: input.fasta not found";
        }

        File outFile = new File(output);
        if (outFile.exists()) outFile.delete();

        ProcessBuilder pb;

        if (tool.equalsIgnoreCase("muscle")) {
            pb = new ProcessBuilder("muscle", "-align", input, "-output", output);
        } else {
            pb = new ProcessBuilder("mafft", "--auto", input);
            pb.redirectOutput(outFile);
            pb.redirectError(new File("mafft.log"));
        }

        Process process = pb.start();
        process.waitFor();

        if (!outFile.exists() || outFile.length() == 0) {
            return "ERROR: alignment failed";
        }

        return "ALIGNMENT SUCCESS";
    }

    // -------- TREE --------
    @GetMapping("/tree")
    public String tree() throws Exception {
        return buildTree("nj");
    }

    // -------- SIMILARITY TREE (NEW FIX) --------
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
            // FastTree doesn't truly support UPGMA → fallback
            pb = new ProcessBuilder("FastTree", "-noml", "aligned.fasta");
        } else {
            pb = new ProcessBuilder("FastTree", "aligned.fasta");
        }

        pb.redirectErrorStream(true);

        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

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