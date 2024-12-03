import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedMap;
import java.io.FileWriter;
import java.io.File;

public class MapExperiment {
    static final int MIN_SIZE = 100;
    static final int MAX_SIZE = 200; // largest possible size is 100,000
    static final int NUM_REPS = 2;
    static final double TTL = 1e12;
    static final String methodName[] = {"Tree", "Hash Table"};
    static final String RESULTS_NAME = "results.csv";
    static final String DATA_NAME = "cities.txt";

    public static void main(String[] args){
        MapExperiment singleton = new MapExperiment();
        try{
            singleton.verify(10);  // Do a few checks to verify that both approaches work!
            singleton.run();
        }
        catch(IOException e){
            System.err.println("Error: Trying to run experiment");
            System.err.println(e.getMessage());
        }
    }

    public void verify(int numTests) throws IOException {
        AbstractMap<String,Integer> map0 = generateMap(0, 100_000);
        AbstractMap<String,Integer> map1 = generateMap(1, 100_000);
        ArrayList<String> cityList = new ArrayList<>(map0.keySet());
        
        for (int test = 0; test < numTests; test++) {
            String query = generateQuery(cityList);
            ArrayList<String> result0 = runTest(map0, query, 0);
            ArrayList<String> result1 = runTest(map1, query, 1);
            if (result0.size() != result1.size()) {
                System.err.println("Error Test " + test + ": The two techniques did not return the same number of cities!");
            } else {
                for (int i = 0; i < result0.size(); i++) {
                    if (!result0.get(i).equals(result1.get(i))) {
                        System.err.println("Error Test " + test + ": The two techniques did not return the same order of cities.");
                        break;
                    }
                }
            }
        }
    }

    public void run() throws IOException{
        int size, method, reps;

        PrintWriter out = new PrintWriter(new FileWriter(RESULTS_NAME)); // Store results in this file

        out.print("Size, ");
        for (size = MIN_SIZE; size <= MAX_SIZE; size += 1)
            out.print(size + ",");
        out.println();

        ArrayList<String> cityList = generateCityList();
        String[] queries = new String[NUM_REPS];
        for(int i=0; i<NUM_REPS; i++){
            queries[i] = generateQuery(cityList);
        }

        for(method = 0; method < methodName.length; method++){
            System.out.println("Testing Method " + methodName[method]);
            out.print("Method "+methodName[method]+", ");
            
            // Create the appropriate map
            for(size = MIN_SIZE; size <= MAX_SIZE; size += 1){
                AbstractMap<String,Integer> map = generateMap(method, size);
                
                long totalTime = 0;
                for(reps = 0; reps < NUM_REPS; reps++){
                    String query = queries[reps];
                    long startTime = System.nanoTime();
                    runTest(map, query, method);
                    long stopTime = System.nanoTime();
                    totalTime += (stopTime - startTime);                    
                }
                // Report the average time
                double averageTime = (double) totalTime/(double) NUM_REPS;
                out.print(averageTime + ",");
                if(averageTime > TTL){
                    System.out.println("Terminating early. Took too long at size: "+ size);
                    break; // Abort at this point
                }
            }
            out.println();
        }
        out.close(); // Close file so it can be saved
    }

    public ArrayList<String> runTest(AbstractMap<String,Integer> m, String query, int method){
        ArrayList<String> results = null;

        switch(method){
            case 0: results = this.treeSearch((TreeMap<String, Integer>) m, query);
            break;
            case 1: results = this.tableSearch((HashMap<String, Integer>) m, query);
            break;
            default: System.err.println("Unrecognized method!");
        }

        return results;
    }

    public ArrayList<String> treeSearch(TreeMap<String, Integer> m, String query){
        // Identify all keys that match the query
        SortedMap<String,Integer> matches = m.subMap(query, query+"|");
        
        // Sort the matches in ascending order (smallest to largest)
        ArrayList<String> matchingKeys = new ArrayList<String>(matches.keySet());
        if(matchingKeys.size() > 0){
            Collections.sort(matchingKeys, new Comparator<String>() {
                public int compare(String s1, String s2){
                    int p1 = m.get(s1);
                    int p2 = m.get(s2);
                    if (p1 == p2) {
                        return s1.compareTo(s2);
                    } else {
                        return p1 - p2;
                    }
                }
            });
        }
        return matchingKeys;
    }

    public ArrayList<String> tableSearch(HashMap<String, Integer> m, String query){ // WE must solve
        ArrayList<String> matchingKeys = new ArrayList<String>();
        return matchingKeys;
    }

    public AbstractMap<String,Integer> generateMap(int type, int size) throws IOException{
        AbstractMap<String,Integer> map = null;
        switch(type){
            case 0: map = new TreeMap<String, Integer>();
            break;
            case 1: map = new HashMap<String, Integer>();
            break;
            default: System.err.println("Unrecognized type of map!");
        }

        Scanner scan = new Scanner(new File(DATA_NAME));
        while(scan.hasNextLine() && map.size() < size){
            String line = scan.nextLine().trim();
            String[] parts = line.split("\t");
            int population = Integer.parseInt(parts[0]);
            String city = parts[1];
            map.put(city, population);
        }
        scan.close();

        return map;
    }

    public ArrayList<String> generateCityList() throws IOException{
        ArrayList<String> list = new ArrayList<>();

        Scanner scan = new Scanner(new File(DATA_NAME));
        while(scan.hasNextLine()){
            String line = scan.nextLine().trim();
            String[] parts = line.split("\t");
            String city = parts[1];
            list.add(city);
        }
        scan.close();

        return list;
    }

    public String generateQuery(ArrayList<String> list){
        Random rand = new Random();
        int index = rand.nextInt(list.size());
        String queryCity = list.get(index);
        
        int firstComma = queryCity.indexOf(",");
        int queryLength = firstComma;
        if (firstComma > 3) {
            int maxLength = Math.min(7, firstComma);
            int minLength = 3;
            queryLength = rand.nextInt(maxLength - minLength) + minLength;
        }
        return queryCity.substring(0, queryLength);
    }
}