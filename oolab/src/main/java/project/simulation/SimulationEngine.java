package project.simulation;

import project.map.Vector2d;
import project.map.RectangularGrassField;
import project.mapElements.Animal;
import project.mapElements.BigGrass;
import project.mapElements.Grass;
import project.gui.ESimulationProperty;
import project.gui.SimulationPropertyFile;

import java.util.*;

public class SimulationEngine implements IEngine, Runnable{

    private static Random random = new Random();
    private final RectangularGrassField map;

    private int currentStep;
    private List<ISimulationStepObserver> observerList;
    private int moveDelay;
    private boolean paused;
    private int grassEnergy;
    private int bigGrassEnergy; // Tak dokładnie jest to energia wielkiej trawy
    private int reproductionEnergy;
    private int minMutations;
    private int maxMutations;

    private List<Integer> deadAnimalsAgeList;

    private SimulationPropertyFile simulationPropertyFile;

    public SimulationEngine(RectangularGrassField map, SimulationPropertyFile propertyFileLoader) {

        this.simulationPropertyFile = propertyFileLoader;
        this.map = map;
        this.observerList = new ArrayList<>();
        this.moveDelay = propertyFileLoader.getIntValue(ESimulationProperty.animationStepDelay);
        this.grassEnergy = propertyFileLoader.getIntValue(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny);
        this.bigGrassEnergy = propertyFileLoader.getIntValue(ESimulationProperty.energiaZapewnianiaPrzezZjedzenieWielkiejRosliny);
        this.reproductionEnergy = propertyFileLoader.getIntValue(ESimulationProperty.energiaRodzicowDoTworzeniaPotomka);
        this.minMutations = propertyFileLoader.getIntValue(ESimulationProperty.minimalnaLiczbaMutacji);
        this.maxMutations = propertyFileLoader.getIntValue(ESimulationProperty.maksymalnaLiczbaMutacji);
        this.currentStep = 0;
        this.paused = true;
        this.deadAnimalsAgeList = new ArrayList<>();
        initAnimals();
    }

    // Inicjalizacja zwierząt na mapie
    private void initAnimals() {
        int genomeLength = this.simulationPropertyFile.getIntValue(ESimulationProperty.dlugoscGenomuZwierzakow);
        int animalCount = this.simulationPropertyFile.getIntValue(ESimulationProperty.startowaLiczbaZwierzakow);
        int startEnergy = this.simulationPropertyFile.getIntValue(ESimulationProperty.startowaEnergiaZwierzakow);
        for (int i=0; i<animalCount; i++) {
            Vector2d position = map.generateRandomPosition();
            Animal animal = new Animal(map, position, genomeLength, startEnergy);
        }
    }

