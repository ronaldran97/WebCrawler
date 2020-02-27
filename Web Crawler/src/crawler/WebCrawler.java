package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

import java.util.*;

public class WebCrawler {

    private HashSet<String> links;
    private static List<String> htmls;
    private static List<String> bodies;
    private int count;
    //added by Ronald
    private String language;

    static String seedUrl = "https://www.japaneseknifeimports.com/";

    public WebCrawler() {
        links = new HashSet<String>(); //Hash set of all the URLs
        htmls = new ArrayList<String>(); //Arraylist of the corresponding html
        bodies = new ArrayList<String>(); //Arraylist of the corresponding text
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
                    bodies.add(body);
                    count++;
                }

                //Fetch the HTML code
                Document document = Jsoup.connect(URL).get();

                //added by Ronald
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

    //added by Ronald
    private boolean checkLang(Document document) {
        Element language = document.select("html").first();
        String docLanguage = language.attr("lang");
        System.out.println(docLanguage);

        return docLanguage.equals(this.language);
    }

    public static void main(String[] args) throws IOException{
        //Pick a URL from the frontier
        new WebCrawler().getPageLinks(seedUrl);

        System.out.println(bodies.get(6));

//
//    	String html = Jsoup.connect(seedUrl).get().html();
//    	System.out.println(html);
//
//        Document document = Jsoup.parse(html);
//
//        String text = document.body().text();
//        System.out.printf("Body: %s", text);

    }

}