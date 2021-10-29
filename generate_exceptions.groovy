import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

// add your full path package name for your exceptions
packageName = "com.example.exceptions"
entityPackageName = packageName.replaceAll(".exceptions", ".models.entities")

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
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, className + "Exception.java").withPrintWriter { out -> generate(out, className, fields) }
}

def generate(out, className, fields) {
    out.println "package $packageName;"
    out.println ""
    out.println "import $entityPackageName" + ".*;"
    out.println "import java.util.Arrays;"
    out.println ""
    out.println "public class $className" + "Exception extends RuntimeException {"
    out.println ""
    out.println "    public $className" + "Exception(String message) {\n" +
            "        super(message);\n" +
            "    }\n" +
            "\n" +
            "    public $className" + "Exception notFound() {\n" +
            "        return new $className" + "Exception(\"$className" + " not found\");\n" +
            "    }\n" +
            "\n" +
            "    public $className" + "Exception alreadyExists() {\n" +
            "        return new $className" + "Exception(\"$className" + " already exists\");\n" +
            "    }\n" +
            "\n" +
            "    public $className" + "Exception requiredFields(String... fields) {\n" +
            "        return new $className" + "Exception(\"Required fields: \" + Arrays.toString(fields));\n" +
            "    }\n" +
            "\n" +
            "    public $className" + "Exception null$className" + "Exception() {\n" +
            "        return new $className" + "Exception(\"$className" + " cannot be null\");\n" +
            "    }"
    out.println ""
    out.println "}"
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
