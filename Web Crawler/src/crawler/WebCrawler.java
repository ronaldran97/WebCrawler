package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//added by Ronald
import com.cybozu.labs.langdetect.*;

public class WebCrawler {

    private HashSet<String> links;
    private static List<String> htmls;
    private static String bodies;
    private int count;
    private String language;


    static String seedUrl = "https://www.cpp.edu/~bsteichen/";

    public WebCrawler() {
        links = new HashSet<String>(); //Hash set of all the URLs
        htmls = new ArrayList<String>(); //Arraylist of the corresponding html
        bodies = ""; //Arraylist of the corresponding text
        count = 0;
    }

    public void getPageLinks(String URL) {
        //Check if you have already crawled the URLs 
        if (!links.contains(URL) && count < 10) {
            try {

                //If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);

                    String html = Jsoup.connect(URL).get().html();
                    String body = Jsoup.parse(html).body().text();

                    htmls.add(html);
                    bodies = bodies + " " + body;
                    count++;
                }

                //Fetch the HTML code
                Document document = Jsoup.connect(URL).get();

                checkLang(document);

                //Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }


            } catch (IOException e) {
                //System.err.println("For '" + URL + "': " + e.getMessage());
            }

        }

    }

    private boolean checkLang(Document document) {
        String textLang = detect(document.body().text());
        Element language = document.select("html").first();
        String docLanguage = language.attr("lang");
        System.out.println(docLanguage);

        return textLang.equalsIgnoreCase(this.language) && textLang.equalsIgnoreCase(docLanguage);
    }

    private String detect(String text) {
        try {
            Detector detector = DetectorFactory.create();
            detector.append(text);

            return detector.detect();

        } catch (LangDetectException e) {
            System.out.println(e.getMessage());
        }

        return "Failed";
    }

    public static void main(String[] args) throws IOException{
        //Pick a URL from the frontier
        new WebCrawler().getPageLinks(seedUrl);


        List <String> list = Stream.of(bodies).map(w -> w.split("\\s+")).flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Map <String, Integer > wordCounter = list.stream()
                .collect(Collectors.toMap(w -> w.toLowerCase(), w -> 1, Integer::sum));



        wordCounter = wordCounter.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        int rank = 1;
        int total = 0;
        for (Map.Entry<String,Integer> entry : wordCounter.entrySet())  {
            if (isAWord(entry.getKey())) {
                total += entry.getValue();
            }
        }

        for (Map.Entry<String,Integer> entry : wordCounter.entrySet())  {


            if (isAWord(entry.getKey())) {

                double pr = (double)entry.getValue() / total;
                System.out.println("Word = " + entry.getKey() +
                        ", Frequency = " + entry.getValue() +
                        ", Rank = " + rank +
                        ", Pr = " +  pr +
                        ", rPr = " + (double)rank*pr);
                rank++;
            }


        }



    }

    public static boolean isAWord(String w) {
        for (int i = 0; i < w.length(); i++){
            int ascii = (int)(w.charAt(i));

            if (65 > ascii || ascii > 122 || ascii == 91 || ascii == 93) {
                return false;
            }
            //Process char
        }

        return true;
    }



}

