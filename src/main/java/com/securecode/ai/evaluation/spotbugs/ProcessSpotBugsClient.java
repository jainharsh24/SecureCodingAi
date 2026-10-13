package com.securecode.ai.evaluation.spotbugs;

import com.securecode.ai.evaluation.spotbugs.config.SpotBugsProperties;
import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Compiles an isolated submission, runs SpotBugs/FindSecBugs, and reads only SECURITY bugs from its XML report. */
@Component
public class ProcessSpotBugsClient implements SpotBugsClient {
    private final SpotBugsProperties properties;

    public ProcessSpotBugsClient(SpotBugsProperties properties) { this.properties = properties; }

    @Override
    public SpotBugsScanResult scanJava(String sourceCode) {
        if (!properties.enabled()) return new SpotBugsScanResult(SecurityEvaluationStatus.SKIPPED, List.of(), "SpotBugs is disabled");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("securecode-spotbugs-");
            Path sourceFile = directory.resolve("Main.java");
            Path classesDirectory = Files.createDirectory(directory.resolve("classes"));
            Path report = directory.resolve("spotbugs-report.xml");
            Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);
            Process compiler = new ProcessBuilder(properties.javacExecutable(), "-d", classesDirectory.toString(),
                    sourceFile.toString()).start();
            if (!compiler.waitFor(properties.timeoutMs(), TimeUnit.MILLISECONDS)) {
                compiler.destroyForcibly();
                return new SpotBugsScanResult(SecurityEvaluationStatus.TIMEOUT, List.of(), "Java compilation for SpotBugs timed out");
            }
            String compilerError = read(compiler.getErrorStream());
            if (compiler.exitValue() != 0) return new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(),
                    errorMessage(compilerError, "Could not compile Java source for SpotBugs"));

            List<String> spotBugsArguments = new ArrayList<>(List.of("-textui", "-effort:max", "-xml:withMessages"));
            if (!isBundledPlugin()) {
                spotBugsArguments.addAll(List.of("-pluginList", properties.findSecBugsPluginPath()));
            }
            spotBugsArguments.addAll(List.of("-output", report.toString(), classesDirectory.toString()));
            Process process = windowsProcess(properties.executable(), spotBugsArguments.toArray(String[]::new));
            if (!process.waitFor(properties.timeoutMs(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return new SpotBugsScanResult(SecurityEvaluationStatus.TIMEOUT, List.of(),
                        "SpotBugs scan exceeded " + properties.timeoutMs() + " ms");
            }
            String stderr = read(process.getErrorStream());
            if (!Files.exists(report)) return new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(),
                    errorMessage(stderr, "SpotBugs did not create an XML report"));
            return new SpotBugsScanResult(SecurityEvaluationStatus.COMPLETED, parseSecurityFindings(report, sourceCode), null);
        } catch (IOException exception) {
            return new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(), "Could not run SpotBugs on Windows: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(), "SpotBugs scan was interrupted");
        } catch (Exception exception) {
            return new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(), "Could not read SpotBugs XML: " + exception.getMessage());
        } finally { deleteTemporaryDirectory(directory); }
    }

    List<SecurityFinding> parseSecurityFindings(Path report, String sourceCode) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder().parse(report.toFile());
        List<SecurityFinding> findings = new ArrayList<>();
        NodeList bugs = document.getElementsByTagName("BugInstance");
        for (int i = 0; i < bugs.getLength(); i++) {
            Element bug = (Element) bugs.item(i);
            if (!"SECURITY".equals(bug.getAttribute("category"))) continue;
            Element sourceLine = firstElement(bug, "SourceLine");
            Integer line = integerAttribute(sourceLine, "start");
            String type = bug.getAttribute("type");
            String cwe = bug.hasAttribute("cweid") ? "CWE-" + bug.getAttribute("cweid") : null;
            findings.add(new SecurityFinding(type, textOf(bug, "LongMessage"), bug.getAttribute("priority"), type, cwe,
                    sourceLine == null ? null : sourceLine.getAttribute("sourcepath"), line, null, line, null, sourceLine(sourceCode, line)));
        }
        return List.copyOf(findings);
    }

    private Element firstElement(Element parent, String name) {
        NodeList elements = parent.getElementsByTagName(name);
        return elements.getLength() == 0 ? null : (Element) elements.item(0);
    }
    private String textOf(Element parent, String name) { Element element = firstElement(parent, name); return element == null ? "" : element.getTextContent().strip(); }
    private Integer integerAttribute(Element element, String attribute) {
        if (element == null || !element.hasAttribute(attribute)) return null;
        try { return Integer.valueOf(element.getAttribute(attribute)); } catch (NumberFormatException ignored) { return null; }
    }
    private String sourceLine(String sourceCode, Integer line) {
        if (line == null) return null;
        String[] lines = sourceCode.split("\\R", -1);
        return line > 0 && line <= lines.length ? lines[line - 1].strip() : null;
    }
    private Process windowsProcess(String executable, String... arguments) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command).start();
    }
    private boolean isBundledPlugin() {
        Path executable = Path.of(properties.executable()).toAbsolutePath().normalize();
        Path plugin = Path.of(properties.findSecBugsPluginPath()).toAbsolutePath().normalize();
        Path pluginDirectory = executable.getParent() == null ? null : executable.getParent().getParent();
        return pluginDirectory != null && plugin.startsWith(pluginDirectory.resolve("plugin"));
    }
    private String read(java.io.InputStream stream) throws IOException { return new String(stream.readAllBytes(), StandardCharsets.UTF_8); }
    private String errorMessage(String error, String fallback) { return error == null || error.isBlank() ? fallback : error.strip(); }
    private void deleteTemporaryDirectory(Path directory) {
        if (directory == null) return;
        try (var paths = Files.walk(directory)) { paths.sorted(Comparator.reverseOrder()).forEach(path -> { try { Files.deleteIfExists(path); } catch (IOException ignored) { } }); }
        catch (IOException ignored) { }
    }
}
