package ru.mirea.aslanyan.notebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private EditText fileNameEditor;
    private EditText fileContent;
    private SharedPreferences preferences;
    final String LAST_FILENAME = "last_filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileNameEditor = findViewById(R.id.fileNameET);
        fileContent = findViewById(R.id.fileContent);
        preferences = getPreferences(MODE_PRIVATE);

        if (!isExternalStorageReadable())
            Toast.makeText(this, "Не удалось открыть последний файл", Toast.LENGTH_SHORT).show();

        fileNameEditor.setText(preferences.getString(LAST_FILENAME, ""));
        new Thread(() ->
                fileContent.post(() ->
                        fileContent.setText(
                                getTextFromFile(fileNameEditor.getText().toString())))
        ).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onSaveClicked(View view){
        String lastFileName = fileNameEditor.getText().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_FILENAME, lastFileName);
        editor.apply();

        if (!isExternalStorageWritable()) return;
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(lastFileName, Context.MODE_PRIVATE);
            outputStream.write(fileContent.getText().toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Проверяем хранилище на доступность чтения и записи*/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Проверяем внешнее хранилище на доступность чтения */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public String getTextFromFile(String fileName) {
        FileInputStream fin = null;
        try {
            fin = openFileInput(fileName);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            return new String(bytes);
        } catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fin != null)
                    fin.close();
            } catch (IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }
}