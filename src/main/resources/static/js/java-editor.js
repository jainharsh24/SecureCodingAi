export function normalizeJavaSource(source) {
  return String(source ?? "").replace(/\r\n?/g, "\n");
}

/** Formats compact Java only for the editable display; it does not change stored challenge data. */
export function formatJavaSourceForEditor(source) {
  const input = normalizeJavaSource(source);
  let output = "";
  let indent = 0;
  let parentheses = 0;
  let quote = null;
  let escaped = false;
  let lineComment = false;
  let blockComment = false;
  let lineStart = true;

  const trimLine = () => { output = output.replace(/[ \t]+$/u, ""); };
  const newline = () => {
    trimLine();
    if (!output.endsWith("\n")) output += "\n";
    lineStart = true;
  };
  const write = character => {
    if (lineStart && character !== "\n") {
      output += "  ".repeat(Math.max(0, indent));
      lineStart = false;
    }
    output += character;
  };

  for (let index = 0; index < input.length; index += 1) {
    const current = input[index];
    const next = input[index + 1];

    if (lineComment) {
      if (current === "\n") { lineComment = false; newline(); } else write(current);
      continue;
    }
    if (blockComment) {
      write(current);
      if (current === "*" && next === "/") { write(next); index += 1; blockComment = false; }
      continue;
    }
    if (quote) {
      write(current);
      if (escaped) escaped = false;
      else if (current === "\\") escaped = true;
      else if (current === quote) quote = null;
      continue;
    }
    if (current === "/" && next === "/") { write(current); write(next); index += 1; lineComment = true; continue; }
    if (current === "/" && next === "*") { write(current); write(next); index += 1; blockComment = true; continue; }
    if (current === '"' || current === "'") { quote = current; write(current); continue; }
    if (current === "\n") {
      if (lineStart) output += "\n";
      else newline();
      continue;
    }
    if (lineStart && (current === " " || current === "\t")) continue;
    if (current === "(") { parentheses += 1; write(current); continue; }
    if (current === ")") { parentheses = Math.max(0, parentheses - 1); write(current); continue; }
    if (current === "{") { write(current); indent += 1; newline(); continue; }
    if (current === "}") {
      if (!lineStart) newline();
      indent = Math.max(0, indent - 1);
      write(current);
      if (next !== ";" && next !== "," && next !== ")") newline();
      continue;
    }
    if (current === ";") {
      write(current);
      if (parentheses === 0) newline();
      continue;
    }
    write(current);
  }
  trimLine();
  return output.replace(/^\n+|\n+$/gu, "");
}
