package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Solution{
    Data data;
    public int[][][] D;
    public int[][] H;
    public int[] X;

    public Solution(Data data, int[][][] d, int[][] h, int[] x) {
        this.data = data;
        D = d;
        H = h;
        X = x;
    }

    public Solution(Data data, int[][][] d) {
        this.data = data;
        D = d;

        H = new int[data.Ns][data.Nt];
        for (int s = 0; s < data.Ns; s++) {
            for (int t = 0; t < data.Nt; t++) {
                int sumInvigilator = 0;
                for (int i = 0; i < data.Ni; i++) {
                    sumInvigilator += D[s][t][i];
                }
                if (sumInvigilator > 0) {
                    H[s][t] = 1;
                } else {
                    H[s][t] = 0;
                }
            }
        }

        X = new int[data.Ns];
        for (int s = 0; s < data.Ns; s++) {
            for (int t = 0; t < data.Nt; t++) {
                if (H[s][t] == 1) {
                    X[s] = t;
                    break;
                }
            }
        }
    }

    public static Solution createSolution(Data data) {
        int[][][] D = new int[data.Ns][data.Nt][data.Ni];
        int[][] H = new int[data.Ns][data.Nt];
        Random random = new Random();
        boolean done = false;

        while (!done) {
            H = new int[data.Ns][data.Nt];
            for (int subject = 0; subject < data.Ns; subject++) {
                List<Integer> slotList = new ArrayList<>();
                for (int i = 0; i < data.Nt; i++) {
                    slotList.add(i);
                }

                while (true) {
                    int index = random.nextInt(slotList.size());
                    int slot = slotList.get(index);
                    slotList.remove(index);

                    if (Solution.passConstraint8(data, subject, slot)
                            && Solution.passConstraint5(data, H, subject, slot)
                            && Solution.passConstraint1(data, H, subject, slot)) {
                        for (int l = 0; l < data.L[subject]; l++) {
                            H[subject][slot + l] = 1;
                        }
                        if (subject == data.Ns - 1) {
                            done = true;
                        }
                        break;
                    } else {
                        if (slotList.isEmpty()) {
                            break;
                        }
                    }
                }
                if (slotList.isEmpty()) {
                    break;
                }
            }
        }

        done = false;
        while (!done) {
            D = new int[data.Ns][data.Nt][data.Ni];
            for (int s = 0; s < data.Ns; s++) {
                int slot = -10;
                for (int t = 0; t < data.Nt; t++) {
                    if (H[s][t] == 1) {
                        slot = t;
                        break;
                    }
                }

                List<Integer> invigilatorList = new ArrayList<>();
                for (int i = 0; i < data.Ni; i++) {
                    invigilatorList.add(i);
                }

                int g = data.G[s];
                while (true) {
                    int index = random.nextInt(invigilatorList.size());
                    int invigilator = invigilatorList.get(index);
                    invigilatorList.remove(index);

                    if (D[s][slot][invigilator] == 1) {
                        if (invigilatorList.isEmpty()) {
                            break;
                        }
                        continue;
                    }

                    if (Solution.passConstraint3(data, D, s, slot, invigilator)
                            && Solution.passConstraint4(data, s, invigilator)) {
                        for (int l = 0; l < data.L[s]; l++) {
                            D[s][slot + l][invigilator] = 1;
                        }
                        g -= 1;
                        if (g == 0) {
                            if (s == data.Ns - 1) {
                                done = true;
                            }
                            break;
                        }
                    }
                }
                if (g == 0) {
                    continue;
                }
                if (invigilatorList.isEmpty()) {
                    break;
                }
            }
        }
        return new Solution(data, D);
    }

    //    1. No student should be required to sit two examinations simultaneously
    public boolean passConstraint1() {
        for (int m = 0; m < data.Nm; m++) {
            for (int t = 0; t < data.Nt; t++) {
                boolean joined = false;
                for (int s = 0; s < data.Ns; s++) {
                    if (data.A[m][s] == 1 && H[s][t] == 1) {
                        if (joined) {
                            return false;
                        } else {
                            joined = true;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean passConstraint1(Data data, int[][] H, int subject, int slot) {
        for (int m = 0; m < data.Nm; m++) {
            for (int l = 0; l < data.L[subject]; l++) {
                for (int s = 0; s < data.Ns; s++) {
                    if (s == subject) {
                        continue;
                    }
                    if (data.A[m][s] == 1 && data.A[m][subject] == 1 && H[s][slot + l] == 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //  2. The number of supervisors must be equal to the number of rooms needed to organize each subject
    public boolean passConstraint2() {
        for (int s = 0; s < data.Ns; s++) {
            for (int t = 0; t < data.Nt; t++) {
                int sum = 0;
                for (int i = 0; i < data.Ni; i++) {
                    sum += D[s][t][i];
                }
                if (0 < sum && sum < data.G[s]) {
                    return false;
                }
            }
        }
        return true;
    }

    //    3. No invigilator should be required to sit two examinations simultaneously
    public boolean passConstraint3() {
        for (int i = 0; i < data.Ni; i++) {
            for (int t = 0; t < data.Nt; t++) {
                int sum = 0;
                for (int s = 0; s < data.Ns; s++) {
                    sum += D[s][t][i];
                }
                if (sum > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean passConstraint3(Data data, int[][][] D, int subject, int slot, int invigilator) {
        for (int l = 0; l < data.L[subject]; l++) {
            for (int s = 0; s < data.Ns; s++) {
                if (D[s][slot + l][invigilator] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    //  4. Examiners only supervise subjects they are capable of supervising
    public boolean passConstraint4() {
        for (int s = 0; s < data.Ns; s++) {
            for (int i = 0; i < data.Ni; i++) {
                for (int t = 0; t < data.Nt; t++) {
                    if (D[s][t][i] == 1 && data.C[s][i] == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean passConstraint4(Data data, int subject, int invigilator) {
        return data.C[subject][invigilator] == 1;
    }

    //    5. The number of rooms used in a slot must not exceed the allowed
    public boolean passConstraint5() {
        for (int t = 0; t < data.Nt; t++) {
            int sum = 0;
            for (int s = 0; s < data.Ns; s++) {
                for (int i = 0; i < data.Ni; i++) {
                    sum += D[s][t][i];
                }
            }
            if (sum > data.Nr) {
                return false;
            }
        }
        return true;
    }

    public static boolean passConstraint5(Data data, int[][] H, int subject, int slot) {
        for (int l = 0; l < data.L[subject]; l++) {
            int sum = 0;
            for (int s = 0; s < data.Ns; s++) {
                if (H[s][slot + l] == 1) {
                    sum += data.G[s];
                }
            }
            sum += data.G[subject];
            if (sum > data.Nr) {
                return false;
            }
        }
        return true;
    }

    //    6. Each subject can only be held once
    public boolean passConstraint6() {
        for (int s = 0; s < data.Ns; s++) {
            int sum = 0;
            for (int t = 0; t < data.Nt; t++) {
                sum += H[s][t];
            }
            if (sum != data.L[s]) {
                return false;
            }
        }
        return true;
    }

    //    7. For each exam, the assigned invigilator needs to monitor all consecutive slots in which that exam takes place
    public boolean passConstraint7() {
        for (int s = 0; s < data.Ns; s++) {
            for (int i = 0; i < data.Ni; i++) {
                int sum = 0;
                for (int t = X[s]; t < X[s] + data.L[s]; t++) {
                    sum += D[s][t][i];
                }
                if (0 < sum && sum < data.L[s]) {
                    return false;
                }
            }

        }
        return true;
    }

    //    8. For exams with L slot time, it cannot end later than morning or afternoon
    public boolean passConstraint8() {
        for (int s = 0; s < data.Ns; s++) {
            if ((int) (X[s] / (data.beta / 2)) != (int) ((X[s] + data.L[s] - 1) / (data.beta / 2))) {
                return false;
            }
        }
        return true;
    }

    public static boolean passConstraint8(Data data, int subject, int slot) {
        return slot / (data.beta / 2) == (slot + data.L[subject] - 1) / (data.beta / 2);
    }

    public boolean passAllConstraint() {
        return passConstraint1()
                && passConstraint2()
                && passConstraint3()
                && passConstraint4()
                && passConstraint5()
                && passConstraint6()
                && passConstraint7()
                && passConstraint8();
    }

    public double getFitness() {
        double w1 = 1.0 / 3.0;
        double w2 = 1.0 / 3.0;
        double w3 = 1.0 / 3.0;
        return w1 * getPayoffP() + w2 * getPayoffAllM() + w3 * getPayoffAllI();
    }

    public double getPayoffP() {
        double mean = 0;
        for (int t = 0; t < data.Nt; t++) {
            for (int s = 0; s < data.Ns; s++) {
                for (int i = 0; i < data.Ni; i++) {
                    mean += D[s][t][i];
                }
            }
        }
        mean /= data.Nt;
        double std = 0;
        for (int t = 0; t < data.Nt; t++) {
            int sum = 0;
            for (int s = 0; s < data.Ns; s++) {
                for (int i = 0; i < data.Ni; i++) {
                    sum += D[s][t][i];
                }
            }
            std += (sum - mean) * (sum - mean);
        }
        std /= ((data.Nt - 1) * data.Nt);
        std = Math.sqrt(std);
        return std;
    }

    public double getPayoffAllM() {
        double payoffAllM = 0;
        for (int m = 0; m < data.Nm; m++) {
            payoffAllM += getPayoffM(m);
        }
        return payoffAllM / data.Nm;
    }

    public double getPayoffM(int m) {
        int[] U = new int[data.Ns];
        for (int s = 0; s < data.Ns; s++) {
            U[s] = data.A[m][s] * X[s];
        }

        Arrays.sort(U);

        double payoffM = 0;
        for (int i = data.Ns - 1; i > (data.Ns - data.E[m]); i--) {
            payoffM += Math.abs(U[i] - U[i - 1] - (double) data.Nt / data.E[m]);
        }
        return payoffM / data.E[m];
    }

    public double getPayoffAllI() {
        double payoffAllI = 0;
        for (int i = 0; i < data.Ni; i++) {
            payoffAllI += getPayoffI(i);
        }
        return payoffAllI / data.Ni;
    }

    public double getPayoffI(int i) {
        double w4 = 1.0 / 2.0;
        double w5 = 1.0 / 2.0;
        double numberOfDay = 0;
        int sum;
        for (int d = 0; d < data.Nd; d++) {
            sum = 0;
            for (int t = data.beta * d; t < data.beta * (d + 1); t++) {
                for (int s = 0; s < data.Ns; s++) {
                    sum += D[s][t][i];
                }
            }
            if (sum > 0) {
                numberOfDay += 1;
            }
        }

        double numberOfSlot = 0;
        for (int s = 0; s < data.Ns; s++) {
            for (int t = 0; t < data.Nt; t++) {
                numberOfSlot += D[s][t][i];
            }
        }

        double payoffI = 0;
        payoffI += w4 * numberOfDay + w5 * Math.abs(numberOfSlot - data.Q[i]);
        return payoffI;
    }

    public static Solution readSolution(Data data, String fileName) {
        int[][][] D = new int[data.Ns][data.Nt][data.Ni];
        try {
            File file = new File(fileName);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                String[] words = line.split("\\D+");
                D[Integer.parseInt(words[1])][Integer.parseInt(words[2])][Integer.parseInt(words[3])] = Integer.parseInt(words[4]);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Solution(data, D);
    }

    public static ArrayList<ArrayList<Integer>> getSubjectInvigilator(Data data, int[][] H, int[][][] D) {
        ArrayList<ArrayList<Integer>> subjectInvigilator = new ArrayList<>();
        for (int i = 0; i < data.Ns; i++) {
            subjectInvigilator.add(new ArrayList<Integer>());
        }

        for (int s = 0; s < data.Ns; s++) {
            for (int t = 0; t < data.Nt; t++) {
                if (H[s][t] == 1) {
                    for (int i = 0; i < data.Ni; i++) {
                        if (D[s][t][i] == 1) {
                            subjectInvigilator.get(s).add(i);
                        }
                    }
                    break;
                }
            }
        }
        return subjectInvigilator;
    }

    public void writeSolution(String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            for (int x = 0; x < D.length; x++) {
                for (int y = 0; y < D[x].length; y++) {
                    for (int z = 0; z < D[x][y].length; z++) {
                        // Write the values of x, y, z and D[x][y][z] separated by commas
                        writer.println("Subject " + x + "," + "Slot " + y + "," + "Invigilator " + z + ":" + D[x][y][z]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public Solution clone(){
        int[] newX = Arrays.copyOf(X, X.length);
        int[][] newH = Arrays.stream(H).map(int[]::clone).toArray(int[][]::new);
        int[][][] newD = Arrays.stream(D).map(row -> Arrays.stream(row).map(int[]::clone).toArray(int[][]::new)).toArray(int[][][]::new);
        return new Solution(data, newD, newH, newX);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Arrays.deepEquals(D, solution.D);
    }
}
