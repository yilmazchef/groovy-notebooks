import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.example.repositories"
entityPackageName = packageName.replaceAll(".repositories", ".models.entities")
dataBinderPackageName = packageName.replaceAll(".repositories", ".models.binders")
exceptionsPackageName = packageName.replaceAll(".repositories", ".exceptions")

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
    new File(dir, className + "RepositoryTest.java").withPrintWriter { out ->
        generateSourceCode(out, className,
                fields)
    }
}

def generateSourceCode(out, className, fields) {
    out.println "package ${packageName};"
    out.println ""
    out.println ""
    out.println ""
    out.println "import org.junit.jupiter.api.Test;"
    out.println ""
    out.println ""
    out.println "class ${className}RepositoryTest {"
    out.println ""
    out.println ""
    out.println "\t${className}Repository repository = new ${className}Repository();"
    def methods = MyClass.declaredMethods.findAll { !it.synthetic }.name
    methods.each() {
        out.println ""
        out.println ""
        out.println "\t@Test"
        out.println "\tvoid ${it}Success() {"
        out.println ""
        out.println ""
        out.println "\t}"
        out.println ""
        out.println ""
        out.println "\t@Test"
        out.println "\tvoid ${it}Failure() {"
        out.println ""
        out.println ""
        out.println "\t}"
    }
    out.println ""
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

def toCamelCase(String text, boolean capitalized = false) {
    text = text.replaceAll("(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    return capitalized ? capitalize(text) : text
}

def toSnakeCase(String text) {
    text.replaceAll(/([A-Z])/, /_$1/).toLowerCase().replaceAll(/^_/, '')
}
