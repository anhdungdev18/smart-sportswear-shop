const { chromium } = require("playwright");

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  const errors = [];
  page.on("console", (msg) => {
    if (msg.type() === "error") errors.push(msg.text());
  });
  page.on("pageerror", (err) => errors.push(String(err)));

  await page.goto("http://localhost:3001/__combobox-test", { waitUntil: "networkidle" });
  await page.waitForSelector("text=Thao tác kho");
  await page.screenshot({ path: "combobox-1-initial.png" });

  await page.click("input.combobox-input");
  await page.screenshot({ path: "combobox-2-focused-open.png" });

  await page.fill("input.combobox-input", "SEED-BOOT");
  await page.waitForTimeout(300);
  await page.screenshot({ path: "combobox-3-filtered.png" });

  const optionTexts = await page.$$eval(".combobox-option", (els) => els.map((e) => e.textContent));
  console.log("FILTERED_OPTIONS:", JSON.stringify(optionTexts));

  await page.click(".combobox-option >> nth=0");
  await page.waitForTimeout(200);
  await page.screenshot({ path: "combobox-4-selected.png" });

  const inputValue = await page.inputValue("input.combobox-input");
  console.log("INPUT_VALUE_AFTER_SELECT:", inputValue);

  console.log("CONSOLE_ERRORS:", JSON.stringify(errors));

  await browser.close();
})();
