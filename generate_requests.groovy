import com.intellij.database.model.*
import com.intellij.database.util.*

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.example.models.requests"

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
    new File(dir, className + "Request.java").withPrintWriter { out -> generateJavaSourceCode(out, className, fields) }
}

def generateJavaSourceCode(out, className, fields) {
    out.println "package ${packageName};"
    out.println ""
    out.println "import java.io.Serializable;"
    out.println "import java.sql.*;"
    out.println "import java.util.*;"
    out.println ""
    out.println "public class ${className}Request implements Serializable, Comparable<${className}Request> {"
    out.println ""
    fields.each() {
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "  private ${it.type} ${it.name};"
    }
    out.println ""
    out.println "      public ${className}Request(){  }"
    out.println ""
    fields.each() {
        out.println ""
        out.println "  public ${it.type} get${it.name.capitalize()}() {"
        out.println "    return ${it.name};"
        out.println "  }"
        out.println ""
        out.println "  public void set${it.name.capitalize()}(${it.type} ${it.name}) {"
        out.println "    this.${it.name} = ${it.name};"
        out.println "  }"
        out.println ""
        out.println "  public void parseAndSet${it.name.capitalize()}(String ${it.name}) {"
        if (it.type == "Integer")
            out.println "    this.${it.name} = Integer.parseInt(${it.name});"
        else if (it.type == "Double")
            out.println "    this.${it.name} = Double.parseDouble(${it.name});"
        else if (it.type == "Float")
            out.println "    this.${it.name} = Float.parseFloat(${it.name});"
        else if (it.type == "java.math.BigInteger" || it.type == "BigInteger")
            out.println "    this.${it.name} = java.math.BigInteger.valueOf(${it.name});"
        else if (it.type == "java.math.BigDecimal" || it.type == "BigDecimal")
            out.println "    this.${it.name} = java.math.BigDecimal.valueOf(${it.name});"
        else if (it.type == "java.sql.Date" || it.type == "Date")
            out.println "    this.${it.name} = java.sql.Date.valueOf(${it.name});"
        else if (it.type == "java.sql.Time" || it.type == "Time")
            out.println "    this.${it.name} = java.sql.Time.valueOf(${it.name});"
        else if (it.type == "java.sql.Timestamp" || it.type == "Timestamp")
            out.println "    this.${it.name} = java.sql.Timestamp.valueOf(${it.name});"
        else
            out.println "    this.${it.name} = ${it.name};"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Request with${it.name.capitalize()}(${it.type} ${it.name}) {"
        out.println "    this.set${it.name.capitalize()}(${it.name});"
        out.println "    return this;"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Request withParsed${it.name.capitalize()}(String ${it.name}) {"
        out.println "    this.parseAndSet${it.name.capitalize()}(${it.name});"
        out.println "    return this;"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Request without${it.name.capitalize()}( ${it.type} ${it.name}) {"
        out.println "    this.set${it.name.capitalize()}(null);"
        out.println "    return this;"
        out.println "  }"
        out.println ""
        out.println "  public ${className}Request withoutParsed${it.name.capitalize()}(String ${it.name}) {"
        out.println "    this.set${it.name.capitalize()}(null);"
        out.println "    return this;"
        out.println "  }"
        out.println ""
    }
    out.println ""
    out.println "    @Override"
    out.println "    public int compareTo(${className}Request other${className.capitalize()}) {"
    out.println "        // define here default comparison criteria "
    out.println "        return 0;"
    out.println "    }"
    out.println ""
    out.println "    @Override"
    out.println "    public boolean equals(Object obj) {"
    out.println "        if (this == obj) return true;"
    out.println "        if (!(obj instanceof ${className}Request)) return false;"
    out.println "        ${className}Request other${className.capitalize()} = (${className}Request) obj;"
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
        out.print "        \"${it.name}:\""
        out.print " + "
        out.print "this.get${it.name.capitalize()}() "
        if (it == fields.last())
            out.print " + "
        else
            out.print " + \", \" + "
        out.println ""
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
