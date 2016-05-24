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

public class DownloadTask extends AsyncTask<String, Integer, AsyncTaskResult> {
    private static String URL  = "http://www.student.agh.edu.pl/~rutka/";

    private static String SCHEDULE_PREFIX = "schedule-v";
    private static String BEACON_PREFIX = "beacons-v";
    private Context context;

    public DownloadTask(Context context) {
        this.context = context;
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
        //todo poprawic wyswitlanie sie wiadomosci koncowej
        if ( result.getError() != null ) {
            Toast.makeText(context, "Błąd: " + result.getError(), Toast.LENGTH_LONG).show();
        }  else if ( isCancelled()) {
            Toast.makeText(context, "Przerwano pobieranie aktualizacji", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, result.getResult(), Toast.LENGTH_LONG).show();
        }
    };

    private String downloadFile() throws IOException {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        PageParser pageParser = new PageParser();
        String latestBeaconFileName = pageParser.getLatestBeaconFileName();
        // todo pobieranie planu zajec
        String latestScheduleFileName = pageParser.getLatestScheduleFileName();
        try {

            connection = connect(latestBeaconFileName);

            if (!isConnected(connection)) {
                throw new IllegalStateException("Brak połączenia " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
            int fileLength = connection.getContentLength();
            createFolder();
            File file = new File(Environment.getExternalStorageDirectory() + "/MySchedule/" + latestBeaconFileName);
            Log.d("DEBUG", file.getPath());
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
        return "File " + latestBeaconFileName + " downloaded";
    }

    private void createFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/MySchedule");
        if(!folder.exists()) {
            Log.d("DEBUG", "Create dir: " + folder.getPath());
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
