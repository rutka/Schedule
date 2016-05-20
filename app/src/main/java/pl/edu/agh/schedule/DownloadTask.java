package pl.edu.agh.schedule;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by anna on 27.04.16.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {
    private static String URL  = "http://www.student.agh.edu.pl/~rutka/";

    private Context context;
    private ProgressDialog progressDialog;

    public DownloadTask(Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.progressDialog = progressDialog;
    }

    @Override
    protected String doInBackground(String... params) {
        return downloadFile();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();
        if (result != null) {
            Toast.makeText(context,result, Toast.LENGTH_LONG).show();
            Log.e("ERROR", result);
        }
        else
            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
    }

    private String downloadFile() {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            PageParser pageParser = new PageParser();
            String latestFileName = pageParser.getLatestFileName(URL);
            connection = connect(latestFileName);

            if (!isConnected(connection)) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            createFolder();
            File file = new File(Environment.getExternalStorageDirectory() + "/MySchedule/" + latestFileName);
            if(file.exists()) {
                Log.d("DEBUG", "Brak aktualizacji");
                return "Brak aktualizacji";
            } else {
                Log.d("DEBUG", "Pobieranie pliku: " + file.getPath());

                input = connection.getInputStream();
                output = new FileOutputStream(file.getPath());

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;

                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            closeStream(output);
            closeStream(input);
            disconnect(connection);
        }
        return null;
    }

    private void createFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/MySchedule");
        if(!folder.exists()) {
            Log.d("DEBUG", "Tworze folder: " + folder.getPath());
            folder.mkdir();
        }
    }

    private boolean isConnected(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private HttpURLConnection connect(String fileName) throws IOException {
        URL url = new URL(URL + fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        return connection;

    }

    private void disconnect(HttpURLConnection connection) {
        if (connection != null)
            connection.disconnect();
    }

    private void closeStream(Closeable stream) {
        try {
            close(stream);
        } catch (IOException ignored) {
        }
    }

    private void close(Closeable stream) throws IOException {
        if (stream != null)
            stream.close();
    }


}
