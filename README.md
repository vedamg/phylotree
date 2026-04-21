PhyloTree Web App

Live Website: https://treephylo-app.onrender.com

Introduction
PhyloTree is a web-based bioinformatics application designed to perform multiple sequence alignment and generate phylogenetic trees from biological sequence data.
The application allows users to input either raw FASTA sequences or UniProt IDs, processes them through established bioinformatics tools, and visualizes evolutionary relationships directly in the browser.
This project demonstrates the integration of computational biology tools with web technologies**, making sequence analysis accessible through a simple and interactive interface.

 What the Application Does
1. Accepts input from the user:
   - Raw FASTA sequences  
   - UniProt IDs (automatically fetches sequences)

2. Converts input into a FASTA file

3. Performs multiple sequence alignment using:
   - MAFFT  
   - MUSCLE  

4. Constructs a phylogenetic tree using:
   - FastTree  

5. Displays:
   - Newick tree format  
   - Visual tree rendered in the browser

Workflow (Pipeline)
User Input → FASTA Generation → Sequence Alignment → Tree Construction → Visualization → Download

Step-by-step:

- Input is saved as `input.fasta`  
- Alignment produces `aligned.fasta`  
- FastTree generates `tree.nwk`  
- Tree is rendered using D3.js  

Tools & Technologies Used:

Backend
- Java 17  
- Spring Boot (REST API framework)

Frontend
- HTML  
- JavaScript (Fetch API for backend communication and logic)  
- D3.js (phylogenetic tree visualization)  
- Basic CSS (embedded styling for layout)

  Bioinformatics Tools
- **MAFFT** → multiple sequence alignment  
- **MUSCLE** → alternative alignment method  
- **FastTree** → phylogenetic tree construction

Deployment
- Docker (containerization)  
- Render (cloud deployment)

API Endpoints:

1. Fetch / Save Sequences
GET /api/fasta?data=INPUT&method=raw|uniprot
- `data` → FASTA sequences OR UniProt IDs  
- `method`:
  - `raw` → uses input directly  
  - `uniprot` → fetches sequences from UniProt API

2. Run Full Pipeline
GET /api/runPipeline?tool=mafft|muscle
- `tool`:
  - `mafft` → uses MAFFT for alignment
  -  - `muscle` → uses MUSCLE
Returns:
- Phylogenetic tree in Newick format

How It Was Implemented:

Backend Logic
- Spring Boot REST controller handles API requests
  
- Input is written to:
/app/input.fasta
- Alignment commands executed:
mafft input.fasta > aligned.fasta
muscle -align input.fasta -output aligned.fasta

- Tree generation:
fasttree aligned.fasta > tree.nwk
- Output file is read and returned to frontend  

UniProt Integration

- Uses UniProt REST API:
https://rest.uniprot.org/uniprotkb/{id}.fasta
Fetches sequences dynamically for given IDs  

Frontend Logic:
- User input captured from textarea  
- API calls performed using `fetch()`  
- Newick tree parsed into hierarchical structure  
- Tree rendered using D3.js

Docker Setup
The application is containerized using a multi-stage Docker build.

Build Stage
- Uses Maven to compile the project

Deployment
- Deployed on Render using Docker  
- Connected to GitHub repository  
- Automatic builds on push

Limitations:
- FastTree provides approximate phylogenies  
- Not suitable for high-precision evolutionary analysis  
- Performance may be limited on free-tier hosting  
- Large datasets may take longer to process  

Future Improvements:
- Interactive tree (zoom, collapse nodes)  
- Clade-based coloring  
- Support for larger datasets


