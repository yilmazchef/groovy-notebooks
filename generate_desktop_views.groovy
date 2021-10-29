import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.example.views"
entityPackageName = packageName.replaceAll(".views", ".models.entities")
dataBinderPackageName = packageName.replaceAll(".views", ".models.binders")
exceptionsPackageName = packageName.replaceAll(".views", ".exceptions")

typeMapping = [
        (~/(?i)boolean/)            : "Boolean",
        (~/(?i)int/)                : "Integer",
        (~/(?i)long/)               : "Long",
        (~/(?i)float/)              : "Float",
        (~/(?i)double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/) : "java.sql.Timestamp",
        (~/(?i)date/)               : "java.sql.Date",
        (~/(?i)time/)               : "java.sql.Time",
        (~/(?i)/)                   : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generateJavasFile(it, dir) }
}

def generateJavasFile(table, dir) {
    def className = javaName(table.getName(), true)
    def fields = calcFields(table)
    def menuSourceCode = new File(dir, "MainMenuDesktopView.java");
    if (!menuSourceCode.exists()) {
        menuSourceCode.withPrintWriter { out -> generateMenuSourceCode(out) }
    }
    new File(dir, className + "DesktopView.java").withPrintWriter { out -> generateSourceCode(out, className, fields) }
}

def generateMenuSourceCode(out) {

    out.println "package ${packageName};\n" +
            "\n" +
            "import com.example.exceptions.*;\n" +
            "import com.example.models.entities.*;\n" +
            "import com.example.models.binders.*;\n" +
            "import com.example.repositories.*;\n" +
            "import javafx.application.Application;\n" +
            "import javafx.beans.property.*;\n" +
            "import javafx.collections.FXCollections;\n" +
            "import javafx.collections.ObservableList;\n" +
            "import javafx.event.EventHandler;\n" +
            "import javafx.geometry.Insets;\n" +
            "import javafx.scene.Group;\n" +
            "import javafx.scene.Scene;\n" +
            "import javafx.scene.control.*;\n" +
            "import javafx.scene.control.TableColumn.CellEditEvent;\n" +
            "import javafx.scene.control.cell.PropertyValueFactory;\n" +
            "import javafx.scene.control.cell.TextFieldTableCell;\n" +
            "import javafx.scene.layout.HBox;\n" +
            "import javafx.scene.layout.VBox;\n" +
            "import javafx.scene.text.Font;\n" +
            "import javafx.stage.Stage;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class MainMenuDesktopView extends Application {\n" +
            "\n" +
            "    final HBox hBox = new HBox();\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        launch(args);\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void start(Stage stage) {\n" +
            "\n" +
            "        Scene scene = new Scene(new Group());\n" +
            "        stage.setTitle(\"Main Menu View\");\n" +
            "        stage.setWidth(750);\n" +
            "        stage.setHeight(600);\n" +
            "\n" +
            "        final Label header = new Label(\"Main Menu\");\n" +
            "        header.setFont(new Font(\"Arial\", 14));\n" +
            "\n" +
            "        final Label message = new Label(\"Status: loaded.\");\n" +
            "        header.setFont(new Font(\"Arial\", 10));\n" +
            "\n" +
            "        final Button categoryRoute = new Button(\"Category\");\n" +
            "        categoryRoute.setOnAction(onClick -> {\n" +
            "            new CategoryDesktopView().start(stage);\n" +
            "        });\n" +
            "\n" +
            "        final Button brewerRoute = new Button(\"Brewer\");\n" +
            "        brewerRoute.setOnAction(onClick -> {\n" +
            "            new BrewerDesktopView().start(stage);\n" +
            "        });\n" +
            "\n" +
            "        final Button beerRoute = new Button(\"Beer\");\n" +
            "        beerRoute.setOnAction(onClick -> {\n" +
            "            new BeerDesktopView().start(stage);\n" +
            "        });\n" +
            "\n" +
            "        final Button customerRoute = new Button(\"Customer\");\n" +
            "        customerRoute.setOnAction(onClick -> {\n" +
            "            new CustomerDesktopView().start(stage);\n" +
            "        });\n" +
            "\n" +
            "        hBox.getChildren().addAll(categoryRoute, brewerRoute, beerRoute, customerRoute);\n" +
            "        hBox.setSpacing(3);\n" +
            "\n" +
            "        final VBox vbox = new VBox();\n" +
            "        vbox.setSpacing(5);\n" +
            "        vbox.setPadding(new Insets(10, 0, 0, 10));\n" +
            "        vbox.getChildren().addAll(header, hBox, message);\n" +
            "\n" +
            "        ((Group) scene.getRoot()).getChildren().addAll(vbox);\n" +
            "\n" +
            "        stage.setScene(scene);\n" +
            "\n" +
            "        stage.show();\n" +
            "    }\n" +
            "\n" +
            "}\n"
}

