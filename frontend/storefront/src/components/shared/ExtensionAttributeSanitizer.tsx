const SANITIZER = String.raw`
(() => {
  const attribute = "fdprocessedid";

  const clean = (root) => {
    if (!root) return;
    if (root.nodeType === Node.ELEMENT_NODE && root.hasAttribute(attribute)) {
      root.removeAttribute(attribute);
    }
    if (root.querySelectorAll) {
      root.querySelectorAll("[" + attribute + "]").forEach((element) => {
        element.removeAttribute(attribute);
      });
    }
  };

  clean(document.documentElement);

  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      if (mutation.type === "attributes") {
        mutation.target.removeAttribute(attribute);
        continue;
      }
      mutation.addedNodes.forEach(clean);
    }
  });

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: [attribute],
    childList: true,
    subtree: true,
  });

  window.addEventListener(
    "load",
    () => window.setTimeout(() => observer.disconnect(), 3000),
    { once: true },
  );
})();
`;

export function ExtensionAttributeSanitizer() {
  return <script id="extension-attribute-sanitizer" dangerouslySetInnerHTML={{ __html: SANITIZER }} />;
}
