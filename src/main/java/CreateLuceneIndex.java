import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CreateLuceneIndex {


    public CreateLuceneIndex(String Data_File, String Index_Dir)
            throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        System.out.println("Indexing beginning");
        Directory dir = FSDirectory.open(Paths.get(Index_Dir));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // set write mode
        config.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, config);

        // Add files to index
        Indexing_Data_From_Twitter(writer, Data_File);
        System.out.println("Indexing finished");

        writer.close();
        System.out.println("=====================================");
        System.out.println("=====================================");

        System.out.println("Query Searching beginning");
        DirectoryReader indexReader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser parser = new QueryParser("text", analyzer);
        Query query = parser.parse("Ali Baba");

        System.out.println(query.toString());
        int topHitCount = 100;
        ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;

        for (int rank = 0; rank < hits.length; ++rank) {
            Document hitDoc = indexSearcher.doc(hits[rank].doc);
            System.out.println((rank + 1) + " (score:" + hits[rank].score + ") --> " + hitDoc.get("text"));
        }

        System.out.println("Query Searching finished");



    }

    private void Indexing_Data_From_Twitter(IndexWriter writer, String Data_File) throws IOException {
        System.out.println("Parsering");
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(Data_File));
            JSONArray jsonArray = (JSONArray) obj;
            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                //get twitter contents
                String text = (String) jsonObject.get("text");

                //get twitter user id
                JSONObject user_info = (JSONObject) jsonObject.get("user");
                Long userId = (Long) user_info.get("id");

                //entites contents
                JSONObject entities = (JSONObject) jsonObject.get("entities");

                //get twitter hashtags
                JSONArray hashtags = (JSONArray) entities.get("hashtags");
                String hash_text = "null";
                if (hashtags.size() != 0){
                    JSONObject hash_info = (JSONObject) hashtags.get(0);
                    hash_text = (String) hash_info.get("text") ;
                }

                //get twitter url
                JSONArray urls = (JSONArray) entities.get("urls");
                JSONObject url_info = (JSONObject) urls.get(0);
                String url = (String) url_info.get("url");

                //get geolocation information
                JSONObject coordinates = (JSONObject) jsonObject.get("coordinates");
                JSONArray geolocation = (JSONArray) coordinates.get("coordinates");
                String geo_location = (String) ( geolocation.get(0) + "," + geolocation.get(1));

                //get timestamp
                String timestamp = (String) jsonObject.get("timestamp_ms");


                Document doc = new Document();
                doc.add(new TextField("text", text, Field.Store.YES));
                doc.add(new StoredField("id", userId));
                doc.add(new StoredField("hashTag", hash_text));
                doc.add(new StoredField("url", url));
                doc.add(new StoredField("geo_location", geo_location));
                doc.add(new StoredField("timestamp", timestamp));


                writer.addDocument(doc);

//                System.out.println(doc);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
        e.printStackTrace();
        }





    }


    public static void main(String[] args) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        if (args.length < 2) {
            System.out.println("Please input args[0]:data.json, args[1]:output_directory");
            System.exit(-1);
        }
        long startTime = System.currentTimeMillis();
        CreateLuceneIndex Lucene_generator = new CreateLuceneIndex(args[0], args[1]);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time:" + (endTime-startTime));
    }
}