def generateSourceCode(out, className, fields) {
    out.println ""
    out.println "package ${packageName};"
    out.println ""
    out.println "import ${exceptionsPackageName}.${className}Exception;"
    out.println "import ${entityPackageName}.${className}Entity;"
    out.println "import com.example.repositories.${className}Repository;"
    out.println "import javafx.application.Application;"
    out.println "import javafx.beans.property.SimpleIntegerProperty;"
    out.println "import javafx.beans.property.SimpleStringProperty;"
    out.println "import javafx.collections.FXCollections;"
    out.println "import javafx.collections.ObservableList;"
    out.println "import javafx.event.EventHandler;"
    out.println "import javafx.geometry.Insets;"
    out.println "import javafx.scene.Group;"
    out.println "import javafx.scene.Scene;"
    out.println "import javafx.scene.control.*;"
    out.println "import javafx.scene.control.TableColumn.CellEditEvent;"
    out.println "import javafx.scene.control.cell.PropertyValueFactory;"
    out.println "import javafx.scene.control.cell.TextFieldTableCell;"
    out.println "import javafx.scene.layout.HBox;"
    out.println "import javafx.scene.layout.VBox;"
    out.println "import javafx.scene.text.Font;"
    out.println "import javafx.stage.Stage;"
    out.println ""
    out.println "import java.util.List;"
    out.println "import ${dataBinderPackageName}.*;"
    out.println ""
    out.println "public class ${className}DesktopView extends Application {"
    out.println ""
    out.println "    private final TableView<${className}Binder> table = new TableView<>();"
    out.println "    private final ObservableList<${className}Binder> data = FXCollections.observableArrayList();"
    out.println "    private final ${className}Repository repository = new ${className}Repository();"
    out.println "    final HBox actBox = new HBox();"
    out.println "    final HBox navBox = new HBox();"
    out.println ""
    out.println "    public static void main(String[] args) {"
    out.println "        launch(args);"
    out.println "    }"
    out.println ""
    out.println "    @Override"
    out.println "    public void start(Stage stage) {"
    out.println ""
    out.println "        Scene scene = new Scene(new Group());"
    out.println "        stage.setTitle(\"${className} Desktop View\");"
    out.println "        stage.setWidth(500);"
    out.println "        stage.setHeight(650);"
    out.println ""
    out.println "        final Label header = new Label(\"${className} Records\");"
    out.println "        header.setFont(new Font(\"Arial\", 14));"
    out.println ""
    out.println "        final Label message = new Label(\"Status: Form loaded.\");"
    out.println "        header.setFont(new Font(\"Arial\", 10));"
    out.println ""
    out.println "        table.setEditable(true);"
    out.println ""
    fields.eachWithIndex() { it, index ->
        out.println "        TableColumn column${index} = new TableColumn(\"${it.name}\");"
        out.println "        column${index}.setCellValueFactory("
        out.println "                new PropertyValueFactory<${className}Binder, ${it.type}>(\"${it.name}\"));"
        if (it == fields.first()) {
            out.println "        column${index}.setEditable(false);"
        } else {
            out.println "        column${index}.setEditable(true);"
            out.println "        column${index}.setOnEditCommit((EventHandler<CellEditEvent<${className}Binder, ${it.type}>>) t -> {"
            out.println ""
            out.println "                    // UPDATE COLUMN IN THE REPO"
            out.println ""
            out.println ""
            out.println "                    // UPDATE TABLE COLUMN"
            out.println ""
            out.println "                    t.getTableView().getItems().get("
            out.println "                            t.getTablePosition().getRow()).set${it.name.capitalize()}(t.getNewValue());"

            out.println "                }"
            out.println "        );"
        }
    }
    out.println ""
    out.println "        table.setItems(data);"
    out.print "        table.getColumns().addAll("
    fields.eachWithIndex() { it, index ->
        out.print "column${index}"
        if (it != fields.last())
            out.print ", "
    }
    out.print ");"
    out.println ""
    out.println ""
    fields.eachWithIndex() { it, index ->
        if (it != fields.first()) {
            out.println "        final TextField column${index}EditField = new TextField();"
            out.println "        column${index}EditField.setPromptText(\"${it.name}\");"
            out.println "        column${index}EditField.setMaxWidth(column${index}.getPrefWidth());"
        }
    }
    out.println ""
    out.println "        final Button addButton = new Button(\"Add\");"
    out.println "        addButton.setOnAction(onClick -> {"
    out.println ""
    out.println "            // SAVE NEW RECORD TO DB"
    out.println "            try {"
    out.println "                int noOfNewRecords = repository.create("
    out.println "                        new ${className}Entity()"
    fields.eachWithIndex() { it, index ->
        if (it != fields.first())
            out.println "                                .withParsed${it.name.capitalize()}(column${index}EditField.getText())"
    }
    out.println "                );"
    out.println ""
    out.println "                if (noOfNewRecords > 0) {"
    out.println ""
    out.println "            // CLEAR TABLE"
    out.println "            table.getItems().clear();"
    out.println ""
    out.println "            // MAP DB RECORDS TO VIEW COMPONENTS"
    out.println "            List<${className}Entity> itemList = repository.read();"
    out.println ""
    out.println "            for (${className}Entity item : itemList) {"
    out.println "                data.add(new ${className}Binder("
    fields.eachWithIndex() { it, index ->
        out.print "                        item.get${it.name.capitalize()}()"
        if (it != fields.last())
            out.print ", "
        out.println ""
    }
    out.println "                ));"
    out.println "            }"
    out.println ""
    out.println "            table.setItems(data);"
    out.println ""
    out.println "            message.setText(\"New records from ${className} are created and to the list.\");"
    fields.eachWithIndex() { it, index ->
        if (it != fields.first())
            out.println "                    column${index}EditField.clear();"
    }
    out.println ""
    out.println "                    message.setStyle(\"-fx-text-fill: green; -fx-font-size: 16px;\");"
    out.println "                    message.setText(\"${className} created!\");"
    out.println ""
    out.println "                } else {"
    out.println "                    message.setStyle(\"-fx-text-fill: red; -fx-font-size: 16px;\");"
    out.println "                    message.setText(\"ERROR: ${className} could NOT be created!\");"
    out.println "                }"
    out.println "            } catch (${className}Exception ${className.toLowerCase()}Exception) {"
    out.println "                message.setStyle(\"-fx-text-fill: red; -fx-font-size: 16px;\");"
    out.println "                message.setText(${className.toLowerCase()}Exception.getMessage());"
    out.println "            }"
    out.println ""
    out.println "        });"
    out.println ""
    out.println "        final Button viewButton = new Button(\"View\");"
    out.println "        viewButton.setOnAction(onClick -> {"
    out.println ""
    out.println "            // CLEAR TABLE"
    out.println "            table.getItems().clear();"
    out.println ""
    out.println "            // MAP DB RECORDS TO VIEW COMPONENTS"
    out.println "            List<${className}Entity> itemList = repository.read();"
    out.println ""
    out.println "            for (${className}Entity item : itemList) {"
    out.println "                data.add(new ${className}Binder("
    fields.eachWithIndex() { it, index ->
        out.print "                        item.get${it.name.capitalize()}()"
        if (it != fields.last())
            out.print ", "
        out.println ""
    }
    out.println "                ));"
    out.println "            }"
    out.println ""
    out.println "            table.setItems(data);"
    out.println ""
    out.println "            message.setText(\"${className} records are refreshed\");"
    out.println ""
    out.println "        });"
    out.println ""
    out.println "        final Button backButton = new Button(\"Back\");"
    out.println "        backButton.setOnAction(onClick -> {"
    out.println "            scene.getWindow().hide();"
    out.println "            new MainMenuDesktopView().start(stage);"
    out.println "        });"
    out.println ""
    out.println "        navBox.getChildren().addAll(backButton);"
    out.println "        navBox.setSpacing(3);"
    out.println ""
    out.print "        actBox.getChildren().addAll("
    fields.eachWithIndex() { it, index ->
        if (it != fields.first()) {
            out.print "column${index}EditField"
            if (it != fields.last())
                out.print ","
        }
    }
    out.print ", addButton, viewButton);"
    out.println ""
    out.println "        actBox.setSpacing(3);"
    out.println ""
    out.println "        final VBox vbox = new VBox();"
    out.println "        vbox.setSpacing(5);"
    out.println "        vbox.setPadding(new Insets(10, 0, 0, 10));"
    out.println "        vbox.getChildren().addAll(header, table, actBox, navBox, message);"
    out.println ""
    out.println "        ((Group) scene.getRoot()).getChildren().addAll(vbox);"
    out.println ""
    out.println "        stage.setScene(scene);"
    out.println ""
    out.println "        stage.show();"
    out.println "    }"
    out.println ""
    out.println "}"
    out.println ""
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           type : typeStr,
                           annos: ""]]
    }
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def toCamelCase(String text, boolean capitalized = false) {
    text = text.replaceAll("(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    return capitalized ? capitalize(text) : text
}

def toSnakeCase(String text) {
    text.replaceAll(/([A-Z])/, /_$1/).toLowerCase().replaceAll(/^_/, '')
}
