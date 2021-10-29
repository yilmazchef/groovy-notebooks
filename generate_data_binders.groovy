import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.example.models.binders"
typeMapping = [
        (~/(?i)boolean/)            : "Boolean",
        (~/(?i)int/)                : "Integer",
        (~/(?i)long/)               : "Long",
        (~/(?i)float/)              : "Float",
        (~/(?i)double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/) : "Timestamp",
        (~/(?i)date/)               : "java.sql.Date",
        (~/(?i)time/)               : "java.sql.Time",
        (~/(?i)/)                   : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generateJavaFile(it, dir) }
}

def generateJavaFile(table, dir) {
    def className = javaName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, className + "Binder.java").withPrintWriter { out -> generateJavaSourceCode(out, className, fields) }
}

def generateJavaSourceCode(out, className, fields) {
    out.println "package $packageName;"
    out.println ""
    out.println "import javafx.beans.property.*;"
    out.println "import java.io.Serializable;"
    out.println "import java.sql.*;"
    out.println "import java.util.*;"
    out.println "import java.text.*;"
    out.println ""
    out.println "public class ${className}Binder implements Serializable {"
    out.println ""
    fields.each() {
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "  private Simple${it.type.replaceAll("java.sql.Date", "String").replaceAll("Timestamp", "String")}Property ${it.name};"
    }
    out.println ""
    out.println ""
    out.print "  public ${className}Binder ( "
    fields.each() {
        out.print "${it.type} _${it.name}"
        if (it != fields.last())
            out.print ", "
    }
    out.print ") { "
    out.println ""
    fields.each() {
        if (it.type == "java.sql.Date" || it.type == "Timestamp")
            out.println "    this.${it.name} = new SimpleStringProperty(new SimpleDateFormat(\"yyyy.MM.dd.HH.mm.ss\").format(_${it.name}));"
        else
            out.println "    this.${it.name} = new Simple${it.type.replaceAll("java.sql.", "")}Property(_${it.name});"
    }
    out.println "  }"
    out.println ""
    out.print "  public ${className}Binder(){ }"
    out.println ""
    fields.each() {
        out.println ""
        out.println "  public ${it.type} get${it.name.capitalize()}() {"
        if (it.type == "java.sql.Date")
            out.println "    return java.sql.Date.valueOf(this.${it.name}.get());"
        else if (it.type == "Timestamp")
            out.println "    return Timestamp.valueOf(this.${it.name}.get());"
        else
            out.println "    return this.${it.name}.get();"
        out.println "  }"
        out.println ""
        out.println "  public void set${it.name.capitalize()}(${it.type} _${it.name}) {"
        if (it.type == "java.sql.Date")
            out.println "    this.${it.name}.set(_${it.name}.toString());"
        else if (it.type == "Timestamp")
            out.println "    this.${it.name}.set(String.valueOf(_${it.name}));"
        else
            out.println "    this.${it.name}.set(_${it.name});"
        out.println "  }"
        out.println ""
        out.println "  public void parseAndSet${it.name.capitalize()}(String _${it.name}) {"
        if (it.type == "Integer")
            out.println "    this.set${it.name.capitalize()}(Integer.parseInt(_${it.name}));"
        else if (it.type == "Double")
            out.println "    this.set${it.name.capitalize()}(Double.parseDouble(_${it.name}));"
        else if (it.type == "Float")
            out.println "    this.set${it.name.capitalize()}(Float.parseFloat(_${it.name}));"
        else if (it.type == "java.math.BigInteger" || it.type == "BigInteger")
            out.println "    this.set${it.name.capitalize()}(java.math.BigInteger.valueOf(_${it.name}));"
        else if (it.type == "java.math.BigDecimal" || it.type == "BigDecimal")
            out.println "    this.set${it.name.capitalize()}(java.math.BigDecimal.valueOf(_${it.name}));"
        else if (it.type == "java.sql.Date" || it.type == "Date")
            out.println "    this.set${it.name.capitalize()}(java.sql.Date.valueOf(_${it.name}));"
        else if (it.type == "java.sql.Time" || it.type == "Time")
            out.println "    this.set${it.name.capitalize()}(java.sql.Time.valueOf(_${it.name}));"
        else if (it.type == "java.sql.Timestamp" || it.type == "Timestamp")
            out.println "    this.set${it.name.capitalize()}(java.sql.Timestamp.valueOf(_${it.name}));"
        else
            out.println "    this.${it.name} = new SimpleStringProperty(_${it.name});"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Binder with${it.name.capitalize()}(${it.type} _${it.name}) {"
        out.println "    this.set${it.name.capitalize()}(_${it.name});"
        out.println "    return this;"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Binder without${it.name.capitalize()}(${it.type} _${it.name}) {"
        out.println "    this.set${it.name.capitalize()}(null);"
        out.println "    return this;"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Binder withParsed${it.name.capitalize()}(String _${it.name}) {"
        out.println "    this.parseAndSet${it.name.capitalize()}(_${it.name});"
        out.println "    return this;"
        out.println "  }"
        out.println ""
    }
    out.println ""
    out.println "    @Override"
    out.println "    public boolean equals(Object obj) {"
    out.println "        if (this == obj) return true;"
    out.println "        if (!(obj instanceof ${className}Binder)) return false;"
    out.println "        ${className}Binder other${className.capitalize()} = (${className}Binder) obj;"
    if (fields.contains("id")) {
        out.println "        return this.getId().equals(other${className.capitalize()}.getId());"
    } else {
        out.println "        return "
        fields.each() {
            out.println "        this.get${it.name.capitalize()}().equals(other${className.capitalize()}.get${it.name.capitalize()}()) "
            if (it != fields.last()) {
                out.print " && "
            }
        }
        out.print ";"
    }
    out.println "    }"
    out.println ""
    out.println "    @Override"
    out.println "    public int hashCode() {"
    if (fields.contains("id")) {
        out.println "        return Objects.hash(this.getId());"
    } else {
        out.println "        return Objects.hash( "
        fields.each() {
            out.println "        this.get${it.name.capitalize()}()"
            if (it != fields.last()) {
                out.print ", "
            }
        }
        out.print " );"
    }
    out.println "    }"
    out.println ""
    out.println "    @Override"
    out.println "    public String toString() {"
    out.println ""
    out.println "        return \"{ \" + "
    fields.each() {
        out.println "        this.get${it.name.capitalize()}() + \", \" + "
    }
    out.println "    \" } \";"
    out.println "    }"
    out.println ""
    out.println "    public boolean isNew(){"
    out.println "        return this.getId() == null;"
    out.println "    }"
    out.println ""
    out.println "    public boolean isEmpty(){"
    out.print "        return ( "
    fields.eachWithIndex() { it, index ->
        out.print "this.get${it.name.capitalize()}() == null"
        if (it != fields.last())
            out.println " && "
    }
    out.print " );"
    out.println "    }"
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
