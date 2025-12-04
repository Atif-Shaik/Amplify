Steps to create standalone app

Step 1: Use "mvn clean package" maven command to create final jar.

Step 2: Test the jar file using java command from any CLI.

Full command: java --module-path "C:\JavaFX\javafx-sdk-25.0.1\lib" --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.media --enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics --enable-native-access=javafx.media --sun-misc-unsafe-memory-access=allow -jar AmplifyMAX-1.0.4.0-SNAPSHOT.jar

Step 3: Crete a custom runtime environment using jlink for the app to make it standalone.

Full command: jlink --module-path "C:\Program Files\Java\jdk-25\jmods;C:\JavaFX\javafx-jmods-25.0.1" --add-modules java.desktop,java.sql,java.base,javafx.base,javafx.controls,javafx.fxml,javafx.media,javafx.web --strip-debug --no-header-files --no-man-pages --output "C:\My Runtime\AmplifyMAX-1.0.2.0-Runtime"

Step 4: Use jpackage to create .msi app by using the jar and custom runtime environment.

Full command: jpackage --type msi --name AmplifyMax --app-version 1.0.4.0 --vendor "Atif Studios" --description "Offline Music Player" --copyright "© 2025 Atif Studios" --input . --main-jar AmplifyMAX-1.0.4.0-SNAPSHOT.jar --main-class com.example.amplify.Main --icon "C:\AmplifyMAX jars\AmplifyMAXLogo.ico" --runtime-image "C:\My Runtime\AmplifyMAX-1.0.2.0-Runtime" --win-dir-chooser --win-menu --win-shortcut --java-options "--enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics --enable-native-access=javafx.media --sun-misc-unsafe-memory-access=allow" --dest "C:\My MSI Installers"

Step 5: (Optional) Use MSIX packaging tool to create .msix app.

Note: You must modify the commands based on your java and javafx version and file location.
