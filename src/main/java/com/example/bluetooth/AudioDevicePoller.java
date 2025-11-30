package com.example.bluetooth;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class AudioDevicePoller extends ScheduledService<String> {
    String deviceName;

    public AudioDevicePoller() {
        this.setPeriod(Duration.seconds(3));
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                getBluetoohInformation();
                return deviceName;
            }
        };
    }

    private void getBluetoohInformation() {
        try {
            // 1. Load the exe from resources
            InputStream inputStream = getClass().getResourceAsStream("/Bluetooth-Helper/BluetoothHelper.exe");

            if (inputStream != null) {
                Path localState = Path.of(System.getenv("LOCALAPPDATA"), "AmplifyMusic", "Executables");
                Files.createDirectories(localState);
                Path exe = localState.resolve("BluetoothHelper.exe");

                // 2. Copy exe to a temp file (Windows requires a real file to execute)
                Files.copy(inputStream, exe, StandardCopyOption.REPLACE_EXISTING);

                // 3. Make the temp exe executable
                exe.toFile().setExecutable(true);

                // 4. Run the exe
                ProcessBuilder processBuilder = new ProcessBuilder(exe.toString());
                Process process = processBuilder.start();

                // 5. Read standard output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String deviceName = reader.readLine();
                process.waitFor();

                this.deviceName = Objects.requireNonNullElse(deviceName, "No Output");
            } // end

        } catch (Exception e) {
            this.deviceName = "Connection Error";
        }
    } // method ends

} // class ends
