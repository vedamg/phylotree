package com.phylo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Scanner;

@RestController
@RequestMapping("/api")
public class PhyloController {

    private final String WORK_DIR = "/app/";

    // Step 1: Handle Input (UniProt fetch or Manual Paste)
    @GetMapping("/fasta")
    public String getFasta(@RequestParam String data, @RequestParam String method) {
        try {
            StringBuilder content = new StringBuilder();
            if ("uniprot".equalsIgnoreCase(method)) {
                String[] ids = data.split("[,\\s\\n]+");
                for (String id : ids) {
                    if (id.trim().isEmpty()) continue;
                    URL url = new URL("https://rest.uniprot.org/uniprotkb/" + id.trim() + ".fasta");
                    try (Scanner s = new Scanner(url.openStream())) {
                        while (s.hasNextLine()) content.append(s.nextLine()).append("\n");
                    }
                }
            } else {
                content.append(data.replace("\\n", "\n").replace("\r", ""));
            }

            Files.write(Paths.get(WORK_DIR + "input.fasta"), content.toString().getBytes());
            return content.toString(); 
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Step 2: Run Pipeline (Align -> Tree)
    @GetMapping("/runPipeline")
    public ResponseEntity<String> runPipeline(@RequestParam String tool) {
        try {
            // Alignment Step
            String alignCmd = tool.equalsIgnoreCase("mafft") 
                ? "mafft input.fasta > aligned.fasta" 
                : "muscle -in input.fasta -out aligned.fasta";
            
            Process alignProc = new ProcessBuilder("/bin/sh", "-c", alignCmd).directory(new File(WORK_DIR)).start();
            if (alignProc.waitFor() != 0) return ResponseEntity.status(500).body("Alignment tool failed.");

            // Tree Step
            Process treeProc = new ProcessBuilder("/bin/sh", "-c", "fasttree aligned.fasta > tree.nwk")
                .directory(new File(WORK_DIR)).start();
            if (treeProc.waitFor() != 0) return ResponseEntity.status(500).body("FastTree failed.");

            String nwk = new String(Files.readAllBytes(Paths.get(WORK_DIR + "tree.nwk")));
            return ResponseEntity.ok(nwk);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Pipeline Error: " + e.getMessage());
        }
    }
}
