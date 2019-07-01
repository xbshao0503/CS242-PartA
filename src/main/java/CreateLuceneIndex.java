import java.io.*;
import java.nio.file.Paths;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
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
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import static java.lang.Math.min;

public class CreateLuceneIndex {

    // some function are annotated, they can still work. Please check it!
    public CreateLuceneIndex(String Data_File, String Index_Dir, String Index_Dir1, String query_text)
            throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        System.out.println("Indexing beginning");
        Directory dir = FSDirectory.open(Paths.get(Index_Dir));
        System.out.println(dir);
        //Directory dir1 = FSDirectory.open(Paths.get(Index_Dir1));
        Analyzer analyzer = new StopAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);


        //For tweets contents, removing stop words
        Analyzer analyzer1 = new StopAnalyzer();
        //IndexWriterConfig config1 = new IndexWriterConfig(analyzer1);

        // set write mode
        config.setOpenMode(OpenMode.CREATE);
        //config1.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, config);
        //IndexWriter writer1 = new IndexWriter(dir1, config1);

        // Add files to index
        Indexing_Data_From_Twitter(writer, Data_File);

        //Indexing_Tweets_From_Twitter(writer1, Data_File);



        System.out.println("Indexing finished");

        writer.close();
        System.out.println("=====================================");
        System.out.println("=====================================");

        Using_query_search(dir, analyzer, query_text,Index_Dir);

    }

    private void Using_query_search(Directory dir,Analyzer analyzer, String query_text, String Index_Dir ){
        //        Seaching query
        try {
            System.out.println("Query Searching beginning");
            DirectoryReader indexReader = DirectoryReader.open(dir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser parser = new QueryParser("textandhashTag", analyzer);

            // attention: query need to parse
            Query query = parser.parse(query_text);


            System.out.println(query.toString());
            int topHitCount = 100;
            ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;

            JsonArray result = new JsonArray();
            for (int rank = 0; rank < hits.length; ++rank) {
                Document hitDoc = indexSearcher.doc(hits[rank].doc);
                System.out.println((rank + 1) + " (score:" + hits[rank].score + ") --> " + "tweet_content:" + hitDoc.get("text") + " --> " +" hashtag:" + hitDoc.get("hashTag") +" user_id:" + " --> " +  hitDoc.get("id") + " --> " +" time:"+  hitDoc.get("timestamp") +" geo-location:"+  hitDoc.get("geo_location"));
                JsonObject tweet_info = new JsonObject();
                tweet_info.addProperty("rank",rank+1);
                tweet_info.addProperty("text",hitDoc.get("text"));
                tweet_info.addProperty("hashtag",hitDoc.get("hashTag"));
                tweet_info.addProperty("user_id",hitDoc.get("id"));
                tweet_info.addProperty("timestamp",hitDoc.get("timestamp"));;
                tweet_info.addProperty("geo_location",hitDoc.get("geo_location"));
                result.add(tweet_info);

            }
            System.out.println(result.toString());
            System.out.println("Query Searching finished");
            String json_dir = Index_Dir+"\\data1.json";
            System.out.println(json_dir);
            File result_file = new File(json_dir);
            if (result_file.exists()) { // 如果已存在,删除旧文件
                result_file.delete();
            }
            Writer write = new OutputStreamWriter(new FileOutputStream(result_file), "UTF-8");
            write.write(result.toString());
            write.flush();
            write.close();

        }catch (IOException e) {
            e.printStackTrace();
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }


    }


    private void Indexing_Tweets_From_Twitter(IndexWriter writer, String Data_File) throws IOException {
        System.out.println("Parsering");
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(Data_File));
            JSONArray jsonArray = (JSONArray) obj;
            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                //get twitter contents
                String text = (String) jsonObject.get("text");

                Document doc = new Document();
                doc.add(new TextField("text", text, Field.Store.YES));

                writer.addDocument(doc);

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }





    }

    private void Indexing_Data_From_Twitter(IndexWriter writer, String Data_File) throws IOException {
        System.out.println("Parsering");
        JSONParser parser = new JSONParser();
        BufferedReader br = new BufferedReader(new FileReader(Data_File),1024);

        try {
            Object obj = parser.parse(br);
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
                String url = "null";
                if (urls.size() !=0) {
                    JSONObject url_info = (JSONObject) urls.get(0);
                    url = (String) url_info.get("url");
                }

                //get geolocation information
                JSONObject coordinates = (JSONObject) jsonObject.get("coordinates");
                JSONArray geolocation = (JSONArray) coordinates.get("coordinates");
                String geo_location = (String) ( geolocation.get(0) + "," + geolocation.get(1));

                //get timestamp
                String timestamp = (String) jsonObject.get("timestamp_ms");


                Document doc = new Document();
                doc.add(new TextField("text", text, Field.Store.YES));
                doc.add(new TextField("id", userId.toString(), Field.Store.YES));
                doc.add(new TextField("hashTag", hash_text, Field.Store.YES));
                doc.add(new TextField("url", url, Field.Store.YES));
                doc.add(new TextField("geo_location", geo_location, Field.Store.YES));
                doc.add(new TextField("timestamp", timestamp, Field.Store.YES));
//                doc.add(new StoredField("id", userId));
//                doc.add(new StoredField("hashTag", hash_text));
//                doc.add(new StoredField("url", url));
//                doc.add(new StoredField("geo_location", geo_location));
//                doc.add(new StoredField("timestamp", timestamp));

                writer.addDocument(doc);

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
        e.printStackTrace();
        }





    }


    public static void main(String[] args) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        if (args.length < 4) {
            System.out.println("Please input args[0]:data.json, args[1]:output_directory_for_full_text, args[2]:output_directory_for_only_tweets_contents ");
            System.exit(-1);
        }
        long startTime = System.currentTimeMillis();
        CreateLuceneIndex Lucene_generator = new CreateLuceneIndex(args[0], args[1], args[2], args[3]);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time:" + (endTime-startTime));
    }
}