package pl.edu.agh.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
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

import pl.edu.agh.schedule.util.ConstUtils;

public class DownloadTask extends AsyncTask<String, Integer, AsyncTaskResult> {
    private final String type;

    private Context context;

    public DownloadTask(Context context, String type) {

        this.context = context;
        this.type = type;
    }

    @Override
    protected AsyncTaskResult doInBackground(String... params) {
        try {
            return new AsyncTaskResult(downloadFile());
        } catch (Exception error) {
            return new AsyncTaskResult(error);
        }
    }

    protected void onPostExecute(AsyncTaskResult result) {
        if (result.getError() != null) {
            result.getError().printStackTrace();
        } else {
            result.notifyObserver();
            String realResult = result.getResult();
            savePreferences(realResult);
        }
    }

    private void savePreferences(String fileName) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (fileName.contains(ConstUtils.SCHEDULE)) {
            editor.putString(ConstUtils.SCHEDULE, fileName);
            editor.apply();
            printToast(fileName);
        } else if(fileName.contains(ConstUtils.BEACON)) {
            editor.putString(ConstUtils.BEACON, fileName);
            editor.apply();
            printToast(fileName);
        }
    }

    private void printToast(String fileName) {
        Toast.makeText(context, "File " + fileName + " downloaded", Toast.LENGTH_LONG).show();
    }

    private String downloadFile() throws IOException {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        PageParser pageParser = new PageParser();
        String latestFileName = pageParser.getLatestFileName(type);
        try {

            connection = connect(latestFileName);

            if (!isConnected(connection)) {
                throw new IllegalStateException("Brak połączenia " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
            int fileLength = connection.getContentLength();
            createFolder();
            File file = new File(Environment.getExternalStorageDirectory() + "/MySchedule/" + latestFileName);
            Log.d("DEBUG", file.getPath());
            if (file.exists()) {
                Log.d("DEBUG", "Brak aktualizacji");
                return "Brak aktualizacji";
            } else {
                Log.d("DEBUG", "Downloading file: " + file.getPath());

                input = connection.getInputStream();
                output = new FileOutputStream(file.getPath());

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return "";
                    }
                    total += count;

                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            }
        } finally {
            closeStream(output);
            closeStream(input);
            disconnect(connection);
        }
        return latestFileName;
    }

    private void createFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/MySchedule");
        if (!folder.exists()) {
            Log.d("DEBUG", "Create dir: " + folder.getPath());
            folder.mkdir();
        }
    }

    private boolean isConnected(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private HttpURLConnection connect(String fileName) throws IOException {
        URL url = new URL(ConstUtils.URL + fileName);
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
