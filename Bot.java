package Bot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.Math;

public class Bot {

    public Bot(){
        clearDb();

    }

    public static String getReply(String message){
        ArrayList<String> posLst;
        Map<String,Double> map = new HashMap<>();
        String sen="";
        String res="";

        if(isGreeting(message)){
            return sendGreet(message);
        }
        else{
            message = stemming(message);
            System.out.println("Input: "+message);

            writeToDb(message);
            System.out.println("Get Tfidf sentence");
            map = getTFIDF(message);
            System.out.println(map);

            System.out.println("Get pos of sentence");
            sen = sortTfidfMap(map);

            posLst = storePos(sen);
            for(String a: posLst){
                System.out.println(a);
            }

            System.out.println("Get exact respond to what user asks");
            res = getRespond(posLst,message,sen);
            System.out.println(res);

            return res;

        }




    }

    //Preprocessing: convert to lower case
    public static String lowerCase(String userInput){
        return userInput.toLowerCase();
    }

    //Preprocessing: Split sentence into an array of words
    public static ArrayList<String> tokenization(String userInput){
        userInput = lowerCase(userInput);
        String[] initSplit;
        initSplit = userInput.split(" ");
        ArrayList<String> finalSplit = new ArrayList<>();
        for(String w:initSplit){
            finalSplit.add(w);
        }
        return finalSplit;
    }

    //Converts user info and question into sentence without past tense/plural
    public static String stemming(String userInput){
        userInput = lowerCase(userInput);
        ArrayList<String> inputArr = tokenization(userInput);
        String s;
        String[] arr;
        String result = "";
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/stemming.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                s = input.nextLine();
                arr = s.split(",");
                for (int i=0; i<inputArr.size();i++){

                    for(String w:arr){
                        //Converts past tense to present tense
                        if (w.equals(inputArr.get(i))){
                            inputArr.set(i,s.substring(0,s.indexOf(",")));
                        }
//                        if(inputArr[i].length()>3){
//                            //Removes plural words
//                            if(inputArr[i].substring(inputArr[i].length()-1).equals("s")){
//                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-1);
//                            }
//                            //Removes tenses with past tense
//                            else if(inputArr[i].substring(inputArr[i].length()-2,inputArr[i].length()).equals("ed")){
//                                inputArr[i] = inputArr[i].substring(0,inputArr[i].length()-2);
//                            }
//                        }
                    }

                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }

        for (String c: inputArr){
            result+=c + " ";
        }
        result = result.replaceAll("\\p{P}", "");

        return result;

    }

    //Check if what user entered is an info. Only store info because tfidf only uses documents
    public static boolean isInfo(String userInput){
        String []questionWords = {"?","why","who","where"};
        for (String s:questionWords){
            if(userInput.contains(s)){
                return false;
            }

        }
        return true;

    }

    //Detect intent if is a greeting
    public static boolean isGreeting(String userInput){
        String[] greet = {"hi","hello","who are you"};
        for(String w: greet){
            if(userInput.equals(w)){
                return true;
            }
        }

        return false;

    }

    //Give respond to greeting
    public static String sendGreet(String userInput){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        Random rdn = new Random();

        String time = dateFormat.format(date);
        String[] greet = {"what's up","hello","what can I do for you"};
        int idx = rdn.nextInt(greet.length);

        int getHour = Integer.parseInt(time.substring(0,time.indexOf(":")));
        if(getHour>=6 && getHour<12){
            return "Good morning " + greet[idx];

        }
        else if(getHour>=12 && getHour<17){
            return "Good afternoon " + greet[idx];

        }
        else if (getHour>=17 && getHour<20){
            return "Good evening    " + greet[idx];

        }
        else{
            return "Oh gosh it's late " + greet[idx];
        }


    }


    //Clear database everytime the program refreshes
    public static void clearDb(){
        File f = new File("database.txt");
        if(f.exists()) f.delete();
    }

