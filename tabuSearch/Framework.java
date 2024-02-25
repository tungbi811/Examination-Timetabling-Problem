package tabuSearch;

import myThread.NeighborsThread;
import utils.Data;
import utils.Solution;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Framework {
    Data data;
    int maxIteration;
    int maxTabuSize;
    int neighborsSize;
    int generateThreadNum;
    int initializationThreadNum;
    Solution bestSolution;

    public Framework(Data data, int maxIteration, int maxTabuSize, int neighborsSize, int generateThreadNum, int initializationThreadNum) {
        this.data = data;
        this.maxIteration = maxIteration;
        this.maxTabuSize = maxTabuSize;
        this.neighborsSize = neighborsSize;
        this.generateThreadNum = generateThreadNum;
        this.initializationThreadNum = initializationThreadNum;
    }

    public void implement() throws CloneNotSupportedException {
        Solution solution =  Solution.createSolution(data);// Solution.readSolution(data, "Solution.txt");
        bestSolution = (Solution) solution.clone();
        double bestFitness = bestSolution.getFitness();
        Solution curSolution = (Solution) solution.clone();
        ArrayList<Solution> tabuList = new ArrayList<>();
        Solution bestNeighbor = null;
        double bestNeighborFitness;

        for (int i = 0; i < maxIteration; i++) {
            bestNeighbor = getBestNeighbor(curSolution, tabuList);

            if (bestNeighbor == null) {
                System.out.println("No non-tabu neighbors found");
                break;
            }

            bestNeighborFitness = bestNeighbor.getFitness();
            curSolution = (Solution) bestNeighbor.clone();
            tabuList.add(bestNeighbor);

            if (tabuList.size() > maxTabuSize) {
                tabuList.removeFirst();
            }

            if (bestNeighborFitness < bestFitness) {
                bestSolution = (Solution) bestNeighbor.clone();
                bestFitness = bestNeighborFitness;
            }
            System.out.println("Iter: " + i + " - Cur Fitness: " + bestNeighborFitness + " - Best Fitness: " + bestFitness);
        }
    }

    public Solution getBestNeighbor(Solution curSolution, ArrayList<Solution> tabuList){
        ExecutorService executorService = Executors.newFixedThreadPool(generateThreadNum);

        int[] X = curSolution.X;
        ArrayList<ArrayList<Integer>> subjectInvigilator = Solution.getSubjectInvigilator(data, curSolution.H, curSolution.D);
        ArrayList<Solution> neighbors = new ArrayList<>();
        ArrayList<Double> neighborsFitness = new ArrayList<>();

        for (int i = 0; i < neighborsSize; i++) {
            Runnable thread = new NeighborsThread(data, curSolution, neighbors, neighborsFitness, tabuList, subjectInvigilator);
            executorService.execute(thread);
        }
        executorService.shutdown();

        Solution bestNeighbor = neighbors.getFirst();
        double bestNeighborFitness = neighborsFitness.getFirst();

        for (int i = 1; i < neighbors.size(); i++) {
            if (neighborsFitness.get(i) < bestNeighborFitness) {
                bestNeighbor = neighbors.get(i);
                bestNeighborFitness = neighborsFitness.get(i);
            }
        }
        return bestNeighbor;
    }

}