    @Override
    public void run() {
        do {
            List<Animal> animalList = map.getAnimals();
            List<Animal> animalsToDie = new ArrayList<>();
            for (Animal a : animalList) {
                if (a.getEnergy() <= 0) {
                    animalsToDie.add(a);
                    a.setDeathDay(currentStep);
                } else {
                    a.move(simulationPropertyFile);
                }
            }

            for (Animal a : animalsToDie) {
                deadAnimalsAgeList.add(currentStep-a.getBirthDay());
                animalList.remove(a);
                map.getMapElements().remove(a);
            }

            Map<Vector2d, Grass> grassMap = this.map.getPositionToGrassMap();
            Map<Vector2d, BigGrass> BigGrassMap = this.map.getPositionToBigGrassMap();

            Map<Vector2d, List<Animal>> animalMap = new HashMap<>();
            for (Animal animal: animalList) {
                animalMap.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>())
                        .add(animal);
            }

            for (Vector2d position : animalMap.keySet()) {
                List<Animal> animalsOnPosition = animalMap.get(position);
                // Normal grass
                if (grassMap.keySet().contains(position)) {
                    animalsOnPosition.sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                            .thenComparingInt(Animal::getBirthDay)
                            .thenComparingInt(Animal::getChildrenCount).reversed());

                    Animal winner = animalsOnPosition.get(0);
                    winner.setEnergy(winner.getEnergy() + this.grassEnergy);
                    winner.incrementPlantsEaten();
                    this.map.getMapElements().remove(grassMap.get(position));
                    grassMap.remove(position);
                }

                else if (BigGrassMap.keySet().contains(position) && simulationPropertyFile.getIntValue(ESimulationProperty.wariantWzrostuRoslin) == 1) {
                    animalsOnPosition.sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                            .thenComparingInt(Animal::getBirthDay)
                            .thenComparingInt(Animal::getChildrenCount).reversed());
                    Animal winner = animalsOnPosition.get(0);
                    winner.setEnergy(winner.getEnergy() + this.bigGrassEnergy);
                    winner.incrementBigPlantsEaten();
                    // Usuń dużą trawę z mapy
                    BigGrass bigGrass = BigGrassMap.get(position);
                    this.map.getMapElements().remove(BigGrassMap.get(position));
                    for (Vector2d pos : bigGrass.getCoveredPositions()) {
                        BigGrassMap.remove(pos);
                    }
                }
            }


            for (Vector2d position : animalMap.keySet()) {
                List<Animal> animalsOnPosition = animalMap.get(position);
                if (animalsOnPosition.size() > 1) {
                    animalsOnPosition.sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                            .thenComparingInt(Animal::getBirthDay)
                            .thenComparingInt(Animal::getChildrenCount).reversed());
                    Animal winner = animalsOnPosition.get(0);
                    Animal secondParent = animalsOnPosition.get(1);
                    if (secondParent.getEnergy() > this.simulationPropertyFile.getIntValue(ESimulationProperty.energiaZwierzakaGotowegoDoRozmnazania))
                        animalReproduction(position, winner, secondParent);
                }
            }


            int grassCount = this.simulationPropertyFile.getIntValue(ESimulationProperty.liczbaRoslinWyrastajacaKazdegoDnia);

            int plantVariant = simulationPropertyFile.getIntValue(ESimulationProperty.wariantWzrostuRoslin);
            if (plantVariant == 0) {
                map.plantOnlyNormalGrass(grassCount, grassMap);
            }
            else{
                map.plantNormalAndBigGrass(grassCount, grassMap, BigGrassMap);
            }

            notifyObservers(currentStep);
            currentStep++;

            if (paused)
                return;

            try {
                Thread.sleep(moveDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } while (!paused);
        }

    // Co tu się wyrabia, fiu fiu
    public void animalReproduction(Vector2d position, Animal winner, Animal secondParent) {
        int genomeLength = this.simulationPropertyFile.getIntValue(ESimulationProperty.dlugoscGenomuZwierzakow);
        List<Integer> childGenome = new ArrayList<>();
        int cutIndex = genomeLength * winner.getEnergy()/ (winner.getEnergy()+secondParent.getEnergy());

        int side = random.nextInt(2);
        if (side == 0) {
            childGenome.addAll(winner.getGenome().subList(0, cutIndex));
            childGenome.addAll(secondParent.getGenome().subList(cutIndex, genomeLength));
        }
        else {
            cutIndex = genomeLength-cutIndex;
            childGenome.addAll(secondParent.getGenome().subList(0, cutIndex));
            childGenome.addAll(winner.getGenome().subList(cutIndex, genomeLength));
        }

        geneticMutation(genomeLength, childGenome);

        Animal childAnimal = new Animal(this.map, position, childGenome, 2*this.reproductionEnergy, currentStep);
        childAnimal.setParents(winner, secondParent);
        winner.addChild(childAnimal);
        secondParent.addChild(childAnimal);
        winner.setEnergy(winner.getEnergy() - this.reproductionEnergy);
        secondParent.setEnergy(secondParent.getEnergy() - this.reproductionEnergy);
        winner.setChildrenCount(winner.getChildrenCount()+1);
        secondParent.setChildrenCount(secondParent.getChildrenCount()+1);
    }

    public void geneticMutation(int genomeLength, List<Integer> childGenome) {
        int mutations = random.nextInt(this.minMutations, this.maxMutations+1);
        Set<Integer> mutationIndexSet = new HashSet<>();

        while (mutationIndexSet.size()<mutations) {
            int newIndex = random.nextInt(0, genomeLength);
            mutationIndexSet.add(newIndex);
        }

        //Pełna losowość
        for (int index : mutationIndexSet) {
                int newGene = (childGenome.get(index) + random.nextInt(1, 8)) % 8;
                childGenome.set(index, newGene);
        }

    }

    public void addObserver(ISimulationStepObserver observer){
        observerList.add(observer);
    }

    private void notifyObservers(int currentStep){
        for (ISimulationStepObserver o : observerList) {
            o.stepCompleted(currentStep);
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean b) {
        paused = b;
    }
    public int getCurrentStep() {
        return currentStep;
    }
    public OptionalDouble averageLifeLength() {
        return deadAnimalsAgeList.stream().mapToDouble(e -> e).average();
    }
    public int totalDeadAnimal(){
        return deadAnimalsAgeList.size();
    }

    public double averageChildrenCount() {
        List<Animal> animals = map.getAnimals();

        List<Animal> livingAnimals = animals.stream()
                .filter(animal -> animal.getEnergy() > 0)
                .toList();

        int totalChildren = livingAnimals.stream()
                .mapToInt(Animal::getChildrenCount)
                .sum();

        return livingAnimals.isEmpty() ? 0.0 : (double) totalChildren / livingAnimals.size();
    }
}
