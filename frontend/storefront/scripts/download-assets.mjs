// Downloads all IVY moda homepage assets discovered during extraction into public/images/ivymoda.
// Run with: node scripts/download-assets.mjs
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";

const OUT_ROOT = path.join(process.cwd(), "public", "images", "ivymoda");

const ASSETS = {
  common: [
    "https://pubcdn.ivymoda.com/ivy2/images/logo.png",
    "https://pubcdn.ivymoda.com/ivy2/images/logo-footer.png",
    "https://pubcdn.ivymoda.com/ivy2/images/dmca.png",
    "https://pubcdn.ivymoda.com/ivy2/images/img-congthuong.png",
    "https://pubcdn.ivymoda.com/ivy2/images/ic_fb.svg",
    "https://pubcdn.ivymoda.com/ivy2/images/ic_gg.svg",
    "https://pubcdn.ivymoda.com/ivy2/images/ic_instagram.svg",
    "https://pubcdn.ivymoda.com/ivy2/images/appstore.png",
    "https://pubcdn.ivymoda.com/ivy2/images/googleplay.png",
  ],
  banner: [
    "https://cotton4u.vn/files/news/2026/06/19/6a051c7c1a148911a0f04bb13704e9e4.webp",
    "https://cotton4u.vn/files/news/2026/06/23/da4faa3fe3af0cef91c4696275413c54.webp",
  ],
  brand: [
    "https://cotton4u.vn/files/news/2026/05/07/59eeeabf630f72988274fb1a3840a980.webp",
    "https://cotton4u.vn/files/news/2026/05/12/3a41dbc144753c0b810e8eecb1104835.webp",
  ],
  gallery: [
    "https://cotton4u.vn/files/news/2026/04/16/7b06c32a834e8032b0139df98ff1e2ce.webp",
    "https://cotton4u.vn/files/news/2026/04/16/719650f8a4399ebad50b32b42f4e2098.webp",
    "https://cotton4u.vn/files/news/2026/04/16/892245aeb1635dc06c48acb0dfb130f6.webp",
    "https://cotton4u.vn/files/news/2024/06/18/52b32974abb653aa0b54ee95d8d77cc8.webp",
    "https://cotton4u.vn/files/news/2025/12/25/7f4c9433ea83a0ea92619d1ac9469aad.webp",
    "https://cotton4u.vn/files/news/2026/04/16/6351c0d504bed1fc5ecb737e700d81cd.webp",
    "https://cotton4u.vn/files/news/2026/04/16/114db62022947c3cf9997a9f4dca5095.webp",
  ],
  products: [
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/2cf9b85228b3e78f22cfc3718f6e24b6.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/15562de9138d1c5927ee7f195c512e06.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/84ceec2e174853ef07c4201f1c60aee3.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/9b5bf42f1df7ad28f286fab5b2cd6622.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/5877f3534b232631d1cf7fdd8277859b.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/7b92778cdf3ca6ecdda23623e14c73dd.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/09/a9f9d92197126efdabe0be59ef2006e7.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/09/049ba3d1ae8ce2ec91c7cdf00d01e0d7.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/ac6ca29438fd3746a3c2e84b75b2a46a.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/ffef76478137d3b4273a125121c96be7.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/17/1c576e917832620b2d4b0f3cb78642a5.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/17/b8b5eaf1a0640756809e0bdfeb2039ad.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/b10d17283df624309e40f049630a8672.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/b6264126391943ea38c630483f9956a1.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/ba427d7547f973c1c0b1a3aac2cec1c3.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/08/183ee7c3ea75e886fdc61cfbe9a1ffd3.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/09/e30b9c0a0dd23cbc40aacdb3e0cd47f4.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/06/09/8c6fdb7b299f90db3971b659f19b5ad6.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/14/6c11f0f699cbf42f24f64c95c7923276.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/14/145e5c1c63d03a76d10f23d4c89b7723.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/03a90dba7165fd770b5ee22d5a4460a0.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/f2bb04611bf0f1d67b35d57b7a34e402.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/c6ca0a664adff97ed17bcefbe0c3edae.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/23b930813fdb472c25dd09fe6f6fdf66.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/8f4eb4a124198497b85bf2efdaa811a5.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/e13c4d413613d49fe2aa50729f0496d1.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/aba820e5aca51542c7fea0a4da78854e.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/daec5eab3ff306ec23fa0909f7c50984.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/3207e9df533a53bf889a9f97aa7b72ab.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/d99c5eb38fc914895b16c4dd3edf780a.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/608ff62c918cfef47d372e1e4eaddcd7.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/e6aa6d8ad9a789589acb4ab9b7f345f7.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/4611f30351e0b708a3887c6a03091f0c.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/0a15a4f09c111bfb2f2cff383d897df6.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/47df0db8ebdef29d758b7523ce8881be.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/5b03e8a2f3a8fb3e0b61c82bdccf9bbf.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/0d52bbb2915f7fcfaf1cc56e87c92671.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/27c9d3f6b05529b2717358b49a9a3506.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/8375bf5e76b75eb981645f18ac10bfa9.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/47ff9628191a3696f1f4d20ba26a0f6c.jpg",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/c93add532002f65e80d83860a13e560c.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/ae04423ceee236859f755a7e2f066700.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/06/15/f3527d374c3e038d40454bd667b0ed23.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/06/15/c8f2a665529316ae727ddae0484831ea.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/d1bbd95c2c658214dcc569873c556c6d.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/636e80d7ccdd48a9e423ab1f92681f35.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/58b0bbc0b98b112e263a1d7efa9dc209.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/e02a2d4560504385fde2c344ddf4578f.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/3cea5000260703bb54bdd346b6365704.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/636d0ca04b95d3d87c2e2f0d49400063.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/23/a9ce6b5f673854c421ceeedbf6b53b34.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/23/97e8ac59e39e7049f36aa04a17770521.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/4120c25ce0ebf4a93b936e06f6a502da.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/21/c474e1583d40e88fd509fcd844100e19.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/19/187bf4a79415058a70d5c3a59ec0aba3.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/19/28a6ba501f9b7a487b8de665b0cefba3.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/26/8139ba69ca55e890e4d6da3ccd859d34.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/05/26/fc2170d255113c1972871682532dca10.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/06/18/9998531dfa3609f3f661088b76f387eb.webp",
    "https://pubcdn.ivymoda.com/files/product/thumab/400/2026/06/18/b5c2adb3a6a52d3abd2d6f91de8c0498.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/04/12/ee32ed354accc0fde3eec39f7e4e9ad6.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/04/12/0bdfa4229827fcdf3d0da8b2b6c8ed02.webp",
    "https://cotton4u.vn/files/product/thumab/400/2023/03/21/e5d337aa2ad7f994a507778c310bfa48.webp",
    "https://cotton4u.vn/files/product/thumab/400/2023/03/10/1b87780e1a7cb8bfed979da5cfa0ce59.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/05/fe189f3f06ea21d1e71b9e78f49a7f6f.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/05/82a734bb9824fceebe0bccb1052e2221.webp",
    "https://cotton4u.vn/files/product/thumab/400/2023/11/21/58963bb1a8709d6fb2fbc0df4c95cac3.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2023/11/21/03b041ffa59ceab4ede5c065b47dde0b.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/22/e16447016e1280fb66d221bed6a12e4d.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/22/60aad941648b59197eac563b91c400ea.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/22/6d99f9f974702e2ea3607b0bd6594d6c.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/22/c7da62588610e789d949ac6875d6dd71.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/05/d2c39ef280405d7aba165006ab83dc40.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/03/05/78524582f9518368d095df8d1602b780.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/07/31/a83bc666879e970ebfa39facf7e4ef4f.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/07/31/bf5ddde9bd5745bd01e69c3280568c49.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/12/06/06ae1f0d2157a7b14479f3e01b18c3bc.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/12/06/cbb4a2c4c9b1d71883b32cecdf956f86.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/07/13/e5b7e87599dc84b8f98af3ffdd98e5e6.webp",
    "https://cotton4u.vn/files/product/thumab/400/2024/07/13/69ed1eb33d3be7e0a272c6620db4495e.webp",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/d557ee88abffdc4720a47df783bcc090.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/8228728a826d3a5e95b9b785550b471c.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/40146f76a13c6b530ecd18f05ef25684.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/31a49a220313401e83d40be5d328f558.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/b2bda0b9f83a7591bf3ec4b05b8e7ce1.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/05/26/c507ceb51e306d4a91f01a6a5dca1f0c.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/8649c48c30e74fa673fe744fa8409d2b.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/11e658978c71fd09d16ede2679404ea5.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/4cbd9707ef4e42edc60c05efb73da645.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/51651e3abc6fa445c74bf7cd4c2aa581.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/fa1256828bf73b0aaf91b71c95fef5f0.jpg",
    "https://cotton4u.vn/files/product/thumab/400/2026/03/28/d816404f888045b43187682ff290f2aa.jpg",
  ],
  colors: [
    "https://cotton4u.vn/ivy2/images/color/h01.png",
    "https://cotton4u.vn/ivy2/images/color/k49.png",
    "https://cotton4u.vn/ivy2/images/color/h49.png",
    "https://cotton4u.vn/ivy2/images/color/001.png",
    "https://cotton4u.vn/ivy2/images/color/h02.png",
    "https://cotton4u.vn/ivy2/images/color/049.png",
    "https://cotton4u.vn/ivy2/images/color/002.png",
    "https://cotton4u.vn/ivy2/images/color/h03.png",
    "https://cotton4u.vn/ivy2/images/color/h50.png",
    "https://cotton4u.vn/ivy2/images/color/041.png",
    "https://cotton4u.vn/ivy2/images/color/052.png",
    "https://cotton4u.vn/ivy2/images/color/022.png",
    "https://cotton4u.vn/ivy2/images/color/017.png",
    "https://cotton4u.vn/ivy2/images/color/024.png",
    "https://cotton4u.vn/ivy2/images/color/048.png",
    "https://cotton4u.vn/ivy2/images/color/060.png",
    "https://cotton4u.vn/ivy2/images/color/k02.png",
    "https://cotton4u.vn/ivy2/images/color/k52.png",
    "https://cotton4u.vn/ivy2/images/color/h61.png",
    "https://cotton4u.vn/ivy2/images/color/h09.png",
    "https://cotton4u.vn/ivy2/images/color/h13.png",
    "https://cotton4u.vn/ivy2/images/color/013.png",
    "https://cotton4u.vn/ivy2/images/color/004.png",
    "https://cotton4u.vn/ivy2/images/color/008.png",
    "https://cotton4u.vn/ivy2/images/color/014.png",
    "https://cotton4u.vn/ivy2/images/color/018.png",
    "https://cotton4u.vn/ivy2/images/color/050.png",
    "https://cotton4u.vn/ivy2/images/color/065.png",
    "https://cotton4u.vn/ivy2/images/color/003.png",
    "https://cotton4u.vn/ivy2/images/color/009.png",
    "https://cotton4u.vn/ivy2/images/color/h66.png",
    "https://cotton4u.vn/ivy2/images/color/k24.png",
  ],
};

async function downloadOne(url, destDir) {
  const filename = url.split("/").pop().split("?")[0];
  const dest = path.join(destDir, filename);
  const res = await fetch(url, {
    headers: { Referer: "https://ivymoda.com/", "User-Agent": "Mozilla/5.0" },
  });
  if (!res.ok) {
    console.error(`FAILED ${res.status} ${url}`);
    return;
  }
  const buf = Buffer.from(await res.arrayBuffer());
  await writeFile(dest, buf);
}

async function downloadBatch(urls, destDir, batchSize = 4) {
  await mkdir(destDir, { recursive: true });
  for (let i = 0; i < urls.length; i += batchSize) {
    const batch = urls.slice(i, i + batchSize);
    await Promise.all(batch.map((url) => downloadOne(url, destDir)));
    console.log(`${destDir}: ${Math.min(i + batchSize, urls.length)}/${urls.length}`);
  }
}

async function main() {
  for (const [category, urls] of Object.entries(ASSETS)) {
    await downloadBatch(urls, path.join(OUT_ROOT, category));
  }
  console.log("Done.");
}

main();
