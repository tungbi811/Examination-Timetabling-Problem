package tabuSearch;

import utils.Data;
import utils.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TabuSearchImplement {
    TabuSearch tabuSearch;
    Solution bestSolution;

    public TabuSearchImplement(TabuSearch tabuSearch) {
        this.tabuSearch = tabuSearch;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void implement() throws CloneNotSupportedException {
        Data data = tabuSearch.getData();
        Solution solution = Solution.readSolution(data, "Solution.txt"); // Solution.createSolution(data);
        bestSolution = (Solution) solution.clone();
        double bestFitness = bestSolution.getFitness();
        Solution curSolution = (Solution) solution.clone();
        ArrayList<Solution> tabuList = new ArrayList<>();
        Solution bestNeighbor;
        double bestNeighborFitness;

        for (int i = 0; i < tabuSearch.getMaxIteration(); i++) {
            ArrayList<Solution> neighbors = getNeighbors(curSolution);
            bestNeighbor = getBestNeighbor(neighbors, tabuList);

            if (bestNeighbor == null) {
                System.out.println("No non-tabu neighbors found");
                break;
            }

            bestNeighborFitness = bestNeighbor.getFitness();
            curSolution = (Solution) bestNeighbor.clone();
            tabuList.add(bestNeighbor);

            if (tabuList.size() > tabuSearch.getMaxTabuSize()) {
                tabuList.removeFirst();
            }

            if (bestNeighborFitness < bestFitness) {
                bestSolution = (Solution) bestNeighbor.clone();
                bestFitness = bestNeighborFitness;
            }
            System.out.println("Iter: " + i + " - Cur Fitness: " + bestNeighborFitness + " - Best Fitness: " + bestFitness);
        }
    }

    public ArrayList<Solution> getNeighbors(Solution solution) {
        Data data = tabuSearch.getData();
        ArrayList<Solution> neighbors = new ArrayList<>();
        int[] X = solution.X;
        ArrayList<ArrayList<Integer>> subjectInvigilator = Solution.getSubjectInvigilator(data, solution.H, solution.D);
        ArrayList result;

        for (int n = 0; n < tabuSearch.getNeighborsSize(); n++) {
//            System.out.println("----------------");
//            for (ArrayList<Integer> list : subjectInvigilator) {
//                System.out.print(list.size() + " - ");
//            }
//            System.out.println("----------------");

            result = getNeighborSubjectSlot(X, solution.D, subjectInvigilator);
            if (result == null) {
                continue;
            }

            int[] newX = (int[]) result.get(0);
            int[][][] newD = (int[][][]) result.get(1);
            getNeighborSubjectInvigilator(newX, newD, subjectInvigilator);

            neighbors.add(new Solution(data, newD));
        }
        return neighbors;
    }

    private void getNeighborSubjectInvigilator(int[] newX, int[][][] D, ArrayList<ArrayList<Integer>> subjectInvigilator) {
        Data data = tabuSearch.getData();
        Random random = new Random();

        int subject1, subject2, index1, index2, oldInvigilator1, oldInvigilator2, newInvigilator1, newInvigilator2;

        do {
            subject1 = random.nextInt(data.Ns);
            subject2 = random.nextInt(data.Ns);
        } while (subject1 == subject2);

        ArrayList<Integer> invigilatorList1 = subjectInvigilator.get(subject1);
        ArrayList<Integer> invigilatorList2 = subjectInvigilator.get(subject2);

        index1 = random.nextInt(invigilatorList1.size());
        index2 = random.nextInt(invigilatorList2.size());

        oldInvigilator1 = invigilatorList1.get(index1);
        newInvigilator1 = random.nextInt(data.Ni);
        oldInvigilator2 = invigilatorList2.get(index2);
        newInvigilator2 = random.nextInt(data.Ni);

        for (int l = 0; l < data.L[subject1]; l++) {
            D[subject1][newX[subject1] + l][oldInvigilator1] = 0;
            D[subject1][newX[subject1] + l][newInvigilator1] = 1;
        }
        for (int l = 0; l < data.L[subject2]; l++) {
            D[subject2][newX[subject2] + l][oldInvigilator2] = 0;
            D[subject2][newX[subject2] + l][newInvigilator2] = 1;
        }
    }

    private ArrayList getNeighborSubjectSlot(int[] X, int[][][] D, ArrayList<ArrayList<Integer>> subjectInvigilator) {
        Data data = tabuSearch.getData();
        int[][][] newD = Arrays.stream(D).map(row -> Arrays.stream(row).map(int[]::clone).toArray(int[][]::new)).toArray(int[][][]::new);
        Random random = new Random();

        ArrayList result = new ArrayList();
        int[] tempX = Arrays.copyOf(X, X.length);
        int subject1, subject2;

        do {
            subject1 = random.nextInt(data.Ns);
            subject2 = random.nextInt(data.Ns);
        } while (subject1 == subject2);

        ArrayList<Integer> invigilatorList1 = subjectInvigilator.get(subject1);
        ArrayList<Integer> invigilatorList2 = subjectInvigilator.get(subject2);

        int oldSlotSubject1 = tempX[subject1];
        int oldSlotSubject2 = tempX[subject2];

        int newSlotSubject1 = random.nextInt(data.Nt);
        int newSlotSubject2 = random.nextInt(data.Nt);

        try {
            for (int l = 0; l < data.L[subject1]; l++) {
                for (int i : invigilatorList1) {
                    newD[subject1][oldSlotSubject1 + l][i] = 0;
                    newD[subject1][newSlotSubject1 + l][i] = 1;
                }
            }
            for (int l = 0; l < data.L[subject2]; l++) {
                for (int i : invigilatorList2) {
                    newD[subject2][oldSlotSubject2 + l][i] = 0;
                    newD[subject2][newSlotSubject2 + l][i] = 1;
                }
            }

            tempX[subject1] = newSlotSubject1;
            tempX[subject2] = newSlotSubject2;
        } catch (Exception e) {
            return null;
        }
        result.add(tempX);
        result.add(newD);
        return result;
    }

    public Solution getBestNeighbor(ArrayList<Solution> neighbors, ArrayList<Solution> tabuList) throws CloneNotSupportedException {
        Data data = tabuSearch.getData();
        Solution bestNeighbor = null;
        double bestNeighborFitness = Double.MAX_VALUE;

        for (Solution neighbor : neighbors) {
            if (!tabuList.contains(neighbor)) {
                double neighborFitness = neighbor.getFitness();
                if (!neighbor.passAllConstraint()) {
                    neighborFitness *= 1000;
                }
                if (neighborFitness < bestNeighborFitness) {
                    bestNeighbor = (Solution) neighbor.clone();
                    bestNeighborFitness = neighborFitness;
                }
            }
        }
        return bestNeighbor;
    }
}