    //Stores user info for tfidf
    public static String writeToDb(String userInput){
        if(isInfo(userInput)){
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("database.txt", true));
                writer.write(userInput);
                writer.write("\n");
                writer.close();

            } catch (Exception e){
                e.printStackTrace();
            }

        }

        return "Thank you for your info";

    }
    //Remove stopwords
    public static String removeStopwords(String ques){
        String getInfo;
        String result="";
        ArrayList<String> arr= tokenization(ques);
        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/stopwords_eng.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            //Loop through database
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                ques = stemming(ques);
                for(int i =0 ;i<arr.size();i++){
                    if(getInfo.equals(arr.get(i))){
                        arr.set(i,"");
                    }
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        for(int i =0 ;i<arr.size();i++){
            result += arr.get(i) + " ";
        }
        return result.replaceAll("\\s{2,}", " ").trim();

    }


    //Return correct respond to questions that are not even relevant to database
    public static boolean isRelevant(String ques){
        String getInfo = sortTfidfMap(getTFIDF(ques));
        boolean flag = false;
        ques = removeStopwords(stemming(ques));
        ArrayList<String> t = tokenization(ques);
        for(String a: t){
            if(getInfo.contains(a)){
                flag = true;
            }
            else{
                return false;
            }
        }

        return flag;

    }

    //Get total number of lines in database
    public static int totalDoc(){
        int totalDoc = 0;
        String getInfo;

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                totalDoc+=1;


            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return totalDoc;
    }

    //Get Inverse Document Frequency (IDF) from user question (formula: total num of db lines/occurence of term in whole db)
    public static Map<String, Integer> getIdf(String ques){
        ArrayList<String> quesArr = tokenization(ques);
        ArrayList<String> getInfoArr;
        String getInfo;
        Map<String,Integer> idfMap = new HashMap<>();

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while(input.hasNextLine()){
                getInfo = input.nextLine();
                getInfoArr = tokenization(getInfo);
                for (String doc:getInfoArr){
                    for (String s:quesArr){
                        if(doc.equals(s)){
                            if(idfMap.get(s)!=null){
                                idfMap.put(s,idfMap.get(s)+1);
                            }
                            else{
                                idfMap.put(s,1);
                            }
                        }
                    }

                }


            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return idfMap;

    }

    //Calculate tfidf for user question for each document in database
    public static Map<String,Double> getTFIDF(String ques){
        int totalDoc = totalDoc();
        //Split ques into words
        ArrayList<String> quesArr = tokenization(ques);

        ArrayList<String> getInfoArr;
        String getInfo;
        double tfCount = 0;
        double sum = 0;
        Map<String,Integer> idfMap = getIdf(ques);
        Map<String,Double> tfidfMap = new HashMap<>(100);
        ArrayList<Double> storeTf = new ArrayList<>(100);

        try{
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/database.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            //Loop through documents
            while(input.hasNextLine()){
                getInfo = input.nextLine();

                //Example Question: who went to bangkok
                for(String s:quesArr){
                    //Split document into words
                    getInfoArr = tokenization(getInfo);
                    //Example Document: harry went to bangkok
                    for (String doc:getInfoArr){
                        if (doc.equals(s)){
                            tfCount +=1;
                        }
                    }
                    if(tfCount>=1){
                        storeTf.add(Math.log10(totalDoc/idfMap.get(s)+1)*tfCount/getInfoArr.size());
                    }

                    tfCount = 0;
                }
                for(double tfidf: storeTf){
                    sum+=tfidf;
                }
                tfidfMap.put(getInfo,sum);
                storeTf.clear();
                sum=0;


            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return tfidfMap;

    }

    public static <K,V extends Comparable<? super V>>
    K sortTfidfMap(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );
        System.out.println("Print sorted tfidfmap");
        System.out.println(sortedEntries);
        return sortedEntries.get(0).getKey();
    }

    //Take highest tfidf and return part of speech of the words
    public static ArrayList<String> storePos(String userInput) {
        String getWordPos;
        ArrayList<String> matches=tokenization(userInput);
        try {
            Scanner input = new Scanner("/home/chiayi/IdeaProjects/CYChatbot/src/Bot/pos_tag.txt");
            File file = new File(input.nextLine());
            input = new Scanner(file);
            while (input.hasNextLine()) {
                getWordPos = input.nextLine();
                for (int i=0; i<matches.size();i++) {
                    if (getWordPos.substring(0, getWordPos.indexOf(" ")).equals(matches.get(i))) {
                        matches.set(i, matches.get(i)+" " + getWordPos.substring(getWordPos.indexOf(" ")+1,getWordPos.length()));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i=0; i<matches.size();i++){
            System.out.println(matches.get(i));
            matches.set(i,matches.get(i)+" ");
            if(matches.get(i).substring(matches.get(i).indexOf(" ")+1,matches.get(i).length()).equals("")){
                matches.set(i,matches.get(i)+ "NOUN ");
            }
        }
        return matches;
    }

    //Get respond by classifying situations asking where/who/why
    public static String getRespond(ArrayList<String> posLst, String ques,String userInput){
        ArrayList<String> quesArr = tokenization(ques);
        ArrayList<String> quesPos = storePos(ques);
        ArrayList<String> storePos = new ArrayList<>();
        String res="";
        String term,pos;
        boolean because=false;
        boolean conj = false;
        if (!isInfo(ques)){
            for(String word: quesArr){
                if(word.equals("where") && isRelevant(ques)){
                    for(String a: quesPos){
                        if(a.contains("ADP")){
                            storePos.add(a);
                        }
                    }
                    for(String adp: storePos){
                        System.out.println(adp);
                        for(int i=posLst.indexOf(adp); i<posLst.size();i++){
                            if(i+1<posLst.size()){
                                term = posLst.get(i+1).substring(0, posLst.get(i+1).indexOf(" ")+1);
                                pos = posLst.get(i+1).substring(posLst.get(i+1).indexOf(" ")+1,posLst.get(i+1).length());
                                if(i<posLst.size()){
                                    if(!term.equals("to ") && pos.contains("NOUN") || pos.contains("PRON") || pos.contains("ADJ")){

                                        res+=term;
                                    }
                                    else{
                                        return res;
                                    }

                                }

                            }
                            else{
                                return res;
                            }


                        }

                    }


                }

                else if(word.equals("who") && isRelevant(ques)){
                    int conjPos = 0;
                    for(int i=0; i<posLst.size();i++){
                        if(posLst.get(i).contains("CONJ")){
                            conj = true;
                            conjPos = i;
                        }

                    }
                    if(conj){
                        if(posLst.get(conjPos-1).substring(posLst.get(conjPos-1).indexOf(" ")+1,posLst.get(conjPos-1).length()).equals("NOUN ")
                                && posLst.get(conjPos+1).substring(posLst.get(conjPos+1).indexOf(" ")+1,posLst.get(conjPos+1).length()).equals("NOUN ")){
                            return posLst.get(conjPos-1).substring(0,posLst.get(conjPos-1).indexOf(" ")+1) +posLst.get(conjPos).substring(0,posLst.get(conjPos).indexOf(" ")+1) + posLst.get(conjPos+1).substring(0,posLst.get(conjPos+1).indexOf(" ")+1);
                        }
                    }
                    else{
                        for(String a:quesPos){
                            if(a.contains("VERB")){
                                System.out.println(a);
                                storePos.add(a);
                            }
                        }
                        for(String verb: storePos){
                            for(int i=posLst.indexOf(verb); i>=0; i--){
                                if(i-1>=0){
                                    pos = posLst.get(i-1).substring(posLst.get(i-1).indexOf(" ")+1,posLst.get(i-1).length());
                                    term = posLst.get(i-1).substring(0, posLst.get(i-1).indexOf(" ")+1);
                                    if(pos.contains("NOUN")){
                                        res+=term;
                                    }
                                }


                            }
                            return res;
                        }

                    }


                }
                else if(word.equals("why")){
                    int start =0;
                    for(int i=0; i<posLst.size();i++){
                        term = posLst.get(i).substring(0, posLst.get(i).indexOf(" "));
                        if(term.equals("because") || term.equals("to") || term.equals("as")){
                            because = true;
                            start = i;
                        }
                    }
                    if(because){
                        for(int i=0; i<posLst.size();i++){
                            term = posLst.get(i).substring(0, posLst.get(i).indexOf(" "));
                            if(i>=start){
                                res += term + " ";

                            }
                        }
                        return res;

                    }
                    else{
                        return "Sorry the info you gave is insufficient";
                    }

                }
            }
            System.out.println(res);
            return "Sorry I don't know";

        } else {
            return "Thank you for your info~!";
        }

    }

    public static void main(String[]args){




    }

}
