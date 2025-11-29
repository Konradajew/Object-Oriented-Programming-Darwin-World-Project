# Darwin World Simulator

**Object-Oriented Programming Course — Group 4 Project**  
**Authors:** Kajetan Frątczak & Konrad Szymański  
**AGH University of Science and Technology, 2024/25**

A beautiful, multi-threaded natural selection simulator written in **Java** with **JavaFX**.  
Watch hundreds of creatures evolve in real time: eat, reproduce, mutate, age, avoid deadly fires — all while multiple independent simulations run in parallel!

![Simulation Demo](demo.gif)

## Features

- Fully object-oriented evolutionary model (genes, mutations, inheritance)
- Multi-threaded — run many simulations simultaneously
- Independent pause/resume for each simulation
- Detailed real-time statistics (global map + selected animal)
- Random deadly fire events
- Smooth, responsive JavaFX interface
- All graphics AI-generated

## Implemented World Variants (G4)

### Globe Map (mandatory)
- Left/right edges wrap around (torus-style)
- Top/bottom edges are impassable poles — animals bounce back and reverse direction when trying to cross

### Forested Equator (mandatory)
- Plants preferentially spawn in a horizontal central belt simulating the equator and tropics

### Bountiful Crops [G]
- Plants grow uniformly, but 20% of the map is a “fertile zone”
- Occasionally spawns large 2×2 plants that give significantly more energy
- Multiple animals competing for a large plant are resolved the same way as for regular grass

### Old Age Penalty [4] — “Old age is no joy”
- Older animals move slower
- Every few turns they skip their move (but still lose energy)
- Skip probability increases with age, up to a maximum of **80%**

## Screenshots

### Start Configuration
![Initial parameters](screenshots/start.png)

### Global Map Statistics
![Map stats](screenshots/map_stats.png)

### Selected Animal Details
![Animal statistics](screenshots/animal_stats.png)

### Running Simulation
![Simulation in action](screenshots/simulation.jpg)

## How to Run

```bash
git clone https://github.com/matwoj8/Darwin_World_Simulation.git
cd Darwin_World_Simulation
./gradlew run
