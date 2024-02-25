package tabuSearch;

import com.opencsv.exceptions.CsvValidationException;
import utils.Data;

import java.io.IOException;

public class MainTabu {
    public static void main(String[] args) throws CsvValidationException, IOException, CloneNotSupportedException {
        Data data = new Data();
        int maxIteration = 100;
        int maxTabuSize = 100;
        int neighborsSize = 500;

        Framework tabu = new Framework(data, maxIteration, maxTabuSize, neighborsSize, 10, 10);
        System.out.println(data.Ni);
//        tabu.implement();
//        TabuSearch tabuSearch = new TabuSearch(data, maxIteration, maxTabuSize, neighborsSize);
//        TabuSearchImplement tabuSearchImplement = new TabuSearchImplement(tabuSearch);
//        tabuSearchImplement.implement();
//        tabuSearchImplement.bestSolution.writeSolution("Solution.txt");
    }
}
